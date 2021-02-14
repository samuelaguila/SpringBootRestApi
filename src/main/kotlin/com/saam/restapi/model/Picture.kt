package com.saam.restapi.model

import javax.persistence.*

@Entity
@Table(name = "picture")
data class Picture(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Int? = null,

    @Column(name = "picture_name")
    var pictureName: String,

    @Column(name = "profile_picture")
    val pictureType: String,

    @Lob
    var data: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Picture

        if (id != other.id) return false
        if (pictureName != other.pictureName) return false
        if (pictureType != other.pictureType) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + pictureName.hashCode()
        result = 31 * result + pictureType.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }


}



