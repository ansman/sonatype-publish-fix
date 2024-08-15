import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.gradle.publish)
}

if (providers.gradleProperty("signArtifacts").orNull?.toBooleanStrict() == true) {
    pluginManager.apply("signing")
}

group = "se.ansman.sonatype-publish-fix"
version = providers.gradleProperty("version").get()

tasks.withType<Test> {
    useJUnitPlatform()
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    maxHeapSize = "1g"
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
        languageVersion.set(KotlinVersion.KOTLIN_1_9)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.AZUL)
        implementation.set(JvmImplementation.VENDOR_SPECIFIC)
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

val buildDirMavenRepo = layout.buildDirectory.dir("repo")
publishing {
    repositories {
        val buildDirRepo = maven {
            name = "buildDir"
            url = uri(buildDirMavenRepo)
        }
        tasks.withType<AbstractPublishToMaven>().configureEach {
            doLast {
                if (this is PublishToMavenRepository && repository == buildDirRepo) {
                    return@doLast
                }
                println("Published ${publication.groupId}:${publication.artifactId}:${publication.version}")
            }
        }
    }
}


val functionalTest: SourceSet by sourceSets.creating

val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    dependsOn("publishAllPublicationsToBuildDirRepository")
    mustRunAfter(tasks.test)
    systemProperty("mavenRepo", buildDirMavenRepo.get().asFile.absolutePath)
    systemProperty("pluginVersion", version.toString())
    systemProperty("currentGradleVersion", gradle.gradleVersion)
}

tasks.check {
    dependsOn(functionalTestTask)
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    isAutomatedPublishing = true
    testSourceSets(sourceSets.test.get(), functionalTest)
    plugins {
        register("sonatypePublishFix") {
            id = "se.ansman.sonatype-publish-fix"
            implementationClass = "se.ansman.sonatypepublishfix.SonatypePublishFixPlugin"
            displayName = "Sonatype Publish Fix"
            description = "A Gradle Plugin that prevents parallel publishing to maven repositories."
            tags = setOf("publishing", "maven", "sonatype")
        }
    }
    vcsUrl.set("https://github.com/ansman/sonatype-publish-fix")
    website.set("https://github.com/ansman/sonatype-publish-fix")
}

pluginManager.withPlugin("org.gradle.signing") {
    configure<SigningExtension> {
        useGpgCmd()
    }
}

dependencies {
    "functionalTestImplementation"(platform(libs.junit.bom))
    "functionalTestImplementation"(libs.junit.jupiter)
    "functionalTestRuntimeOnly"(libs.junit.platform.launcher)
    "functionalTestImplementation"(libs.assertk)
}