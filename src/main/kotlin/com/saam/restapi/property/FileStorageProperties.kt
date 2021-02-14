package com.saam.restapi.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "file")
class FileStorageProperties {

    var uploadDir: String= ""
}