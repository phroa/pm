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
public class Project implements TextRepresentable {

    public String pluginId;
    public Date createdAt;
    public String name;
    public String owner;
    public String description;
    public String href;
    public List<ProjectMember> members;
    public List<ProjectChannel> channels;
    public ProjectVersion recommended;
    public Category category;
    public int views;
    public int downloads;
    public int stars;

    @Override
    public Text toText() {
        Text.Builder builder = Text.builder()
                .append(Text.of(TextColors.AQUA, name, TextColors.GRAY, " by ", TextColors.AQUA, owner,
                        TextColors.GRAY, " - since " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(createdAt)))
                .append(Text.NEW_LINE)
                .append(Text.of(TextColors.GRAY, "[" + pluginId + "]"))
                .append(Text.of(TextColors.GRAY, " (" + category.title + ")").toBuilder()
                        .onClick(TextActions.runCommand("/ore -c " + category.title.split(" ")[0].toLowerCase()))
                        .build());

        if (description != null) {
            builder.append(Text.NEW_LINE)
                    .append(Text.NEW_LINE)
                    .append(Text.of(description));
        }

        return builder.append(Text.NEW_LINE)
                .append(Text.NEW_LINE)
                .append(Text.of("Stars: ", TextColors.GOLD, stars))
                .append(Text.NEW_LINE)
                .append(Text.of("Downloads: ", TextColors.AQUA, downloads))
                .append(Text.NEW_LINE)
                .append(Text.of("Recommended Version: ", recommended).toBuilder()
                        .onClick(TextActions.runCommand("/ore install " + pluginId + " " + recommended.name))
                        .build())
                .build();
    }

    @Override
    public void applyTo(Text.Builder builder) {
        builder.append(toText());
    }
}
