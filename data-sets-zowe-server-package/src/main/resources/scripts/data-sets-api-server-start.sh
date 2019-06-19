# Variables to be replaced:
# - ZOWE_JAVA_HOME - JAVA_HOME that zowe is using
# - FILES_API_PORT - The port the data sets server will use
# - KEYSTORE - The keystore to use for SSL certificates
# - KEYSTORE_PASSWORD - The password to access the keystore supplied by KEYSTORE
# - KEY_ALIAS - The alias of the key within the keystore
# - ZOSMF_PORT - The SSL port z/OSMF is listening on.
# - ZOSMF_IP_ADDRESS - The IP Address z/OSMF can be reached

export JAVA_HOME=$ZOWE_JAVA_HOME
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
    -Dserver.port=${FILES_API_PORT} \
    -Dserver.ssl.keyAlias=${KEY_ALIAS} \
    -Dserver.ssl.keyStore=${KEYSTORE} \
    -Dserver.ssl.keyStorePassword=${KEYSTORE_PASSWORD} \
    -Dserver.ssl.keyStoreType=PKCS12 \
    -Dzosmf.httpsPort=${ZOSMF_PORT} \
    -Dzosmf.ipAddress=${ZOSMF_IP_ADDRESS} \
    -jar $DIR/../{{jar_name}} &
