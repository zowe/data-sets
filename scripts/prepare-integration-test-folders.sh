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
chmod 777 "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITH_ACCESS"
mkdir "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITH_ACCESS/directoryInDirectoryWithAccess"
touch "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITH_ACCESS/fileInDirectoryWithAccess"

#Create unreadable directory 
mkdir "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITHOUT_ACCESS"
mkdir "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITHOUT_ACCESS/directoryInDirectoryWithAccess"
touch "$TEST_DIRECTORY_ROOT/$DIRECTORY_WITHOUT_ACCESS/fileInDirectoryWithAccess"
chmod 700 "$TEST_DIRECTORY_ROOT/directoryWithoutAccess"

#Create readable file
cat <<EOF >$TEST_DIRECTORY_ROOT/fileWithAccess
Hello world
hello world on new line.
EOF

#Create readable ebcdic file
touch "$TEST_DIRECTORY_ROOT/fileWithAccessEbcdic"
chtag -t -c IBM-1047 "$TEST_DIRECTORY_ROOT/fileWithAccessEbcdic"
cat <<EOF >$TEST_DIRECTORY_ROOT/fileWithAccessEbcdic
Hello world
hello world on new line.
EOF

#Create readable ascii file
iconv -f IBM-1047 -t ISO8859-1 "$TEST_DIRECTORY_ROOT/fileWithAccessEbcdic" > "$TEST_DIRECTORY_ROOT/fileWithAccessAscii"
chtag -t -c ISO8859-1 "$TEST_DIRECTORY_ROOT/fileWithAccessAscii"

#Create unreadable file
cp "$TEST_DIRECTORY_ROOT/fileWithAccess" "$TEST_DIRECTORY_ROOT/fileWithoutAccess"
chmod 600 "$TEST_DIRECTORY_ROOT/fileWithoutAccess"

#Create editable files
touch "$TEST_DIRECTORY_ROOT/editableFile"
chtag -r "$TEST_DIRECTORY_ROOT/editableFile"
chmod a+w "$TEST_DIRECTORY_ROOT/editableFile"

touch "$TEST_DIRECTORY_ROOT/editableAsciiTaggedFile"
chtag -t -c ISO8859-1 "$TEST_DIRECTORY_ROOT/editableAsciiTaggedFile"
chmod a+w "$TEST_DIRECTORY_ROOT/editableAsciiTaggedFile"

touch "$TEST_DIRECTORY_ROOT/editableEbcdicTaggedFile"
chtag -t -c IBM-1047 "$TEST_DIRECTORY_ROOT/editableEbcdicTaggedFile"
chmod a+w "$TEST_DIRECTORY_ROOT/editableEbcdicTaggedFile"

touch "$TEST_DIRECTORY_ROOT/editableEbcdicFileUntaggedFile"
chtag -r "$TEST_DIRECTORY_ROOT/editableEbcdicFileUntaggedFile"
chmod a+w "$TEST_DIRECTORY_ROOT/editableEbcdicFileUntaggedFile"

#Create file and directory for post/create already exists error
touch "$TEST_DIRECTORY_ROOT/fileAlreadyExists"
mkdir "$TEST_DIRECTORY_ROOT/directoryAlreadyExists"

ls -alT $TEST_DIRECTORY_ROOT