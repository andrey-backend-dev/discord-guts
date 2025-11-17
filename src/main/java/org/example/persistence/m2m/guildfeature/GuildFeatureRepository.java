package org.example.persistence.m2m.guildfeature;

import org.example.persistence.feature.FeatureName;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GuildFeatureRepository extends CrudRepository<GuildFeature, GuildFeatureId> {

    List<GuildFeature> findByFeature_NameAndEnabledTrue(FeatureName name);

    Optional<GuildFeature> findByGuild_IdAndFeature_Name(Long guildId, FeatureName name);

    void deleteAllByGuild_Id(Long guildId);
}
