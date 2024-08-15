package se.ansman.sonatypepublishfix

import assertk.assertThat
import assertk.assertions.contains
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream
import kotlin.streams.asStream

class FunctionalTest {
    @TempDir
    lateinit var projectDir: File

    private fun gradleRunner(version: String) = GradleRunner.create()
        .withGradleVersion(version)
        .withArguments("--parallel", "--max-workers=2", "--stacktrace")
        .withProjectDir(projectDir)
        .forwardOutput()

    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun preventsConcurrentExecution(gradleVersion: String) {
        gradleRunner(gradleVersion)
            .build("dummyPublish")
    }

    @ParameterizedTest
    @MethodSource("provideGradleVersions")
    fun doesntPreventIfExcluded(gradleVersion: String) {
        projectDir.resolve("module1/build.gradle.kts").appendText("""
            sonatypePublishFix {
              excludeRepositoryNamed("repo1")
              excludeRepositoryNamed("repo2")
            }
        """.trimIndent())
        projectDir.resolve("module2/build.gradle.kts").appendText("""
            sonatypePublishFix {
              excludeRepositoryNamed("repo1")
              excludeRepositoryNamed("repo2")
            }
        """.trimIndent())

        val result = gradleRunner(gradleVersion)
            .buildAndFail("dummyPublish")
        assertThat(result.output).contains("Concurrent execution detected")
    }

    private fun GradleRunner.build(vararg tasks: String) =
        appendArguments(*tasks)
            .also { println("Running ./gradlew ${arguments.joinToString(" ")}") }
            .build()

    private fun GradleRunner.buildAndFail(vararg tasks: String) =
        appendArguments(*tasks)
            .also { println("Running ./gradlew ${arguments.joinToString(" ")}") }
            .buildAndFail()

    private fun GradleRunner.appendArguments(vararg arguments: String) =
        withArguments(this.arguments + arguments)

    @BeforeEach
    fun setup() {
        projectDir.resolve("settings.gradle.kts").writeKotlin("""
            pluginManagement {
                repositories {
                    maven("${System.getProperty("mavenRepo")}")
                    gradlePluginPortal()
                }
            }
            
            include(":module1")
            include(":module2")
        """.trimIndent())

        projectDir.resolve("build.gradle.kts").writeKotlin(
            """
            import java.util.concurrent.atomic.AtomicInteger

            check(gradle.startParameter.maxWorkerCount >= 2 )
            ext.set("concurrentTasks", AtomicInteger(0))
            """.trimIndent()
        )
        val moduleScript = """
             import java.util.concurrent.atomic.AtomicInteger

            plugins {
                `maven-publish`
                id("se.ansman.sonatype-publish-fix") version "${System.getProperty("pluginVersion")}"
            }
            
            abstract class DummyPublishTask : org.gradle.api.publish.maven.tasks.PublishToMavenRepository() {
                private val concurrentTasks = project.rootProject.ext["concurrentTasks"] as AtomicInteger 
            
                @org.gradle.api.tasks.TaskAction
                override fun publish() {
                    println("Task ${'$'}name started")
                    check(concurrentTasks.incrementAndGet() == 1) { "Concurrent execution detected" }
                    Thread.sleep(1000)
                    concurrentTasks.decrementAndGet()
                    println("Task ${'$'}name ended")
                }
            }
            val publ = publishing.publications.create<MavenPublication>("publication") {
                groupId = "com.example"
                artifactId = "example"
                version = "1.0.0"
            }
            
            publishing {
                repositories {
                    maven {
                        name = "repo1"
                        url = uri("https://example.com/repo1")
                    }
                    maven {
                        name = "repo2"
                        url = uri("https://example.com/repo2")
                    }
                }
            }
        """.trimIndent()

        projectDir.resolve("module1/build.gradle.kts").writeKotlin(
            """
            $moduleScript
            tasks.register<DummyPublishTask>("dummyPublish") {
                repository = publishing.repositories.getByName("repo1") as MavenArtifactRepository
                publication = publ
            }
            """.trimIndent()
        )

        projectDir.resolve("module2/build.gradle.kts").writeKotlin(
            """
            $moduleScript
            tasks.register<DummyPublishTask>("dummyPublish") {
                repository = publishing.repositories.getByName("repo2") as MavenArtifactRepository
                publication = publ
            }
            """.trimIndent()
        )
    }

    companion object {
        private fun File.writeKotlin(content: String) {
            parentFile.mkdirs()
            writeText(content.trimIndent() + "\n")
        }

        @JvmStatic
        private fun provideGradleVersions(): Stream<Arguments> =
            sequenceOf(
                System.getProperty("currentGradleVersion"),
                "8.8",
                "8.7",
                "8.6",
                "8.5",
                "8.4",
                "8.3",
                "8.2.1",
                "8.1.1",
                "8.0.2",
                "7.6.4",
            )
                .distinct()
                .map { Arguments.of(it) }
                .asStream()
    }
}