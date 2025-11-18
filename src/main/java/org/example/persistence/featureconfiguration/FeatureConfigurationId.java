package org.example.persistence.featureconfiguration;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FeatureConfigurationId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "guild_id")
    private Long guildId;

    @Column(name = "feature_id")
    private Long featureId;
}
