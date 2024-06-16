package moe.wolfgirl.probejs.typescript;

import moe.wolfgirl.probejs.java.clazz.ClassPath;
import moe.wolfgirl.probejs.typescript.code.Code;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TypeScriptFile {
    public final Declaration declaration;
    public final List<Code> codeList;
    public final ClassPath classPath;

    public TypeScriptFile(ClassPath self) {
        this.declaration = new Declaration();
        this.codeList = new ArrayList<>();

        if (self != null) {
            declaration.addClass(self);
        }
        this.classPath = self;
    }

    public void excludeSymbol(String name) {
        declaration.exclude(name);
    }

    public void addCode(Code code) {
        codeList.add(code);
        for (ClassPath usedClassPath : code.getUsedClassPaths()) {
            declaration.addClass(usedClassPath);
        }
    }

    public String format() {
        List<String> formatted = new ArrayList<>();

        for (Code code : codeList) {
            formatted.addAll(code.format(declaration));
        }

        return String.join("\n", formatted);
    }

    public void write(Path writeTo) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(writeTo)) {
            boolean written = false;
            for (Reference value : declaration.references.values()) {
                if (value.classPath().equals(classPath)) continue;
                writer.write(value.getImport() + "\n");
                written = true;
            }
            if (!written) {
                writer.write("export {} // Mark the file as a module, do not remove unless there are other import/exports!");
            }

            writer.write("\n");
            writer.write(format());
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Code> Optional<T> findCode(Class<T> type) {
        for (Code code : codeList) {
            if (type.isInstance(code)) {
                return Optional.of((T) code);
            }
        }
        return Optional.empty();
    }
}