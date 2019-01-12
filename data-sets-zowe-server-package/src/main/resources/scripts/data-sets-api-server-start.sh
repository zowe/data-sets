# Variables to be replaced:
# - JAVA_SETUP -- sets JAVA_HOME by ZOWE_JAVA_HOME
# - SERVER_PORT - The port the jobs server will use
# - KEYSTORE - The keystore to use for SSL certificates
# - KEYSTORE_PASSWORD - The password to access the keystore supplied by KEYSTORE
# - KEY_ALIAS - The alias of the key within the keystore
# - ZOSMF_HTTPS_PORT - The SSL port z/OSMF is listening on.
# - ZOSMF_IP - The IP Address z/OSMF can be reached

**JAVA_SETUP**
if [[ ":$PATH:" == *":$JAVA_HOME/bin:"* ]]; then
  echo "ZOWE_JAVA_HOME already exists on the PATH"
else
  echo "Appending ZOWE_JAVA_HOME/bin to the PATH..."
  export PATH=$PATH:$JAVA_HOME/bin
  echo "Done."
fi

DIR=`dirname $0`

java -Xms16m -Xmx512m -Dibm.serversocket.recover=true -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/tmp -Xquickstart \
    -Dserver.port=**SERVER_PORT** \
    -Dserver.ssl.keyAlias=**KEY_ALIAS** \
    -Dserver.ssl.keyStore=**KEYSTORE** \
    -Dserver.ssl.keyStorePassword=**KEYSTORE_PASSWORD** \
    -Dserver.ssl.keyStoreType=PKCS12 \
    -Dzosmf.httpsPort=**ZOSMF_HTTPS_PORT** \
    -Dzosmf.ipAddress=**ZOSMF_IP** \
    -jar $DIR/../**JOBS_JAR** &
