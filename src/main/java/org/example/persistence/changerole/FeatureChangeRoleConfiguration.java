package org.example.persistence.changerole;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.example.persistence.feature.Feature;
import org.example.persistence.featureconfiguration.FeatureConfigurationId;
import org.example.persistence.guild.Guild;
import org.example.persistence.role.GuildRole;

@Data
@Entity
@Table(name = "feature_change_role_configuration")
@Accessors(chain = true)
public class FeatureChangeRoleConfiguration {

    @EmbeddedId
    private FeatureConfigurationId id;

    @MapsId("guildId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    private Guild guild;

    @MapsId("featureId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id")
    private Feature feature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changeable_role_id")
    private GuildRole changeableRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settable_role_id")
    private GuildRole settableRole;
}
