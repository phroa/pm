package net.phroa.pm.model;

import lombok.Data;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

@Data
public class ProjectMember implements TextRepresentable {

    public int userId;
    public String name;
    public List<String> roles;
    public String headRole;

    @Override
    public Text toText() {
        Text.Builder builder = Text.builder()
                .append(Text.of("- ", TextColors.GOLD, headRole + " ", TextColors.AQUA, name));
        roles.forEach(s -> {
            if (!s.equalsIgnoreCase(headRole)) {
                builder.append(Text.of(TextColors.GRAY, " (" + s + ")"));
            }
        });
        return builder.build();
    }

    @Override
    public void applyTo(Text.Builder builder) {
        builder.append(toText());
    }
}
