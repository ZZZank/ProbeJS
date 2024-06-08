package moe.wolfgirl.specials.special.recipe.component;

import moe.wolfgirl.docs.formatter.SpecialTypes;
import moe.wolfgirl.jdoc.property.PropertyType;
import moe.wolfgirl.util.Util;
import dev.latvian.mods.kubejs.typings.desc.*;
import net.minecraft.tags.TagKey;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

public class ComponentConverter {
    public static PropertyType<?> fromDescription(TypeDescJS description) {
        if (description instanceof ArrayDescJS array) {
            return new PropertyType.Array(fromDescription(array.type()));
        } else if (description instanceof FixedArrayDescJS fixedArray) {
            return new PropertyType.JSArray(Arrays.stream(fixedArray.types()).map(ComponentConverter::fromDescription).collect(Collectors.toList()));
        } else if (description instanceof GenericDescJS parameterized) {
            if (parameterized.type() instanceof PrimitiveDescJS primitive &&
                    primitive.type().equals("Map") &&
                    fromDescription(parameterized.types()[0]) instanceof PropertyType.Native keyType &&
                    parameterized.types().length == 2
            ) {
                // Map is not an object in JS, so we need to use a special type `{[key: keyType]: valueType}`
                // In case of Special.XXXX or whatever, you can still have good mappings
                return new PropertyType.JSObject()
                        .add(new PropertyType.JSObjectKey().withType(keyType),
                                fromDescription(parameterized.types()[1])
                        );
            }
            if (parameterized.type() instanceof PrimitiveDescJS primitive && primitive.type().equals("$probejs$" + TagKey.class.getName()) && parameterized.types()[0] instanceof PrimitiveDescJS registryType) {
                // If we have a TagKey<RegistryType>, we can use the special type generated by ProbeJS
                try {
                    String special = SpecialTypes.getRegistryTagName(Class.forName(registryType.type().substring(9)));
                    if (special != null) {
                        return new PropertyType.Native(special);
                    }
                } catch (Exception ignored) {
                }
            }
            return new PropertyType.Parameterized(
                    fromDescription(parameterized.type()),
                    Arrays.stream(parameterized.types())
                            .map(ComponentConverter::fromDescription)
                            .collect(Collectors.toList())
            );
        } else if (description instanceof ObjectDescJS object) {
            return new PropertyType.JSObject(
                    object.types().stream()
                            .map(pair -> Pair.of(
                                    new PropertyType.JSObjectKey()
                                            .withName(Util.getSafeName(pair.key()))
                                            .optional(pair.optional()),
                                    fromDescription(pair.value())
                            ))
                            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
            );
        } else if (description instanceof OrDescJS or) {
            return new PropertyType.Union(
                    Arrays.stream(or.types())
                            .map(ComponentConverter::fromDescription)
                            .collect(Collectors.toList())
            );
        } else if (description instanceof PrimitiveDescJS primitive) {
            String name = primitive.type();
            if (name.equals("null")) // return any here because we don't know what to do with null
                return new PropertyType.Clazz(Object.class);
            if (name.startsWith("$probejs$")) {
                String clazzName = name.substring(9);
                try {
                    Class<?> clazz = Class.forName(clazzName);
                    int variables = clazz.getTypeParameters().length;
                    if (variables > 0) {
                        return new PropertyType.Parameterized(
                                new PropertyType.Clazz(clazzName),
                                Collections.nCopies(variables, new PropertyType.Clazz(Object.class))
                        );
                    }
                } catch (ClassNotFoundException ignored) {
                }
                return new PropertyType.Clazz(clazzName);
            } else {
                return new PropertyType.Native(name);
            }
        } else {
            return new PropertyType.Clazz(Object.class);
        }
    }

    // Prefixes the class name with "$probejs$" to avoid conflicts with other types
    public static DescriptionContext PROBEJS_CONTEXT = new DescriptionContext() {
        @Override
        public String typeName(Class<?> type) {
            return "$probejs$%s".formatted(type.getName());
        }
    };
}