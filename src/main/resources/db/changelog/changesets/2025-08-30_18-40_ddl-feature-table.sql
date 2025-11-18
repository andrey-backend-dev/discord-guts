CREATE TABLE feature (
    id bigint generated always as identity primary key,
    name varchar not null unique,
    enabled boolean not null default true
);

CREATE TABLE guild_feature (
    guild_id bigint not null references guild(id) on delete cascade,
    feature_id bigint not null references feature(id) on delete cascade,
    enabled boolean not null default true,
    primary key (guild_id, feature_id)
);