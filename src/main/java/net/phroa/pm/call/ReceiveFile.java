package net.phroa.pm.call;

import com.google.gson.Gson;
import net.phroa.pm.Pm;
import net.phroa.pm.model.DownloadConfirmation;
import net.phroa.pm.model.ProjectVersion;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.function.Consumer;

import javax.xml.bind.DatatypeConverter;

public class ReceiveFile implements Callback<byte[]> {

    private final Pm pm;
    private final CommandSource src;
    private final ProjectVersion version;

    public ReceiveFile(Pm pm, CommandSource src, ProjectVersion version) {
        this.pm = pm;
        this.src = src;
        this.version = version;
    }

    @Override
    public void onResponse(Call<byte[]> call, Response<byte[]> response) {
        if (response.code() == 404) {
            src.sendMessage(Text.of(version.name + " doesn't exist."));
            return;
        }

        if (response.code() == 300) {
            src.sendMessage(Text.of(TextColors.RED, "Warning!"));
            src.sendMessage(Text.NEW_LINE);
            src.sendMessage(Text.of("This version of this plugin hasn't yet been approved by Sponge staff."));
            src.sendMessage(Text.of("Sponge, pm, and phroa disclaim all responsibility for any harm to your server or system "
                    + "should you choose not to heed this warning."));
            src.sendMessage(Text.NEW_LINE);
            src.sendMessage(Text.of("Continue anyway? ", Text.of("[I Understand]").toBuilder()
                    .color(TextColors.YELLOW)
                    .onClick(TextActions.executeCallback(run(version, response)))
                    .build()));
        } else {
            run(version, response).accept(src);
        }
    }

    private Consumer<CommandSource> run(ProjectVersion version, Response<byte[]> response) {
        return src -> {
            src.sendMessage(Pm.LOADING);
            try {
                byte[] body = response.body();
                String contentDisposition;
                if (response.code() == 300) {
                    okhttp3.Response response1 = pm.getOreClient().newCall(new Request.Builder()
                            .url(response.raw().request().url())
                            .build())
                            .execute();

                    DownloadConfirmation confirmation = new Gson().fromJson(response1.body().string(), DownloadConfirmation.class);
                    okhttp3.Response response2 = pm.getOreClient().newCall(new Request.Builder()
                            .post(RequestBody.create(MediaType.parse("text/plain"), "hi=meow"))
                            .url(confirmation.post)
                            .build())
                            .execute();

                    okhttp3.Response response3 = pm.getOreClient().newCall(new Request.Builder()
                            .url(response2.request().url())
                            .build())
                            .execute();

                    contentDisposition = response3.header("Content-Disposition");
                    body = response3.body().bytes();
                } else {
                    contentDisposition = response.headers().get("Content-Disposition");
                }
                String filename = contentDisposition.substring(contentDisposition.indexOf('"') + 1,
                        contentDisposition.lastIndexOf('"'));

                MessageDigest d = MessageDigest.getInstance("MD5");
                d.update(body);
                String md5 = DatatypeConverter.printHexBinary(d.digest());

                if (md5.equalsIgnoreCase(version.md5)) {
                    Path path = Sponge.getPluginManager().fromInstance(pm).get().getSource().get().getParent().resolve(filename);
                    src.sendMessage(Text.of(TextColors.GRAY, "Saving to " + path));
                    Files.write(path, body, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                    src.sendMessage(Text.of(TextColors.GRAY, "The plugin has been downloaded.  It will be inactive until the server is restarted."));
                } else {
                    src.sendMessage(Text.of(TextColors.RED, "The MD5 hash", TextColors.GRAY, " (" + md5.toLowerCase() + ") ",
                            TextColors.RED, "didn't match the expected result!", TextColors.GRAY, " (" + version.md5.toLowerCase() + ") "));
                    src.sendMessage(Text.of(TextColors.GOLD, "[Try Again]").toBuilder()
                            .onClick(TextActions.runCommand("/ore install " + version.pluginId + " " + version.name))
                            .build());
                }
            } catch (Exception e) {
                e.printStackTrace();
                src.sendMessage(Text.of(TextColors.RED, "Internal error: " + e.getLocalizedMessage()));
                src.sendMessage(Text.of(TextColors.GOLD, "[Try Again]").toBuilder()
                        .onClick(TextActions.runCommand("/ore install " + version.pluginId + " " + version.name))
                        .build());
            }
        };
    }

    @Override
    public void onFailure(Call<byte[]> call, Throwable t) {
        src.sendMessage(Text.of(TextColors.RED, "Network request failure: " + t.getLocalizedMessage()));
        t.printStackTrace();
    }
}
