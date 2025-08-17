package org.example.persistence.role;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.persistence.guild.Guild;

@Data
@Entity
@NoArgsConstructor
public class GuildRole {
    @Id
    @SequenceGenerator(name = "guild_role_sequence_generator", sequenceName = "guild_role_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @ManyToOne
    @JoinTable(
            joinColumns = @JoinColumn(name = "guild_role_id"),
            inverseJoinColumns = @JoinColumn(name = "guild_id")
    )
    private Guild guild;
    private boolean featureCrIsChangeable;
    private boolean featureCrIsSettable;
}
