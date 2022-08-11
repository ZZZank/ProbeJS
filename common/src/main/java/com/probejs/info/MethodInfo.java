package com.probejs.info;

import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.InfoTypeResolver;
import com.probejs.info.type.TypeInfoClass;
import dev.latvian.mods.rhino.mod.util.RemappingHelper;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.Remapper;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodInfo {


    private final String name;
    private final boolean shouldHide;
    private final boolean defaultMethod;
    private final int modifiers;
    private final Class<?> from;
    private ITypeInfo returnType;
    private List<ParamInfo> params;
    private List<ITypeInfo> typeVariables;
    public static final Remapper RUNTIME = RemappingHelper.createModRemapper();

    private static String getRemappedOrDefault(Method method, Class<?> from) {
        String s = method.getName();
        while (from != null && from != Object.class) {
            s = RUNTIME.getMappedMethod(from, method);
            if (!s.equals(method.getName()))
                break;
            for (Class<?> implemented : from.getInterfaces()) {
                s = RUNTIME.getMappedMethod(implemented, method);
                if (!s.equals(method.getName()))
                    break;
            }
            from = from.getSuperclass();
        }
        return s;
    }

    public MethodInfo(Method method, Class<?> from) {
        Map<Type, Type> typeGenericMap = new HashMap<>();
        if (method.getDeclaringClass() != from) {
            rewindGenerics(method, from).forEach((key, value) -> typeGenericMap.put(value, key));
        }
        this.name = getRemappedOrDefault(method, method.getDeclaringClass());
        this.shouldHide = method.getAnnotation(HideFromJS.class) != null;
        this.from = from;
        this.modifiers = method.getModifiers();
        this.returnType = InfoTypeResolver.resolveType(
                method.getGenericReturnType(),
                type -> typeGenericMap.getOrDefault(type, type)
        );
        this.params = Arrays.stream(method.getParameters()).map(param -> new ParamInfo(param, typeGenericMap)).collect(Collectors.toList());
        this.typeVariables = Arrays.stream(method.getTypeParameters()).map(InfoTypeResolver::resolveType).collect(Collectors.toList());
        this.defaultMethod = method.isDefault();
    }

    public String getName() {
        return name;
    }

    public boolean shouldHide() {
        return shouldHide;
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers) && !from.isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

    public boolean isDefaultMethod() {
        return defaultMethod;
    }

    public ITypeInfo getReturnType() {
        return returnType;
    }

    public List<ParamInfo> getParams() {
        return params;
    }

    public List<ITypeInfo> getTypeVariables() {
        return typeVariables;
    }

    public ClassInfo getFrom() {
        return ClassInfo.getOrCache(from);
    }

    public void setParams(List<ParamInfo> params) {
        this.params = params;
    }

    public void setReturnType(ITypeInfo returnType) {
        this.returnType = returnType;
    }

    public void setTypeVariables(List<ITypeInfo> typeVariables) {
        this.typeVariables = typeVariables;
    }

    public static class ParamInfo {
        private final String name;
        private final boolean isVararg;
        private ITypeInfo type;

        public ParamInfo(Parameter parameter) {
            this(parameter, new HashMap<>());
        }

        public ParamInfo(Parameter parameter, Map<Type, Type> typeMap) {
            this.name = parameter.getName();
            this.isVararg = parameter.isVarArgs();
            try {
                this.type = InfoTypeResolver.resolveType(parameter.getParameterizedType(), t -> typeMap.getOrDefault(t, t));
            } catch (Exception e) {
                //#3, WTF???
                e.printStackTrace();
                this.type = new TypeInfoClass(Object.class);
            }
        }

        public String getName() {
            return name;
        }

        public ITypeInfo getType() {
            return type;
        }

        public boolean isVararg() {
            return isVararg;
        }

        public void setTypeInfo(ITypeInfo type) {
            this.type = type;
        }
    }

    private static Class<?> unwrapGenerics(Type type) {
        if (type instanceof Class<?>)
            return (Class<?>) type;
        else if (type instanceof ParameterizedType parameterizedType)
            return unwrapGenerics(parameterizedType.getRawType());
        return null;
    }

    private static boolean testTypeAssignable(Type type1, Type type2) {
        Class<?> clazz1 = unwrapGenerics(type1);
        Class<?> clazz2 = unwrapGenerics(type2);
        return clazz1 != null && clazz2 != null && clazz1.isAssignableFrom(clazz2);
    }

    private static Map<Type, Type> rewindGenerics(Method method, Class<?> currentClass) {
        Class<?> targetClass = method.getDeclaringClass();
        Map<Type, Type> currentMap = new HashMap<>();

        if (targetClass != currentClass) {
            Type parentType = testTypeAssignable(targetClass, currentClass.getGenericSuperclass()) ?
                    currentClass.getGenericSuperclass() :
                    Arrays.stream(currentClass.getGenericInterfaces())
                            .filter(i -> testTypeAssignable(targetClass, i))
                            .findFirst()
                            .orElse(null);
            if (parentType instanceof ParameterizedType parameterizedType) {
                Class<?> paramClass = unwrapGenerics(parameterizedType);
                if (paramClass != null) {
                    Type[] remappedTypes = parameterizedType.getActualTypeArguments();
                    Type[] originalTypes = paramClass.getTypeParameters();
                    for (int i = 0; i < remappedTypes.length; i++)
                        currentMap.put(remappedTypes[i], originalTypes[i]);
                }

                Map<Type, Type> parentMap = rewindGenerics(method, unwrapGenerics(parentType));
                for (Type key : currentMap.keySet()) {
                    if (parentMap.containsKey(currentMap.get(key)))
                        currentMap.put(key, parentMap.get(currentMap.get(key)));
                }

            } else if (parentType != null) {
                currentMap = rewindGenerics(method, unwrapGenerics(parentType));
            }

        } else {
            //Only get up to what this method wants.
            for (TypeVariable<? extends Class<?>> type : currentClass.getTypeParameters()) {
                currentMap.put(type, type);
            }
        }
        return currentMap;
    }

}
