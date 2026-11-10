package com.example.data.repository

import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository interface abstracting student data access for ViewModels.
 */
interface StudentRepository {
    val students: StateFlow<List<Student>>

    fun searchStudents(query: String, classId: String?): Flow<List<Student>>

    suspend fun getStudentById(id: String): Student?

    suspend fun getStudentByUniqueId(uniqueId: String): Student?

    suspend fun addStudent(student: Student)

    suspend fun updateStudent(student: Student)

    suspend fun updateStudentById(student: Student)

    suspend fun deleteStudent(id: String)

    suspend fun deleteStudents(ids: List<String>)

    suspend fun bulkImportStudents(students: List<Student>)
}
