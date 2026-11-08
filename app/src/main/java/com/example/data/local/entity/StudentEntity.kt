package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Student

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["uniqueId"], unique = true),
        Index(value = ["classId"]),
        Index(value = ["rollNo"])
    ]
)
data class StudentEntity(
    @PrimaryKey
    val id: String,
    val uniqueId: String,
    val rollNo: String,
    val name: String,
    val classId: String,
    val className: String,
    val birthRegNo: String,
    val fatherName: String,
    val motherName: String,
    val guardianPhone: String,
    val address: String,
    val avatarColorHex: Long = 0xFF1E3A8A
) {
    fun toDomain(): Student {
        return Student(
            id = id,
            uniqueId = uniqueId,
            rollNo = rollNo,
            name = name,
            classId = classId,
            className = className,
            birthRegNo = birthRegNo,
            fatherName = fatherName,
            motherName = motherName,
            guardianPhone = guardianPhone,
            address = address,
            avatarColorHex = avatarColorHex
        )
    }

    companion object {
        fun fromDomain(student: Student): StudentEntity {
            return StudentEntity(
                id = student.id,
                uniqueId = student.uniqueId,
                rollNo = student.rollNo,
                name = student.name,
                classId = student.classId,
                className = student.className,
                birthRegNo = student.birthRegNo,
                fatherName = student.fatherName,
                motherName = student.motherName,
                guardianPhone = student.guardianPhone,
                address = student.address,
                avatarColorHex = student.avatarColorHex
            )
        }
    }
}
