Sonatype Publish Fix [![Build Gradle](https://github.com/ansman/sonatype-publish-fix/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/ansman/sonatype-publish-fix/actions/workflows/gradle.yml) [![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v.svg?label=gradle&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Fse%2Fansman%2Fsonatype-publish-fix%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/se.ansman.sonatype-publish-fix)
===
When publishing to any sonatype repository, it's important to not publishing multiple artifacts in parallel. This is
often done by using `--no-parallel` or `--max-workers` which slows down the build.

This plugin will limit the parallelism of the publication tasks to 1 when publishing to avoid this issue.

For the changelog see the [releases page](https://github.com/ansman/sonatype-publish-fix/releases).

Setup
---
To set it up, all you need to do is to apply the plugin:
```kotlin
plugins {
    `maven-publish`
    id("se.ansman.sonatype-publish-fix") version "1.0.0"
}
```

Configuration
---
By default, publications to all remote maven repos are limited. You can change this by using the extension:
```kotlin
plugins {
    `maven-publish`
    id("se.ansman.sonatype-publish-fix") version "1.0.0"
}

sonatypePublishFix {
    // Includes a repo, by default all repos are included
    includeRepositoryNamed("sonatype")
    includeRepositoryWithUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
    
    // Excludes a repo
    excludeRepositoryNamed("artifactory")
    excludeRepositoryWithUrl("https://artifactory.example.com/artifactory/libs-release-local/")
}
```

License
---
This project is licensed under the Apache-2.0 license. See [LICENSE.txt](LICENSE.txt) for the full license.
```plain

Copyright 2024 Nicklas Ansman

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
