package org.example.persistence.m2m.guildfeature;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
    private Guild guild;

    @ManyToOne
    private Feature feature;
}
