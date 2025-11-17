package org.example.features.changerole;

public record ChangeRoleConfiguration(
        long guildId,
        String guildName,
        long changeableRoleId,
        String changeableRoleName,
        long settableRoleId,
        String settableRoleName
) {
}
