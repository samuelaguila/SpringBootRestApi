package com.saam.restapi.payload

data class UploadFileResponse(
        var fileName: String?,
        var fileDownloadUri: String,
        var fileType: String?)