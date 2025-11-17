package org.example.persistence.m2m.guildfeature;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Embeddable
@AllArgsConstructor
public class GuildFeatureId {
    private Long guildId;
    private Long featureId;
}
