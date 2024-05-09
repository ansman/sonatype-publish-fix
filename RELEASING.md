1. `git checkout main`
2. `git pull origin main`
3. Change the version in `gradle.properties` to a non-snapshot version.
4. Update the `README.md` with the new version.
5. `git add . && git commit -am "Prepare for release X.Y.Z"`
6. `./publish.sh`.
7. `git push origin main`
8. Release on GitHub:
    1. Create a new release [here](https://github.com/ansman/sonatype-publish-fix/releases/new).
    2. Use the automatic changelog. Update if needed.
    3. Ensure you pick the "Prepare for release X.Y.Z" as the target commit.
9. `git pull origin main --tags`
10. Update the `gradle.properties` to the next SNAPSHOT version.
11. `git commit -am "Prepare next development version"`
12. `git push origin main`