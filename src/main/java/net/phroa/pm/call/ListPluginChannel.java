package net.phroa.pm.call;

import com.google.common.collect.ImmutableMap;
import net.phroa.pm.Pm;
import net.phroa.pm.model.Project;
import net.phroa.pm.model.ProjectChannel;
import net.phroa.pm.model.ProjectVersion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ListPluginChannel implements Consumer<CommandSource> {

    private final Pm pm;
    private final Project project;
    private final ProjectChannel c;

    public ListPluginChannel(Pm pm, Project project, ProjectChannel c) {
        this.pm = pm;
        this.project = project;
        this.c = c;
    }

    @Override
    public void accept(CommandSource src) {
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
                        src.sendMessage(
                                Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
                        t.printStackTrace();
                    }
                });
    }
}
