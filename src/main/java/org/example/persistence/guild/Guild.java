package org.example.persistence.guild;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.role.GuildRole;
import org.example.persistence.user.User;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
public class Guild {
    @Id
    @SequenceGenerator(name = "guild_sequence_generator", sequenceName = "guild_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @OneToMany(mappedBy = "guild")
    private List<GuildRole> roles;
    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "guild_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> users;
}
