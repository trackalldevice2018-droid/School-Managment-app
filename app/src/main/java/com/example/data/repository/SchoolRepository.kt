package com.example.data.repository

import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassScheduleItem
import com.example.data.model.FeeRecord
import com.example.data.model.FeeStatus
import com.example.data.model.GradeRecord
import com.example.data.model.Notice
import com.example.data.model.NoticePriority
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Teacher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SchoolRepository private constructor() {

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _students = MutableStateFlow<List<Student>>(initialStudents())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _teachers = MutableStateFlow<List<Teacher>>(initialTeachers())
    val teachers: StateFlow<List<Teacher>> = _teachers.asStateFlow()

    private val _classes = MutableStateFlow<List<SchoolClass>>(initialClasses())
    val classes: StateFlow<List<SchoolClass>> = _classes.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(initialAttendance())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _grades = MutableStateFlow<List<GradeRecord>>(initialGrades())
    val grades: StateFlow<List<GradeRecord>> = _grades.asStateFlow()

    private val _notices = MutableStateFlow<List<Notice>>(initialNotices())
    val notices: StateFlow<List<Notice>> = _notices.asStateFlow()

    private val _fees = MutableStateFlow<List<FeeRecord>>(initialFees())
    val fees: StateFlow<List<FeeRecord>> = _fees.asStateFlow()

    fun addStudent(student: Student) {
        _students.value = listOf(student) + _students.value
        // Update class student count
        updateClassCounts()
    }

    fun updateStudent(student: Student) {
        _students.value = _students.value.map { if (it.id == student.id) student else it }
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filterNot { it.id == studentId }
        _attendance.value = _attendance.value.filterNot { it.studentId == studentId }
        _grades.value = _grades.value.filterNot { it.studentId == studentId }
        _fees.value = _fees.value.filterNot { it.studentId == studentId }
        updateClassCounts()
    }

    fun addTeacher(teacher: Teacher) {
        _teachers.value = listOf(teacher) + _teachers.value
    }

    fun updateTeacher(teacher: Teacher) {
        _teachers.value = _teachers.value.map { if (it.id == teacher.id) teacher else it }
    }

    fun deleteTeacher(teacherId: String) {
        _teachers.value = _teachers.value.filterNot { it.id == teacherId }
    }

    fun addClass(newClass: SchoolClass) {
        _classes.value = _classes.value + newClass
    }

    fun saveAttendance(date: String, classId: String, statusMap: Map<String, AttendanceStatus>) {
        val existingFiltered = _attendance.value.filterNot { it.date == date && it.classId == classId }
        val newRecords = statusMap.map { (studentId, status) ->
            AttendanceRecord(
                id = UUID.randomUUID().toString(),
                date = date,
                classId = classId,
                studentId = studentId,
                status = status
            )
        }
        _attendance.value = existingFiltered + newRecords

        // Update student attendance percentage dynamically
        recalculateAttendancePercentages()
    }

    fun addGrade(record: GradeRecord) {
        _grades.value = listOf(record) + _grades.value
    }

    fun deleteGrade(gradeId: String) {
        _grades.value = _grades.value.filterNot { it.id == gradeId }
    }

    fun addNotice(notice: Notice) {
        _notices.value = listOf(notice) + _notices.value
    }

    fun deleteNotice(noticeId: String) {
        _notices.value = _notices.value.filterNot { it.id == noticeId }
    }

    fun updateFeeStatus(feeId: String, status: FeeStatus, paidAmount: Double) {
        _fees.value = _fees.value.map { fee ->
            if (fee.id == feeId) {
                fee.copy(status = status, paidAmount = paidAmount)
            } else fee
        }
    }

    private fun updateClassCounts() {
        _classes.value = _classes.value.map { cls ->
            val count = _students.value.count { it.grade == cls.grade && it.section == cls.section }
            cls.copy(studentCount = count)
        }
    }

    private fun recalculateAttendancePercentages() {
        val allRecords = _attendance.value
        _students.value = _students.value.map { student ->
            val studentRecords = allRecords.filter { it.studentId == student.id }
            if (studentRecords.isNotEmpty()) {
                val presentCount = studentRecords.count { it.status == AttendanceStatus.PRESENT || it.status == AttendanceStatus.LATE }
                val pct = (presentCount * 100) / studentRecords.size
                student.copy(attendancePercentage = pct)
            } else {
                student
            }
        }
    }

    companion object {
        @Volatile
        private var instance: SchoolRepository? = null

        fun getInstance(): SchoolRepository {
            return instance ?: synchronized(this) {
                instance ?: SchoolRepository().also { instance = it }
            }
        }

        private fun initialStudents(): List<Student> {
            return listOf(
                Student(
                    id = "std_1",
                    rollNo = "101",
                    firstName = "Liam",
                    lastName = "Harper",
                    grade = "Grade 10",
                    section = "A",
                    gender = "Male",
                    email = "liam.harper@student.oakridge.edu",
                    phone = "+1 (555) 234-5678",
                    parentName = "Robert Harper",
                    parentPhone = "+1 (555) 345-6789",
                    parentEmail = "r.harper@gmail.com",
                    bloodGroup = "O+",
                    attendancePercentage = 96,
                    feeStatus = FeeStatus.PAID,
                    avatarColorHex = 0xFF1E3A8A
                ),
                Student(
                    id = "std_2",
                    rollNo = "102",
                    firstName = "Sophia",
                    lastName = "Chen",
                    grade = "Grade 10",
                    section = "A",
                    gender = "Female",
                    email = "sophia.chen@student.oakridge.edu",
                    phone = "+1 (555) 234-5679",
                    parentName = "Mei Chen",
                    parentPhone = "+1 (555) 345-6790",
                    parentEmail = "mei.chen@gmail.com",
                    bloodGroup = "A+",
                    attendancePercentage = 98,
                    feeStatus = FeeStatus.PAID,
                    avatarColorHex = 0xFF0D9488
                ),
                Student(
                    id = "std_3",
                    rollNo = "103",
                    firstName = "Marcus",
                    lastName = "Johnson",
                    grade = "Grade 10",
                    section = "A",
                    gender = "Male",
                    email = "marcus.j@student.oakridge.edu",
                    phone = "+1 (555) 234-5680",
                    parentName = "Tanya Johnson",
                    parentPhone = "+1 (555) 345-6791",
                    parentEmail = "tanya.j@gmail.com",
                    bloodGroup = "B+",
                    attendancePercentage = 91,
                    feeStatus = FeeStatus.PENDING,
                    avatarColorHex = 0xFF7C3AED
                ),
                Student(
                    id = "std_4",
                    rollNo = "104",
                    firstName = "Emma",
                    lastName = "Davis",
                    grade = "Grade 10",
                    section = "B",
                    gender = "Female",
                    email = "emma.davis@student.oakridge.edu",
                    phone = "+1 (555) 234-5681",
                    parentName = "William Davis",
                    parentPhone = "+1 (555) 345-6792",
                    parentEmail = "wdavis@gmail.com",
                    bloodGroup = "AB+",
                    attendancePercentage = 94,
                    feeStatus = FeeStatus.PAID,
                    avatarColorHex = 0xFFDB2777
                ),
                Student(
                    id = "std_5",
                    rollNo = "105",
                    firstName = "Noah",
                    lastName = "Miller",
                    grade = "Grade 10",
                    section = "B",
                    gender = "Male",
                    email = "noah.miller@student.oakridge.edu",
                    phone = "+1 (555) 234-5682",
                    parentName = "Sarah Miller",
                    parentPhone = "+1 (555) 345-6793",
                    parentEmail = "smiller@gmail.com",
                    bloodGroup = "O-",
                    attendancePercentage = 88,
                    feeStatus = FeeStatus.OVERDUE,
                    avatarColorHex = 0xFFD97706
                ),
                Student(
                    id = "std_6",
                    rollNo = "201",
                    firstName = "Olivia",
                    lastName = "Rodriguez",
                    grade = "Grade 9",
                    section = "A",
                    gender = "Female",
                    email = "olivia.r@student.oakridge.edu",
                    phone = "+1 (555) 234-5683",
                    parentName = "Carlos Rodriguez",
                    parentPhone = "+1 (555) 345-6794",
                    parentEmail = "crodriguez@gmail.com",
                    bloodGroup = "A-",
                    attendancePercentage = 95,
                    feeStatus = FeeStatus.PAID,
                    avatarColorHex = 0xFF2563EB
                ),
                Student(
                    id = "std_7",
                    rollNo = "202",
                    firstName = "Ethan",
                    lastName = "Taylor",
                    grade = "Grade 9",
                    section = "A",
                    gender = "Male",
                    email = "ethan.taylor@student.oakridge.edu",
                    phone = "+1 (555) 234-5684",
                    parentName = "Laura Taylor",
                    parentPhone = "+1 (555) 345-6795",
                    parentEmail = "laura.t@gmail.com",
                    bloodGroup = "B-",
                    attendancePercentage = 92,
                    feeStatus = FeeStatus.PARTIAL,
                    avatarColorHex = 0xFF059669
                ),
                Student(
                    id = "std_8",
                    rollNo = "301",
                    firstName = "Aria",
                    lastName = "Patel",
                    grade = "Grade 11",
                    section = "A",
                    gender = "Female",
                    email = "aria.patel@student.oakridge.edu",
                    phone = "+1 (555) 234-5685",
                    parentName = "Raj Patel",
                    parentPhone = "+1 (555) 345-6796",
                    parentEmail = "raj.patel@gmail.com",
                    bloodGroup = "O+",
                    attendancePercentage = 99,
                    feeStatus = FeeStatus.PAID,
                    avatarColorHex = 0xFF4F46E5
                ),
                Student(
                    id = "std_9",
                    rollNo = "302",
                    firstName = "Lucas",
                    lastName = "Wilson",
                    grade = "Grade 11",
                    section = "A",
                    gender = "Male",
                    email = "lucas.w@student.oakridge.edu",
                    phone = "+1 (555) 234-5686",
                    parentName = "David Wilson",
                    parentPhone = "+1 (555) 345-6797",
                    parentEmail = "dwilson@gmail.com",
                    bloodGroup = "A+",
                    attendancePercentage = 90,
                    feeStatus = FeeStatus.PAID,
                    avatarColorHex = 0xFF6366F1
                ),
                Student(
                    id = "std_10",
                    rollNo = "401",
                    firstName = "Chloe",
                    lastName = "Anderson",
                    grade = "Grade 12",
                    section = "A",
                    gender = "Female",
                    email = "chloe.a@student.oakridge.edu",
                    phone = "+1 (555) 234-5687",
                    parentName = "Elena Anderson",
                    parentPhone = "+1 (555) 345-6798",
                    parentEmail = "eanderson@gmail.com",
                    bloodGroup = "AB-",
                    attendancePercentage = 97,
                    feeStatus = FeeStatus.PAID,
                    avatarColorHex = 0xFF9333EA
                )
            )
        }

        private fun initialTeachers(): List<Teacher> {
            return listOf(
                Teacher(
                    id = "tch_1",
                    employeeId = "EMP-1001",
                    name = "Dr. Eleanor Vance",
                    department = "Mathematics",
                    subjects = listOf("Algebra II", "Calculus", "Geometry"),
                    assignedClass = "Grade 10-A",
                    email = "eleanor.vance@oakridge.edu",
                    phone = "+1 (555) 789-0101",
                    qualification = "Ph.D. in Applied Mathematics",
                    experienceYears = 12,
                    avatarColorHex = 0xFF1E3A8A
                ),
                Teacher(
                    id = "tch_2",
                    employeeId = "EMP-1002",
                    name = "Prof. Samuel Wright",
                    department = "Science",
                    subjects = listOf("Physics", "Chemistry"),
                    assignedClass = "Grade 11-A",
                    email = "samuel.wright@oakridge.edu",
                    phone = "+1 (555) 789-0102",
                    qualification = "M.Sc. in Physics, B.Ed.",
                    experienceYears = 9,
                    avatarColorHex = 0xFF0D9488
                ),
                Teacher(
                    id = "tch_3",
                    employeeId = "EMP-1003",
                    name = "Ms. Grace Thompson",
                    department = "English Literature",
                    subjects = listOf("World Literature", "Creative Writing"),
                    assignedClass = "Grade 9-A",
                    email = "grace.t@oakridge.edu",
                    phone = "+1 (555) 789-0103",
                    qualification = "M.A. in English Literature",
                    experienceYears = 7,
                    avatarColorHex = 0xFF7C3AED
                ),
                Teacher(
                    id = "tch_4",
                    employeeId = "EMP-1004",
                    name = "Mr. Daniel Martinez",
                    department = "Computer Science",
                    subjects = listOf("AP Computer Science", "Web Development"),
                    assignedClass = "Grade 12-A",
                    email = "daniel.m@oakridge.edu",
                    phone = "+1 (555) 789-0104",
                    qualification = "B.S. in Computer Science",
                    experienceYears = 6,
                    avatarColorHex = 0xFF059669
                ),
                Teacher(
                    id = "tch_5",
                    employeeId = "EMP-1005",
                    name = "Mrs. Abigail Scott",
                    department = "Social Studies",
                    subjects = listOf("Modern World History", "Civics"),
                    assignedClass = "Grade 10-B",
                    email = "abigail.scott@oakridge.edu",
                    phone = "+1 (555) 789-0105",
                    qualification = "M.A. in History",
                    experienceYears = 14,
                    avatarColorHex = 0xFFD97706
                )
            )
        }

        private fun initialClasses(): List<SchoolClass> {
            val sampleSchedule = listOf(
                ClassScheduleItem("Monday", 1, "08:30 - 09:20", "Mathematics", "Dr. Eleanor Vance", "Room 204"),
                ClassScheduleItem("Monday", 2, "09:25 - 10:15", "Physics", "Prof. Samuel Wright", "Lab 2"),
                ClassScheduleItem("Monday", 3, "10:30 - 11:20", "English Literature", "Ms. Grace Thompson", "Room 105"),
                ClassScheduleItem("Monday", 4, "11:25 - 12:15", "Computer Science", "Mr. Daniel Martinez", "Comp Lab 1"),
                ClassScheduleItem("Tuesday", 1, "08:30 - 09:20", "World History", "Mrs. Abigail Scott", "Room 302"),
                ClassScheduleItem("Tuesday", 2, "09:25 - 10:15", "Mathematics", "Dr. Eleanor Vance", "Room 204"),
                ClassScheduleItem("Wednesday", 1, "08:30 - 09:20", "Physics", "Prof. Samuel Wright", "Lab 2"),
                ClassScheduleItem("Wednesday", 2, "09:25 - 10:15", "Physical Education", "Coach Miller", "Gymnasium")
            )

            return listOf(
                SchoolClass("cls_10a", "Grade 10-A", "Grade 10", "A", "Room 204", "tch_1", "Dr. Eleanor Vance", 3, 30, sampleSchedule),
                SchoolClass("cls_10b", "Grade 10-B", "Grade 10", "B", "Room 205", "tch_5", "Mrs. Abigail Scott", 2, 30, sampleSchedule),
                SchoolClass("cls_9a", "Grade 9-A", "Grade 9", "A", "Room 101", "tch_3", "Ms. Grace Thompson", 2, 32, sampleSchedule),
                SchoolClass("cls_11a", "Grade 11-A", "Grade 11", "A", "Room 301", "tch_2", "Prof. Samuel Wright", 2, 28, sampleSchedule),
                SchoolClass("cls_12a", "Grade 12-A", "Grade 12", "A", "Room 401", "tch_4", "Mr. Daniel Martinez", 1, 25, sampleSchedule)
            )
        }

        private fun initialAttendance(): List<AttendanceRecord> {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            return listOf(
                AttendanceRecord("att_1", today, "cls_10a", "std_1", AttendanceStatus.PRESENT),
                AttendanceRecord("att_2", today, "cls_10a", "std_2", AttendanceStatus.PRESENT),
                AttendanceRecord("att_3", today, "cls_10a", "std_3", AttendanceStatus.LATE, "Bus delay"),
                AttendanceRecord("att_4", today, "cls_10b", "std_4", AttendanceStatus.PRESENT),
                AttendanceRecord("att_5", today, "cls_10b", "std_5", AttendanceStatus.ABSENT, "Sick leave"),
                AttendanceRecord("att_6", today, "cls_9a", "std_6", AttendanceStatus.PRESENT),
                AttendanceRecord("att_7", today, "cls_9a", "std_7", AttendanceStatus.PRESENT),
                AttendanceRecord("att_8", today, "cls_11a", "std_8", AttendanceStatus.PRESENT),
                AttendanceRecord("att_9", today, "cls_11a", "std_9", AttendanceStatus.PRESENT),
                AttendanceRecord("att_10", today, "cls_12a", "std_10", AttendanceStatus.PRESENT)
            )
        }

        private fun initialGrades(): List<GradeRecord> {
            return listOf(
                GradeRecord("grd_1", "std_1", "Liam Harper", "Grade 10", "Mathematics", "Mid-Term Exam", 94.0, 100.0, "A", "2026-10-15", "Excellent analytical thinking"),
                GradeRecord("grd_2", "std_1", "Liam Harper", "Grade 10", "Physics", "Mid-Term Exam", 88.0, 100.0, "B+", "2026-10-18", "Good laboratory performance"),
                GradeRecord("grd_3", "std_2", "Sophia Chen", "Grade 10", "Mathematics", "Mid-Term Exam", 98.0, 100.0, "A+", "2026-10-15", "Outstanding score"),
                GradeRecord("grd_4", "std_2", "Sophia Chen", "Grade 10", "English Literature", "Mid-Term Exam", 95.0, 100.0, "A", "2026-10-20", "Insightful essay"),
                GradeRecord("grd_5", "std_3", "Marcus Johnson", "Grade 10", "Mathematics", "Mid-Term Exam", 78.0, 100.0, "C+", "2026-10-15", "Needs algebra practice"),
                GradeRecord("grd_6", "std_4", "Emma Davis", "Grade 10", "World History", "Mid-Term Exam", 91.0, 100.0, "A-", "2026-10-19", "Very good presentation"),
                GradeRecord("grd_7", "std_8", "Aria Patel", "Grade 11", "Chemistry", "Mid-Term Exam", 99.0, 100.0, "A+", "2026-10-16", "Top of the class")
            )
        }

        private fun initialNotices(): List<Notice> {
            return listOf(
                Notice(
                    id = "ntc_1",
                    title = "Annual STEM & Science Fair 2026",
                    content = "The annual Science Fair will take place on November 20 in the Grand Auditorium. All Grade 9-12 students are invited to submit project abstracts by Friday.",
                    date = "Nov 03, 2026",
                    priority = NoticePriority.HIGH,
                    category = "Events",
                    targetAudience = "All Students & Faculty",
                    author = "Science Department"
                ),
                Notice(
                    id = "ntc_2",
                    title = "Parent-Teacher Conference Q3",
                    content = "Q3 Conferences will be held next Tuesday from 2:00 PM to 6:00 PM. Parents can book 15-minute slots via the student portal or by contacting the homeroom teacher.",
                    date = "Nov 02, 2026",
                    priority = NoticePriority.NORMAL,
                    category = "Academic",
                    targetAudience = "Parents & Teachers",
                    author = "Principal's Office"
                ),
                Notice(
                    id = "ntc_3",
                    title = "Inter-School Basketball Finals",
                    content = "Congratulations to our Oakridge Tigers Varsity team for reaching the regional finals! Support the team this Friday at 4:30 PM in Gym A.",
                    date = "Oct 30, 2026",
                    priority = NoticePriority.LOW,
                    category = "Sports",
                    targetAudience = "All Community",
                    author = "Athletics Board"
                ),
                Notice(
                    id = "ntc_4",
                    title = "Winter Term Tuition Reminders",
                    content = "Please ensure that second-installment tuition fees are settled prior to November 15 to avoid any administrative hold on student examination portals.",
                    date = "Oct 28, 2026",
                    priority = NoticePriority.HIGH,
                    category = "Administrative",
                    targetAudience = "Parents",
                    author = "Finance Office"
                )
            )
        }

        private fun initialFees(): List<FeeRecord> {
            return listOf(
                FeeRecord("fee_1", "std_1", "Liam Harper", "Grade 10", "Term 1 (Fall 2026)", 1850.0, 1850.0, FeeStatus.PAID, "2026-09-30"),
                FeeRecord("fee_2", "std_2", "Sophia Chen", "Grade 10", "Term 1 (Fall 2026)", 1850.0, 1850.0, FeeStatus.PAID, "2026-09-30"),
                FeeRecord("fee_3", "std_3", "Marcus Johnson", "Grade 10", "Term 1 (Fall 2026)", 1850.0, 0.0, FeeStatus.PENDING, "2026-11-15"),
                FeeRecord("fee_4", "std_5", "Noah Miller", "Grade 10", "Term 1 (Fall 2026)", 1850.0, 0.0, FeeStatus.OVERDUE, "2026-10-15"),
                FeeRecord("fee_5", "std_7", "Ethan Taylor", "Grade 9", "Term 1 (Fall 2026)", 1750.0, 1000.0, FeeStatus.PARTIAL, "2026-11-15"),
                FeeRecord("fee_6", "std_8", "Aria Patel", "Grade 11", "Term 1 (Fall 2026)", 1950.0, 1950.0, FeeStatus.PAID, "2026-09-30")
            )
        }
    }
}
