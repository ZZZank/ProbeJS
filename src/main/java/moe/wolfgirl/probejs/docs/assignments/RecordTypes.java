package moe.wolfgirl.probejs.docs.assignments;

import dev.latvian.mods.rhino.type.RecordTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSArrayType;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSObjectType;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

public class RecordTypes extends ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        TypeConverter converter = scriptDump.transpiler.typeConverter;
        for (Clazz recordedClass : scriptDump.recordedClasses) {
            if (!recordedClass.original.isRecord()) continue;
            RecordTypeInfo typeWrapper = (RecordTypeInfo) TypeInfo.of(recordedClass.original);

            JSObjectType.Builder objectType = Types.object();
            JSArrayType.Builder arrayType = Types.arrayOf();

            for (RecordTypeInfo.Component component : typeWrapper.recordComponents().values()) {
                BaseType type = converter.convertType(component.type());

                objectType.member(component.name(), true, type);
                arrayType.member(component.name(), true, type);
            }

            scriptDump.assignType(recordedClass.classPath, objectType.build());
            scriptDump.assignType(recordedClass.classPath, arrayType.build());
        }
    }
}