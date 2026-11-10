package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ExamResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamResultDao {

    @Query("SELECT * FROM exam_results")
    fun getAllExamResults(): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND examCategory = :category AND year = :year")
    fun getResultsForStudent(studentId: String, category: String, year: Int): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND examCategory = :category AND year = :year")
    suspend fun getResultsForStudentSync(studentId: String, category: String, year: Int): List<ExamResultEntity>

    @Query("SELECT * FROM exam_results WHERE classId = :classId AND examCategory = :category AND year = :year")
    fun getResultsForClass(classId: String, category: String, year: Int): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResult(result: ExamResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResults(results: List<ExamResultEntity>)

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId AND subjectId = :subjectId AND examCategory = :category AND year = :year LIMIT 1")
    suspend fun findResult(studentId: String, subjectId: String, category: String, year: Int): ExamResultEntity?

    @Update
    suspend fun updateExamResult(result: ExamResultEntity)

    @Query("DELETE FROM exam_results WHERE id = :id")
    suspend fun deleteExamResultById(id: String)

    @Query("DELETE FROM exam_results WHERE studentId = :studentId")
    suspend fun deleteResultsByStudentId(studentId: String)

    @Query("DELETE FROM exam_results WHERE studentId IN (:studentIds)")
    suspend fun deleteResultsByStudentIds(studentIds: List<String>)

    @Query("DELETE FROM exam_results")
    suspend fun deleteAllExamResults()
}
