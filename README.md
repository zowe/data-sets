# Explorer Data Sets API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=zowe_data-sets&metric=alert_status)](https://sonarcloud.io/dashboard?id=zowe_data-sets)

## Development

These instructions are intended for developers using Eclipse as their IDE ( Other IDEs may be used but setup is your own responsibility). 

Development requires an API ML [Quick Start here](https://github.com/zowe/api-layer#quick-start) - The optional step of enabling z/OSMF Authentication is required, an api-definition file for the Data Sets api service is also required. (Sample api-definition can be found in scripts/fvt/files-api.yml.template) (sample api-definition for z/OSMF can be found in scripts/fvt/zosmf.yml.template)

1. Git clone repo
2. Import > Gradle > Existing Gradle Project
3. Generate test certificate

  ```
  keytool -genkeypair -keystore localhost.keystore.p12 -storetype PKCS12 \
      -storepass password -alias localhost -keyalg RSA -keysize 2048 -validity 99999 \
      -dname \"CN=Zowe Explorer Data Sets API Default Certificate, OU=Zowe API Squad, O=Zowe, L=Hursley, ST=Hampshire, C=UK\" \
      -ext san=dns:localhost,ip:127.0.0.1
  ```
4. Create run configuration with main class `org.zowe.DataSetsAndUnixFilesApplication`
5. Add environment variables to run configuration
  ```
  connection.httpsPort : ${GATEWAY_PORT}
  connection.ipAddress : ${GATEWAY_HOST}
  server.port : {DATASETS_API_PORT}
  server.ssl.keyAlias : localhost
  server.ssl.keyStore : localhost.keystore.p12
  server.ssl.keyStorePassword : password
  server.ssl.keyStoreType : PKCS12
  ```
6. Run and navigate to https://localhost:${GATEWAY_PORT}/swagger-ui.html to see endpoints

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

- Install, Configure and Start API Mediation layer

  [API ML Quick Start](https://github.com/zowe/api-layer#quick-start) - 
  The optional step of enabling z/OSMF Authentication is required, an api-definition file for the data-sets api service is also required. (Sample api-definition can be found in scripts/fvt/files-api.yml.template)



- Start test server

  ```
  java -Xms16m -Xmx512m -Dibm.serversocket.recover=true -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/tmp \
    -Dserver.port=8443 \
    -Dserver.ssl.keyAlias=localhost \
    -Dserver.ssl.keyStore=localhost.keystore.p12 \
    -Dserver.ssl.keyStorePassword=password \
    -Dserver.ssl.keyStoreType=PKCS12 \
    -Dserver.compression.enabled=true \
    -Dconnection.httpsPort=${GATEWAY_PORT} \
    -Dconnection.ipAddress=${GATEWAY_HOST} \
    -jar $(ls -1 data-sets-api-server/build/libs/data-sets-api-server-*-boot.jar) &
  ```

  *Note: Replace the `${GATEWAY_PORT}` and `${GATEWAY_HOST}` variable in above with your API Gateway server information.

- Run integration test

  ```
  ./gradlew runIntegrationTests \
    -Pserver.host=${GATEWAY_HOST} \
    -Pserver.port=${GATEWAY_PORT} \
    -Pserver.username=${USERNAME} \
    -Pserver.password=${PASSWORD} \
    -Ptest.version=${VERSION_UNDER_TEST}
  ```
  *Note: Replace the `${GATEWAY_HOST}` and `$GATEWAY_PORT}` variables with your API Gateway server information

  *Note: Replace the `${USERNAME}` and `${PASSWORD}` variable in above with your z/OSMF server information.

  *Note: Replace the `${VERSION_UNDER_TEST}` variable with the api version you wish to test (accepted values or 1 or 2)

## Package and Deploy

```
# packaging for Zowe
./gradlew packageDataSetsApiServer
# deploy artifact
./gradlew publishArtifacts --info -Pdeploy.username=${USERNAME} -Pdeploy.password=${PASSWORD}
```

*Note: please replace the `${USERNAME}` and `${PASSWORD}` variable in above with your artifactory account information.
