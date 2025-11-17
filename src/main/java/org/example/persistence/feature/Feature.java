package org.example.persistence.feature;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.guild.Guild;
import org.example.persistence.m2m.guildfeature.GuildFeature;

import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feature {
    @Id
    @SequenceGenerator(name = "featureSequence", sequenceName = "feature_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private FeatureName name;

    private boolean enabled;

    @OneToMany(mappedBy = "feature")
    private List<GuildFeature> guildFeatures;

    public List<Guild> getGuilds() {
        return guildFeatures != null ? guildFeatures.stream().map(GuildFeature::getGuild).toList() : null;
    }
}
