package net.phroa.pm.model;

import lombok.Data;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.plugin.meta.version.DefaultArtifactVersion;
import org.spongepowered.plugin.meta.version.InvalidVersionSpecificationException;
import org.spongepowered.plugin.meta.version.VersionRange;

import java.util.Optional;

@Data
public class Dependency implements TextRepresentable {

    public String pluginId;
    public String version;

    @Override
    public Text toText() {
        LiteralText.Builder builder = Text.of("- " + pluginId + " @ " + version).toBuilder()
                .onClick(TextActions.runCommand("/ore info " + pluginId));

        try {
            switch (dependencyStatus(this)) {
                case MET:
                    builder.color(TextColors.GREEN);
                    builder.onHover(TextActions.showText(Text.of("A compatible version of " + pluginId + " is already installed.")));
                    break;
                case INCOMPATIBLE:
                    builder.color(TextColors.YELLOW);
                    builder.onHover(TextActions.showText(Text.of(pluginId + " is already installed but may not be compatible.")));
                    break;
                case UNMET:
                    builder.color(TextColors.RED);
                    builder.onHover(TextActions.showText(Text.of(pluginId + " isn't installed.")));
            }
        } catch (InvalidVersionSpecificationException e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    private Status dependencyStatus(Dependency dependency) throws InvalidVersionSpecificationException {
        Optional<PluginContainer> container = Sponge.getPluginManager().getPlugin(dependency.pluginId);
        if (container.isPresent()) {
            VersionRange versionSpec = VersionRange.createFromVersionSpec(dependency.version);
            DefaultArtifactVersion pluginArtifactVersion = new DefaultArtifactVersion(container.get().getVersion().orElse(""));
            if (versionSpec.containsVersion(pluginArtifactVersion)) {
                return Status.MET;
            }
            return Status.INCOMPATIBLE;
        }
        return Status.UNMET;
    }

    @Override
    public void applyTo(Text.Builder builder) {
        builder.append(toText());
    }

    public enum Status {
        MET,
        INCOMPATIBLE,
        UNMET,
    }
}
