package com.probejs.next.java.type;

import com.probejs.next.java.base.AnnotationHolder;
import com.probejs.next.java.base.ClassPathProvider;
import com.probejs.next.java.base.ClassProvider;
import com.probejs.next.java.clazz.ClassPath;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TypeDescriptor extends AnnotationHolder implements ClassPathProvider, ClassProvider {
    public TypeDescriptor(Annotation[] annotations) {
        super(annotations);
    }

    /**
     * Iterate through contained types.
     * <br>
     * For simple classes, the class yields itself.
     */
    public abstract Stream<TypeDescriptor> stream();

    /**
     * Gets the class paths required to use the type.
     */
    public Collection<ClassPath> getClassPaths() {
        return stream().flatMap(t -> t.getClassPaths().stream()).collect(Collectors.toSet());
    }

    /**
     * Gets the classes involved in the type.
     */
    public Collection<Class<?>> getClasses() {
        return stream().flatMap(t -> t.getClasses().stream()).collect(Collectors.toSet());
    }
}