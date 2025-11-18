package org.example.persistence.role;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.example.persistence.guild.Guild;
import org.example.persistence.m2m.userguildrole.UserGuildRole;
import org.example.persistence.user.User;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Accessors(chain = true)
public class GuildRole {
    @Id
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "guild_id")
    private Guild guild;

    /**
     * Many-To-Many сущность users_guild_role
     */
    @OneToMany(mappedBy = "role")
    private List<UserGuildRole> userGuildRoles;

    public List<User> getUsers() {
        return userGuildRoles != null ? userGuildRoles.stream().map(UserGuildRole::getUser).toList() : null;
    }
}
