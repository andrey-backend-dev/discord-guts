package org.example.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.example.features.changerole.ChangeRoleConfiguration;
import org.example.features.changerole.ChangeRoleFeatureService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeRoleScheduler {

    private final JDA jda;
    private final ChangeRoleFeatureService changeRoleFeatureService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void changeRoleScheduled() {
        changeRoleFeatureService.findEnabledConfigurations().forEach(this::changeRoleScheduled);
    }

    private void changeRoleScheduled(ChangeRoleConfiguration configuration) {
        Guild guild = jda.getGuildById(configuration.guildId());
        if (guild == null) {
            log.warn("Guild with id '{}' is no longer available for role rotation.", configuration.guildId());
            return;
        }

        Role changeableRole = guild.getRoleById(configuration.changeableRoleId());
        if (changeableRole == null) {
            log.warn("Guild '{}' lost changeable role '{}' ({}). Skipping.",
                    configuration.guildName(), configuration.changeableRoleName(), configuration.changeableRoleId());
            return;
        }

        Role settableRole = guild.getRoleById(configuration.settableRoleId());
        if (settableRole == null) {
            log.warn("Guild '{}' lost target role '{}' ({}). Skipping.",
                    configuration.guildName(), configuration.settableRoleName(), configuration.settableRoleId());
            return;
        }

        List<Member> candidates = getEligibleMembers(guild, changeableRole);
        if (candidates.isEmpty()) {
            log.warn("Guild '{}' has no members with '{}' role.", guild.getName(), changeableRole.getName());
            return;
        }

        Member nextMember = pickMemberForToday(candidates);
        Member currentHolder = guild.getMembersWithRoles(settableRole).stream().findFirst().orElse(null);

        if (currentHolder != null && currentHolder.getIdLong() == nextMember.getIdLong()) {
            log.debug("Guild '{}' already has '{}' role assigned to '{}'. Skipping rotation.",
                    guild.getName(), settableRole.getName(), nextMember.getEffectiveName());
            return;
        }

        assignRole(guild, settableRole, currentHolder, nextMember);
    }

    private List<Member> getEligibleMembers(Guild guild, Role role) {
        return guild.getMembersWithRoles(role).stream()
                .sorted((first, second) -> Long.compare(first.getIdLong(), second.getIdLong()))
                .collect(Collectors.toList());
    }

    private Member pickMemberForToday(List<Member> members) {
        int dayOfYear = LocalDate.now().getDayOfYear();
        int memberIndex = dayOfYear % members.size();
        return members.get(memberIndex);
    }

    private void assignRole(Guild guild, Role targetRole, Member currentHolder, Member nextMember) {
        guild.addRoleToMember(nextMember, targetRole).queue(
                _ -> log.info("Role '{}' granted to '{}' in guild '{}'.", targetRole.getName(),
                        nextMember.getEffectiveName(), guild.getName()),
                failure -> log.error("Failed to grant role '{}' to '{}' in guild '{}'.", targetRole.getName(),
                        nextMember.getEffectiveName(), guild.getName(), failure)
        );

        if (currentHolder != null) {
            guild.removeRoleFromMember(currentHolder, targetRole).queue(
                    _ -> log.info("Role '{}' removed from '{}' in guild '{}'.", targetRole.getName(),
                            currentHolder.getEffectiveName(), guild.getName()),
                    failure -> log.error("Failed to remove role '{}' from '{}' in guild '{}'.", targetRole.getName(),
                            currentHolder.getEffectiveName(), guild.getName(), failure)
            );
        }
    }
}
