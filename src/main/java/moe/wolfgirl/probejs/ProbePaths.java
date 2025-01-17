package moe.wolfgirl.probejs;

import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.util.UtilsJS;

import java.nio.file.Files;
import java.nio.file.Path;

public class ProbePaths {

    public static Path PROBE = KubeJSPaths.GAMEDIR.resolve(".probe");
    public static Path WORKSPACE_SETTINGS = KubeJSPaths.GAMEDIR.resolve(".vscode");
    public static Path SETTINGS_JSON = KubeJSPaths.CONFIG.resolve("probe-settings.json");
    public static Path VSCODE_JSON = WORKSPACE_SETTINGS.resolve("settings.json");
    public static Path GIT_IGNORE = KubeJSPaths.GAMEDIR.resolve(".gitignore");
    public static Path DECOMPILED = PROBE.resolve("decompiled");

    public static void init() {
        if (Files.notExists(PROBE)) {
            UtilsJS.tryIO(() -> Files.createDirectories(PROBE));
        }
        if (Files.notExists(WORKSPACE_SETTINGS)) {
            UtilsJS.tryIO(() -> Files.createDirectories(WORKSPACE_SETTINGS));
        }
        if (Files.notExists(ProbePaths.DECOMPILED)) {
            UtilsJS.tryIO(() -> Files.createDirectories(ProbePaths.DECOMPILED));
        }
    }

    static {
        init();
    }
}
