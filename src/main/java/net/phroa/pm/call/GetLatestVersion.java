package net.phroa.pm.call;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.phroa.pm.Pm;
import net.phroa.pm.command.InstallProject;
import net.phroa.pm.model.Project;
import net.phroa.pm.model.ProjectChannel;
import net.phroa.pm.model.ProjectVersion;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GetLatestVersion implements Consumer<ProjectChannel> {

    private final Pm pm;
    private final CommandSource src;
    private final Project project;
    private final String argProject;

    public GetLatestVersion(Pm pm, CommandSource src, Project project, String argProject) {
        this.pm = pm;
        this.src = src;
        this.project = project;
        this.argProject = argProject;
    }

    @Override
    public void accept(ProjectChannel c) {
        final Map<ProjectChannel, ProjectVersion> latestForChannel = Maps.newHashMap();
        CommandSource src1 = src;
        Map<ProjectChannel, ProjectVersion> latestForChannel1 = latestForChannel;
        Project project1 = project;
        pm.getOre().listProjectVersions(argProject, ImmutableMap.of("channels", c.name))
                .enqueue(new Callback<List<ProjectVersion>>() {
                    private final CommandSource src = src1;
                    private final ProjectChannel channel = c;
                    private final Map<ProjectChannel, ProjectVersion> latestForChannel = latestForChannel1;
                    private final Project project = project1;

                    @Override
                    public void onResponse(Call<List<ProjectVersion>> call, Response<List<ProjectVersion>> response) {
                        List<ProjectVersion> versions = response.body();
                        if (response.code() == 404 || versions == null || versions.isEmpty()) {
                            src.sendMessage(Text.of(channel.name + " doesn't exist."));
                            return;
                        }

                        latestForChannel.put(channel, versions.stream()
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
                .enqueue(new ReceiveFile(pm, src, version));

    }
}
