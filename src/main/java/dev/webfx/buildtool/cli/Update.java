package dev.webfx.buildtool.cli;

import dev.webfx.buildtool.Platform;
import dev.webfx.buildtool.ProjectModule;
import dev.webfx.buildtool.TargetTag;
import dev.webfx.buildtool.sourcegenerators.GluonFilesGenerator;
import dev.webfx.buildtool.sourcegenerators.GwtFilesGenerator;
import dev.webfx.buildtool.sourcegenerators.JavaFilesGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * @author Bruno Salmon
 */
@Command(name = "update", description = "Update build files from webfx.xml files.")
final class Update extends CommonSubcommand implements Runnable {

    @Option(names={"-o", "--only",}, arity = "1..*", description = "Run only the specified update tasks *.")
    String[] only;

    @Option(names={"-s", "--skip",}, arity = "1..*", description = "Skip the specified update tasks *.")
    String[] skip;

    private Boolean
            mavenPom,
            moduleInfoJava,
            metaInfServices,
            indexHtml,
            gwtXml,
            gwtSuperSources,
            gwtServiceLoader,
            gwtResourceBundles;

    private static final String[] TASK_WORDS = {
            "pom.xml",
            "module-info.java",
            "meta-inf/services",
            "index.html",
            "gwt.xml",
            "gwt-super-sources",
            "gwt-service-loader",
            "gwt-resource-bundles",
    };

    private static final char[] TASK_LETTERS = {
            'p', // mavenPom
            'j', // moduleInfoJava
            'm', // metaInfServices
            'h', // indexHtml
            'g', // gwtXml
            's', // gwtSuperSources
            'l', // gwtServiceLoader
            'b', // gwtResourceBundles
    };

    private void processTaskFlags(String[] flags, boolean value) {
        if (flags != null)
            for (String flag : flags)
                processTaskFlag(flag, value);
    }

    private void processTaskFlag(String flag, boolean value) {
        for (int taskIndex = 0; taskIndex < TASK_WORDS.length; taskIndex++)
            if (flag.equalsIgnoreCase(TASK_WORDS[taskIndex])) {
                enableTask(taskIndex, value);
                return;
            }
        if (!processTaskLetters(flag, value))
            throw new IllegalArgumentException("Unrecognized task " + flag);
    }

    private boolean processTaskLetters(String flag, boolean value) {
        for (int taskIndex = 0; taskIndex < TASK_WORDS.length; taskIndex++)
            if (flag.charAt(0) == TASK_LETTERS[taskIndex]) {
                enableTask(taskIndex, value);
                if (flag.length() > 1)
                    return processTaskLetters(flag.substring(1), value);
                return true;
            }
        return false;
    }

    private void enableTask(int taskIndex, boolean value) {
        if (taskIndex == 0)
            mavenPom = value;
        else if (taskIndex == 1)
            moduleInfoJava = value;
        else if (taskIndex == 2)
            metaInfServices = value;
        else if (taskIndex == 3)
            indexHtml = value;
        else if (taskIndex == 4)
            gwtXml = value;
        else if (taskIndex == 5)
            gwtSuperSources = value;
        else if (taskIndex == 6)
            gwtServiceLoader = value;
        else if (taskIndex == 7)
            gwtResourceBundles = value;
    }

    @Override
    public void run() {
        setUpLogger();

        for (int i = 0; i < TASK_LETTERS.length; i++)
            enableTask(i, only == null);
        processTaskFlags(only, true);
        processTaskFlags(skip, false);
/*
        System.out.println(
                "mavenPom = " + mavenPom +
                "\nmoduleInfoJava = " + moduleInfoJava +
                "\nmetaInfServices = " + metaInfServices +
                "\nindexHtml = " + indexHtml +
                "\ngwtXml = " + gwtXml +
                "\ngwtSuperSources = " + gwtSuperSources +
                "\ngwtServiceLoader = " + gwtServiceLoader +
                "\ngwtResourceBundles = " + gwtResourceBundles
                );
*/

        ProjectModule rootModule = getWorkingProjectModule();

        // Updating Maven module files for all source modules (<dependencies> section in pom.xml)
        if (mavenPom)
            rootModule
                    .getThisAndChildrenModulesInDepth()
                    .filter(ProjectModule::hasSourceDirectory)
                    .forEach(m -> m.getMavenModuleFile().updateAndWrite())
            ;

        // Generating files for Java modules (module-info.java and META-INF/services)
        if (moduleInfoJava || metaInfServices)
            rootModule
                    .getThisAndChildrenModulesInDepth()
                    .filter(ProjectModule::hasSourceDirectory)
                    .filter(ProjectModule::hasJavaSourceDirectory)
                    .filter(m -> m.getTarget().isPlatformSupported(Platform.JRE))
                    .forEach(JavaFilesGenerator::generateJavaFiles)
            ;

        if (gwtXml || indexHtml || gwtSuperSources || gwtServiceLoader || gwtResourceBundles)
        // Generate files for executable GWT modules (module.gwt.xml, index.html, super sources, service loader, resource bundle)
            rootModule
                    .getThisAndChildrenModulesInDepth()
                    .filter(m -> m.isExecutable(Platform.GWT))
                    .forEach(GwtFilesGenerator::generateGwtFiles);

        // Generate files for executable Gluon modules (graalvm_config/reflection.json)
        rootModule
                .getThisAndChildrenModulesInDepth()
                .filter(m -> m.isExecutable(Platform.JRE))
                .filter(m -> m.getTarget().hasTag(TargetTag.GLUON))
                .forEach(GluonFilesGenerator::generateGraalVmReflectionJson);

    }
}