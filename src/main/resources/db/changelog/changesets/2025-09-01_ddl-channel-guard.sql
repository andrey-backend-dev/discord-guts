CREATE TABLE channel_guard_configuration (
    guild_id bigint primary key references guild(id) on delete cascade,
    text_channel_id bigint,
    media_channel_id bigint,
    music_channel_id bigint
);
