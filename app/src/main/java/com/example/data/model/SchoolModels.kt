package com.example.data.model

enum class AttendanceStatus(val label: String) {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late"),
    EXCUSED("Excused")
}

enum class NoticePriority(val label: String) {
    HIGH("Urgent"),
    NORMAL("Normal"),
    LOW("Info")
}

enum class FeeStatus(val label: String) {
    PAID("Paid"),
    PARTIAL("Partial"),
    PENDING("Pending"),
    OVERDUE("Overdue")
}

data class Student(
    val id: String,
    val rollNo: String,
    val firstName: String,
    val lastName: String,
    val grade: String,
    val section: String,
    val gender: String,
    val email: String,
    val phone: String,
    val parentName: String,
    val parentPhone: String,
    val parentEmail: String,
    val bloodGroup: String = "O+",
    val address: String = "124 Academy Way",
    val attendancePercentage: Int = 94,
    val feeStatus: FeeStatus = FeeStatus.PAID,
    val avatarColorHex: Long = 0xFF1E3A8A
) {
    val fullName: String get() = "$firstName $lastName"
    val classIdentifier: String get() = "$grade-$section"
}

data class Teacher(
    val id: String,
    val employeeId: String,
    val name: String,
    val department: String,
    val subjects: List<String>,
    val assignedClass: String,
    val email: String,
    val phone: String,
    val qualification: String,
    val experienceYears: Int,
    val avatarColorHex: Long = 0xFF0D9488
)

data class ClassScheduleItem(
    val dayOfWeek: String,
    val period: Int,
    val timeRange: String,
    val subject: String,
    val teacherName: String,
    val room: String
)

data class SchoolClass(
    val id: String,
    val name: String,
    val grade: String,
    val section: String,
    val roomNumber: String,
    val classTeacherId: String,
    val classTeacherName: String,
    val studentCount: Int,
    val capacity: Int = 35,
    val schedule: List<ClassScheduleItem> = emptyList()
)

data class AttendanceRecord(
    val id: String,
    val date: String,
    val classId: String,
    val studentId: String,
    val status: AttendanceStatus,
    val note: String = ""
)

data class GradeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val grade: String,
    val subject: String,
    val examName: String,
    val marksObtained: Double,
    val maxMarks: Double,
    val gradeLetter: String,
    val date: String,
    val remarks: String = ""
) {
    val percentage: Double get() = if (maxMarks > 0) (marksObtained / maxMarks) * 100 else 0.0
}

data class Notice(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val priority: NoticePriority,
    val category: String,
    val targetAudience: String,
    val author: String = "Administration"
)

data class FeeRecord(
    val id: String,
    val studentId: String,
    val studentName: String,
    val grade: String,
    val term: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val status: FeeStatus,
    val dueDate: String
) {
    val remainingAmount: Double get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}
