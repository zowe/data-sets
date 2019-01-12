# Explorer Data Sets API

[![Code Quality](https://jayne.zowe.org:9000/api/project_badges/measure?project=explorer%3Adata-sets-api-server&metric=alert_status)](https://jayne.zowe.org:9000/dashboard/index/explorer:data-sets-api-server)

## Build

```
./gradlew build
```

## SonarQube Static Code Scan

```
./gradlew --info sonarqube -Psonar.host.url=${SONAR_HOST_URL}
```

*Note: please replace the `${SONAR_HOST_URL}` variable in above with your SonarQube server URL.

## Test

### Unit Test

```
./gradlew test
```

### Integration Test

- Generate test certificate

  ```
  keytool -genkeypair -keystore localhost.keystore.p12 -storetype PKCS12 \
      -storepass password -alias localhost -keyalg RSA -keysize 2048 -validity 99999 \
      -dname \"CN=Zowe Explorer Data Sets API Default Certificate, OU=Zowe API Squad, O=Zowe, L=Hursley, ST=Hampshire, C=UK\" \
      -ext san=dns:localhost,ip:127.0.0.1
  ```

- Start test server

  ```
  java -Xms16m -Xmx512m -Dibm.serversocket.recover=true -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/tmp \
    -Dserver.port=8443 \
    -Dserver.ssl.keyAlias=localhost \
    -Dserver.ssl.keyStore=localhost.keystore.p12 \
    -Dserver.ssl.keyStorePassword=password \
    -Dserver.ssl.keyStoreType=PKCS12 \
    -Dzosmf.httpsPort=${ZOSMF_PORT} \
    -Dzosmf.ipAddress=${ZOSMF_HOST} \
    -jar $(ls -1 data-sets-api-server/build/libs/data-sets-api-server-*-boot.jar) &
  ```

  *Note: please replace the `${ZOSMF_PORT}` and `${ZOSMF_HOST}` variable in above with your z/OSMF server information.

- Run integration test

  ```
  ./gradlew runIntegrationTests \
    -Pserver.host=localhost \
    -Pserver.port=8443 \
    -Pserver.username=${USERNAME} \
    -Pserver.password=${PASSWORD}
  ```

  *Note: please replace the `${USERNAME}` and `${PASSWORD}` variable in above with your z/OSMF server information.

## Package and Deploy

```
# packaging for Zowe
./gradlew packageDataSetsApiServer
# deploy artifact
./gradlew publishArtifacts --info -Pdeploy.username=${USERNAME} -Pdeploy.password=${PASSWORD}
```

*Note: please replace the `${USERNAME}` and `${PASSWORD}` variable in above with your artifactory account information.
