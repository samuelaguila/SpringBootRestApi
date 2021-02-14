package com.saam.restapi.controller

import com.saam.restapi.exceptions.FileStorageException
import com.saam.restapi.model.Picture
import com.saam.restapi.model.PictureUri
import com.saam.restapi.payload.UploadFileResponse
import com.saam.restapi.service.PictureStorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.util.StringUtils
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

//    @GetMapping("/downloadFile/{fileId}")
//    fun downloadFile(@PathVariable fileId: String): ResponseEntity<ByteArrayResource> {
//
//        val savedPicture = storageService.getFile(fileId)

//  Snippet #1
//  return ResponseEntity.ok()
//  .contentType(MediaType.parseMediaType(savedPicture.pictureType))
//  .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + savedPicture.pictureName + "\""
//  )
// .body(ByteArrayResource(savedPicture.data))

//    Snippet #2
//    @GetMapping(value = ["/downloadFile/{fileId}"], produces = [MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE])
//    @ResponseBody
//    @Throws(IOException::class)
//    fun getImageWithMediaType(@PathVariable(name = "imageName") fileName: String?): ByteArray? {
//        return storageService.getImageWithMediaType(fileName)
//    }

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

    @PostMapping("/uploadFile1")
    fun uploadFile1(
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

    @PostMapping("/uploadFile")
    fun uploadFile(
            @RequestParam("file") file: MultipartFile
    ): ResponseEntity<UploadFileResponse> {

        // Normalize file name
        val fileName = StringUtils.cleanPath(file.originalFilename!!)

        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            throw FileStorageException("Sorry! Filename contains invalid path sequence $fileName")
        }

        val picture = Picture(
                pictureName = fileName,
                pictureType = file.contentType!!,
                data = file.bytes
        )

        val savedPicture = storageService.storePicture(picture)

        val fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(savedPicture.id.toString())
                .toUriString()

        val pictureUri = PictureUri(
                pictureName = fileName,
                pictureType = file.contentType!!,
                pictureUri = fileDownloadUri
        )

        storageService.storePictureUri(pictureUri)

        return ResponseEntity(UploadFileResponse(
                file.name,
                fileDownloadUri,
                file.contentType),
                HttpStatus.OK)
    }

//    @Throws(FileStorageException::class)
//    @PutMapping("/update")
//    fun updateFile(picture: Picture) {
//
//        try {
//            storageService.updateFile(picture)
//
//        } catch (ex: IOException) {
//            throw FileStorageException("Could not store file ${picture.pictureName} Please try again!", ex)
//        }
//    }
}