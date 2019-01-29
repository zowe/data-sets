#!/usr/bin/env sh

set -ex

./bootstrap_gradlew.sh
AUTH="-Pdeploy.username=$USERNAME -Pdeploy.password=$PASSWORD"

case $RELEASE_TYPE in
   "SNAPSHOT_RELEASE")
   echo "Make SNAPSHOT release"
   ./gradlew publishAllVersions $AUTH
   git archive --format tar.gz -9 --output explorer-jobs.tar.gz HEAD~1
   ;;
   "PATCH_RELEASE")
   echo "Make PATCH release"
   ./gradlew release -Prelease.useAutomaticVersion=true -Prelease.scope=patch $AUTH
   git archive --format tar.gz -9 --output explorer-jobs.tar.gz HEAD~1
   ;;
   "MINOR_RELEASE")
   echo "Make MINOR release"
   ./gradlew release -Prelease.useAutomaticVersion=true -Prelease.scope=minor $AUTH
   git archive --format tar.gz -9 --output explorer-jobs.tar.gz HEAD~1
   ;;
   "MAJOR_RELEASE")
   echo "Make MAJOR release"
   ./gradlew release -Prelease.useAutomaticVersion=true -Prelease.scope=major $AUTH
   git archive --format tar.gz -9 --output explorer-jobs.tar.gz HEAD~1
   ;;
   "SPECIFIC_RELEASE")
   echo "Make specific release"
   ./gradlew release -Prelease.useAutomaticVersion=true -Prelease.releaseVersion=$RELEASE_VERSION -Prelease.newVersion=$NEW_VERSION $AUTH
   git archive --format tar.gz -9 --output explorer-jobs.tar.gz "v$RELEASE_VERSION"
esac

echo "End of publish and release"