package com.saam.restapi.controller

import com.saam.restapi.model.PictureUri
import com.saam.restapi.payload.UploadFileResponse
import com.saam.restapi.service.PictureStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
class FileController {

    companion object {
        private val logger = LoggerFactory.getLogger(FileController::class.java)
    }

    @Autowired
    private lateinit var storageService: PictureStorageService

    @GetMapping("/list")
    @Throws(IOException::class)
    fun listAll(): Map<String, List<PictureUri>> {
        val response: MutableMap<String, List<PictureUri>> = HashMap<String, List<PictureUri>>()
        response["casas"] = storageService.getAllFiles()
        return response
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    fun downloadFile(
            @PathVariable fileName: String?,
            request: HttpServletRequest
    ): ResponseEntity<Resource?>? {

    // Load file as Resource
    val resource: Resource = storageService.loadFileAsResource(fileName)

    // Try to determine file's content type
    var contentType: String? = null
    try {
        contentType = request.servletContext.getMimeType(resource.file.absolutePath)
    } catch (ex: IOException) {
        logger.info("Could not determine file type.")
    }

    // Fallback to the default content type if type could not be determined
    if (contentType == null) {
        contentType = "application/octet-stream"
    }

    return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.filename + "\"")
            .body(resource)
    }

    @PostMapping("/uploadFile")
    fun uploadFile(
            @RequestParam("file") file: MultipartFile
    ): UploadFileResponse? {

    val fileName: String = storageService.storeFile(file)

    val fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/downloadFile/")
            .path(fileName)
            .toUriString()

    val pictureUri = PictureUri(
            pictureName = fileName,
            pictureType = file.contentType!!,
            pictureUri = fileDownloadUri
    )

    storageService.storePictureUri(pictureUri)

    return UploadFileResponse(fileName, fileDownloadUri,
            file.contentType)
    }
}