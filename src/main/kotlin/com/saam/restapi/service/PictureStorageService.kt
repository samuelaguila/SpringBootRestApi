package com.saam.restapi.service

import com.saam.restapi.dao.PictureDao
import com.saam.restapi.dao.PictureUriDao
import com.saam.restapi.exceptions.FileStorageException
import com.saam.restapi.exceptions.MyFileNotFoundException
import com.saam.restapi.model.Picture
import com.saam.restapi.model.PictureUri
import com.saam.restapi.property.FileStorageProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Suppress("JoinDeclarationAndAssignment")
@Service
class PictureStorageService
@Autowired constructor(fileStorageProperties: FileStorageProperties) {

    private lateinit var fileStorageLocation: Path

    init {
        fileStorageLocation = Paths.get(fileStorageProperties.uploadDir)
                .toAbsolutePath().normalize()

        try {
            Files.createDirectories(fileStorageLocation)
        } catch (ex: Exception) {
            throw FileStorageException("Could not create the directory where the uploaded files will be stored.", ex)
        }
    }

    @Autowired
    private lateinit var pictureDao: PictureDao

    @Autowired
    private lateinit var pictureUriDao: PictureUriDao

    @Throws(FileStorageException::class)
    fun storePicture(picture: Picture): Picture {
        return try {
            pictureDao.save(picture)
        } catch (ex: IOException) {
            throw FileStorageException("Could not store file ${picture.pictureName} Please try again!", ex)
        }
    }

    @Throws(FileStorageException::class)
    fun storePictureUri(pictureUri: PictureUri) {
        try {
            pictureUriDao.save(pictureUri)
        } catch (ex: IOException) {
            throw FileStorageException("Could not store file ${pictureUri.pictureName} Please try again!", ex)
        }
    }

    fun storeFile(file: MultipartFile): String {

        // Normalize file name
        val fileName: String = StringUtils.cleanPath(file.originalFilename!!)

        return try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw FileStorageException("Sorry! Filename contains invalid path sequence $fileName")
            }

            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation: Path = this.fileStorageLocation.resolve(fileName)

            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            fileName

        } catch (ex: IOException) {
            throw FileStorageException("Could not store file $fileName. Please try again!", ex)
        }
    }

    @Throws(RuntimeException::class)
    fun getFile(fileId: String): Picture {
        return pictureDao.findById(fileId.toInt())
                .orElseThrow { MyFileNotFoundException("File not found with id $fileId") }
    }

    fun loadFileAsResource(fileName: String?): Resource {
        return try {

            val filePath = fileStorageLocation.resolve(fileName).normalize()

            val resource: Resource = UrlResource(filePath.toUri())

            if (resource.exists()) {
                resource
            } else {
                throw MyFileNotFoundException("File not found $fileName")
            }

        } catch (ex: MalformedURLException) {
            throw MyFileNotFoundException("File not found $fileName", ex)
        }
    }

    @Throws(RuntimeException::class)
    fun getAllFiles(): List<PictureUri> {
        return try {
            pictureUriDao.findAll()
        } catch (exception: MyFileNotFoundException) {
            throw MyFileNotFoundException("Files not found")
        }
    }

//    @Throws(FileStorageException::class)
//    fun updateFile(picture: Picture) {
//        try {
//            pictureDao.save(picture)
//        }catch (exception: IOException){
//            throw FileStorageException("Files not found")
//        }
//    }

}