package moe.wolfgirl.probejs.java.clazz.members;

import moe.wolfgirl.probejs.java.base.ClassPathProvider;
import moe.wolfgirl.probejs.java.base.TypeVariableHolder;
import moe.wolfgirl.probejs.java.clazz.ClassPath;
import moe.wolfgirl.probejs.java.type.impl.VariableType;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public class ConstructorInfo extends TypeVariableHolder implements ClassPathProvider {

    public final List<ParamInfo> params;

    public ConstructorInfo(Constructor<?> constructor) {
        super(constructor.getTypeParameters(), constructor.getAnnotations());
        this.params = Arrays.stream(constructor.getParameters()).map(ParamInfo::new).collect(Collectors.toList());
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (ParamInfo param : params) {
            paths.addAll(param.getClassPaths());
        }
        for (VariableType variableType : variableTypes) {
            paths.addAll(variableType.getClassPaths());
        }
        return paths;
    }
}