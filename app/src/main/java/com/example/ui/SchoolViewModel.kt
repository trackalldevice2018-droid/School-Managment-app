package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClassMeritRank
import com.example.data.model.ExamCategory
import com.example.data.model.ExamRoutineSlot
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentMark
import com.example.data.model.StudentMarksheet
import com.example.data.model.Subject
import com.example.data.model.SubjectType
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
import java.util.UUID

enum class AppTab(val titleBn: String, val titleEn: String) {
    CLASS_SUBJECT("শ্রেণি ও বিষয়", "Classes & Subjects"),
    STUDENTS("শিক্ষার্থী", "Students"),
    EXAMS_MARKS("পরীক্ষা ও নম্বর", "Exams & Marks"),
    RESULTS("ফলাফল", "Results & Merit")
}

class SchoolViewModel(
    private val repository: SchoolRepository = SchoolRepository.getInstance()
) : ViewModel() {

    // Main Tab Navigation
    private val _currentTab = MutableStateFlow(AppTab.CLASS_SUBJECT)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Global Notifications
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Repository Streams
    val classes: StateFlow<List<SchoolClass>> = repository.classes
    val subjects: StateFlow<List<Subject>> = repository.subjects
    val students: StateFlow<List<Student>> = repository.students
    val routines: StateFlow<List<ExamRoutineSlot>> = repository.routines
    val marks: StateFlow<List<StudentMark>> = repository.marks

    // -------------------------------------------------------------
    // MODULE 1: Class & Subject Management
    // -------------------------------------------------------------
    private val _selectedClassId = MutableStateFlow("cls_12") // Default to Class 10
    val selectedClassId: StateFlow<String> = _selectedClassId.asStateFlow()

    val currentClassSubjects: StateFlow<List<Subject>> = combine(subjects, _selectedClassId) { list, classId ->
        list.filter { it.classId == classId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddClassDialog = MutableStateFlow(false)
    val showAddClassDialog: StateFlow<Boolean> = _showAddClassDialog.asStateFlow()

    private val _showAddSubjectDialog = MutableStateFlow(false)
    val showAddSubjectDialog: StateFlow<Boolean> = _showAddSubjectDialog.asStateFlow()

    fun setSelectedClassId(id: String) {
        _selectedClassId.value = id
    }

    fun openAddClassDialog() { _showAddClassDialog.value = true }
    fun closeAddClassDialog() { _showAddClassDialog.value = false }

    fun addClass(name: String, section: String) {
        repository.addClass(name, section)
        closeAddClassDialog()
        showMessage("নতুন শ্রেণি '$name' সফলভাবে যুক্ত হয়েছে")
    }

    fun deleteClass(classId: String) {
        repository.deleteClass(classId)
        if (_selectedClassId.value == classId) {
            _selectedClassId.value = classes.value.firstOrNull()?.id ?: ""
        }
        showMessage("শ্রেণি মুছে ফেলা হয়েছে")
    }

    fun openAddSubjectDialog() { _showAddSubjectDialog.value = true }
    fun closeAddSubjectDialog() { _showAddSubjectDialog.value = false }

    fun addSubject(name: String, code: String, fullMarks: Double, passMarks: Double, type: SubjectType) {
        val classId = _selectedClassId.value
        repository.addSubject(classId, name, code, fullMarks, passMarks, type)
        closeAddSubjectDialog()
        showMessage("বিষয় '$name' (${type.labelBn}) সফলভাবে যুক্ত হয়েছে")
    }

    fun deleteSubject(subjectId: String) {
        repository.deleteSubject(subjectId)
        showMessage("বিষয় মুছে ফেলা হয়েছে")
    }

    // -------------------------------------------------------------
    // MODULE 2: Student Management
    // -------------------------------------------------------------
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _studentClassFilter = MutableStateFlow("All")
    val studentClassFilter: StateFlow<String> = _studentClassFilter.asStateFlow()

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    private val _showAddStudentDialog = MutableStateFlow(false)
    val showAddStudentDialog: StateFlow<Boolean> = _showAddStudentDialog.asStateFlow()

    private val _showBulkUploadDialog = MutableStateFlow(false)
    val showBulkUploadDialog: StateFlow<Boolean> = _showBulkUploadDialog.asStateFlow()

    val filteredStudents: StateFlow<List<Student>> = combine(
        students,
        _studentSearchQuery,
        _studentClassFilter
    ) { list, query, classFilter ->
        list.filter { student ->
            val matchesQuery = query.isBlank() ||
                student.name.contains(query, ignoreCase = true) ||
                student.uniqueId.contains(query, ignoreCase = true) ||
                student.rollNo.contains(query, ignoreCase = true) ||
                student.guardianPhone.contains(query, ignoreCase = true)

            val matchesClass = classFilter == "All" || student.className == classFilter
            matchesQuery && matchesClass
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStudentSearchQuery(query: String) { _studentSearchQuery.value = query }
    fun setStudentClassFilter(cls: String) { _studentClassFilter.value = cls }
    fun selectStudent(student: Student?) { _selectedStudent.value = student }

    fun openAddStudentDialog() { _showAddStudentDialog.value = true }
    fun closeAddStudentDialog() { _showAddStudentDialog.value = false }

    fun addStudent(
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        birthRegNo: String,
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String
    ) {
        val targetClass = classes.value.find { it.id == classId } ?: classes.value.first()
        val student = Student(
            id = "std_${UUID.randomUUID().toString().take(6)}",
            uniqueId = uniqueId.ifBlank { "STU-${(1000..9999).random()}" },
            rollNo = rollNo,
            name = name,
            classId = targetClass.id,
            className = targetClass.name,
            birthRegNo = birthRegNo,
            fatherName = fatherName,
            motherName = motherName,
            guardianPhone = guardianPhone,
            address = address
        )
        repository.addStudent(student)
        closeAddStudentDialog()
        showMessage("শিক্ষার্থী '${student.name}' সফলভাবে নিবন্ধিত হয়েছে")
    }

    fun deleteStudent(studentId: String) {
        repository.deleteStudent(studentId)
        if (_selectedStudent.value?.id == studentId) {
            _selectedStudent.value = null
        }
        showMessage("শিক্ষার্থীর তথ্য মুছে ফেলা হয়েছে")
    }

    fun openBulkUploadDialog() { _showBulkUploadDialog.value = true }
    fun closeBulkUploadDialog() { _showBulkUploadDialog.value = false }

    fun bulkUploadCsv(csvContent: String) {
        val count = repository.bulkImportStudentsCsv(csvContent)
        closeBulkUploadDialog()
        showMessage("$count জন শিক্ষার্থী সফলভাবে বাল্ক আপলোড করা হয়েছে")
    }

    fun exportStudentsCsv(): String {
        return repository.exportStudentsCsv(_studentClassFilter.value)
    }

    // -------------------------------------------------------------
    // MODULE 3: Exam Management (Routine & Verified Marks Entry)
    // -------------------------------------------------------------
    private val _examSubTab = MutableStateFlow(0) // 0: Routine Generator, 1: Marks Entry
    val examSubTab: StateFlow<Int> = _examSubTab.asStateFlow()

    fun setExamSubTab(tab: Int) { _examSubTab.value = tab }

    // Routine Generator State
    private val _routineClassId = MutableStateFlow("cls_12")
    val routineClassId: StateFlow<String> = _routineClassId.asStateFlow()

    private val _routineExamCategory = MutableStateFlow(ExamCategory.ANNUAL)
    val routineExamCategory: StateFlow<ExamCategory> = _routineExamCategory.asStateFlow()

    private val _routineYear = MutableStateFlow(2026)
    val routineYear: StateFlow<Int> = _routineYear.asStateFlow()

    val currentRoutineSlots: StateFlow<List<ExamRoutineSlot>> = combine(
        routines,
        _routineClassId,
        _routineExamCategory,
        _routineYear
    ) { list, classId, cat, year ->
        list.filter { it.classId == classId && it.examCategory == cat && it.year == year }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRoutineClassId(id: String) { _routineClassId.value = id }
    fun setRoutineExamCategory(cat: ExamCategory) { _routineExamCategory.value = cat }
    fun setRoutineYear(year: Int) { _routineYear.value = year }

    fun generateRoutine() {
        repository.generateRoutineForClass(_routineClassId.value, _routineExamCategory.value, _routineYear.value)
        showMessage("পরীক্ষার রুটিন স্বয়ংক্রিয়ভাবে জেনারেট সম্পন্ন হয়েছে")
    }

    // Marks Entry with Student Verification
    private val _marksUniqueIdInput = MutableStateFlow("STU-2026-001")
    val marksUniqueIdInput: StateFlow<String> = _marksUniqueIdInput.asStateFlow()

    private val _verifiedStudent = MutableStateFlow<Student?>(null)
    val verifiedStudent: StateFlow<Student?> = _verifiedStudent.asStateFlow()

    private val _marksExamCategory = MutableStateFlow(ExamCategory.ANNUAL)
    val marksExamCategory: StateFlow<ExamCategory> = _marksExamCategory.asStateFlow()

    private val _marksExamYear = MutableStateFlow(2026)
    val marksExamYear: StateFlow<Int> = _marksExamYear.asStateFlow()

    private val _marksSelectedSubjectId = MutableStateFlow<String?>(null)
    val marksSelectedSubjectId: StateFlow<String?> = _marksSelectedSubjectId.asStateFlow()

    private val _marksObtainedInput = MutableStateFlow("")
    val marksObtainedInput: StateFlow<String> = _marksObtainedInput.asStateFlow()

    fun setMarksUniqueIdInput(id: String) {
        _marksUniqueIdInput.value = id
    }

    fun verifyStudent() {
        val student = repository.verifyStudentByUniqueId(_marksUniqueIdInput.value)
        _verifiedStudent.value = student
        if (student != null) {
            // Select first subject of student's class
            val classSubs = subjects.value.filter { it.classId == student.classId }
            _marksSelectedSubjectId.value = classSubs.firstOrNull()?.id
            showMessage("ভেরিফিকেশন সফল: ${student.name} (${student.className}, রোল: ${student.rollNo})")
        } else {
            showMessage("ভুল ইউনিক আইডি! কোনো শিক্ষার্থী পাওয়া যায়নি")
        }
    }

    fun setMarksExamCategory(cat: ExamCategory) { _marksExamCategory.value = cat }
    fun setMarksExamYear(year: Int) { _marksExamYear.value = year }
    fun setMarksSelectedSubjectId(id: String) { _marksSelectedSubjectId.value = id }
    fun setMarksObtainedInput(marks: String) { _marksObtainedInput.value = marks }

    fun saveStudentMarks() {
        val student = _verifiedStudent.value ?: return
        val subjectId = _marksSelectedSubjectId.value ?: return
        val obtained = _marksObtainedInput.value.toDoubleOrNull()
        if (obtained == null) {
            showMessage("সঠিক প্রাপ্ত নম্বর দিন")
            return
        }

        val subject = subjects.value.find { it.id == subjectId }
        if (subject != null && (obtained < 0 || obtained > subject.fullMarks)) {
            showMessage("নম্বর ০ থেকে ${subject.fullMarks.toInt()} এর মধ্যে হতে হবে")
            return
        }

        val saved = repository.saveMark(
            uniqueId = student.uniqueId,
            subjectId = subjectId,
            examCategory = _marksExamCategory.value,
            year = _marksExamYear.value,
            marksObtained = obtained
        )
        if (saved != null) {
            _marksObtainedInput.value = ""
            showMessage("নম্বর সংরক্ষণ সম্পন্ন: ${saved.subjectName} -> ${saved.marksObtained.toInt()} (${saved.letterGrade})")
        }
    }

    // -------------------------------------------------------------
    // MODULE 4: Result System (Individual Marksheet & Class Merit)
    // -------------------------------------------------------------
    private val _resultSubTab = MutableStateFlow(0) // 0: Individual Marksheet, 1: Class Merit List
    val resultSubTab: StateFlow<Int> = _resultSubTab.asStateFlow()

    fun setResultSubTab(tab: Int) { _resultSubTab.value = tab }

    // Individual Result Search
    private val _resultSearchUniqueId = MutableStateFlow("STU-2026-001")
    val resultSearchUniqueId: StateFlow<String> = _resultSearchUniqueId.asStateFlow()

    private val _resultExamCategory = MutableStateFlow(ExamCategory.ANNUAL)
    val resultExamCategory: StateFlow<ExamCategory> = _resultExamCategory.asStateFlow()

    private val _resultYear = MutableStateFlow(2026)
    val resultYear: StateFlow<Int> = _resultYear.asStateFlow()

    private val _currentMarksheet = MutableStateFlow<StudentMarksheet?>(null)
    val currentMarksheet: StateFlow<StudentMarksheet?> = _currentMarksheet.asStateFlow()

    fun setResultSearchUniqueId(id: String) { _resultSearchUniqueId.value = id }
    fun setResultExamCategory(cat: ExamCategory) { _resultExamCategory.value = cat }
    fun setResultYear(year: Int) { _resultYear.value = year }

    fun searchIndividualMarksheet() {
        val ms = repository.getStudentMarksheet(
            uniqueId = _resultSearchUniqueId.value,
            examCategory = _resultExamCategory.value,
            year = _resultYear.value
        )
        _currentMarksheet.value = ms
        if (ms != null) {
            val status = if (ms.isPassed) "উত্তীর্ণ (GPA: ${ms.gpa})" else "অনুত্তীর্ণ (F)"
            showMessage("মার্কশীট লোড হয়েছে: ${ms.student.name} - $status")
        } else {
            showMessage("এই আইডি বা পরীক্ষায় কোনো ফলাফল পাওয়া যায়নি")
        }
    }

    // Class Merit List State
    private val _meritClassId = MutableStateFlow("cls_12") // Class 10
    val meritClassId: StateFlow<String> = _meritClassId.asStateFlow()

    private val _meritExamCategory = MutableStateFlow(ExamCategory.ANNUAL)
    val meritExamCategory: StateFlow<ExamCategory> = _meritExamCategory.asStateFlow()

    private val _meritYear = MutableStateFlow(2026)
    val meritYear: StateFlow<Int> = _meritYear.asStateFlow()

    val classMeritList: StateFlow<List<ClassMeritRank>> = combine(
        marks,
        _meritClassId,
        _meritExamCategory,
        _meritYear
    ) { _, classId, cat, year ->
        repository.getClassMeritList(classId, cat, year)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMeritClassId(id: String) { _meritClassId.value = id }
    fun setMeritExamCategory(cat: ExamCategory) { _meritExamCategory.value = cat }
    fun setMeritYear(year: Int) { _meritYear.value = year }

    init {
        // Initial auto-verification of default demo student
        verifyStudent()
        searchIndividualMarksheet()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    private fun showMessage(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }
}
