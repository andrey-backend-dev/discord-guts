package org.example.schedulers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeRoleScheduler {

    @Value("${scheduler.change-role.role.from-update}")
    private String fromRoleName;
    @Value("${scheduler.change-role.role.to-update}")
    private String toRoleName;
    private final JDA jda;

    private static final String ITERATION_MEMBERS_COUNT_TXT_FILENAME = "iteration-members-count.txt";

    @Scheduled(cron = "0 0 0 * * ?")
    public void changeRoleScheduled() {
        Guild guild = jda.getGuildsByName("dominance", false).getFirst();
        List<Member> ssMembers = getMembersWithRoleFrom(guild);
        Role toRole = jda.getRolesByName(toRoleName, false).getFirst();
        Member ssGodMember = guild.getMembersWithRoles(toRole).getFirst();

        var dayOfYear = LocalDate.now().getDayOfYear();
        var path = Paths.get(ITERATION_MEMBERS_COUNT_TXT_FILENAME);

        try {
            String iterationMembersCountStr = new String(Files.readAllBytes(path));
            int count = updateIterationMembersAndGetMembersCount(iterationMembersCountStr, ssMembers.size(), dayOfYear);
            Member memberWithNewRole = ssMembers.get(dayOfYear % count);
            guild.addRoleToMember(memberWithNewRole, toRole).queue(
                    _ -> log.info("{} role successfully given to {}.", toRoleName, memberWithNewRole.getNickname()),
                    failure -> log.error("Failed to give {} role to {}.", toRoleName, memberWithNewRole.getNickname(), failure)
            );
            guild.removeRoleFromMember(ssGodMember, toRole).queue(
                    _ -> log.info("{} role successfully removed from {}.", toRoleName, ssGodMember.getNickname()),
                    failure -> log.error("Failed to give {} role to {}.", toRoleName, ssGodMember.getNickname(), failure)
            );
        } catch(IOException e) {
            log.error("Failed to read iteration members count txt file.", e);
        }
    }

    private List<Member> getMembersWithRoleFrom(Guild guild) {
        Role role = jda.getRolesByName(fromRoleName, false).getFirst();
        List<Member> members = new ArrayList<>(guild.getMembersWithRoles(role));
        members.sort(Comparator.comparingLong(ISnowflake::getIdLong));
        return members;
    }

    private int updateIterationMembersAndGetMembersCount(String iterationMembersCountStr, int membersSize, int dayOfYear) {
        if (iterationMembersCountStr.isEmpty() ||
                (dayOfYear % Integer.parseInt(iterationMembersCountStr) == 0 && Integer.parseInt(iterationMembersCountStr) != membersSize)) {
            try {
                var path = Paths.get(ITERATION_MEMBERS_COUNT_TXT_FILENAME);
                Files.write(path, String.valueOf(membersSize).getBytes());
                log.info("New iteration members count has written successfully.");
            } catch (IOException e) {
                log.error("Failed to write iteration members count to txt file.", e);
            }
            return membersSize;
        }
        return Integer.parseInt(iterationMembersCountStr);
    }
}
