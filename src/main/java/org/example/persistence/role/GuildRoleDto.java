package org.example.persistence.role;

import lombok.Data;
import org.example.persistence.guild.GuildDto;

@Data
public class GuildRoleDto {
    private Long id;
    private String name;
    private GuildDto guild;
    private boolean featureCrIsChangeable;
    private boolean featureCrIsSettable;
}
