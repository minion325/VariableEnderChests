package me.saif.betterenderchests.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public final class FoliaScheduler {

    private static final boolean FOLIA;

    private static Method getGlobalRegionScheduler;
    private static Method getAsyncScheduler;
    private static Method getRegionScheduler;
    private static Method entity_getScheduler;

    private static Method global_run;
    private static Method global_runDelayed;
    private static Method global_runAtFixedRate;
    private static Method global_execute;

    private static Method async_runNow;
    private static Method async_runDelayed;

    private static Method region_run;
    private static Method region_execute;

    private static Method entity_run;
    private static Method entity_runDelayed;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;

        if (FOLIA) {
            try {
                getGlobalRegionScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler");
                getAsyncScheduler = Bukkit.class.getMethod("getAsyncScheduler");
                getRegionScheduler = Bukkit.class.getMethod("getRegionScheduler");
                entity_getScheduler = Entity.class.getMethod("getScheduler");

                Class<?> globalScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
                global_run = globalScheduler.getMethod("run", Plugin.class, java.util.function.Consumer.class);
                global_runDelayed = globalScheduler.getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, long.class);
                global_runAtFixedRate = globalScheduler.getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class, long.class, long.class);
                global_execute = globalScheduler.getMethod("execute", Plugin.class, Runnable.class);

                Class<?> asyncScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
                async_runNow = asyncScheduler.getMethod("runNow", Plugin.class, java.util.function.Consumer.class);
                async_runDelayed = asyncScheduler.getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, long.class, TimeUnit.class);

                Class<?> regionScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
                region_run = regionScheduler.getMethod("run", Plugin.class, Location.class, java.util.function.Consumer.class);
                region_execute = regionScheduler.getMethod("execute", Plugin.class, Location.class, Runnable.class);

                Class<?> entityScheduler = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                entity_run = entityScheduler.getMethod("run", Plugin.class, java.util.function.Consumer.class, Runnable.class);
                entity_runDelayed = entityScheduler.getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, Runnable.class, long.class);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private FoliaScheduler() {
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static void runGlobal(Plugin plugin, Runnable runnable) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTask(plugin, runnable);
            return;
        }
        try {
            Object scheduler = getGlobalRegionScheduler.invoke(null);
            global_run.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> runnable.run());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void runGlobalLater(Plugin plugin, Runnable runnable, long delayTicks) {
        if (delayTicks <= 0) delayTicks = 1;
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
            return;
        }
        try {
            Object scheduler = getGlobalRegionScheduler.invoke(null);
            global_runDelayed.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> runnable.run(), delayTicks);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void runGlobalTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (delayTicks <= 0) delayTicks = 1;
        if (periodTicks <= 0) periodTicks = 1;
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
            return;
        }
        try {
            Object scheduler = getGlobalRegionScheduler.invoke(null);
            global_runAtFixedRate.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> runnable.run(), delayTicks, periodTicks);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void runAsync(Plugin plugin, Runnable runnable) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            return;
        }
        try {
            Object scheduler = getAsyncScheduler.invoke(null);
            async_runNow.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> runnable.run());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void runEntity(Plugin plugin, Entity entity, Runnable runnable, Runnable retired) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTask(plugin, runnable);
            return;
        }
        try {
            Object scheduler = entity_getScheduler.invoke(entity);
            entity_run.invoke(scheduler, plugin, (java.util.function.Consumer<Object>) task -> runnable.run(), retired);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void runRegion(Plugin plugin, Location location, Runnable runnable) {
        if (!FOLIA) {
            Bukkit.getScheduler().runTask(plugin, runnable);
            return;
        }
        try {
            Object scheduler = getRegionScheduler.invoke(null);
            region_run.invoke(scheduler, plugin, location, (java.util.function.Consumer<Object>) task -> runnable.run());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
