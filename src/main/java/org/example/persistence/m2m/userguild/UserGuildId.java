package org.example.persistence.m2m.userguild;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class UserGuildId {
    private long userId;
    private long guildId;
}
