package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val aspectRatio: String,
    val canvasBg: String,
    val clipsJson: String,
    val audioTracksJson: String,
    val overlaysJson: String,
    val thumbnailUri: String? = null
)
