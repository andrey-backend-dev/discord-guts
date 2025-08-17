package org.example.persistence.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @SequenceGenerator(name = "guild_sequence_generator", sequenceName = "guild_id_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private long discordId;
    private String username;
}
