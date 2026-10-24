package com.simplyag.customizablebrightness.smoke;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Pre-publish smoke test (test-only, never part of a release jar).
 *
 * <p>Runs inside a real production client next to the mod jar under test. Each launch of the game is one step
 * of a persistence scenario; the launch number comes from the {@code cbsmoke.launch} system property. Every
 * launch first records the brightness it came back with, then changes it the way a player would (real B key
 * presses through the keyboard handler, or the Brightness option the Video Settings slider drives, with the
 * real screen open and closed), records the value it quits with, and quits. Results go to
 * {@code cbsmoke/results.jsonl}; screenshots (taken with a real F2 press) to {@code cbsmoke/shots}.
 * The Gradle smokeCheck task evaluates them.
 */
public class SmokeClient implements ClientModInitializer {
    static final Logger LOG = LogManager.getLogger("CBSMOKE");
    static final String WORLD = "cbsmoke";
    private static final int PHASE_TIMEOUT_TICKS = 20 * 240;

    private enum Phase { BOOT, JOINING, SETTLING, RUNNING, DONE }

    private final Deque<Step> steps = new ArrayDeque<>();
    private Phase phase = Phase.BOOT;
    private int phaseTicks = 0;
    private int wait = 0;
    private int launch;
    // The launch that creates the world: 0 in the full matrix (it leaves the key hint alone), else 1.
    private int first;
    private boolean startedMarked;
    private boolean createPressed = false;
    private Path dir;

    private interface Step {
        void run(Object client) throws Exception;
    }

    @Override
    public void onInitializeClient() {
        launch = Integer.getInteger("cbsmoke.launch", 1);
        first = Integer.getInteger("cbsmoke.first", 1);
        dir = FabricLoader.getInstance().getGameDir().resolve("cbsmoke");
        try {
            Files.createDirectories(dir.resolve("shots"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOG.info("smoke launch {} (sodium={}, modmenu={}, cloth={})", launch,
                FabricLoader.getInstance().isModLoaded("sodium"),
                FabricLoader.getInstance().isModLoaded("modmenu"),
                FabricLoader.getInstance().isModLoaded("cloth-config") || FabricLoader.getInstance().isModLoaded("cloth-config2"));
        // The JVM options this launch really got (Mojang manifest options included), for comparison with a launcher.
        LOG.info("smoke jvm {} {} {}", System.getProperty("java.vendor"), System.getProperty("java.version"),
                java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments());
        startWatchdog();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            try {
                tick(client);
            } catch (Throwable t) {
                fail("exception in phase " + phase + ": " + t);
                LOG.error("smoke step failed", t);
                Mc.stop(client);
                phase = Phase.DONE;
            }
        });
    }

    // A hung game must not hang the whole sweep.
    private void startWatchdog() {
        Thread watchdog = new Thread(() -> {
            try {
                Thread.sleep(10 * 60 * 1000L);
            } catch (InterruptedException e) {
                return;
            }
            fail("watchdog: launch " + launch + " still running after 10 minutes (phase " + phase + ")");
            Runtime.getRuntime().halt(3);
        }, "cbsmoke-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private void tick(Object client) throws Exception {
        if (phase != Phase.DONE && phase != Phase.RUNNING && phaseTicks % 100 == 0) {
            LOG.info("waiting in phase {} on screen {}", phase, Mc.currentScreenName(client));
        }
        if (++phaseTicks > PHASE_TIMEOUT_TICKS && phase != Phase.DONE) {
            fail("timeout in phase " + phase + " on screen " + Mc.currentScreenName(client));
            Mc.stop(client);
            phase = Phase.DONE;
            return;
        }
        if (wait > 0) {
            wait--;
            return;
        }
        switch (phase) {
            case BOOT:
                if (!startedMarked && (Mc.isTitleScreen(client) || Mc.isOnboardingScreen(client))) {
                    // The game has finished starting; the launch task only retries a launch that dies before this.
                    markStarted();
                }
                if (Mc.isOnboardingScreen(client)) {
                    // Vanilla ignored the seeded options.txt (seen only on a run that also downloaded assets).
                    // Not a mod failure, but recorded so the result shows it happened.
                    result("{\"event\":\"seedIgnored\"}");
                    Mc.showTitleScreen(client);
                    wait = 20;
                    break;
                }
                if (Mc.isTitleScreen(client)) {
                    if (launch == first) {
                        Mc.createWorld(client, WORLD);
                    } else {
                        Mc.openWorld(client, savedWorld());
                    }
                    enter(Phase.JOINING);
                    wait = 20;
                }
                break;
            case JOINING:
                if (launch == first && !createPressed) {
                    createPressed = Mc.pressCreateButton(client); // the default new world, as a player would make it
                }
                Mc.dismissBlockingScreens(client);
                if (Mc.inWorld(client)) {
                    enter(Phase.SETTLING);
                    wait = 100;
                }
                break;
            case SETTLING:
                Mc.command(client, "gamemode creative @a");
                Mc.command(client, "difficulty peaceful");
                Mc.command(client, "weather clear 1000000");
                Mc.command(client, "time set 18000");
                // Same view every launch: look down at the ground in front of the player.
                Mc.command(client, "execute as @a at @s run tp @s ~ ~ ~ 0 40");
                pressKey(client, Mc.KEY_F1); // hide the HUD so screenshots measure the world only
                planLaunch();
                enter(Phase.RUNNING);
                wait = 200; // chunks render
                break;
            case RUNNING:
                if (Mc.currentScreenIsPause(client)) {
                    Mc.setScreen(client, null);
                }
                Step next = steps.poll();
                if (next == null) {
                    enter(Phase.DONE);
                    return;
                }
                next.run(client);
                break;
            default:
                break;
        }
    }

    // The first launch created the world through the normal "Create New World" screen; it is the only save.
    private static String savedWorld() throws IOException {
        try (java.util.stream.Stream<Path> saves = Files.list(FabricLoader.getInstance().getGameDir().resolve("saves"))) {
            return saves.filter(Files::isDirectory).map(p -> p.getFileName().toString()).findFirst()
                    .orElseThrow(() -> new IllegalStateException("no saved world from the first launch"));
        }
    }

    private void enter(Phase next) {
        phase = next;
        phaseTicks = 0;
    }

    // ---------------------------------------------------------------------------------------------------
    // Scenario. Launch N records what it restored, changes brightness, and records what it quits with;
    // launch N+1 must restore exactly that. Default stops: 0, 1, 2, 5, 10 (multipliers).

    private void planLaunch() {
        record(launch == first ? "initial" : "restored", true);
        // Key hint toast: up until B is first pressed (launch 1), then never again.
        toastCheck(launch <= 1 ? "hint" : "hint-absent");
        // The last launch (9 for the full matrix, 2 for a quick check) only records what it restored.
        switch (launch == Integer.getInteger("cbsmoke.last", 9) ? -1 : launch) {
            case 0:
                quit("key hint left alone");          // no B press: the hint must come back in launch 1
                break;
            case 1:
                steps.add(c -> checkKeyCategory(c));
                steps.add(c -> checkConfigScreen(c));
                for (int i = 1; i <= 5; i++) {      // 100 -> 200, 500, 1000, 0, 100
                    pressB();
                    record("press-" + i, true);
                    if (i == 1) toastCheck("hint-dismissed");
                }
                for (int i = 6; i <= 8; i++) {      // -> 200, 500, 1000
                    pressB();
                }
                saveOptions();                       // vanilla saves options.txt while brightness is above 100%
                quit("B above 100%");
                break;
            case 2:
                pressB();                            // 1000 -> 0
                quit("B at 0%");
                break;
            case 3:
                pressB();                            // 0 -> 100
                quit("B at 100%");
                break;
            case 4:
                slider(0.5);
                quit("slider at 50%");
                break;
            case 5:
                slider(0.25);                        // options.txt now says 0.25 ...
                pressB();
                pressB();                            // ... but we quit somewhere above 100%, with no options save
                quit("slider then B (stale options.txt)");
                break;
            case 6:
                pressB();
                pressB();
                slider(0.75);
                quit("B then slider at 75%");
                break;
            case 7:
                slider(1.0);
                quit("slider at 100%");
                break;
            case 8:
                slider(0.0);
                quit("slider at 0%");
                break;
            default:
                steps.add(c -> result("{\"event\":\"finished\"}"));
                quit("end");
                break;
        }
    }

    // Records whether the game's toast manager holds a tutorial toast, and whether it is the mod's key hint.
    private void toastCheck(String label) {
        // With the HUD shown, as a player sees it: toasts are drawn (and a hidden one finishes sliding out, about half
        // a second) only while it is.
        steps.add(c -> {
            pressKey(c, Mc.KEY_F1);
            wait = 30;
        });
        steps.add(c -> takeScreenshot(c, "toast-" + label));
        steps.add(c -> {
            Object hint = modHintToast();
            List<Object> toasts = tutorialToasts(Mc.toastManager(c));
            boolean ours = false;
            for (Object t : toasts) ours |= t == hint;
            result(String.format(Locale.ROOT, "{\"event\":\"toast\",\"label\":\"%s\",\"present\":%s,\"ours\":%s,\"count\":%d,\"modHint\":%s,\"config\":%s,\"shot\":\"%s\"}",
                    label, !toasts.isEmpty(), ours, toasts.size(), hint != null, str(readConfig()), shotName("toast-" + label)));
            pressKey(c, Mc.KEY_F1); // HUD hidden again for the brightness screenshots
        });
    }

    // Tutorial toasts showing or queued, found without version-specific names: the manager keeps them in
    // collections or arrays, either directly or wrapped in an entry object.
    private static List<Object> tutorialToasts(Object manager) throws IllegalAccessException {
        List<Object> found = new ArrayList<>();
        for (Object value : fieldValues(manager)) {
            Iterable<?> items = value instanceof Iterable ? (Iterable<?>) value
                    : value instanceof Object[] ? java.util.Arrays.asList((Object[]) value) : null;
            if (items == null) continue;
            for (Object item : items) {
                if (item == null || item.getClass().getName().startsWith("java.")) continue;
                if (Mc.isTutorialToast(item)) {
                    found.add(item);
                    continue;
                }
                for (Object inner : fieldValues(item)) {
                    if (Mc.isTutorialToast(inner)) found.add(inner);
                }
            }
        }
        return found;
    }

    private static List<Object> fieldValues(Object owner) throws IllegalAccessException {
        List<Object> values = new ArrayList<>();
        for (Class<?> c = owner.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (java.lang.reflect.Field field : c.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) continue;
                field.setAccessible(true);
                values.add(field.get(owner));
            }
        }
        return values;
    }

    // The toast the mod is showing (its client entrypoint's hintToast field), or null. Fabric keeps one entrypoint
    // instance per requested type, so this asks for the type the game started the mod with.
    private static Object modHintToast() throws Exception {
        Object mod = FabricLoader.getInstance().getEntrypointContainers("client", ClientModInitializer.class).stream()
                .filter(c -> c.getProvider().getMetadata().getId().equals("customizable-brightness"))
                .findFirst().orElseThrow(() -> new IllegalStateException("no client entrypoint")).getEntrypoint();
        java.lang.reflect.Field field = mod.getClass().getDeclaredField("hintToast");
        field.setAccessible(true);
        return field.get(mod);
    }

    private void pressB() {
        steps.add(c -> {
            pressKey(c, Mc.KEY_B);
            wait = 20;
        });
    }

    // Opens and closes Video Settings without touching anything: closing it is what makes vanilla save options.txt.
    private void saveOptions() {
        steps.add(c -> {
            Mc.openVideoSettings(c);
            wait = 20;
        });
        steps.add(c -> {
            Mc.setScreen(c, null);
            wait = 20;
        });
    }

    private void slider(double value) {
        steps.add(c -> {
            Mc.openVideoSettings(c);
            wait = 20;
        });
        steps.add(c -> {
            result(String.format(Locale.ROOT, "{\"event\":\"slider\",\"value\":%s,\"screen\":\"%s\"}",
                    value, Mc.currentScreenName(c)));
            Mc.setVanillaGamma(c, value);
            wait = 10;
        });
        steps.add(c -> {
            Mc.setScreen(c, null); // closing Video Settings is what saves options.txt
            wait = 20;
        });
    }

    private void quit(String scenario) {
        record("quit", true);
        steps.add(c -> {
            result(String.format(Locale.ROOT, "{\"event\":\"quit\",\"scenario\":\"%s\"}", scenario));
            Mc.stop(c);
        });
    }

    private void record(String label, boolean screenshot) {
        if (screenshot) {
            steps.add(c -> {
                Mc.command(c, "time set 18000");
                wait = 20;
            });
            steps.add(c -> {
                takeScreenshot(c, label);
            });
        }
        steps.add(c -> {
            StringBuilder json = new StringBuilder();
            json.append("{\"event\":\"state\",\"label\":\"").append(label).append('"');
            json.append(",\"modGamma\":").append(num(modGamma()));
            json.append(",\"vanillaGamma\":").append(num(Mc.getVanillaGamma(c)));
            int[] lightmap = Mc.sampleLightmap(c);
            if (lightmap != null) {
                json.append(",\"lightmap\":[");
                for (int i = 0; i < lightmap.length; i++) json.append(i > 0 ? "," : "").append(lightmap[i]);
                json.append(']');
            }
            json.append(",\"optionsGamma\":").append(str(readOptionsGamma()));
            json.append(",\"config\":").append(str(readConfig()));
            if (screenshot) json.append(",\"shot\":\"").append(shotName(label)).append('"');
            json.append('}');
            result(json.toString());
        });
    }

    // F2 through the keyboard handler, then move the file vanilla wrote to a predictable name.
    private void markStarted() {
        startedMarked = true;
        try {
            Files.write(dir.resolve("started-" + launch), new byte[0]);
        } catch (IOException e) {
            LOG.error("cannot write the startup marker", e);
        }
    }

    private void takeScreenshot(Object client, String label) throws Exception {
        Path shots = FabricLoader.getInstance().getGameDir().resolve("screenshots");
        Files.createDirectories(shots);
        List<Path> before = list(shots);
        pressKey(client, Mc.KEY_F2);
        steps.addFirst(c -> awaitScreenshot(shots, before, label, 0));
    }

    private void awaitScreenshot(Path shots, List<Path> before, String label, int attempt) throws Exception {
        List<Path> now = list(shots);
        now.removeAll(before);
        if (now.isEmpty() || attempt < 5) {
            if (attempt > 200) throw new IllegalStateException("no screenshot for " + label);
            steps.addFirst(c -> awaitScreenshot(shots, before, label, attempt + 1));
            wait = 2;
            return;
        }
        Files.move(now.get(0), dir.resolve("shots").resolve(shotName(label)));
    }

    // Labels such as "restored" repeat across launches, so the launch number keeps every file distinct.
    private String shotName(String label) {
        return launch + "-" + label + ".png";
    }

    private static List<Path> list(Path dir) throws IOException {
        List<Path> files = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".png")).forEach(files::add);
        }
        return files;
    }

    private static void pressKey(Object client, int key) {
        Mc.pressKey(client, key);
    }

    private void checkKeyCategory(Object client) {
        String[] category = Mc.keyCategory(client, "key.customizable-brightness.cycle");
        result(String.format(Locale.ROOT, "{\"event\":\"keyCategory\",\"key\":%s,\"text\":%s}",
                str(category == null ? null : category[0]), str(category == null ? null : category[1])));
    }

    // Opens the mod's config screen the way Mod Menu does (its "modmenu" entrypoint), if Mod Menu is present.
    private void checkConfigScreen(Object client) {
        if (!FabricLoader.getInstance().isModLoaded("modmenu")) {
            result("{\"event\":\"configScreen\",\"status\":\"skipped (no modmenu)\"}");
            return;
        }
        String status;
        try {
            Object entrypoint = FabricLoader.getInstance().getEntrypointContainers("modmenu", Object.class).stream()
                    .filter(c -> c.getProvider().getMetadata().getId().equals("customizable-brightness"))
                    .findFirst().orElseThrow(() -> new IllegalStateException("no modmenu entrypoint")).getEntrypoint();
            Object factory = entrypoint.getClass().getMethod("getModConfigScreenFactory").invoke(entrypoint);
            java.lang.reflect.Method create = null;
            for (java.lang.reflect.Method m : factory.getClass().getMethods()) {
                if (m.getName().equals("create") && m.getParameterCount() == 1) create = m;
            }
            if (create == null) throw new IllegalStateException("factory has no create(Screen)");
            create.setAccessible(true);
            Object screen = create.invoke(factory, new Object[]{null});
            Mc.setScreen(client, screen);
            status = "opened " + screen.getClass().getName();
        } catch (Throwable t) {
            Throwable cause = t instanceof java.lang.reflect.InvocationTargetException ? t.getCause() : t;
            status = "FAILED " + cause;
            LOG.error("config screen failed", cause);
        }
        result("{\"event\":\"configScreen\",\"status\":" + str(status) + "}");
        steps.addFirst(c -> {
            Mc.setScreen(c, null);
            wait = 10;
        });
        wait = 20;
    }

    // Reflection, so the same harness also drives the 1.0.x jars (getCurrentGamma exists in every release).
    private static Double modGamma() {
        try {
            Class<?> cls = Class.forName("com.simplyag.customizablebrightness.CustomizableBrightnessClient");
            return (Double) cls.getMethod("getCurrentGamma").invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String readOptionsGamma() {
        try {
            for (String line : Files.readAllLines(FabricLoader.getInstance().getGameDir().resolve("options.txt"), StandardCharsets.UTF_8)) {
                if (line.startsWith("gamma:")) return line.substring(6);
            }
            return "(absent)";
        } catch (IOException e) {
            return "(no options.txt)";
        }
    }

    private static String readConfig() {
        try {
            StringBuilder sb = new StringBuilder();
            for (String line : Files.readAllLines(FabricLoader.getInstance().getConfigDir().resolve("customizable_brightness.properties"), StandardCharsets.UTF_8)) {
                if (!line.startsWith("#")) sb.append(sb.length() > 0 ? ";" : "").append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private void result(String json) {
        String line = "{\"launch\":" + launch + "," + json.substring(1) + "\n";
        try {
            Files.write(dir.resolve("results.jsonl"), line.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOG.error("cannot write results", e);
        }
        LOG.info("RESULT {}", line.trim());
    }

    private void fail(String message) {
        result("{\"event\":\"failure\",\"message\":" + str(message) + "}");
    }

    private static String num(Double value) {
        return value == null ? "null" : String.format(Locale.ROOT, "%.6f", value);
    }

    private static String str(String value) {
        if (value == null) return "null";
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}
