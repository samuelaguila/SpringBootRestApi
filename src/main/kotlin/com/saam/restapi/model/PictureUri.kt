package com.saam.restapi.model

import javax.persistence.*

@Entity
@Table(name = "pictureUri")
data class PictureUri(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,

    @Column(name = "picture_name")
    var pictureName: String,

    @Column(name = "profile_picture")
    val pictureType: String,

    @Column(name = "profile_Uri")
    var pictureUri: String? = null,
)
