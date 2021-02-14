package com.saam.restapi

import com.saam.restapi.property.FileStorageProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties::class)
class RestapiApplication

fun main(args: Array<String>) {
	runApplication<RestapiApplication>(*args)
}
