package com.saam.restapi.dao

import com.saam.restapi.model.Picture
import org.springframework.data.jpa.repository.JpaRepository

interface PictureDao: JpaRepository<Picture, Int>

