package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Query("SELECT * FROM students ORDER BY rollNo ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE uniqueId = :uniqueId LIMIT 1")
    suspend fun findStudentByUniqueId(uniqueId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE uniqueId = :uniqueId LIMIT 1")
    fun getStudentByUniqueId(uniqueId: String): Flow<StudentEntity?>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY rollNo ASC")
    fun getStudentsByClass(classId: String): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: String)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()
}
