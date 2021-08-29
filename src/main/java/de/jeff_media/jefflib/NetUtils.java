package de.jeff_media.jefflib;

import de.jeff_media.jefflib.exceptions.JeffLibNotInitializedException;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public final class NetUtils {

    public static List<String> downloadToStringList(final String url) throws IOException {
        if (JeffLib.getPlugin() == null) {
            throw new JeffLibNotInitializedException();
        }
        final HttpURLConnection httpConnection = (HttpURLConnection) new URL(url).openConnection();
        //noinspection HardcodedFileSeparator
        httpConnection.addRequestProperty("User-Agent", JeffLib.getPlugin().getName() + "/" + JeffLib.getPlugin().getDescription().getVersion());
        try (
                final InputStreamReader input = new InputStreamReader(httpConnection.getInputStream());
                final BufferedReader reader = new BufferedReader(input)
        ) {
            final Stream<String> result = reader.lines();
            return result.collect(Collectors.toList());
        }
    }

    public static CompletableFuture<List<String>> downloadToStringListAsync(final String url) {
        if(JeffLib.getPlugin() == null) {
            throw new JeffLibNotInitializedException();
        }
        final CompletableFuture<List<String>> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(JeffLib.getPlugin(), () -> {
            try {
                final List<String> result = downloadToStringList(url);
                future.complete(result);
            } catch (final IOException exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }
}