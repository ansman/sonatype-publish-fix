package se.ansman.sonatypepublishfix

import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

abstract class MavenPublishService : BuildService<BuildServiceParameters.None>