package com.optum.gradle.tigergraph

import com.optum.gradle.tigergraph.Configurations.extensionName
import com.optum.gradle.tigergraph.Configurations.gsqlRuntime
import com.optum.gradle.tigergraph.tasks.GsqlCopySources
import com.optum.gradle.tigergraph.tasks.GsqlShell
import com.optum.gradle.tigergraph.tasks.GsqlTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskProvider

open class GsqlPlugin : Plugin<Project> {
    /**
     * The name of the task that copies the GSQL source files into the build directory.
     *
     * @see com.optum.gradle.tigergraph.tasks.GsqlCopySources
     */
    val copySourcesTaskName = "gsqlCopySources"

    /**
     * The name of the task that runs the interactive GSQL shell.
     *
     * @see com.optum.gradle.tigergraph.tasks.GsqlShell
     */
    val gsqlShellTaskName = "gsqlShell"

    /**
     * The name of the task type for build scripts gsql tasks.
     *
     * @see com.optum.gradle.tigergraph.tasks.GsqlTask
     */
    val gsqlTaskTypeName = "gsqlTaskType"

    /**
     * The default location, relative to the project root that contains the gsql scripts to be executed.
     *
     * @see com.optum.gradle.tigergraph.GsqlPluginExtension
     */
    val defaultGsqlScriptsDirectory = "db_scripts"

    override fun apply(project: Project): Unit = project.run {
        // Register extension for dsl
        val gsqlPluginExtension = extensions.create(extensionName, GsqlPluginExtension::class.java, project)
        gsqlPluginExtension.scriptDir.convention(layout.projectDirectory.dir(defaultGsqlScriptsDirectory))
        gsqlPluginExtension.outputDir.convention(layout.buildDirectory.dir(defaultGsqlScriptsDirectory))

        registerGsqlCopySourcesTask(gsqlPluginExtension)
        registerGsqlShell(gsqlPluginExtension)
        registerGsqlTask(gsqlPluginExtension)

        logger.lifecycle("GSQL Plugin successfully applied to ${project.name}")

        configurations.maybeCreate(gsqlRuntime)
            .description = "Gsql Runtime for Tigergraph Plugin"

        dependencies.add(gsqlRuntime, "com.tigergraph.client:Driver:2.1.7")
        dependencies.add(gsqlRuntime, "commons-cli:commons-cli:1.4")
        dependencies.add(gsqlRuntime, "jline:jline:2.11")
        dependencies.add(gsqlRuntime, "org.json:json:20180130")
        dependencies.add(gsqlRuntime, "javax.xml.bind:jaxb-api:2.3.1")
    }

    private fun Project.registerGsqlCopySourcesTask(gsqlPluginExtension: GsqlPluginExtension): TaskProvider<GsqlCopySources> =
            tasks.register(copySourcesTaskName, GsqlCopySources::class.java) { gsqlCopySources ->
                gsqlCopySources.group = JavaBasePlugin.BUILD_TASK_NAME
                gsqlCopySources.description = "Copy gsql scripts from input directory to build directory prior to execution."
                gsqlCopySources.inputDir.set(gsqlPluginExtension.scriptDir)
                gsqlCopySources.outputDir.set(gsqlPluginExtension.outputDir)
            }

    private fun Project.registerGsqlTask(gsqlPluginExtension: GsqlPluginExtension): TaskProvider<GsqlTask> =
            tasks.register(gsqlTaskTypeName, GsqlTask::class.java)

    private fun Project.registerGsqlShell(gsqlPluginExtension: GsqlPluginExtension): TaskProvider<GsqlShell> =
            tasks.register(gsqlShellTaskName, GsqlShell::class.java) { gsqlShell ->
                gsqlShell.group = "GSQL Interactive"
                gsqlShell.description = "Run an interactive gsql shell session"
            }
}
