package org.example.persistence.m2m.userguild;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.*;
import org.example.persistence.guild.Guild;
import org.example.persistence.user.User;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users_guild")
public class UserGuild {
    @EmbeddedId
    private UserGuildId id;

    @ManyToOne
    @MapsId("userId")
    private User user;

    @ManyToOne
    @MapsId("guildId")
    private Guild guild;
}
