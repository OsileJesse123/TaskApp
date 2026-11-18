package com.jesse.taskapp.core.data.local.converter

import androidx.room.TypeConverter
import com.jesse.taskapp.core.domain.model.PendingOperationStatus
import com.jesse.taskapp.core.domain.model.PendingOperationType

class PendingOperationConverters {

    @TypeConverter
    fun fromOperationType(type: PendingOperationType): String =
        type.name

    @TypeConverter
    fun toOperationType(value: String): PendingOperationType =
        PendingOperationType.valueOf(value)

    @TypeConverter
    fun fromOperationStatus(status: PendingOperationStatus): String =
        status.name

    @TypeConverter
    fun toOperationStatus(value: String): PendingOperationStatus =
        PendingOperationStatus.valueOf(value)
}