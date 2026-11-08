package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Subject
import com.example.data.model.SubjectType

@Entity(
    tableName = "subjects",
    indices = [
        Index(value = ["classId"]),
        Index(value = ["code"])
    ]
)
data class SubjectEntity(
    @PrimaryKey
    val id: String,
    val classId: String,
    val name: String,
    val code: String,
    val fullMarks: Double = 100.0,
    val passMarks: Double = 33.0,
    val type: String = "MANDATORY" // "MANDATORY" or "OPTIONAL"
) {
    fun toDomain(): Subject {
        val subjectType = try {
            SubjectType.valueOf(type)
        } catch (_: Exception) {
            SubjectType.MANDATORY
        }
        return Subject(
            id = id,
            classId = classId,
            name = name,
            code = code,
            fullMarks = fullMarks,
            passMarks = passMarks,
            type = subjectType
        )
    }

    companion object {
        fun fromDomain(subject: Subject): SubjectEntity {
            return SubjectEntity(
                id = subject.id,
                classId = subject.classId,
                name = subject.name,
                code = subject.code,
                fullMarks = subject.fullMarks,
                passMarks = subject.passMarks,
                type = subject.type.name
            )
        }
    }
}
