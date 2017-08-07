package net.phroa.pm.model;

import com.google.common.collect.ImmutableMap;
import lombok.Data;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

@Data
public class ProjectChannel implements TextRepresentable {

    @SuppressWarnings("unchecked")
    public static final ImmutableMap<String, TextColor> MAPPING = new ImmutableMap.Builder()
            .put("#B400FF", TextColors.DARK_PURPLE)
            .put("#C87DFF", TextColors.LIGHT_PURPLE)
            .put("#E100E1", TextColors.LIGHT_PURPLE)
            .put("#0000FF", TextColors.DARK_BLUE)
            .put("#0096FF", TextColors.BLUE)
            .put("#00E1E1", TextColors.AQUA)
            .put("#00DC00", TextColors.GREEN)
            .put("#009600", TextColors.DARK_GREEN)
            .put("#7FFF00", TextColors.GREEN)
            .put("#FFC800", TextColors.YELLOW)
            .put("#FF8200", TextColors.GOLD)
            .put("#DC0000", TextColors.RED)
            .build();

    public String name;
    public String color;

    @Override
    public Text toText() {
        return Text.of(MAPPING.getOrDefault(color.toUpperCase(), TextColors.NONE), "(" + name + ")");
    }

    @Override
    public void applyTo(Text.Builder builder) {
        builder.append(toText());
    }
}
