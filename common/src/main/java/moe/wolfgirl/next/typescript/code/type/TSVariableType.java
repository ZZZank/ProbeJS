package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class TSVariableType extends BaseType {
    public final String symbol;
    public BaseType extendsType;

    public TSVariableType(String symbol, @Nullable BaseType extendsType) {
        this.symbol = symbol;
        this.extendsType = extendsType == TSPrimitiveType.ANY ? null : extendsType;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return extendsType == null ? List.of() : extendsType.getUsedClassPaths();
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of(switch (input) {
            case INPUT, RETURN -> symbol;
            case VARIABLE -> extendsType == null ? symbol :
                    "%s extends %s".formatted(symbol, extendsType.line(declaration, input));
        });
    }
}