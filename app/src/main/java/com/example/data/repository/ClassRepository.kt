package com.example.data.repository

import com.example.data.model.SchoolClass
import com.example.data.model.Subject
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface abstracting class and subject data access for ViewModels.
 */
interface ClassRepository {
    val classes: StateFlow<List<SchoolClass>>
    val subjects: StateFlow<List<Subject>>

    fun getClassById(id: String): SchoolClass?

    fun getClassName(classId: String): String

    fun getSubjectsForClass(classId: String): List<Subject>

    suspend fun addSubject(subject: Subject)

    suspend fun updateSubject(subject: Subject)

    suspend fun deleteSubject(subjectId: String)
}
