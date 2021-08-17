# Files API Image

[![Build ompzowe/files-api](https://github.com/zowe/data-sets/actions/workflows/files-api-images.yml/badge.svg)](https://github.com/zowe/data-sets/actions/workflows/files-api-images.yml)

## General Information

This image can be used to start Datasets and Unix Files REST API.

It includes 2 Linux Distro:

- Ubuntu
- Red Hat UBI

Each image supports both `amd64` and `s390x` CPU architectures.

## Usage

Image `zowe-docker-release.jfrog.io/ompzowe/files-api:latest` should be able to run with minimal environment variables:

- `KEYSTORE`: path to keystore.
- `KEY_ALIAS`: certificate alias stored in keystore.
- `KEYSTORE_PASSWORD`: password of your keystore.
- `GATEWAY_PORT`: port of your APIML Gateway.

Other environment variable(s) can be used:

- `FILES_API_PORT`: starting port, default is `8547`.
- `ZOWE_EXPLORER_HOST`: domain name to access APIML Gateway, default is `localhost`.
- `KEYSTORE_TYPE`: type of keystore, default is `PKCS12`.

Example commands:

```
# pull image
docker pull zowe-docker-release.jfrog.io/ompzowe/files-api:latest
# start container
docker run -it --rm -p 8547:8547 \
    -v $(pwd)/keystore:/home/zowe/keystore \
    -e KEYSTORE=/home/zowe/keystore/localhost/localhost.keystore.p12 \
    -e KEY_ALIAS=localhost \
    -e KEYSTORE_PASSWORD=password \
    -e ZOWE_EXPLORER_HOST=my-gateway-domain.com \
    -e GATEWAY_PORT=7554 \
    zowe-docker-release.jfrog.io/ompzowe/files-api:latest
```
