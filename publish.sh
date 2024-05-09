#!/usr/bin/env bash

set -e

echo "Cleaning..."
./gradlew -q clean

echo "Running checks..."
./gradlew -q check publishPlugins --validate-only

echo "Publishing..."
./gradlew -q publishPlugins \
  -PsignArtifacts=true \
  -Psigning.gnupg.executable=/opt/homebrew/bin/gpg \
  -Psigning.gnupg.keyName=$(op --account my.1password.com read op://private/GnuPG/keyID | xargs) \
  -Psigning.gnupg.passphrase=$(op --account my.1password.com read op://private/GnuPG/password | xargs)

echo "Done"
