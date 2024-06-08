package moe.wolfgirl.util.special_docs;

import moe.wolfgirl.jdoc.document.DocumentClass;
import moe.wolfgirl.jdoc.document.DocumentMethod;
import moe.wolfgirl.jdoc.java.ClassInfo;
import moe.wolfgirl.jdoc.property.PropertyParam;
import moe.wolfgirl.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.recipe.RecipeSchemaRegistryEventJS;

import java.util.List;

public class SchemaRegistryEventJSDocument {
    public static void loadRecipeSchemaRegistryEvent(List<DocumentClass> globalClasses) {
        String clazzName = ClassInfo.getOrCache(RecipeSchemaRegistryEventJS.class).getName();
        DocumentClass recipeSchemaEventJS = globalClasses.stream().filter(documentClass -> documentClass.getName().equals(clazzName)).findAny().get();
        DocumentMethod getComponents = recipeSchemaEventJS.methods.stream().filter(documentMethod -> documentMethod.getName().equals("getComponents")).findAny().get();
        DocumentMethod register = recipeSchemaEventJS.methods.stream().filter(documentMethod -> documentMethod.getName().equals("register")).findAny().get();

        register.params.set(0, new PropertyParam("id", new PropertyType.Native("Special.RecipeType"), false));
        getComponents.returns = new PropertyType.Native("Special.RecipeComponentMap");
    }
}