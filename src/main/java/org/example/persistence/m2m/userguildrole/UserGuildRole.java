package org.example.persistence.m2m.userguildrole;

import jakarta.persistence.*;
import lombok.*;
import org.example.persistence.role.GuildRole;
import org.example.persistence.user.User;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users_guild_role")
public class UserGuildRole {
    @EmbeddedId
    private UserGuildRoleId id;

    @ManyToOne
    @MapsId("userId")
    private User user;

    @ManyToOne
    @MapsId("guildRoleId")
    @JoinColumn(name = "guild_role_id")
    private GuildRole role;
}
