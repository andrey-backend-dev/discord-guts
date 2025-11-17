package org.example.persistence.m2m.guildfeature;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.feature.Feature;
import org.example.persistence.guild.Guild;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuildFeature {
    @EmbeddedId
    private GuildFeatureId id;

    @ManyToOne
    @MapsId("guildId")
    private Guild guild;

    @ManyToOne
    @MapsId("featureId")
    private Feature feature;

    @Column(nullable = false)
    private boolean enabled;
}
