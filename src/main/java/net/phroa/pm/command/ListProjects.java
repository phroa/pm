package net.phroa.pm.command;

import com.google.common.collect.ImmutableMap;
import net.phroa.pm.Pm;
import net.phroa.pm.model.Ore;
import net.phroa.pm.model.Project;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListProjects implements CommandExecutor {

    private final Pm pm;
    private final Ore.Sorting sorting;

    public ListProjects(Pm pm, Ore.Sorting sorting) {
        this.pm = pm;
        this.sorting = sorting;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Collection<Ore.Category> argCategory = args.getAll("category");
        Collection<Ore.Sorting> argSorting = args.getAll("sorting");
        Optional<String> argSearch = args.getOne("search");
        String categories;
        if (argCategory.isEmpty()) {
            categories = Arrays.asList(Ore.Category.values()).parallelStream()
                    .map(category -> Integer.toString(category.value))
                    .collect(Collectors.joining(","));
        } else {
            categories = argCategory.parallelStream()
                    .map(category -> Integer.toString(category.value))
                    .collect(Collectors.joining(","));
        }
        Ore.Sorting sorting;
        if (argSorting.isEmpty()) {
            sorting = Ore.Sorting.MostStars;
        } else {
            sorting = argSorting.toArray(new Ore.Sorting[0])[0];
        }

        src.sendMessage(Pm.LOADING);

        pm.getOre().listProjects(ImmutableMap.of(
                "categories", categories,
                "sort", Integer.toString(sorting.value),
                "limit", Integer.toString(args.<Integer>getOne("limit").orElse(25)),
                "q", argSearch.orElse("")))
                .enqueue(new Callback<List<Project>>() {
                    @Override
                    public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                        List<Project> projects = response.body();
                        Text.Builder header = Text.of(TextColors.GRAY, "Sorted by ", TextColors.AQUA, sorting.longName, Text.NEW_LINE).toBuilder();
                        if (argCategory.isEmpty()) {
                            header.append(Text.of(TextColors.GRAY, "Categories: ", TextColors.AQUA, "All"));
                        } else {
                            header.append(Text.of(TextColors.GRAY, "Categories: ", TextColors.AQUA, argCategory.parallelStream()
                                    .map(category -> category.longName)
                                    .collect(Collectors.joining(", "))));
                        }
                        argSearch.ifPresent(s -> header.append(Text.NEW_LINE).append(Text.of(TextColors.GRAY, "Search: ", TextColors.WHITE, s)));

                        Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder()
                                .title(Text.of(TextColors.GOLD, "Ore Projects"))
                                .header(header.build())
                                .contents(projects.parallelStream().map(project -> Text.builder()
                                        .onHover(TextActions.showText(
                                                Text.of(project)))
                                        .onClick(TextActions.runCommand("/ore info " + project.pluginId))
                                        .append(Text.of(TextColors.GOLD, project.stars + "*", TextColors.RESET, "  " + project.name,
                                                TextColors.GRAY, " by " + project.owner))
                                        .build()).collect(Collectors.toList()))
                                .build()
                                .sendTo(src);
                    }

                    @Override
                    public void onFailure(Call<List<Project>> call, Throwable t) {
                        src.sendMessage(Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
                        t.printStackTrace();
                    }
                });
        return CommandResult.empty();
    }
}
