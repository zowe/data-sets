# Add static definition for files-api
cat <<EOF >${STATIC_DEF_CONFIG_DIR}/files-api.ebcidic.yml
#
services:
  - serviceId: datasets
    title: IBM z/OS Datasets
    description: IBM z/OS Datasets REST API service
    catalogUiTileId: datasetsAndUnixFiles
    instanceBaseUrls:
      - https://${ZOWE_EXPLORER_HOST}:${FILES_API_PORT}/
    homePageRelativeUrl:  # Home page is at the same URL
    routedServices:
      - gatewayUrl: api/v1  # [api/ui/ws]/v{majorVersion}
        serviceRelativeUrl: api/v1/datasets
    apiInfo:
      - apiId: org.zowe.data.sets
        gatewayUrl: api/v1
        version: 1.0.0
        documentationUrl: https://${ZOWE_EXPLORER_HOST}:${FILES_API_PORT}/swagger-ui.html
  - serviceId: unixfiles
    title: IBM z/OS Unix Files
    description: IBM z/OS Unix Files REST API service
    catalogUiTileId: datasetsAndUnixFiles
    instanceBaseUrls:
      - https://${ZOWE_EXPLORER_HOST}:${FILES_API_PORT}/
    homePageRelativeUrl:  # Home page is at the same URL
    routedServices:
      - gatewayUrl: api/v1  # [api/ui/ws]/v{majorVersion}
        serviceRelativeUrl: api/v1/unixfiles 
    apiInfo:
      - apiId: org.zowe.unix.files
        gatewayUrl: api/v1
        version: 1.0.0
        documentationUrl: https://${ZOWE_EXPLORER_HOST}:${FILES_API_PORT}/swagger-ui.html
catalogUiTiles:
  datasetsAndUnixFiles:
    title: z/OS Datasets and Unix Files services
    description: IBM z/OS Datasets and Unix Files REST services
EOF
iconv -f IBM-1047 -t IBM-850 ${STATIC_DEF_CONFIG_DIR}/files-api.ebcidic.yml > $STATIC_DEF_CONFIG_DIR/files-api.yml
rm ${STATIC_DEF_CONFIG_DIR}/files-api.ebcidic.yml
chmod 755 $STATIC_DEF_CONFIG_DIR/files-api.yml