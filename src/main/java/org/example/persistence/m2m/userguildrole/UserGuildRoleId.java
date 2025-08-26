package org.example.persistence.m2m.userguildrole;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class UserGuildRoleId {
    private long userId;
    private long guildRoleId;
}
