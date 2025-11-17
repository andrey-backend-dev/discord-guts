package org.example.features.channelguard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.example.persistence.channelguard.ChannelGuardConfiguration;
import org.example.persistence.channelguard.ChannelGuardConfigurationRepository;
import org.example.persistence.feature.Feature;
import org.example.persistence.feature.FeatureName;
import org.example.persistence.feature.FeatureRepository;
import org.example.persistence.guild.GuildRepository;
import org.example.persistence.m2m.guildfeature.GuildFeature;
import org.example.persistence.m2m.guildfeature.GuildFeatureId;
import org.example.persistence.m2m.guildfeature.GuildFeatureRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelGuardFeatureService {

    private final GuildRepository guildRepository;
    private final FeatureRepository featureRepository;
    private final GuildFeatureRepository guildFeatureRepository;
    private final ChannelGuardConfigurationRepository configurationRepository;

    @Transactional
    public void setFeatureEnabled(Guild jdaGuild, boolean enabled) {
        org.example.persistence.guild.Guild guild = upsertGuild(jdaGuild);
        setFeatureEnabled(guild, enabled);
    }

    @Transactional
    public void updateConfiguration(Guild jdaGuild,
                                    @Nullable GuildMessageChannel textChannel,
                                    @Nullable GuildMessageChannel mediaChannel,
                                    @Nullable GuildMessageChannel musicChannel) {
        org.example.persistence.guild.Guild guild = upsertGuild(jdaGuild);
        ChannelGuardConfiguration configuration = configurationRepository.findById(guild.getId())
                .orElseGet(() -> new ChannelGuardConfiguration().setGuildId(guild.getId()));

        if (textChannel != null) {
            configuration.setTextChannelId(textChannel.getIdLong());
        }
        if (mediaChannel != null) {
            configuration.setMediaChannelId(mediaChannel.getIdLong());
        }
        if (musicChannel != null) {
            configuration.setMusicChannelId(musicChannel.getIdLong());
        }

        configurationRepository.save(configuration);
    }

    @Transactional(readOnly = true)
    public Optional<ChannelGuardConfiguration> findConfiguration(long guildId) {
        return configurationRepository.findById(guildId);
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(long guildId) {
        boolean globalEnabled = featureRepository.findByName(FeatureName.CHANNEL_GUARD)
                .map(Feature::isEnabled)
                .orElse(false);
        if (!globalEnabled) {
            return false;
        }
        return guildFeatureRepository.findByGuild_IdAndFeature_Name(guildId, FeatureName.CHANNEL_GUARD)
                .map(GuildFeature::isEnabled)
                .orElse(false);
    }

    private org.example.persistence.guild.Guild upsertGuild(Guild jdaGuild) {
        return guildRepository.findById(jdaGuild.getIdLong())
                .map(existing -> existing.setName(jdaGuild.getName()))
                .orElseGet(() -> guildRepository.save(new org.example.persistence.guild.Guild()
                        .setId(jdaGuild.getIdLong())
                        .setName(jdaGuild.getName())));
    }

    private void setFeatureEnabled(org.example.persistence.guild.Guild guild, boolean enabled) {
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
        return featureRepository.findByName(FeatureName.CHANNEL_GUARD)
                .orElseGet(() -> featureRepository.save(Feature.builder()
                        .name(FeatureName.CHANNEL_GUARD)
                        .enabled(true)
                        .build()));
    }
}
