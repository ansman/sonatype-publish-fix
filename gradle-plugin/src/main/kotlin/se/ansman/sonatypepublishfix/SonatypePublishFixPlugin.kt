package se.ansman.sonatypepublishfix

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository

abstract class SonatypePublishFixPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("sonatypePublishFix", SonatypePublishFixExtension::class.java)

        val publishService = target.gradle.sharedServices.registerIfAbsent("mavenPublishService", MavenPublishService::class.java) {
            it.maxParallelUsages.set(1)
            it.maxParallelUsages.disallowChanges()
        }

        target.afterEvaluate {
            extension.includedRepositories.disallowChanges()
            extension.excludedRepositories.disallowChanges()
            target.tasks.withType(PublishToMavenRepository::class.java) { task ->
                if (task.repository in extension.includedRepositories.get() && task.repository !in extension.excludedRepositories.get()) {
                    task.usesService(publishService)
                }
            }
        }
    }
}