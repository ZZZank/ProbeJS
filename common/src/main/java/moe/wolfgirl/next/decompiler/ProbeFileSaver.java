package moe.wolfgirl.next.decompiler;

import moe.wolfgirl.next.decompiler.parser.ParsedDocument;
import moe.wolfgirl.next.java.clazz.ClassPath;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class ProbeFileSaver implements IResultSaver {
    public final Map<ClassPath, ParsedDocument> result = new HashMap<>();

    @Override
    public void saveFolder(String path) {

    }

    @Override
    public void copyFile(String source, String path, String entryName) {

    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
        ClassPath classPath = new ClassPath(qualifiedName.replace("/", "."));
        result.put(classPath, new ParsedDocument(content));
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest) {

    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName) {

    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entry) {

    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
        ClassPath classPath = new ClassPath(qualifiedName.replace("/", "."));
        result.put(classPath, new ParsedDocument(content));
    }

    @Override
    public void closeArchive(String path, String archiveName) {

    }

    public void writeTo(Path base) throws IOException {
        for (Map.Entry<ClassPath, ParsedDocument> entry : result.entrySet()) {
            ClassPath classPath = entry.getKey();
            ParsedDocument s = entry.getValue();
            s.getParamTransformations();

            Path full = classPath.makePath(base);
            try (var out = Files.newBufferedWriter(full.resolve(classPath.getName() + ".java"))) {
                String[] lines = s.getCode().split("\\n");
                out.write(Arrays.stream(lines)
                        .filter(l -> !l.strip().startsWith("// $VF: renamed"))
                        .collect(Collectors.joining("\n"))
                );
            }
        }
    }
}