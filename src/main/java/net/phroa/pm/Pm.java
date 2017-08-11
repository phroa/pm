package net.phroa.pm;

import static org.spongepowered.api.command.args.GenericArguments.choices;
import static org.spongepowered.api.command.args.GenericArguments.flags;
import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.none;
import static org.spongepowered.api.command.args.GenericArguments.onlyOne;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.remainingJoinedStrings;
import static org.spongepowered.api.command.args.GenericArguments.string;

import com.google.common.collect.ImmutableList;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import net.phroa.pm.command.InstallProject;
import net.phroa.pm.command.ListProjects;
import net.phroa.pm.command.ShowProject;
import net.phroa.pm.model.Ore;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Plugin(
        id = "pm",
        name = "pm",
        description = "Package manager for Sponge",
        authors = {
                "phroa"
        }
)
public class Pm {

    public static final String USAGE = "Usage:\n"
            + "\n"
            + "/ore [-c category] [-s sorting] [-l limit] [search term...] - List plugins by category\n"
            + "/ore info <pluginid> - Get info on a plugin\n"
            + "/ore install <pluginid> [version|recommended|latest] - Install a plugin\n"
            + "\n"
            + "Most everything is done through clickable menus from the main /ore command.";
    public static final Text LOADING = Text.of(TextColors.GRAY, "Loading...");

    @Inject
    private Logger logger;

    private Ore ore;
    private OkHttpClient oreClient;

    @Listener
    public void onServerStart(GamePreInitializationEvent event) {

        oreClient = new OkHttpClient.Builder()
                .connectionSpecs(ImmutableList.of(ConnectionSpec.MODERN_TLS))
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder()
                        .header("X-Adoreable", "meow")
                        .header("User-Agent", "pm/0.2.0-SNAPSHOT " + chain.request().header("User-Agent"))
                        .build()))
                .addNetworkInterceptor(chain -> {
                    if (!chain.request().isHttps()) {
                        logger.warn("Performing non-HTTPS request to " + chain.request().url().redact() + "!");
                    }
                    return chain.proceed(chain.request());
                })
                .build();
        ore = new Retrofit.Builder()
                .addConverterFactory(new Converter.Factory() {
                    @Override
                    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
                        if (type == byte[].class) {
                            return ResponseBody::bytes;
                        }
                        return super.responseBodyConverter(type, annotations, retrofit);
                    }
                })
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
                        .create()))
                .callbackExecutor(Sponge.getScheduler().createAsyncExecutor(this))
                .baseUrl(Ore.BASE_URL)
                .client(oreClient)
                .build()
                .create(Ore.class);

        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Search for, install, and manage plugins"))
                .extendedDescription(Text.of(TextColors.GRAY, USAGE))
                .permission("pm.base")
                .arguments(flags()
                        .valueFlag(choices(Text.of("category"), Ore.Category.ALL, true), "c")
                        .valueFlag(choices(Text.of("sorting"), Ore.Sorting.ALL, true), "s")
                        .valueFlag(integer(Text.of("limit")), "l")
                        .buildWith(optional(remainingJoinedStrings(Text.of("search")))))
                .executor(new ListProjects(this, Ore.Sorting.MostStars))
                .child(CommandSpec.builder()
                        .arguments(onlyOne(string(Text.of("project"))))
                        .description(Text.of("Show info about an Ore project"))
                        .executor(new ShowProject(this))
                        .build(), "info")
                .child(CommandSpec.builder()
                        .arguments(string(Text.of("project")), optional(string(Text.of("version")), "recommended"))
                        .description(Text.of("Install a project from Ore"))
                        .executor(new InstallProject(this))
                        .build(), "install")
                .build(), "ore");
    }

    public Ore getOre() {
        return ore;
    }

    public OkHttpClient getOreClient() {
        return oreClient;
    }

}
