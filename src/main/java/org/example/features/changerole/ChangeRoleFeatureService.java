package org.example.features.changerole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import org.example.persistence.feature.Feature;
import org.example.persistence.feature.FeatureName;
import org.example.persistence.feature.FeatureRepository;
import org.example.persistence.guild.Guild;
import org.example.persistence.guild.GuildRepository;
import org.example.persistence.m2m.guildfeature.GuildFeature;
import org.example.persistence.m2m.guildfeature.GuildFeatureId;
import org.example.persistence.m2m.guildfeature.GuildFeatureRepository;
import org.example.persistence.role.GuildRole;
import org.example.persistence.role.GuildRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeRoleFeatureService {

    private final GuildRepository guildRepository;
    private final GuildRoleRepository guildRoleRepository;
    private final FeatureRepository featureRepository;
    private final GuildFeatureRepository guildFeatureRepository;

    @Transactional
    public void registerConfiguration(
            net.dv8tion.jda.api.entities.Guild jdaGuild, Role changeableRole, Role settableRole
    ) {
        Guild guild = upsertGuild(jdaGuild);
        GuildRole changeable = upsertGuildRole(guild, changeableRole);
        GuildRole settable = upsertGuildRole(guild, settableRole);

        resetAssignments(guild.getId(), changeable.getId(), settable.getId());

        changeable.setFeatureCrIsChangeable(true);
        changeable.setFeatureCrIsSettable(false);
        settable.setFeatureCrIsSettable(true);
        settable.setFeatureCrIsChangeable(false);

        guildRoleRepository.save(changeable);
        guildRoleRepository.save(settable);

        enableChangeRoleFeature(guild);
    }

    @Transactional(readOnly = true)
    public List<ChangeRoleConfiguration> findEnabledConfigurations() {
        List<GuildFeature> enabledFeatures = guildFeatureRepository
                .findByFeature_NameAndEnabledTrue(FeatureName.CHANGE_ROLE);

        return enabledFeatures.stream()
                .map(GuildFeature::getGuild)
                .map(this::toConfiguration)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ChangeRoleConfiguration> toConfiguration(Guild guild) {
        Optional<GuildRole> changeable = guildRoleRepository
                .findAllByGuild_IdAndFeatureCrIsChangeableTrue(guild.getId())
                .stream()
                .findFirst();
        Optional<GuildRole> settable = guildRoleRepository
                .findAllByGuild_IdAndFeatureCrIsSettableTrue(guild.getId())
                .stream()
                .findFirst();

        if (changeable.isEmpty() || settable.isEmpty()) {
            log.warn("Guild '{}' ({}) has change role feature enabled but configuration is incomplete.",
                    guild.getName(), guild.getId());
            return Optional.empty();
        }

        return Optional.of(new ChangeRoleConfiguration(
                guild.getId(),
                guild.getName(),
                changeable.get().getId(),
                changeable.get().getName(),
                settable.get().getId(),
                settable.get().getName()
        ));
    }

    private Guild upsertGuild(net.dv8tion.jda.api.entities.Guild jdaGuild) {
        Guild guild = guildRepository.findById(jdaGuild.getIdLong())
                .map(existing -> existing.setName(jdaGuild.getName()))
                .orElseGet(() -> new Guild().setId(jdaGuild.getIdLong()).setName(jdaGuild.getName()));
        return guildRepository.save(guild);
    }

    private GuildRole upsertGuildRole(Guild guild, Role role) {
        return guildRoleRepository.findById(role.getIdLong())
                .map(existing -> existing.setName(role.getName()).setGuild(guild))
                .orElseGet(() -> new GuildRole()
                        .setId(role.getIdLong())
                        .setName(role.getName())
                        .setGuild(guild)
                );
    }

    private void resetAssignments(Long guildId, Long changeableRoleId, Long settableRoleId) {
        List<GuildRole> toUpdate = new ArrayList<>();
        guildRoleRepository.findAllByGuild_IdAndFeatureCrIsChangeableTrue(guildId)
                .stream()
                .filter(role -> !role.getId().equals(changeableRoleId))
                .forEach(role -> {
                    role.setFeatureCrIsChangeable(false);
                    toUpdate.add(role);
                });
        guildRoleRepository.findAllByGuild_IdAndFeatureCrIsSettableTrue(guildId)
                .stream()
                .filter(role -> !role.getId().equals(settableRoleId))
                .forEach(role -> {
                    role.setFeatureCrIsSettable(false);
                    toUpdate.add(role);
                });

        if (!toUpdate.isEmpty()) {
            guildRoleRepository.saveAll(toUpdate);
        }
    }

    private void enableChangeRoleFeature(Guild guild) {
        Feature feature = featureRepository.findByName(FeatureName.CHANGE_ROLE)
                .orElseGet(() -> featureRepository.save(Feature.builder()
                        .name(FeatureName.CHANGE_ROLE)
                        .enabled(true)
                        .build()));

        GuildFeature guildFeature = guildFeatureRepository
                .findByGuild_IdAndFeature_Name(guild.getId(), feature.getName())
                .orElseGet(() -> GuildFeature.builder()
                        .id(new GuildFeatureId(guild.getId(), feature.getId()))
                        .guild(guild)
                        .feature(feature)
                        .enabled(true)
                        .build());
        guildFeature.setEnabled(true);
        guildFeatureRepository.save(guildFeature);
    }
}
