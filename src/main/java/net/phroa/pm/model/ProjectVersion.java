package net.phroa.pm.model;

import lombok.Data;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

@Data
public class ProjectVersion implements Comparable, TextRepresentable {

    public int id;
    public Date createdAt;
    public String name;
    public List<Dependency> dependencies;
    public String pluginId;
    public ProjectChannel channel;
    public int fileSize;
    public String md5;
    public boolean staffApproved;

    @Override
    public Text toText() {
        Text.Builder builder = Text.builder()
                .append(Text.of(TextColors.AQUA, name + " ", channel, " ").toBuilder()
                        .onClick(TextActions.runCommand("/ore install " + pluginId + " " + name))
                        .onHover(TextActions.showText(Text.of("Published " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(createdAt))))
                        .build());
        if (staffApproved) {
            builder.append(Text.of(TextColors.GREEN, "(Approved)"));
        } else {
            builder.append(Text.of(TextColors.RED, "(NOT YET APPROVED)"));
        }
        builder.append(Text.NEW_LINE)
                .append(Text.of("Dependencies: "));
        dependencies.forEach(dependency -> builder.append(Text.NEW_LINE).append(Text.of(dependency)));

        return builder.build();
    }

    @Override
    public void applyTo(Text.Builder builder) {
        builder.append(toText());
    }

    @Override
    public int compareTo(Object o) {
        // Dates reversed on purpose.
        return o instanceof ProjectVersion ? ((ProjectVersion) o).createdAt.compareTo(this.createdAt) : 0;
    }
}
