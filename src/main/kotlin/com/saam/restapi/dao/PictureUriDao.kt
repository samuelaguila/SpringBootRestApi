package com.saam.restapi.dao

import com.saam.restapi.model.PictureUri
import org.springframework.data.jpa.repository.JpaRepository

interface PictureUriDao: JpaRepository<PictureUri, Int> {
}