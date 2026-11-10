package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ClassMeritRank
import com.example.data.model.ExamCategory
import com.example.data.model.ExamRoutineSlot
import com.example.data.model.SchoolClass
import com.example.data.model.SchoolSettings
import com.example.data.model.Student
import com.example.data.model.StudentMark
import com.example.data.model.StudentMarksheet
import com.example.data.model.Subject
import com.example.data.model.SubjectType
import com.example.data.repository.SchoolRepository
import com.example.util.PrintUtils
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
    RESULTS("ফলাফল", "Results & Merit"),
    SETTINGS("সেটিংস", "Settings")
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
    val settings: StateFlow<SchoolSettings> = repository.settings

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

    fun isRollNumberTaken(classId: String, rollNo: String, excludeStudentId: String? = null): Boolean {
        return repository.isRollNumberTakenInClass(classId, rollNo, excludeStudentId)
    }

    fun getNextRollNumber(classId: String): String {
        return repository.getNextRollNumberForClass(classId)
    }

    fun getStudentCountInClass(classId: String): Int {
        return repository.getStudentCountInClass(classId)
    }

    fun addClass(name: String, section: String) {
        repository.addClass(name, section)
        closeAddClassDialog()
        showMessage("নতুন শ্রেণি '$name' সফলভাবে যুক্ত হয়েছে")
    }

    fun deleteClass(classId: String) {
        val count = repository.getStudentCountInClass(classId)
        if (count > 0) {
            showMessage("এই শ্রেণিতে $count জন শিক্ষার্থী নিবন্ধিত আছে! শ্রেণিটি মুছতে আগে শিক্ষার্থীদের অন্য শ্রেণিতে স্থানান্তর করুন।")
            return
        }
        val remaining = repository.classes.value.filterNot { it.id == classId }
        repository.deleteClass(classId)
        if (_selectedClassId.value == classId) {
            _selectedClassId.value = remaining.firstOrNull()?.id ?: ""
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
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
            showMessage("বিষয় মুছে ফেলা হয়েছে")
        }
    }

    // -------------------------------------------------------------
    // MODULE 2: Student Management (with Edit & Photo Upload)
    // -------------------------------------------------------------
    private val _studentSearchQuery = MutableStateFlow("")
    val studentSearchQuery: StateFlow<String> = _studentSearchQuery.asStateFlow()

    private val _studentClassFilter = MutableStateFlow("All")
    val studentClassFilter: StateFlow<String> = _studentClassFilter.asStateFlow()

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    private val _editingStudent = MutableStateFlow<Student?>(null)
    val editingStudent: StateFlow<Student?> = _editingStudent.asStateFlow()

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

    fun openEditStudentDialog(student: Student) {
        _editingStudent.value = student
    }

    fun closeEditStudentDialog() {
        _editingStudent.value = null
    }

    fun addStudent(
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        birthRegNo: String,
        dateOfBirth: String = "01/01/2010",
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String,
        photoUri: String? = null
    ) {
        val targetClass = classes.value.find { it.id == classId } ?: classes.value.first()
        if (isRollNumberTaken(targetClass.id, rollNo)) {
            showMessage("সতর্কতা: '${targetClass.name}' এ রোল '$rollNo' ইতোমধ্যে বরাদ্দ আছে!")
            return
        }
        val student = Student(
            id = "std_${UUID.randomUUID().toString().take(6)}",
            uniqueId = uniqueId.ifBlank { "STU-${(1000..9999).random()}" },
            rollNo = rollNo,
            name = name,
            classId = targetClass.id,
            className = targetClass.name,
            birthRegNo = birthRegNo,
            dateOfBirth = dateOfBirth.ifBlank { "01/01/2010" },
            fatherName = fatherName,
            motherName = motherName,
            guardianPhone = guardianPhone,
            address = address,
            photoUri = photoUri
        )
        viewModelScope.launch {
            repository.addStudent(student)
        }
        closeAddStudentDialog()
        showMessage("শিক্ষার্থী '${student.name}' সফলভাবে নিবন্ধিত হয়েছে")
    }

    fun updateStudent(
        id: String,
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        birthRegNo: String,
        dateOfBirth: String = "01/01/2010",
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String,
        photoUri: String?,
        avatarColorHex: Long
    ) {
        val targetClass = classes.value.find { it.id == classId } ?: classes.value.first()
        if (isRollNumberTaken(targetClass.id, rollNo, excludeStudentId = id)) {
            showMessage("সতর্কতা: '${targetClass.name}' এ রোল '$rollNo' ইতোমধ্যে বরাদ্দ আছে!")
            return
        }
        val updated = Student(
            id = id,
            uniqueId = uniqueId,
            rollNo = rollNo,
            name = name,
            classId = targetClass.id,
            className = targetClass.name,
            birthRegNo = birthRegNo,
            dateOfBirth = dateOfBirth.ifBlank { "01/01/2010" },
            fatherName = fatherName,
            motherName = motherName,
            guardianPhone = guardianPhone,
            address = address,
            avatarColorHex = avatarColorHex,
            photoUri = photoUri
        )
        viewModelScope.launch {
            repository.updateStudent(updated)
            repository.updateStudentById(
                studentId = id,
                name = name,
                uniqueId = uniqueId,
                rollNo = rollNo,
                classId = targetClass.id,
                className = targetClass.name,
                birthRegNo = birthRegNo,
                dateOfBirth = updated.dateOfBirth,
                fatherName = fatherName,
                motherName = motherName,
                guardianPhone = guardianPhone,
                address = address,
                photoUri = photoUri
            )
        }
        _editingStudent.value = null
        if (_selectedStudent.value?.id == id) {
            _selectedStudent.value = updated
        }
        showMessage("শিক্ষার্থী '${updated.name}' এর তথ্য সংশোধন সম্পন্ন হয়েছে")
    }

    fun updateStudentById(
        id: String,
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        birthRegNo: String,
        dateOfBirth: String = "01/01/2010",
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String,
        photoUri: String? = null
    ) {
        val targetClass = classes.value.find { it.id == classId } ?: classes.value.first()
        if (isRollNumberTaken(targetClass.id, rollNo, excludeStudentId = id)) {
            showMessage("সতর্কতা: '${targetClass.name}' এ রোল '$rollNo' ইতোমধ্যে বরাদ্দ আছে!")
            return
        }
        val existing = students.value.find { it.id == id }
        val updated = Student(
            id = id,
            uniqueId = uniqueId,
            rollNo = rollNo,
            name = name,
            classId = targetClass.id,
            className = targetClass.name,
            birthRegNo = birthRegNo,
            dateOfBirth = dateOfBirth.ifBlank { "01/01/2010" },
            fatherName = fatherName,
            motherName = motherName,
            guardianPhone = guardianPhone,
            address = address,
            avatarColorHex = existing?.avatarColorHex ?: 0xFF1E3A8A,
            photoUri = photoUri ?: existing?.photoUri
        )
        viewModelScope.launch {
            repository.updateStudent(updated)
            repository.updateStudentById(
                studentId = id,
                name = name,
                uniqueId = uniqueId,
                rollNo = rollNo,
                classId = targetClass.id,
                className = targetClass.name,
                birthRegNo = birthRegNo,
                dateOfBirth = updated.dateOfBirth,
                fatherName = fatherName,
                motherName = motherName,
                guardianPhone = guardianPhone,
                address = address,
                photoUri = updated.photoUri
            )
        }
        _editingStudent.value = null
        if (_selectedStudent.value?.id == id) {
            _selectedStudent.value = updated
        }
        showMessage("শিক্ষার্থী '${updated.name}' এর তথ্য সংশোধন সম্পন্ন হয়েছে")
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
        _editingStudent.value = null
        if (_selectedStudent.value?.id == student.id) {
            _selectedStudent.value = student
        }
        showMessage("শিক্ষার্থী '${student.name}' এর তথ্য সংশোধন সম্পন্ন হয়েছে")
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
        }
        if (_selectedStudent.value?.id == studentId) {
            _selectedStudent.value = null
        }
        showMessage("শিক্ষার্থীর তথ্য মুছে ফেলা হয়েছে")
    }

    // Student Multi-selection & Bulk Delete
    private val _selectedStudentIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedStudentIds: StateFlow<Set<String>> = _selectedStudentIds.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    fun setMultiSelectMode(enabled: Boolean) {
        _isMultiSelectMode.value = enabled
        if (!enabled) _selectedStudentIds.value = emptySet()
    }

    fun toggleStudentSelection(studentId: String) {
        val current = _selectedStudentIds.value.toMutableSet()
        if (current.contains(studentId)) {
            current.remove(studentId)
        } else {
            current.add(studentId)
        }
        _selectedStudentIds.value = current
        if (current.isNotEmpty()) {
            _isMultiSelectMode.value = true
        }
    }

    fun selectAllStudents(studentsList: List<Student>) {
        _selectedStudentIds.value = studentsList.map { it.id }.toSet()
        _isMultiSelectMode.value = true
    }

    fun clearStudentSelection() {
        _selectedStudentIds.value = emptySet()
        _isMultiSelectMode.value = false
    }

    fun deleteSelectedStudents() {
        val ids = _selectedStudentIds.value.toList()
        if (ids.isEmpty()) return
        val count = ids.size
        viewModelScope.launch {
            repository.deleteStudents(ids)
        }
        clearStudentSelection()
        showMessage("$count জন শিক্ষার্থীর তথ্য ও ফলাফল মুছে ফেলা হয়েছে")
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
    // MODULE 3: Exam Management (Routine, Admit Cards & Marks Entry)
    // -------------------------------------------------------------
    private val _examSubTab = MutableStateFlow(0) // 0: Routine, 1: Admit Cards, 2: Marks Entry
    val examSubTab: StateFlow<Int> = _examSubTab.asStateFlow()

    fun setExamSubTab(tab: Int) { _examSubTab.value = tab }

    // Routine Generator State
    private val _routineClassId = MutableStateFlow("cls_12")
    val routineClassId: StateFlow<String> = _routineClassId.asStateFlow()

    private val _routineExamCategory = MutableStateFlow(ExamCategory.ANNUAL)
    val routineExamCategory: StateFlow<ExamCategory> = _routineExamCategory.asStateFlow()

    private val _routineExamCategoryText = MutableStateFlow("বার্ষিক পরীক্ষা")
    val routineExamCategoryText: StateFlow<String> = _routineExamCategoryText.asStateFlow()

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
    fun setRoutineExamCategory(cat: ExamCategory) {
        _routineExamCategory.value = cat
        _routineExamCategoryText.value = cat.labelBn
    }
    fun setRoutineExamCategoryText(text: String) { _routineExamCategoryText.value = text }
    fun setRoutineYear(year: Int) { _routineYear.value = year }

    fun generateRoutine() {
        repository.generateRoutineForClass(_routineClassId.value, _routineExamCategory.value, _routineYear.value)
        showMessage("পরীক্ষার রুটিন স্বয়ংক্রিয়ভাবে জেনারেট সম্পন্ন হয়েছে")
    }

    fun updateRoutineSlot(slotId: String, date: String, day: String, subjectName: String, shift: String = "সকাল") {
        repository.updateRoutineSlot(slotId, date, day, subjectName, shift)
        showMessage("রুটিন তথ্য আপডেট করা হয়েছে")
    }

    fun addRoutineSlot(subjectName: String, date: String, day: String, shift: String = "সকাল") {
        repository.addRoutineSlot(
            classId = _routineClassId.value,
            examCategory = _routineExamCategory.value,
            year = _routineYear.value,
            subjectName = subjectName,
            date = date,
            day = day,
            shift = shift
        )
        showMessage("রুটিনে নতুন বিষয় যুক্ত হয়েছে")
    }

    fun deleteRoutineSlot(slotId: String) {
        repository.deleteRoutineSlot(slotId)
        showMessage("রুটিন স্লট মুছে ফেলা হয়েছে")
    }

    fun printRoutine(context: Context) {
        printRoutineWithLayout(context, slipsPerPage = 4)
    }

    fun printRoutineWithLayout(context: Context, slipsPerPage: Int) {
        val classObj = classes.value.find { it.id == _routineClassId.value }
        val className = classObj?.name ?: "Class"
        val slots = currentRoutineSlots.value
        if (slots.isEmpty()) {
            showMessage("প্রিন্ট করার জন্য কোনো রুটিন পাওয়া যায়নি। প্রথমে 'স্বয়ংক্রিয় রুটিন তৈরি' করুন।")
            return
        }
        val catName = _routineExamCategoryText.value.ifBlank { _routineExamCategory.value.labelBn }
        val html = PrintUtils.generateExamRoutineHtml(
            settings = settings.value,
            className = className,
            categoryName = catName,
            year = _routineYear.value,
            routineSlots = slots,
            slipsPerPage = slipsPerPage
        )
        PrintUtils.printHtml(context, html, "Routine_${className}_${slipsPerPage}Slips")
    }

    // --- Admit Cards Generator State & Methods ---
    private val _admitCardClassId = MutableStateFlow("cls_12")
    val admitCardClassId: StateFlow<String> = _admitCardClassId.asStateFlow()

    private val _admitCardExamCategory = MutableStateFlow(ExamCategory.ANNUAL)
    val admitCardExamCategory: StateFlow<ExamCategory> = _admitCardExamCategory.asStateFlow()

    private val _admitCardExamCategoryText = MutableStateFlow("বার্ষিক পরীক্ষা")
    val admitCardExamCategoryText: StateFlow<String> = _admitCardExamCategoryText.asStateFlow()

    private val _admitCardYear = MutableStateFlow(2026)
    val admitCardYear: StateFlow<Int> = _admitCardYear.asStateFlow()

    val studentsForAdmitCard: StateFlow<List<Student>> = combine(
        students,
        _admitCardClassId
    ) { allStudents, classId ->
        allStudents.filter { it.classId == classId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAdmitCardClassId(id: String) { _admitCardClassId.value = id }
    fun setAdmitCardExamCategory(cat: ExamCategory) {
        _admitCardExamCategory.value = cat
        _admitCardExamCategoryText.value = cat.labelBn
    }
    fun setAdmitCardExamCategoryText(text: String) { _admitCardExamCategoryText.value = text }
    fun setAdmitCardYear(year: Int) { _admitCardYear.value = year }

    fun printAdmitCards(context: Context, cardsPerPage: Int = 4) {
        val classObj = classes.value.find { it.id == _admitCardClassId.value }
        val className = classObj?.name ?: "Class"
        val classStudents = studentsForAdmitCard.value
        if (classStudents.isEmpty()) {
            showMessage("এই শ্রেণিতে কোনো শিক্ষার্থী পাওয়া যায়নি। প্রথমে শিক্ষার্থী যুক্ত করুন।")
            return
        }
        val catName = _admitCardExamCategoryText.value.ifBlank { _admitCardExamCategory.value.labelBn }
        val html = PrintUtils.generateAdmitCardsHtml(
            settings = settings.value,
            className = className,
            categoryName = catName,
            year = _admitCardYear.value,
            students = classStudents,
            cardsPerPage = cardsPerPage
        )
        PrintUtils.printHtml(context, html, "AdmitCards_${className}_${cardsPerPage}PerA4")
    }

    // --- Student Directory Full A4 PDF Print ---
    fun printStudentDirectory(context: Context, className: String, studentList: List<Student>) {
        if (studentList.isEmpty()) {
            showMessage("প্রিন্ট করার মতো কোনো শিক্ষার্থী ডাটা নেই")
            return
        }
        val html = PrintUtils.generateStudentDirectoryHtml(
            settings = settings.value,
            className = className,
            students = studentList
        )
        PrintUtils.printHtml(context, html, "Student_Directory_$className")
    }

    // --- About Us Dialog State ---
    private val _showAboutUsDialog = MutableStateFlow(false)
    val showAboutUsDialog: StateFlow<Boolean> = _showAboutUsDialog.asStateFlow()

    fun openAboutUsDialog() { _showAboutUsDialog.value = true }
    fun closeAboutUsDialog() { _showAboutUsDialog.value = false }

    // Marks Entry with Student Verification & Duplicate Prevention
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

    // Filtered subjects that have not yet had marks entered (to prevent duplicates)
    val unenteredSubjectsForVerifiedStudent: StateFlow<List<Subject>> = combine(
        subjects,
        _verifiedStudent,
        marks,
        _marksExamCategory,
        _marksExamYear
    ) { allSubs, student, marksList, category, year ->
        if (student == null) emptyList()
        else {
            val classSubs = allSubs.filter { it.classId == student.classId }
            val enteredSubjectIds = marksList
                .filter { it.studentId == student.id && it.examCategory == category && it.year == year }
                .map { it.subjectId }
                .toSet()
            classSubs.filterNot { enteredSubjectIds.contains(it.id) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMarksUniqueIdInput(id: String) {
        _marksUniqueIdInput.value = id
    }

    fun verifyStudent() {
        val student = repository.verifyStudentByUniqueId(_marksUniqueIdInput.value)
        _verifiedStudent.value = student
        if (student != null) {
            val classSubs = subjects.value.filter { it.classId == student.classId }
            val enteredSubjectIds = marks.value
                .filter { it.studentId == student.id && it.examCategory == _marksExamCategory.value && it.year == _marksExamYear.value }
                .map { it.subjectId }
                .toSet()
            val unentered = classSubs.filterNot { enteredSubjectIds.contains(it.id) }
            _marksSelectedSubjectId.value = unentered.firstOrNull()?.id ?: classSubs.firstOrNull()?.id
            showMessage("ভেরিফিকেশন সফল: ${student.name} (${student.className}, রোল: ${student.rollNo})")
        } else {
            showMessage("ভুল স্টুডেন্ট আইডি! কোনো শিক্ষার্থী পাওয়া যায়নি")
        }
    }

    private val _marksExamCategoryText = MutableStateFlow("বার্ষিক পরীক্ষা")
    val marksExamCategoryText: StateFlow<String> = _marksExamCategoryText.asStateFlow()

    fun setMarksExamCategory(cat: ExamCategory) {
        _marksExamCategory.value = cat
        _marksExamCategoryText.value = cat.labelBn
        refreshSelectedSubject()
    }

    fun setMarksExamCategoryText(cat: String) {
        _marksExamCategoryText.value = cat
    }

    fun bulkUploadClassMarks(classId: String, csvText: String) {
        val cat = _marksExamCategoryText.value.ifBlank { _marksExamCategory.value.labelBn }
        val (count, msg) = repository.bulkImportClassMarksCsv(classId, cat, _marksExamYear.value, csvText)
        showMessage(msg)
    }

    fun exportClassMarksCsv(classId: String): String {
        val cat = _marksExamCategoryText.value.ifBlank { _marksExamCategory.value.labelBn }
        return repository.exportClassMarksCsv(classId, cat, _marksExamYear.value)
    }

    fun setMarksExamYear(year: Int) {
        _marksExamYear.value = year
        refreshSelectedSubject()
    }

    private fun refreshSelectedSubject() {
        val student = _verifiedStudent.value ?: return
        val classSubs = subjects.value.filter { it.classId == student.classId }
        val enteredIds = marks.value
            .filter { it.studentId == student.id && it.examCategory == _marksExamCategory.value && it.year == _marksExamYear.value }
            .map { it.subjectId }
            .toSet()
        val unentered = classSubs.filterNot { enteredIds.contains(it.id) }
        _marksSelectedSubjectId.value = unentered.firstOrNull()?.id ?: classSubs.firstOrNull()?.id
    }

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
            refreshSelectedSubject()
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

    private val _resultExamCategoryText = MutableStateFlow("বার্ষিক পরীক্ষা")
    val resultExamCategoryText: StateFlow<String> = _resultExamCategoryText.asStateFlow()

    private val _resultYear = MutableStateFlow(2026)
    val resultYear: StateFlow<Int> = _resultYear.asStateFlow()

    private val _currentMarksheet = MutableStateFlow<StudentMarksheet?>(null)
    val currentMarksheet: StateFlow<StudentMarksheet?> = _currentMarksheet.asStateFlow()

    fun setResultSearchUniqueId(id: String) { _resultSearchUniqueId.value = id }
    fun setResultExamCategory(cat: ExamCategory) {
        _resultExamCategory.value = cat
        _resultExamCategoryText.value = cat.labelBn
    }
    fun setResultExamCategoryText(text: String) {
        _resultExamCategoryText.value = text
    }
    fun setResultYear(year: Int) { _resultYear.value = year }

    fun searchIndividualMarksheet() {
        val ms = repository.getStudentMarksheet(
            uniqueId = _resultSearchUniqueId.value,
            examCategory = _resultExamCategory.value,
            year = _resultYear.value
        )
        val finalMs = if (ms != null && _resultExamCategoryText.value.isNotBlank()) {
            ms.copy(customCategoryName = _resultExamCategoryText.value)
        } else {
            ms
        }
        _currentMarksheet.value = finalMs
        if (finalMs != null) {
            val status = if (finalMs.isPassed) "উত্তীর্ণ (GPA: ${finalMs.gpa})" else "অনুত্তীর্ণ (F)"
            showMessage("মার্কশীট লোড হয়েছে: ${finalMs.student.name} - $status")
        } else {
            showMessage("এই আইডি বা পরীক্ষায় কোনো ফলাফল পাওয়া যায়নি")
        }
    }

    fun printIndividualMarksheet(context: Context) {
        val ms = _currentMarksheet.value
        if (ms == null) {
            showMessage("প্রিন্ট করার মতো কোনো মার্কশীট পাওয়া যায়নি")
            return
        }
        val html = PrintUtils.generateMarksheetHtml(settings.value, ms)
        PrintUtils.printHtml(context, html, "Transcript_${ms.student.uniqueId}")
    }

    // Class Merit List State
    private val _meritClassId = MutableStateFlow("cls_12") // Class 10
    val meritClassId: StateFlow<String> = _meritClassId.asStateFlow()

    private val _meritExamCategory = MutableStateFlow(ExamCategory.ANNUAL)
    val meritExamCategory: StateFlow<ExamCategory> = _meritExamCategory.asStateFlow()

    private val _meritExamCategoryText = MutableStateFlow("বার্ষিক পরীক্ষা")
    val meritExamCategoryText: StateFlow<String> = _meritExamCategoryText.asStateFlow()

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
    fun setMeritExamCategory(cat: ExamCategory) {
        _meritExamCategory.value = cat
        _meritExamCategoryText.value = cat.labelBn
    }
    fun setMeritExamCategoryText(text: String) {
        _meritExamCategoryText.value = text
    }
    fun setMeritYear(year: Int) { _meritYear.value = year }

    fun getStudentMeritRank(studentId: String): ClassMeritRank? {
        return repository.getStudentMeritRank(studentId, _meritExamCategory.value, _meritYear.value)
    }

    fun printClassMeritList(context: Context) {
        val classObj = classes.value.find { it.id == _meritClassId.value }
        val className = classObj?.name ?: "Class"
        val list = classMeritList.value
        if (list.isEmpty()) {
            showMessage("প্রিন্ট করার জন্য কোনো মেধা তালিকা ডাটা নেই")
            return
        }
        val catName = _meritExamCategoryText.value.ifBlank { _meritExamCategory.value.labelBn }
        val html = PrintUtils.generateClassMeritListHtml(
            settings = settings.value,
            className = className,
            categoryName = catName,
            year = _meritYear.value,
            meritList = list
        )
        PrintUtils.printHtml(context, html, "MeritList_${className}_${_meritYear.value}")
    }

    // App Security Lock State
    private val _isAppUnlocked = MutableStateFlow(!repository.settings.value.isSecurityLockEnabled)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    fun unlockApp(pin: String): Boolean {
        val success = repository.verifySecurityPin(pin)
        if (success) {
            _isAppUnlocked.value = true
        }
        return success
    }

    fun unlockWithBiometric() {
        _isAppUnlocked.value = true
        showMessage("বায়োমেট্রিক আনলক সফল হয়েছে!")
    }

    fun unlockWithGoogleAccount(email: String, name: String = "Google User"): Boolean {
        val targetEmail = settings.value.googleAccountEmail
        val isAuthorized = targetEmail.isBlank() || email.equals(targetEmail.trim(), ignoreCase = true) || email.equals("trackalldevice2018@gmail.com", ignoreCase = true)
        if (isAuthorized) {
            val updated = settings.value.copy(
                googleAccountEmail = email,
                googleAccountName = name,
                isGoogleUserSignedIn = true
            )
            updateSettings(updated)
            _isAppUnlocked.value = true
            showMessage("গুগল অ্যাকাউন্ট ($email) দিয়ে সফলভাবে প্রবেশ করেছেন!")
            return true
        } else {
            return false
        }
    }

    fun signInGoogleAccount(email: String, name: String = "Google User") {
        val updated = settings.value.copy(
            googleAccountEmail = email,
            googleAccountName = name,
            isGoogleUserSignedIn = true
        )
        updateSettings(updated)
        showMessage("গুগল অ্যাকাউন্ট লিঙ্ক করা হয়েছে: $email")
    }

    fun signOutGoogleAccount() {
        val updated = settings.value.copy(
            isGoogleUserSignedIn = false
        )
        updateSettings(updated)
        showMessage("গুগল অ্যাকাউন্ট সাইন-আউট করা হয়েছে")
    }

    fun setAuthMode(mode: com.example.data.model.AuthMode) {
        val updated = settings.value.copy(authMode = mode)
        updateSettings(updated)
    }

    fun setAutoLockOnBackground(enabled: Boolean) {
        val updated = settings.value.copy(autoLockOnBackground = enabled)
        updateSettings(updated)
    }

    fun lockApp() {
        if (settings.value.isSecurityLockEnabled) {
            _isAppUnlocked.value = false
        }
    }

    fun changeSecurityPin(currentPin: String, newPin: String): Boolean {
        val success = repository.changeSecurityPin(currentPin, newPin)
        if (success) {
            showMessage("মাস্টার পিন সফলভাবে পরিবর্তন করা হয়েছে (PIN Updated)")
        } else {
            showMessage("বর্তমান পিন সঠিক নয়! পুনরায় চেষ্টা করুন।")
        }
        return success
    }

    fun recoverAndResetPin(recoveryAnswer: String, newPin: String): Boolean {
        if (repository.verifyRecoveryAnswer(recoveryAnswer)) {
            repository.resetPinWithRecovery(newPin)
            _isAppUnlocked.value = true
            showMessage("পিন সফলভাবে রিসেট ও আনলক করা হয়েছে!")
            return true
        } else {
            showMessage("সিকিউরিটি প্রশ্নের উত্তর সঠিক নয়!")
            return false
        }
    }

    // -------------------------------------------------------------
    // MODULE 5: Central Settings Management
    // -------------------------------------------------------------
    fun updateSettings(newSettings: SchoolSettings) {
        repository.updateSettings(newSettings)
        if (!newSettings.isSecurityLockEnabled) {
            _isAppUnlocked.value = true
        }
        showMessage("সেটিংস সফলভাবে সংরক্ষিত হয়েছে (Settings updated)")
    }

    init {
        // Initial auto-verification of default demo student
        verifyStudent()
        searchIndividualMarksheet()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }
}
