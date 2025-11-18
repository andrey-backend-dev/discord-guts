CREATE TABLE guild_role (
    id bigint primary key,
    name varchar not null,
    guild_id bigint not null references guild(id),
    UNIQUE (name, guild_id)
);

CREATE TABLE users_guild_role (
    user_id bigint not null references users(id) on delete cascade,
    guild_role_id bigint not null references guild_role(id) on delete cascade,
    primary key (user_id, guild_role_id)
);