package com.behavioralfirewall.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_state")
data class UserStateEntity(
    @PrimaryKey
    val id: String,
    val stressLevel: Int,
    val boredomLevel: Int,
    val fatigueLevel: Int,
    val isolationLevel: Int,
    val updatedAt: Long
)
