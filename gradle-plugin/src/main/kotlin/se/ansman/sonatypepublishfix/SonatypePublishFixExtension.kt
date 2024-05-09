package se.ansman.sonatypepublishfix

import org.gradle.api.NamedDomainObjectList
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.SetProperty
import org.gradle.api.publish.PublishingExtension
import javax.inject.Inject

@Suppress("LeakingThis")
abstract class SonatypePublishFixExtension @Inject constructor(private val project: Project) {
    /**
     * Which repositories that should have the fix applied.
     *
     * By default, all remote maven repositories are included.
     */
    abstract val includedRepositories: SetProperty<MavenArtifactRepository>

    /**
     * Which repositories that should not have the fix applied.
     *
     * By default, no repositories are excluded.
     */
    abstract val excludedRepositories: SetProperty<MavenArtifactRepository>

    init {
        with(includedRepositories) {
            finalizeValueOnRead()
            addAll(publishing.repositories.withType(MavenArtifactRepository::class.java))
        }
        with(excludedRepositories) {
            finalizeValueOnRead()
        }
    }

    /** Includes the given repository in the list of repositories that should have the fix applied. */
    fun includeRepository(repository: MavenArtifactRepository) {
        includedRepositories.add(repository)
    }

    /** Includes all repositories with the given url in the list of repositories that should have the fix applied. */
    fun includeRepositoryWithUrl(url: String) {
        includedRepositories.addAll(repositoriesForUrl(url))
    }

    /** Includes all repositories with the given name in the list of repositories that should have the fix applied. */
    fun includeRepositoryNamed(name: String) {
        includedRepositories.add(repositoriesNamed(name))
    }

    /** Excludes the given repository from the list of repositories that should have the fix applied. */
    fun excludeRepository(repository: MavenArtifactRepository) {
        excludedRepositories.add(repository)
    }

    /** Excludes all repositories with the given url from the list of repositories that should have the fix applied. */
    fun excludeRepositoryWithUrl(url: String) {
        excludedRepositories.addAll(repositoriesForUrl(url))
    }

    /** Excludes all repositories with the given name from the list of repositories that should have the fix applied. */
    fun excludeRepositoryNamed(name: String) {
        excludedRepositories.add(repositoriesNamed(name))
    }

    private fun repositoriesNamed(name: String): NamedDomainObjectProvider<MavenArtifactRepository> =
        publishing.repositories.named(name, MavenArtifactRepository::class.java)

    private fun repositoriesForUrl(url: String): NamedDomainObjectList<MavenArtifactRepository> =
        publishing.repositories.withType(MavenArtifactRepository::class.java).matching {
            it.url.toString() == url
        }

    private val publishing: PublishingExtension
        get() = try {
            project.extensions.getByType(PublishingExtension::class.java)
        } catch (e: UnknownDomainObjectException) {
            throw UnknownDomainObjectException("Could not find the publishing extension, make sure you applied the maven-publish plugin.", e)
        }
}