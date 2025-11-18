package org.example.features.changerole;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import org.example.persistence.changerole.FeatureChangeRoleConfiguration;
import org.example.persistence.changerole.FeatureChangeRoleConfigurationRepository;
import org.example.persistence.feature.Feature;
import org.example.persistence.feature.FeatureName;
import org.example.persistence.feature.FeatureRepository;
import org.example.persistence.featureconfiguration.FeatureConfigurationId;
import org.example.persistence.guild.Guild;
import org.example.persistence.guild.GuildRepository;
import org.example.persistence.m2m.guildfeature.GuildFeature;
import org.example.persistence.m2m.guildfeature.GuildFeatureId;
import org.example.persistence.m2m.guildfeature.GuildFeatureRepository;
import org.example.persistence.role.GuildRole;
import org.example.persistence.role.GuildRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final FeatureChangeRoleConfigurationRepository changeRoleConfigurationRepository;

    @Transactional
    public void registerConfiguration(
            net.dv8tion.jda.api.entities.Guild jdaGuild, Role changeableRole, Role settableRole
    ) {
        Guild guild = upsertGuild(jdaGuild);
        GuildRole changeable = guildRoleRepository.save(upsertGuildRole(guild, changeableRole));
        GuildRole settable = guildRoleRepository.save(upsertGuildRole(guild, settableRole));

        Feature feature = ensureFeature();
        FeatureConfigurationId configurationId = new FeatureConfigurationId(guild.getId(), feature.getId());
        FeatureChangeRoleConfiguration configuration = changeRoleConfigurationRepository
                .findById(configurationId)
                .orElseGet(() -> new FeatureChangeRoleConfiguration()
                        .setId(configurationId)
                        .setGuild(guild)
                        .setFeature(feature));

        configuration.setChangeableRole(changeable);
        configuration.setSettableRole(settable);
        changeRoleConfigurationRepository.save(configuration);

        setFeatureEnabled(guild, true);
    }

    @Transactional(readOnly = true)
    public List<ChangeRoleConfiguration> findEnabledConfigurations() {
        List<GuildFeature> enabledFeatures = guildFeatureRepository
                .findByFeature_NameAndEnabledTrue(FeatureName.CHANGE_ROLE);

        return enabledFeatures.stream()
                .filter(guildFeature -> {
                    Feature feature = guildFeature.getFeature();
                    if (feature == null || !feature.isEnabled()) {
                        log.debug("Feature '{}' is globally disabled. Skipping guild {}.",
                                FeatureName.CHANGE_ROLE, guildFeature.getGuild().getId());
                        return false;
                    }
                    return true;
                })
                .map(guildFeature -> toConfiguration(guildFeature.getGuild(), guildFeature.getFeature()))
                .flatMap(Optional::stream)
                .toList();
    }

    @Transactional
    public void setFeatureEnabled(net.dv8tion.jda.api.entities.Guild jdaGuild, boolean enabled) {
        Guild guild = upsertGuild(jdaGuild);
        setFeatureEnabled(guild, enabled);
    }

    private Optional<ChangeRoleConfiguration> toConfiguration(Guild guild, Feature feature) {
        if (feature == null) {
            return Optional.empty();
        }

        FeatureConfigurationId id = new FeatureConfigurationId(guild.getId(), feature.getId());
        return changeRoleConfigurationRepository.findById(id)
                .flatMap(configuration -> {
                    GuildRole changeable = configuration.getChangeableRole();
                    GuildRole settable = configuration.getSettableRole();

                    if (changeable == null || settable == null) {
                        log.warn("Guild '{}' ({}) has change role feature enabled but configuration is incomplete.",
                                guild.getName(), guild.getId());
                        return Optional.empty();
                    }

                    return Optional.of(new ChangeRoleConfiguration(
                            guild.getId(),
                            guild.getName(),
                            changeable.getId(),
                            changeable.getName(),
                            settable.getId(),
                            settable.getName()
                    ));
                });
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

    private void setFeatureEnabled(Guild guild, boolean enabled) {
        Feature feature = ensureFeature();

        GuildFeature guildFeature = guildFeatureRepository
                .findByGuild_IdAndFeature_Name(guild.getId(), feature.getName())
                .orElseGet(() -> GuildFeature.builder()
                        .id(new GuildFeatureId(guild.getId(), feature.getId()))
                        .guild(guild)
                        .feature(feature)
                        .enabled(false)
                        .build());
        guildFeature.setEnabled(enabled);
        guildFeatureRepository.save(guildFeature);
    }

    private Feature ensureFeature() {
        return featureRepository.findByName(FeatureName.CHANGE_ROLE)
                .orElseGet(() -> featureRepository.save(Feature.builder()
                        .name(FeatureName.CHANGE_ROLE)
                        .enabled(true)
                        .build()));
    }
}
