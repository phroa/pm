package net.phroa.pm.command;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.phroa.pm.DownloadCallback;
import net.phroa.pm.Pm;
import net.phroa.pm.model.Project;
import net.phroa.pm.model.ProjectChannel;
import net.phroa.pm.model.ProjectVersion;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.Map;

public class InstallProject implements CommandExecutor {

    private final Pm pm;

    public InstallProject(Pm pm) {
        this.pm = pm;
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        String argProject = args.<String>getOne("project").orElse("");
        String argVersion = args.<String>getOne("version").orElse("recommended");

        pm.getOre().getProject(argProject)
                .enqueue(new Callback<Project>() {
                    @Override
                    public void onResponse(Call<Project> call, Response<Project> response) {
                        Project project = response.body();
                        if (response.code() == 404 || project == null) {
                            src.sendMessage(Text.of(argProject + " doesn't exist."));
                            return;
                        }

                        if (argVersion.equalsIgnoreCase("recommended")) {
                            src.sendMessage(Text.of(TextColors.GRAY, "Download queued for " + argProject + " @ " + project.recommended.name));
                            pm.getOre().download(argProject, project.recommended.name)
                                    .enqueue(new DownloadCallback(pm, src, project.recommended));
                        } else if (argVersion.equalsIgnoreCase("latest")) {
                            project.channels.forEach(c -> {
                                final Map<ProjectChannel, ProjectVersion> latestForChannel = Maps.newHashMap();
                                pm.getOre().listProjectVersions(argProject, ImmutableMap.of("channels", c.name))
                                        .enqueue(new Callback<List<ProjectVersion>>() {
                                            @Override
                                            public void onResponse(Call<List<ProjectVersion>> call, Response<List<ProjectVersion>> response) {
                                                List<ProjectVersion> versions = response.body();
                                                if (response.code() == 404 || versions == null || versions.isEmpty()) {
                                                    src.sendMessage(Text.of(c.name + " doesn't exist."));
                                                    return;
                                                }

                                                latestForChannel.put(c, versions.stream()
                                                        .sorted()
                                                        .findFirst()
                                                        .orElse(project.recommended));
                                            }

                                            @Override
                                            public void onFailure(Call<List<ProjectVersion>> call, Throwable t) {
                                                src.sendMessage(Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
                                                t.printStackTrace();
                                            }
                                        });

                                ProjectVersion version = latestForChannel.values()
                                        .stream()
                                        .sorted()
                                        .findFirst()
                                        .get();

                                src.sendMessage(Text.of(TextColors.GRAY, "Download queued for " + argProject + " @ " + version.name));
                                pm.getOre().download(argProject, version.name)
                                        .enqueue(new DownloadCallback(pm, src, version));

                            });
                        } else {
                            pm.getOre().getProjectVersion(argProject, argVersion)
                                    .enqueue(new Callback<ProjectVersion>() {
                                        @Override
                                        public void onResponse(Call<ProjectVersion> call, Response<ProjectVersion> response) {
                                            ProjectVersion version = response.body();
                                            if (response.code() == 404 || version == null) {
                                                src.sendMessage(Text.of(argVersion + " doesn't exist."));
                                                return;
                                            }

                                            src.sendMessage(Text.of(TextColors.GRAY, "Download queued for " + argProject + " @ " + version.name));
                                            pm.getOre().download(argProject, version.name)
                                                    .enqueue(new DownloadCallback(pm, src, version));
                                        }

                                        @Override
                                        public void onFailure(Call<ProjectVersion> call, Throwable t) {
                                            src.sendMessage(Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
                                            t.printStackTrace();
                                        }
                                    });
                        }
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
