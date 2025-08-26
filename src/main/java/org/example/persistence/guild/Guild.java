package org.example.persistence.guild;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.example.persistence.m2m.userguild.UserGuild;
import org.example.persistence.role.GuildRole;
import org.example.persistence.user.User;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
public class Guild {
    @Id
    private Long id;

    private String name;

    @OneToMany(mappedBy = "guild")
    private List<GuildRole> roles;

    /**
     * Many-To-Many сущность users_guild
     */
    @OneToMany(mappedBy = "guild")
    private List<UserGuild> userGuilds;

    public List<User> getUsers() {
        return userGuilds != null ? userGuilds.stream().map(UserGuild::getUser).toList() : null;
    }
}
