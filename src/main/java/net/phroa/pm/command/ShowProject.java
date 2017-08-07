package net.phroa.pm.command;

import com.google.common.collect.ImmutableMap;
import net.phroa.pm.Pm;
import net.phroa.pm.model.Project;
import net.phroa.pm.model.ProjectVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShowProject implements CommandExecutor {

    private final Pm pm;

    public ShowProject(Pm pm) {
        this.pm = pm;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String argProject = args.<String>getOne("project").orElse("");

        src.sendMessage(Pm.LOADING);

        pm.getOre().getProject(argProject)
                .enqueue(new Callback<Project>() {
                    @Override
                    public void onResponse(Call<Project> call, Response<Project> response) {
                        if (response.code() == 404) {
                            src.sendMessage(Text.of(argProject + " doesn't exist."));
                            return;
                        }

                        Project project = response.body();
                        Text.Builder builder = Text.builder();
                        project.applyTo(builder);
                        builder.append(Text.NEW_LINE)
                                .append(Text.of("Members:"));
                        project.members.forEach(m -> builder.append(Text.NEW_LINE).append(m.toText()));
                        builder.append(Text.NEW_LINE)
                                .append(Text.of("Channels:"));
                        project.channels.forEach(c -> builder.append(Text.of(" ")).append(c.toText().toBuilder()
                                .onClick(TextActions.executeCallback(src -> {
                                    src.sendMessage(Pm.LOADING);
                                    pm.getOre().listProjectVersions(project.pluginId, ImmutableMap.of("channels", c.name))
                                            .enqueue(new Callback<List<ProjectVersion>>() {
                                                @Override
                                                public void onResponse(Call<List<ProjectVersion>> call, Response<List<ProjectVersion>> response) {
                                                    if (response.code() == 404) {
                                                        src.sendMessage(Text.of(c.name + " doesn't exist."));
                                                        return;
                                                    }

                                                    List<Text> versions = response.body().stream()
                                                            .map(ProjectVersion::toText)
                                                            .collect(Collectors.toList());
                                                    if (versions.size() > 3) {
                                                        Sponge.getServiceManager().provideUnchecked(PaginationService.class).builder()
                                                                .title(Text.of(project.name + " ", c))
                                                                .contents(versions)
                                                                .build()
                                                                .sendTo(src);
                                                    } else {
                                                        src.sendMessage(Text.joinWith(Text.NEW_LINE, versions));
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<List<ProjectVersion>> call, Throwable t) {
                                                    src.sendMessage(Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
                                                    t.printStackTrace();
                                                }
                                            });
                                }))
                                .build()));
                        Optional<PluginContainer> plugin = Sponge.getPluginManager().getPlugin(project.pluginId);
                        plugin.ifPresent(pluginContainer -> builder.append(Text.NEW_LINE)
                                .append(Text.NEW_LINE)
                                .append(Text.of("Version ", TextColors.GOLD, pluginContainer.getVersion().orElse("?"),
                                        TextColors.RESET, " installed.")));
                        src.sendMessage(builder.build());
                    }

                    @Override
                    public void onFailure(Call<Project> call, Throwable t) {
                        src.sendMessage(Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
                        t.printStackTrace();
                    }
                });

        return CommandResult.empty();
    }
}
