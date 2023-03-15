package com.jefflib.jefflibtestplugin;

import com.jeff_media.jefflib.ClassUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestRunner implements Runnable {

    private static final String BR = "\n";

    private final JeffLibTestPlugin plugin;
    private final Deque<NMSTest> tests;
    @Nullable
    private final Player player;
    private final Location originalLocation;
    @Getter
    private final World world;
    @Getter
    private final Location spawn;
    @Getter
    private final Block blockInFront;

    private NMSTest currentTest;
    @Getter
    private String session;

    public TestRunner(JeffLibTestPlugin plugin, @Nullable Player player) {
        this.plugin = plugin;
        this.player = player;
        this.originalLocation = player != null ? player.getLocation() : null;
        this.tests = getTests(player);
        this.world = plugin.getFlatWorld();
        this.spawn = new Location(world, 0.5, world.getHighestBlockYAt(0, 0) + 1, 0.5, 0, 0);
        blockInFront = world.getBlockAt(0, -59, 2);
    }

    @NotNull
    private ArrayDeque<NMSTest> getTests(@Nullable Player player) {
        return ClassUtils.listAllClasses().stream().filter(className -> className.startsWith("com.jefflib.jefflibtestplugin.tests.")).map(className -> {
            try {
                Class<?> clazz = Class.forName(className);
                Class<? extends NMSTest> testClass = clazz.asSubclass(NMSTest.class);
                if(Modifier.isAbstract(testClass.getModifiers())) {
                    return null;
                }
                Constructor<? extends NMSTest> constructor = testClass.getConstructor();
                return constructor.newInstance();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Class " + className + " does not have a no-args constructor");
            } catch (ClassCastException e) {
                throw new RuntimeException("Class " + className + " does not implement NMSTest");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }).filter(Objects::nonNull).sorted().filter((Predicate<NMSTest>) test -> {
            if (player == null) {
                return test.isRunnableFromConsole();
            }
            return true;
        }).collect(Collectors.toCollection(ArrayDeque::new));
    }

    public void beforeEach() {
        if (player != null) {
            player.teleport(spawn);
        }
        world.getEntities().stream().filter(entity -> !entity.equals(player)).forEach(Entity::remove);
    }

    public void cleanup() {
        if (player != null) {
            player.teleport(originalLocation);
        }
    }

    public boolean hasNext() {
        return !tests.isEmpty();
    }

    public boolean runNext() {

        if (currentTest != null) {
            currentTest.cleanup();
        }

        NMSTest test = currentTest = tests.poll();

        if (test == null) {
            return false;
        }


        return runTest(test);
    }

    private boolean runTest(NMSTest test) {

        session = String.valueOf(UUID.randomUUID());

        printBanner(ChatColor.GOLD + "Running test: " + ChatColor.AQUA + ChatColor.BOLD + test.getName());
        beforeEach();

        try {
            test.run(this, player);
        } catch (Throwable throwable) {
            throwException(throwable);
            return false;
        }

        if (test.hasConfirmation() && player != null) {
            String confirmation = test.getConfirmation();
            if(confirmation == null) {
                throw new NullPointerException("confirmation is null although hasConfirmation() returned true");
            }
            ComponentBuilder builder = new ComponentBuilder();
            builder.append("Please confirm: ").color(ChatColor.GOLD).bold(true).append(BR).reset();
            builder.append(confirmation).color(ChatColor.AQUA).append(BR).reset();
            builder.append("[Yes]").color(ChatColor.GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jefflibtest confirm " + session)).append(" ").reset();
            builder.append("[Repeat]").color(ChatColor.YELLOW).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jefflibtest repeat " + session)).append(" ").reset();
            builder.append("[No]").color(ChatColor.RED).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/jefflibtest error " + session));

            player.spigot().sendMessage(builder.create());

            return false;
        }

        return true;
    }

    private void printBanner(String text) {
        print();
        print(ChatColor.GOLD + Utils.repeat("=", 40));
        print(text);
        print(ChatColor.GOLD + Utils.repeat("=", 40));
        print();
    }

    private void throwException(Throwable throwable) {
        print(LogLevel.ERROR, throwable.getMessage());
        throwable.printStackTrace();
    }

    public void print() {
        print("");
    }

    public void print(String... text) {
        print(LogLevel.INFO, text);
    }

    public void print(LogLevel logLevel, String... text) {
        if (player != null) {
            player.sendMessage(text);
        }
        for (String line : text) {
            line = ChatColor.stripColor(line);
            switch (logLevel) {
                case INFO:
                    Bukkit.getLogger().info(line);
                    break;
                case WARNING:
                    Bukkit.getLogger().warning(line);
                    break;
                case ERROR:
                    Bukkit.getLogger().severe(line);
                    break;
            }
        }
    }


    @Override
    public void run() {

        if (!hasNext()) {
            printBanner(ChatColor.GREEN + "Success!");
            plugin.destroyTestRunner();
            return;
        }

        while (runNext()) {

        }
    }

    public void repeat() {
        currentTest.cleanup();
        runTest(currentTest);
    }
}
