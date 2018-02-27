package es.alarcos.archirev;

import the.bytecode.club.bytecodeviewer.DecompilerSettings;

public enum Settings implements DecompilerSettings.Setting {
    DECODE_ENUM_SWITCH("decodeenumswitch", "Decode Enum Switch", true),
    SUGAR_ENUMS("sugarenums", "SugarEnums", true),
    DECODE_STRING_SWITCH("decodestringswitch", "Decode String Switch", true),
    ARRAYITER("arrayiter", "Arrayiter", true),
    COLLECTIONITER("collectioniter", "Collectioniter", true),
    INNER_CLASSES("innerclasses", "Inner Classes", true),
    REMOVE_BOILER_PLATE("removeboilerplate", "Remove Boiler Plate", true),
    REMOVE_INNER_CLASS_SYNTHETICS("removeinnerclasssynthetics", "Remove Inner Class Synthetics", true),
    DECODE_LAMBDAS("decodelambdas", "Decode Lambdas", true),
    HIDE_BRIDGE_METHODS("hidebridgemethods", "Hide Bridge Methods", true),
    LIFT_CONSTRUCTOR_INIT("liftconstructorinit", "Lift Constructor Init", true),
    REMOVE_DEAD_METHODS("removedeadmethods", "Remove Dead Methods", true),
    REMOVE_BAD_GENERICS("removebadgenerics", "Remove Bad Generics", true),
    SUGAR_ASSERTS("sugarasserts", "Sugar Asserts", true),
    SUGAR_BOXING("sugarboxing", "Sugar Boxing", true),
    SHOW_VERSION("showversion", "Show Version", true),
    DECODE_FINALLY("decodefinally", "Decode Finally", true),
    TIDY_MONITORS("tidymonitors", "Tidy Monitors", true),
    LENIENT("lenient", "Lenient"),
    DUMP_CLASS_PATH("dumpclasspath", "Dump Classpath"),
    COMMENTS("comments", "Comments", true),
    FORCE_TOP_SORT("forcetopsort", "Force Top Sort", true),
    FORCE_TOP_SORT_AGGRESSIVE("forcetopsortaggress", "Force Top Sort Aggressive", true),
    STRINGBUFFER("stringbuffer", "StringBuffer"),
    STRINGBUILDER("stringbuilder", "StringBuilder", true),
    SILENT("silent", "Silent", true),
    RECOVER("recover", "Recover", true),
    ECLIPSE("eclipse", "Eclipse", true),
    OVERRIDE("override", "Override", true),
    SHOW_INFERRABLE("showinferrable", "Show Inferrable", true),
    FORCE_AGGRESSIVE_EXCEPTION_AGG("aexagg", "Force Aggressive Exception Aggregation", true),
    FORCE_COND_PROPAGATE("forcecondpropagate", "Force Conditional Propogation", true),
    HIDE_UTF("hideutf", "Hide UTF", true),
    HIDE_LONG_STRINGS("hidelongstrings", "Hide Long Strings"),
    COMMENT_MONITORS("commentmonitors", "Comment Monitors"),
    ALLOW_CORRECTING("allowcorrecting", "Allow Correcting", true),
    LABELLED_BLOCKS("labelledblocks", "Labelled Blocks", true),
    J14_CLASS_OBJ("j14classobj", "Java 1.4 Class Objects"),
    HIDE_LANG_IMPORTS("hidelangimports", "Hide Lang Imports", true),
    RECOVER_TYPE_CLASH("recovertypeclash", "Recover Type Clash", true),
    RECOVER_TYPE_HINTS("recovertypehints", "Recover Type Hints", true),
    FORCE_RETURNING_IFS("forcereturningifs", "Force Returning Ifs", true),
    FOR_LOOP_AGG_CAPTURE("forloopaggcapture", "For Loop Aggressive Capture", true);

    private String name;
    private String param;
    private boolean on;

    Settings(String param, String name) {
        this(param, name, false);
    }

    Settings(String param, String name, boolean on) {
        this.name = name;
        this.param = param;
        this.on = on;
    }

    public String getText() {
        return name;
    }

    public boolean isDefaultOn() {
        return on;
    }

    public String getParam() {
        return param;
    }
}