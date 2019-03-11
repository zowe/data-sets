#!/usr/bin/env sh

set -ex

# pass test root directory from command line parameter
TEST_DIRECTORY_ROOT=$1
if [ -z "$TEST_DIRECTORY_ROOT" ]; then
  echo "[Error] parameter 1 - TEST_DIRECTORY_ROOT is missing."
  exit 1
fi
DIRECTORY_WITH_ACCESS="directoryWithAccess"
DIRECTORY_WITHOUT_ACCESS="directoryWithoutAccess"

#Create readable directory and populate with children
mkdir $TEST_DIRECTORY_ROOT
mkdir "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITH_ACCESS"
mkdir "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITH_ACCESS/directoryInDirectoryWithAccess"
touch "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITH_ACCESS/fileInDirectoryWithAccess"

#Create unreadable directory 
mkdir "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITHOUT_ACCESS"
mkdir "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITHOUT_ACCESS/directoryInDirectoryWithAccess"
touch "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITHOUT_ACCESS/fileInDirectoryWithAccess"
chmod a-r "$TEST_DIRECTORY_ROOT/directoryWithoutAccess"

#Create readable file
cat <<EOF >$TEST_DIRECTORY_ROOT/fileWithAccess
Hello world
hello world on new line.
EOF

#Create unreadable file
cp "$TEST_DIRECTORY_ROOT/fileWithAccess" "$TEST_DIRECTORY_ROOT/fileWithoutAccess"
chmod a-r "$TEST_DIRECTORY_ROOT/fileWithoutAccess"

