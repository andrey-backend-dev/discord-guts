package org.example.persistence.guild;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.example.persistence.feature.Feature;
import org.example.persistence.m2m.guildfeature.GuildFeature;
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

    /**
     * Many-To-Many сущность guild_feature
     */
    @OneToMany(mappedBy = "guild")
    private List<GuildFeature> guildFeatures;

    public List<User> getUsers() {
        return userGuilds != null ? userGuilds.stream().map(UserGuild::getUser).toList() : null;
    }

    public List<Feature> getFeatures() {
        return guildFeatures != null ? guildFeatures.stream().map(GuildFeature::getFeature).toList() : null;
    }
}
