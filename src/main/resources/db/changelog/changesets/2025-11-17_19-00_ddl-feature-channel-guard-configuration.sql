CREATE TABLE feature_channel_guard_configuration (
    guild_id bigint not null references guild(id) on delete cascade,
    feature_id bigint not null references feature(id) on delete cascade,
    text_channel_id bigint,
    media_channel_id bigint,
    music_channel_id bigint,
    primary key (guild_id, feature_id)
);

INSERT INTO feature(name) VALUES ('CHANNEL_GUARD');