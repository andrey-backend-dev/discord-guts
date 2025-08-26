CREATE TABLE users (
    id bigint primary key,
    username varchar not null unique
);

CREATE TABLE guild (
    id bigint primary key,
    name varchar not null
);

CREATE TABLE users_guild (
    user_id bigint not null references users(id) on delete cascade,
    guild_id bigint not null references guild(id) on delete cascade,
    primary key (user_id, guild_id)
);