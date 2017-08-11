package net.phroa.pm.command;

import net.phroa.pm.call.GetLatestVersion;
import net.phroa.pm.call.GetSpecificVersion;
import net.phroa.pm.call.ReceiveFile;
import net.phroa.pm.Pm;
import net.phroa.pm.model.Project;
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
                                    .enqueue(new ReceiveFile(pm, src, project.recommended));
                        } else if (argVersion.equalsIgnoreCase("latest")) {
                            project.channels.forEach(new GetLatestVersion(pm, src, project, argProject));
                        } else {
                            pm.getOre().getProjectVersion(argProject, argVersion)
                                    .enqueue(new GetSpecificVersion(pm, src, argVersion, argProject));
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
