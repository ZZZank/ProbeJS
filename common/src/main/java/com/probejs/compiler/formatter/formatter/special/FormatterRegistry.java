package com.probejs.compiler.formatter.formatter.special;

import com.probejs.ProbeJS;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.java.MethodInfo;
import dev.latvian.mods.kubejs.KubeJSRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormatterRegistry<T> implements IFormatter {
    private final ResourceKey<Registry<T>> registry;
    private final Class<T> clazz;

    public FormatterRegistry(ResourceKey<Registry<T>> registry, Class<T> clazz) {
        this.registry = registry;
        this.clazz = clazz;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> items = new ArrayList<>();
        KubeJSRegistries.genericRegistry(registry).getIds().forEach(rl -> {
            if (rl.getNamespace().equals("minecraft"))
                items.add(ProbeJS.GSON.toJson(rl.getPath()));
            items.add(ProbeJS.GSON.toJson(rl.toString()));
        });
        List<String> remappedName = Arrays.stream(MethodInfo.getRemappedOrOriginalClass(clazz).split("\\.")).toList();
        return List.of("%stype %s = %s;".formatted(" ".repeat(indent), remappedName.get(remappedName.size() - 1), String.join(" | ", items)));
    }
}