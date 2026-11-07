package com.example.data.model

enum class SubjectType(val labelBn: String, val labelEn: String) {
    MANDATORY("আবশ্যিক", "Mandatory"),
    OPTIONAL("ঐচ্ছিক", "Optional")
}

enum class ExamCategory(val labelBn: String, val labelEn: String) {
    FIRST_TERM("প্রথম সাময়িক", "1st Term"),
    SECOND_TERM("দ্বিতীয় সাময়িক", "2nd Term"),
    ANNUAL("বার্ষিক পরীক্ষা", "Annual Exam")
}

data class SchoolClass(
    val id: String,
    val name: String, // e.g. "Nursery", "KG", "Class 1" ... "Class 10"
    val section: String = "A",
    val classTeacher: String = "Designated Teacher",
    val order: Int = 0
)

data class Subject(
    val id: String,
    val classId: String,
    val name: String,
    val code: String,
    val fullMarks: Double = 100.0, // 100 or 50
    val passMarks: Double = 33.0,  // 33 for 100, 17 for 50
    val type: SubjectType = SubjectType.MANDATORY
)

data class Student(
    val id: String,
    val uniqueId: String, // e.g. "OAK-2026-001"
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
)

data class ExamRoutineSlot(
    val id: String,
    val classId: String,
    val className: String,
    val examCategory: ExamCategory,
    val year: Int,
    val subjectName: String,
    val examDate: String,
    val examDay: String,
    val examTime: String,
    val room: String = "Hall A"
)

data class StudentMark(
    val id: String,
    val uniqueId: String,
    val studentId: String,
    val studentName: String,
    val fatherName: String,
    val classId: String,
    val className: String,
    val rollNo: String,
    val examCategory: ExamCategory,
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
    val isPassed: Boolean get() = marksObtained >= passMarks
}

data class StudentMarksheet(
    val student: Student,
    val examCategory: ExamCategory,
    val year: Int,
    val subjectMarks: List<StudentMark>,
    val totalMarks: Double,
    val totalFullMarks: Double,
    val gpa: Double,
    val letterGrade: String,
    val isPassed: Boolean,
    val failedMandatorySubjects: List<String>
)

data class ClassMeritRank(
    val position: Int,
    val student: Student,
    val totalMarks: Double,
    val gpa: Double,
    val isPassed: Boolean,
    val statusText: String
)
