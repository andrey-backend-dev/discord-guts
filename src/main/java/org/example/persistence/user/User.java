package org.example.persistence.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.example.persistence.guild.Guild;
import org.example.persistence.m2m.userguild.UserGuild;
import org.example.persistence.m2m.userguildrole.UserGuildRole;
import org.example.persistence.role.GuildRole;

import java.util.List;

@Data
@Entity
@Table(name = "users")
@Accessors(chain = true)
public class User {
    @Id
    private Long id;

    private String username;

    @OneToMany(mappedBy = "user")
    private List<UserGuildRole> userGuildRoles;

    @OneToMany(mappedBy = "user")
    private List<UserGuild> userGuilds;

    public List<GuildRole> getRoles() {
        return userGuildRoles != null ? userGuildRoles.stream().map(UserGuildRole::getRole).toList() : null;
    }

    public List<Guild> getGuilds() {
        return userGuilds != null ? userGuilds.stream().map(UserGuild::getGuild).toList() : null;
    }
}
