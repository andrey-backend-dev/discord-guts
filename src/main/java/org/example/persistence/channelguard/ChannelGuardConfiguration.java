package org.example.persistence.channelguard;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Entity
@Table(name = "channel_guard_configuration")
@Accessors(chain = true)
public class ChannelGuardConfiguration {

    @Id
    @Column(name = "guild_id")
    private Long guildId;

    @Column(name = "text_channel_id")
    private Long textChannelId;

    @Column(name = "media_channel_id")
    private Long mediaChannelId;

    @Column(name = "music_channel_id")
    private Long musicChannelId;
}
