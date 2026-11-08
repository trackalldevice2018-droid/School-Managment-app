package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ExamCategory
import com.example.data.model.StudentMark

@Entity(
    tableName = "exam_results",
    indices = [
        Index(value = ["studentId"]),
        Index(value = ["uniqueId"]),
        Index(value = ["classId"]),
        Index(value = ["examCategory", "year"])
    ]
)
data class ExamResultEntity(
    @PrimaryKey
    val id: String,
    val uniqueId: String,
    val studentId: String,
    val studentName: String,
    val fatherName: String,
    val classId: String,
    val className: String,
    val rollNo: String,
    val examCategory: String, // "FIRST_TERM", "SECOND_TERM", "ANNUAL"
    val year: Int,
    val subjectId: String,
    val subjectName: String,
    val marksObtained: Double,
    val fullMarks: Double,
    val passMarks: Double,
    val isMandatory: Boolean,
    val letterGrade: String,
    val gradePoint: Double
) {
    fun toDomain(): StudentMark {
        val category = try {
            ExamCategory.valueOf(examCategory)
        } catch (_: Exception) {
            ExamCategory.ANNUAL
        }
        return StudentMark(
            id = id,
            uniqueId = uniqueId,
            studentId = studentId,
            studentName = studentName,
            fatherName = fatherName,
            classId = classId,
            className = className,
            rollNo = rollNo,
            examCategory = category,
            year = year,
            subjectId = subjectId,
            subjectName = subjectName,
            marksObtained = marksObtained,
            fullMarks = fullMarks,
            passMarks = passMarks,
            isMandatory = isMandatory,
            letterGrade = letterGrade,
            gradePoint = gradePoint
        )
    }

    companion object {
        fun fromDomain(mark: StudentMark): ExamResultEntity {
            return ExamResultEntity(
                id = mark.id,
                uniqueId = mark.uniqueId,
                studentId = mark.studentId,
                studentName = mark.studentName,
                fatherName = mark.fatherName,
                classId = mark.classId,
                className = mark.className,
                rollNo = mark.rollNo,
                examCategory = mark.examCategory.name,
                year = mark.year,
                subjectId = mark.subjectId,
                subjectName = mark.subjectName,
                marksObtained = mark.marksObtained,
                fullMarks = mark.fullMarks,
                passMarks = mark.passMarks,
                isMandatory = mark.isMandatory,
                letterGrade = mark.letterGrade,
                gradePoint = mark.gradePoint
            )
        }
    }
}
