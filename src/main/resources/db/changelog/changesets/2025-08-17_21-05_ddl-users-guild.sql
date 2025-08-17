CREATE TABLE users (
    id bigint primary key,
    discord_id bigint not null unique,
    username varchar not null unique
);

CREATE TABLE guild (
    id bigint primary key,
    name varchar not null
);

CREATE TABLE users_guild (
    id bigint primary key,
    user_id bigint not null references users(id),
    guild_id bigint not null references guild(id),
    UNIQUE (user_id, guild_id)
);