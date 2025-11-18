package org.example.persistence.changerole;

import org.example.persistence.featureconfiguration.FeatureConfigurationId;
import org.springframework.data.repository.CrudRepository;

public interface FeatureChangeRoleConfigurationRepository extends CrudRepository<FeatureChangeRoleConfiguration, FeatureConfigurationId> {

    void deleteAllByGuild_Id(Long guildId);
}
