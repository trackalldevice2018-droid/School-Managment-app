package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.FeeRecord
import com.example.data.model.FeeStatus
import com.example.data.model.GradeRecord
import com.example.data.model.Notice
import com.example.data.model.NoticePriority
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.Teacher
import com.example.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppTab(val title: String) {
    DASHBOARD("Overview"),
    STUDENTS("Students"),
    ATTENDANCE("Attendance"),
    CLASSES("Classes & Staff"),
    NOTICES("Notices & Fees")
}

class SchoolViewModel(
    private val repository: SchoolRepository = SchoolRepository.getInstance()
) : ViewModel() {

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Snackbar notifications
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Repository Flows
    val students: StateFlow<List<Student>> = repository.students
    val teachers: StateFlow<List<Teacher>> = repository.teachers
    val classes: StateFlow<List<SchoolClass>> = repository.classes
    val attendance: StateFlow<List<AttendanceRecord>> = repository.attendance
    val grades: StateFlow<List<GradeRecord>> = repository.grades
    val notices: StateFlow<List<Notice>> = repository.notices
    val fees: StateFlow<List<FeeRecord>> = repository.fees

    // Student Filter & Search
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _selectedGradeFilter = MutableStateFlow("All")
    val selectedGradeFilter: StateFlow<String> = _selectedGradeFilter.asStateFlow()

    val filteredStudents: StateFlow<List<Student>> = combine(
        students,
        _studentSearchQuery,
        _selectedGradeFilter
    ) { list, query, gradeFilter ->
        list.filter { student ->
            val matchesQuery = query.isBlank() ||
                student.fullName.contains(query, ignoreCase = true) ||
                student.rollNo.contains(query, ignoreCase = true) ||
                student.email.contains(query, ignoreCase = true)

            val matchesGrade = gradeFilter == "All" || student.grade == gradeFilter
            matchesQuery && matchesGrade
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected student for detail sheet
    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    // Attendance Taking State
    private val _selectedAttendanceClassId = MutableStateFlow("cls_10a")
    val selectedAttendanceClassId: StateFlow<String> = _selectedAttendanceClassId.asStateFlow()

    private val _selectedAttendanceDate = MutableStateFlow(todayDateString)
    val selectedAttendanceDate: StateFlow<String> = _selectedAttendanceDate.asStateFlow()

    // Map of studentId -> AttendanceStatus for the current session
    private val _currentSessionAttendance = MutableStateFlow<Map<String, AttendanceStatus>>(emptyMap())
    val currentSessionAttendance: StateFlow<Map<String, AttendanceStatus>> = _currentSessionAttendance.asStateFlow()

    // Classes / Teachers Tab Sub-section (0: Classes, 1: Teachers)
    private val _classesSubTab = MutableStateFlow(0)
    val classesSubTab: StateFlow<Int> = _classesSubTab.asStateFlow()

    // Notices / Fees Tab Sub-section (0: Notices, 1: Fees, 2: Grades)
    private val _noticesSubTab = MutableStateFlow(0)
    val noticesSubTab: StateFlow<Int> = _noticesSubTab.asStateFlow()

    // Dialog Visibilities
    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog: StateFlow<Boolean> = _showAddStudentDialog.asStateFlow()

    private val _showAddTeacherDialog = MutableStateFlow(false)
    val showAddTeacherDialog: StateFlow<Boolean> = _showAddTeacherDialog.asStateFlow()

    private val _showAddNoticeDialog = MutableStateFlow(false)
    val showAddNoticeDialog: StateFlow<Boolean> = _showAddNoticeDialog.asStateFlow()

    private val _showAddGradeDialog = MutableStateFlow(false)
    val showAddGradeDialog: StateFlow<Boolean> = _showAddGradeDialog.asStateFlow()

    init {
        // Initialize attendance map for default class and date
        loadAttendanceForCurrentSelection()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setClassesSubTab(index: Int) {
        _classesSubTab.value = index
    }

    fun setNoticesSubTab(index: Int) {
        _noticesSubTab.value = index
    }

    fun setStudentSearchQuery(query: String) {
        _studentSearchQuery.value = query
    }

    fun setSelectedGradeFilter(grade: String) {
        _selectedGradeFilter.value = grade
    }

    fun selectStudent(student: Student?) {
        _selectedStudent.value = student
    }

    fun setAttendanceClass(classId: String) {
        _selectedAttendanceClassId.value = classId
        loadAttendanceForCurrentSelection()
    }

    fun setAttendanceDate(date: String) {
        _selectedAttendanceDate.value = date
        loadAttendanceForCurrentSelection()
    }

    fun toggleStudentAttendanceStatus(studentId: String) {
        val current = _currentSessionAttendance.value[studentId] ?: AttendanceStatus.PRESENT
        val next = when (current) {
            AttendanceStatus.PRESENT -> AttendanceStatus.ABSENT
            AttendanceStatus.ABSENT -> AttendanceStatus.LATE
            AttendanceStatus.LATE -> AttendanceStatus.EXCUSED
            AttendanceStatus.EXCUSED -> AttendanceStatus.PRESENT
        }
        _currentSessionAttendance.value = _currentSessionAttendance.value + (studentId to next)
    }

    fun setSingleStudentStatus(studentId: String, status: AttendanceStatus) {
        _currentSessionAttendance.value = _currentSessionAttendance.value + (studentId to status)
    }

    fun markAllPresent() {
        val currentClass = classes.value.find { it.id == _selectedAttendanceClassId.value }
        val classStudents = students.value.filter {
            if (currentClass != null) it.grade == currentClass.grade && it.section == currentClass.section else true
        }
        val updated = _currentSessionAttendance.value.toMutableMap()
        classStudents.forEach { student ->
            updated[student.id] = AttendanceStatus.PRESENT
        }
        _currentSessionAttendance.value = updated
        showMessage("Marked all ${classStudents.size} students Present")
    }

    fun saveAttendance() {
        val classId = _selectedAttendanceClassId.value
        val date = _selectedAttendanceDate.value
        val map = _currentSessionAttendance.value
        repository.saveAttendance(date, classId, map)
        showMessage("Attendance recorded for ${_currentSessionAttendance.value.size} students")
    }

    fun loadAttendanceForCurrentSelection() {
        val classId = _selectedAttendanceClassId.value
        val date = _selectedAttendanceDate.value
        val currentClass = classes.value.find { it.id == classId }
        val classStudents = students.value.filter {
            if (currentClass != null) it.grade == currentClass.grade && it.section == currentClass.section else true
        }

        val existingRecords = attendance.value.filter { it.date == date && it.classId == classId }
        val statusMap = mutableMapOf<String, AttendanceStatus>()

        classStudents.forEach { student ->
            val record = existingRecords.find { it.studentId == student.id }
            statusMap[student.id] = record?.status ?: AttendanceStatus.PRESENT
        }

        _currentSessionAttendance.value = statusMap
    }

    // CRUD - Student
    fun openAddStudentDialog() { _showAddStudentDialog.value = true }
    fun closeAddStudentDialog() { _showAddStudentDialog.value = false }

    fun addStudent(
        firstName: String,
        lastName: String,
        rollNo: String,
        grade: String,
        section: String,
        parentName: String,
        parentPhone: String,
        parentEmail: String,
        gender: String
    ) {
        val newStudent = Student(
            id = "std_${UUID.randomUUID().toString().take(8)}",
            rollNo = rollNo.ifBlank { (100..999).random().toString() },
            firstName = firstName,
            lastName = lastName,
            grade = grade,
            section = section,
            gender = gender,
            email = "${firstName.lowercase()}.${lastName.lowercase()}@student.oakridge.edu",
            phone = "+1 (555) 234-${(1000..9999).random()}",
            parentName = parentName,
            parentPhone = parentPhone,
            parentEmail = parentEmail,
            attendancePercentage = 100,
            feeStatus = FeeStatus.PENDING,
            avatarColorHex = 0xFF1E3A8A
        )
        repository.addStudent(newStudent)
        closeAddStudentDialog()
        loadAttendanceForCurrentSelection()
        showMessage("Student ${newStudent.fullName} added")
    }

    fun deleteStudent(studentId: String) {
        val student = students.value.find { it.id == studentId }
        repository.deleteStudent(studentId)
        if (_selectedStudent.value?.id == studentId) {
            _selectedStudent.value = null
        }
        loadAttendanceForCurrentSelection()
        showMessage("Student ${student?.fullName ?: ""} removed")
    }

    // CRUD - Teacher
    fun openAddTeacherDialog() { _showAddTeacherDialog.value = true }
    fun closeAddTeacherDialog() { _showAddTeacherDialog.value = false }

    fun addTeacher(
        name: String,
        department: String,
        subjects: String,
        assignedClass: String,
        email: String,
        phone: String,
        qualification: String,
        experienceYears: Int
    ) {
        val newTeacher = Teacher(
            id = "tch_${UUID.randomUUID().toString().take(8)}",
            employeeId = "EMP-${(1000..9999).random()}",
            name = name,
            department = department,
            subjects = subjects.split(",").map { it.trim() }.filter { it.isNotBlank() },
            assignedClass = assignedClass,
            email = email.ifBlank { "${name.lowercase().replace(" ", ".")}@oakridge.edu" },
            phone = phone.ifBlank { "+1 (555) 789-${(1000..9999).random()}" },
            qualification = qualification,
            experienceYears = experienceYears,
            avatarColorHex = 0xFF0D9488
        )
        repository.addTeacher(newTeacher)
        closeAddTeacherDialog()
        showMessage("Teacher $name added")
    }

    fun deleteTeacher(teacherId: String) {
        val teacher = teachers.value.find { it.id == teacherId }
        repository.deleteTeacher(teacherId)
        showMessage("Teacher ${teacher?.name ?: ""} removed")
    }

    // CRUD - Notice
    fun openAddNoticeDialog() { _showAddNoticeDialog.value = true }
    fun closeAddNoticeDialog() { _showAddNoticeDialog.value = false }

    fun addNotice(
        title: String,
        content: String,
        category: String,
        priority: NoticePriority,
        targetAudience: String
    ) {
        val notice = Notice(
            id = "ntc_${UUID.randomUUID().toString().take(8)}",
            title = title,
            content = content,
            date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
            priority = priority,
            category = category,
            targetAudience = targetAudience
        )
        repository.addNotice(notice)
        closeAddNoticeDialog()
        showMessage("Announcement posted")
    }

    fun deleteNotice(noticeId: String) {
        repository.deleteNotice(noticeId)
        showMessage("Notice removed")
    }

    // CRUD - Grades
    fun openAddGradeDialog() { _showAddGradeDialog.value = true }
    fun closeAddGradeDialog() { _showAddGradeDialog.value = false }

    fun addGrade(
        studentId: String,
        subject: String,
        examName: String,
        marksObtained: Double,
        maxMarks: Double,
        remarks: String
    ) {
        val student = students.value.find { it.id == studentId }
        val pct = if (maxMarks > 0) (marksObtained / maxMarks) * 100 else 0.0
        val letter = when {
            pct >= 95 -> "A+"
            pct >= 90 -> "A"
            pct >= 85 -> "B+"
            pct >= 80 -> "B"
            pct >= 75 -> "C+"
            pct >= 70 -> "C"
            pct >= 60 -> "D"
            else -> "F"
        }
        val record = GradeRecord(
            id = "grd_${UUID.randomUUID().toString().take(8)}",
            studentId = studentId,
            studentName = student?.fullName ?: "Student",
            grade = student?.grade ?: "Grade 10",
            subject = subject,
            examName = examName,
            marksObtained = marksObtained,
            maxMarks = maxMarks,
            gradeLetter = letter,
            date = todayDateString,
            remarks = remarks
        )
        repository.addGrade(record)
        closeAddGradeDialog()
        showMessage("Grade recorded: $letter (${marksObtained.toInt()}/${maxMarks.toInt()})")
    }

    fun deleteGrade(gradeId: String) {
        repository.deleteGrade(gradeId)
        showMessage("Grade entry deleted")
    }

    fun markFeePaid(feeId: String) {
        val fee = fees.value.find { it.id == feeId }
        if (fee != null) {
            repository.updateFeeStatus(feeId, FeeStatus.PAID, fee.totalAmount)
            // also update student feeStatus
            val student = students.value.find { it.id == fee.studentId }
            if (student != null) {
                repository.updateStudent(student.copy(feeStatus = FeeStatus.PAID))
            }
            showMessage("Payment of $${fee.totalAmount.toInt()} marked as Received")
        }
    }

    private fun showMessage(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }
}
