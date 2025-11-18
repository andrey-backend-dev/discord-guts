CREATE TABLE feature_change_role_configuration (
    guild_id bigint not null references guild(id) on delete cascade,
    feature_id bigint not null references feature(id) on delete cascade,
    changeable_role_id bigint references guild_role(id) on delete set null,
    settable_role_id bigint references guild_role(id) on delete set null,
    primary key (guild_id, feature_id)
);

INSERT INTO feature (name) VALUES ('CHANGE_ROLE');