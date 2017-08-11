package net.phroa.pm.call;

import net.phroa.pm.Pm;
import net.phroa.pm.command.InstallProject;
import net.phroa.pm.model.ProjectVersion;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetSpecificVersion implements Callback<ProjectVersion> {

    private final Pm pm;
    private final CommandSource src;
    private final String version;
    private final String project;

    public GetSpecificVersion(Pm pm, CommandSource src, String version, String project) {
        this.pm = pm;
        this.src = src;
        this.version = version;
        this.project = project;
    }

    @Override
    public void onResponse(Call<ProjectVersion> call, Response<ProjectVersion> response) {
        ProjectVersion version = response.body();
        if (response.code() == 404 || version == null) {
            src.sendMessage(Text.of(this.version + " doesn't exist."));
            return;
        }

        src.sendMessage(Text.of(TextColors.GRAY, "Download queued for " + project + " @ " + version.name));
        pm.getOre().download(project, version.name).enqueue(new ReceiveFile(pm, src, version));
    }

    @Override
    public void onFailure(Call<ProjectVersion> call, Throwable t) {
        src.sendMessage(Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
        t.printStackTrace();
    }
}
