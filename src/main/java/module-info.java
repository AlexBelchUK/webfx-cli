// File managed by WebFX (DO NOT EDIT MANUALLY)

module webfx.buildtool {

    // Direct dependencies modules
    requires info.picocli;
    requires java.base;
    requires java.xml;
    requires maven.invoker;
    requires webfx.lib.reusablestream;

    // Exported packages
    exports dev.webfx.buildtool;
    exports dev.webfx.buildtool.cli;
    exports dev.webfx.buildtool.modulefiles;
    exports dev.webfx.buildtool.modulefiles.abstr;
    exports dev.webfx.buildtool.sourcegenerators;
    exports dev.webfx.buildtool.util.javacode;
    exports dev.webfx.buildtool.util.process;
    exports dev.webfx.buildtool.util.splitfiles;
    exports dev.webfx.buildtool.util.textfile;
    exports dev.webfx.buildtool.util.xml;

    // Resources packages
    opens dev.webfx.buildtool.cli;
    opens dev.webfx.buildtool.jdk;
    opens dev.webfx.buildtool.templates;

}