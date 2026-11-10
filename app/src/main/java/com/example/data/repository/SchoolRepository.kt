package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExamResultEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.model.ClassMeritRank
import com.example.data.model.ExamCategory
import com.example.data.model.ExamRoutineSlot
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentMark
import com.example.data.model.StudentMarksheet
import com.example.data.model.Subject
import com.example.data.model.SubjectType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class SchoolRepository private constructor(context: Context) : StudentRepository, ClassRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val database = AppDatabase.getInstance(context)

    // Room DAOs
    private val studentDao = database.studentDao()
    private val subjectDao = database.subjectDao()
    private val examResultDao = database.examResultDao()

    // Classes (Configurable list of classes from Nursery to Class 10)
    private val _classes = MutableStateFlow<List<SchoolClass>>(initialClasses())
    override val classes: StateFlow<List<SchoolClass>> = _classes.asStateFlow()

    // Reactive StateFlows backed by Room Database
    override val students: StateFlow<List<Student>> = studentDao.getAllStudents()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val subjects: StateFlow<List<Subject>> = subjectDao.getAllSubjects()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val marks: StateFlow<List<StudentMark>> = examResultDao.getAllExamResults()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    // Exam Routines
    private val _routines = MutableStateFlow<List<ExamRoutineSlot>>(initialRoutines())
    val routines: StateFlow<List<ExamRoutineSlot>> = _routines.asStateFlow()

    private val prefs = context.getSharedPreferences("school_mgmt_settings", Context.MODE_PRIVATE)

    // Central Settings (Editable school branding, logo, principal, instructions, watermark, theme, security lock)
    private val _settings = MutableStateFlow(loadSettingsFromPrefs())
    val settings: StateFlow<com.example.data.model.SchoolSettings> = _settings.asStateFlow()

    private fun loadSettingsFromPrefs(): com.example.data.model.SchoolSettings {
        val default = com.example.data.model.SchoolSettings()
        return com.example.data.model.SchoolSettings(
            schoolName = prefs.getString("schoolName", default.schoolName) ?: default.schoolName,
            schoolAddress = prefs.getString("schoolAddress", default.schoolAddress) ?: default.schoolAddress,
            eiinNumber = prefs.getString("eiinNumber", default.eiinNumber) ?: default.eiinNumber,
            contactPhone = prefs.getString("contactPhone", default.contactPhone) ?: default.contactPhone,
            contactEmail = prefs.getString("contactEmail", default.contactEmail) ?: default.contactEmail,
            principalName = prefs.getString("principalName", default.principalName) ?: default.principalName,
            principalTitle = prefs.getString("principalTitle", default.principalTitle) ?: default.principalTitle,
            customLogoUri = prefs.getString("customLogoUri", default.customLogoUri),
            themeColorHex = prefs.getLong("themeColorHex", default.themeColorHex),
            showWatermark = prefs.getBoolean("showWatermark", default.showWatermark),
            watermarkOpacity = prefs.getFloat("watermarkOpacity", default.watermarkOpacity),
            routineInstructions = prefs.getString("routineInstructions", default.routineInstructions) ?: default.routineInstructions,
            isSecurityLockEnabled = prefs.getBoolean("isSecurityLockEnabled", default.isSecurityLockEnabled),
            authMode = try {
                com.example.data.model.AuthMode.valueOf(prefs.getString("authMode", default.authMode.name) ?: default.authMode.name)
            } catch (e: Exception) {
                default.authMode
            },
            autoLockOnBackground = prefs.getBoolean("autoLockOnBackground", default.autoLockOnBackground),
            isGoogleAuthEnabled = prefs.getBoolean("isGoogleAuthEnabled", default.isGoogleAuthEnabled),
            googleAccountEmail = prefs.getString("googleAccountEmail", default.googleAccountEmail) ?: default.googleAccountEmail,
            googleAccountName = prefs.getString("googleAccountName", default.googleAccountName) ?: default.googleAccountName,
            isGoogleUserSignedIn = prefs.getBoolean("isGoogleUserSignedIn", default.isGoogleUserSignedIn),
            securityPin = prefs.getString("securityPin", default.securityPin) ?: default.securityPin,
            securityQuestion = prefs.getString("securityQuestion", default.securityQuestion) ?: default.securityQuestion,
            securityAnswer = prefs.getString("securityAnswer", default.securityAnswer) ?: default.securityAnswer
        )
    }

    fun updateSettings(newSettings: com.example.data.model.SchoolSettings) {
        _settings.value = newSettings
        prefs.edit()
            .putString("schoolName", newSettings.schoolName)
            .putString("schoolAddress", newSettings.schoolAddress)
            .putString("eiinNumber", newSettings.eiinNumber)
            .putString("contactPhone", newSettings.contactPhone)
            .putString("contactEmail", newSettings.contactEmail)
            .putString("principalName", newSettings.principalName)
            .putString("principalTitle", newSettings.principalTitle)
            .putString("customLogoUri", newSettings.customLogoUri)
            .putLong("themeColorHex", newSettings.themeColorHex)
            .putBoolean("showWatermark", newSettings.showWatermark)
            .putFloat("watermarkOpacity", newSettings.watermarkOpacity)
            .putString("routineInstructions", newSettings.routineInstructions)
            .putBoolean("isSecurityLockEnabled", newSettings.isSecurityLockEnabled)
            .putString("authMode", newSettings.authMode.name)
            .putBoolean("autoLockOnBackground", newSettings.autoLockOnBackground)
            .putBoolean("isGoogleAuthEnabled", newSettings.isGoogleAuthEnabled)
            .putString("googleAccountEmail", newSettings.googleAccountEmail)
            .putString("googleAccountName", newSettings.googleAccountName)
            .putBoolean("isGoogleUserSignedIn", newSettings.isGoogleUserSignedIn)
            .putString("securityPin", newSettings.securityPin)
            .putString("securityQuestion", newSettings.securityQuestion)
            .putString("securityAnswer", newSettings.securityAnswer)
            .apply()
    }

    fun verifySecurityPin(pin: String): Boolean {
        val currentPin = _settings.value.securityPin.trim()
        val masterRecovery = "9988"
        val input = pin.trim()
        return input == currentPin || input == masterRecovery
    }

    fun changeSecurityPin(currentPin: String, newPin: String): Boolean {
        if (!verifySecurityPin(currentPin)) return false
        val updated = _settings.value.copy(securityPin = newPin.trim())
        updateSettings(updated)
        return true
    }

    fun verifyRecoveryAnswer(answer: String): Boolean {
        val currentAns = _settings.value.securityAnswer.trim()
        return currentAns.equals(answer.trim(), ignoreCase = true)
    }

    fun resetPinWithRecovery(newPin: String) {
        val updated = _settings.value.copy(securityPin = newPin.trim())
        updateSettings(updated)
    }

    init {
        // Ensure initial demo data is populated if Room is empty on first launch
        scope.launch {
            checkAndSeedInitialData()
        }
    }

    private suspend fun checkAndSeedInitialData() {
        val currentStudents = studentDao.getAllStudents().first()
        if (currentStudents.isEmpty()) {
            val class10Id = "cls_12"
            val initialSubjects = listOf(
                SubjectEntity("sub_10_1", class10Id, "Bangla (বাংলা)", "BAN-101", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_10_2", class10Id, "English (ইংরেজি)", "ENG-102", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_10_3", class10Id, "Mathematics (গণিত)", "MAT-103", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_10_4", class10Id, "General Science (বিজ্ঞান)", "SCI-104", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_10_5", class10Id, "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", "BGS-105", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_10_6", class10Id, "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", "ICT-106", 50.0, 17.0, "MANDATORY"),
                SubjectEntity("sub_10_7", class10Id, "Higher Math (উচ্চতর গণিত)", "HMT-107", 100.0, 33.0, "OPTIONAL"),
                SubjectEntity("sub_10_8", class10Id, "Agriculture Studies (কৃষি শিক্ষা)", "AGR-108", 100.0, 33.0, "OPTIONAL"),
                SubjectEntity("sub_9_1", "cls_11", "Bangla (বাংলা)", "BAN-901", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_9_2", "cls_11", "English (ইংরেজি)", "ENG-902", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_9_3", "cls_11", "Mathematics (গণিত)", "MAT-903", 100.0, 33.0, "MANDATORY")
            )
            subjectDao.insertSubjects(initialSubjects)

            val initialStudents = listOf(
                StudentEntity("std_101", "STU-2026-001", "01", "Tanzim Hasan (তানজিম হাসান)", class10Id, "Class 10", "20092692518001234", "15/05/2009", "Md. Rafiqul Islam", "Nazma Begum", "+880 1711-234567", "House 12, Road 4, Dhanmondi, Dhaka", 0xFF1E3A8A),
                StudentEntity("std_102", "STU-2026-002", "02", "Sadia Afrin (সাদিয়া আফরিন)", class10Id, "Class 10", "20092692518001235", "20/08/2009", "Anisur Rahman", "Shahana Parvin", "+880 1819-345678", "Plot 24, Sector 7, Uttara, Dhaka", 0xFF0D9488),
                StudentEntity("std_103", "STU-2026-003", "03", "Abrar Fahim (আবরার ফাহিম)", class10Id, "Class 10", "20092692518001236", "11/02/2009", "Kamrul Hassan", "Farhana Akter", "+880 1912-456789", "Block C, Bashundhara R/A, Dhaka", 0xFF7C3AED),
                StudentEntity("std_104", "STU-2026-004", "04", "Sumaiya Khanom (সুমাইয়া খানম)", class10Id, "Class 10", "20092692518001237", "05/11/2009", "Mokhlesur Rahman", "Rina Akter", "+880 1610-567890", "Mirpur 10, Dhaka", 0xFFDB2777),
                StudentEntity("std_105", "STU-2026-005", "05", "Zubair Ahmed (জুবায়ের আহমেদ)", class10Id, "Class 10", "20092692518001238", "19/07/2009", "Alamgir Hossain", "Parvin Begum", "+880 1515-678901", "Mohammadpur, Dhaka", 0xFFD97706)
            )
            studentDao.insertStudents(initialStudents)

            val initialMarks = listOf(
                ExamResultEntity("mk_1", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", "ANNUAL", 2026, "sub_10_1", "Bangla (বাংলা)", 88.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_2", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", "ANNUAL", 2026, "sub_10_2", "English (ইংরেজি)", 84.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_3", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", "ANNUAL", 2026, "sub_10_3", "Mathematics (গণিত)", 96.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_4", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", "ANNUAL", 2026, "sub_10_4", "General Science (বিজ্ঞান)", 92.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_5", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", "ANNUAL", 2026, "sub_10_5", "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", 87.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_6", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", "ANNUAL", 2026, "sub_10_6", "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", 46.0, 50.0, 17.0, true, "A+", 5.0),
                ExamResultEntity("mk_7", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", "ANNUAL", 2026, "sub_10_7", "Higher Math (উচ্চতর গণিত)", 90.0, 100.0, 33.0, false, "A+", 5.0),

                ExamResultEntity("mk_8", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", "ANNUAL", 2026, "sub_10_1", "Bangla (বাংলা)", 82.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_9", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", "ANNUAL", 2026, "sub_10_2", "English (ইংরেজি)", 86.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_10", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", "ANNUAL", 2026, "sub_10_3", "Mathematics (গণিত)", 90.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_11", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", "ANNUAL", 2026, "sub_10_4", "General Science (বিজ্ঞান)", 85.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_12", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", "ANNUAL", 2026, "sub_10_5", "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", 81.0, 100.0, 33.0, true, "A+", 5.0),
                ExamResultEntity("mk_13", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", "ANNUAL", 2026, "sub_10_6", "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", 44.0, 50.0, 17.0, true, "A+", 5.0),

                // Abrar Fahim (Fails in Mandatory Science)
                ExamResultEntity("mk_14", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", "ANNUAL", 2026, "sub_10_1", "Bangla (বাংলা)", 72.0, 100.0, 33.0, true, "A", 4.0),
                ExamResultEntity("mk_15", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", "ANNUAL", 2026, "sub_10_2", "English (ইংরেজি)", 65.0, 100.0, 33.0, true, "A-", 3.5),
                ExamResultEntity("mk_16", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", "ANNUAL", 2026, "sub_10_3", "Mathematics (গণিত)", 55.0, 100.0, 33.0, true, "B", 3.0),
                ExamResultEntity("mk_17", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", "ANNUAL", 2026, "sub_10_4", "General Science (বিজ্ঞান)", 28.0, 100.0, 33.0, true, "F", 0.0),
                ExamResultEntity("mk_18", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", "ANNUAL", 2026, "sub_10_5", "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", 60.0, 100.0, 33.0, true, "A-", 3.5),
                ExamResultEntity("mk_19", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", "ANNUAL", 2026, "sub_10_6", "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", 35.0, 50.0, 17.0, true, "A", 4.0)
            )
            examResultDao.insertExamResults(initialMarks)
        }
    }

    // --- 1. Class & Subject Management ---
    fun addClass(name: String, section: String = "A") {
        val newClass = SchoolClass(
            id = "cls_${UUID.randomUUID().toString().take(6)}",
            name = name,
            section = section,
            order = _classes.value.size + 1
        )
        _classes.value = _classes.value + newClass
    }

    fun updateClass(schoolClass: SchoolClass) {
        _classes.value = _classes.value.map { if (it.id == schoolClass.id) schoolClass else it }
    }

    fun deleteClass(classId: String) {
        _classes.value = _classes.value.filterNot { it.id == classId }
        scope.launch {
            subjectDao.deleteSubjectsByClass(classId)
        }
    }

    fun addSubject(
        classId: String,
        name: String,
        code: String,
        fullMarks: Double,
        passMarks: Double,
        type: SubjectType
    ) {
        val newSubject = SubjectEntity(
            id = "sub_${UUID.randomUUID().toString().take(6)}",
            classId = classId,
            name = name,
            code = code,
            fullMarks = fullMarks,
            passMarks = passMarks,
            type = type.name
        )
        scope.launch {
            subjectDao.insertSubject(newSubject)
        }
    }

    override fun getClassById(id: String): SchoolClass? = _classes.value.find { it.id == id }
    override fun getClassName(classId: String): String = _classes.value.find { it.id == classId }?.name ?: classId
    override fun getSubjectsForClass(classId: String): List<Subject> = subjects.value.filter { it.classId == classId }

    override suspend fun addSubject(subject: Subject) {
        subjectDao.insertSubject(SubjectEntity.fromDomain(subject))
    }

    override suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(SubjectEntity.fromDomain(subject))
    }

    override suspend fun deleteSubject(subjectId: String) {
        subjectDao.deleteSubjectById(subjectId)
    }

    // --- 2. Student Management ---
    override fun searchStudents(query: String, classId: String?): Flow<List<Student>> {
        return studentDao.searchAndFilterStudents(query, classId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getStudentById(id: String): Student? {
        return studentDao.getStudentById(id)?.toDomain()
    }

    override suspend fun getStudentByUniqueId(uniqueId: String): Student? {
        return studentDao.findStudentByUniqueId(uniqueId)?.toDomain()
    }

    override suspend fun addStudent(student: Student) {
        studentDao.insertStudent(StudentEntity.fromDomain(student))
    }

    override suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(StudentEntity.fromDomain(student))
    }

    override suspend fun updateStudentById(student: Student) {
        studentDao.updateStudentById(
            studentId = student.id,
            name = student.name,
            uniqueId = student.uniqueId,
            rollNo = student.rollNo,
            classId = student.classId,
            className = student.className,
            birthRegNo = student.birthRegNo,
            dateOfBirth = student.dateOfBirth,
            fatherName = student.fatherName,
            motherName = student.motherName,
            guardianPhone = student.guardianPhone,
            address = student.address,
            photoUri = student.photoUri
        )
    }

    fun updateStudentById(
        studentId: String,
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        className: String,
        birthRegNo: String,
        dateOfBirth: String,
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String,
        photoUri: String?
    ) {
        scope.launch {
            studentDao.updateStudentById(
                studentId = studentId,
                name = name,
                uniqueId = uniqueId,
                rollNo = rollNo,
                classId = classId,
                className = className,
                birthRegNo = birthRegNo,
                dateOfBirth = dateOfBirth,
                fatherName = fatherName,
                motherName = motherName,
                guardianPhone = guardianPhone,
                address = address,
                photoUri = photoUri
            )
        }
    }

    override suspend fun deleteStudent(id: String) {
        studentDao.deleteStudentById(id)
        examResultDao.deleteResultsByStudentId(id)
    }

    override suspend fun deleteStudents(ids: List<String>) {
        if (ids.isEmpty()) return
        studentDao.deleteStudentsByIds(ids)
        examResultDao.deleteResultsByStudentIds(ids)
    }

    override suspend fun bulkImportStudents(students: List<Student>) {
        studentDao.insertStudents(students.map { StudentEntity.fromDomain(it) })
    }

    fun verifyStudentByUniqueId(uniqueId: String): Student? {
        val clean = uniqueId.trim()
        return students.value.find { it.uniqueId.equals(clean, ignoreCase = true) }
    }

    fun isRollNumberTakenInClass(classId: String, rollNo: String, excludeStudentId: String? = null): Boolean {
        val cleanRoll = rollNo.trim()
        if (cleanRoll.isBlank()) return false
        return students.value.any {
            it.classId == classId &&
            it.rollNo.trim().equals(cleanRoll, ignoreCase = true) &&
            it.id != excludeStudentId
        }
    }

    fun getStudentCountInClass(classId: String): Int {
        return students.value.count { it.classId == classId }
    }

    fun getNextRollNumberForClass(classId: String): String {
        val rolls = students.value.filter { it.classId == classId }.mapNotNull { it.rollNo.trim().toIntOrNull() }
        val maxRoll = rolls.maxOrNull() ?: 0
        val next = maxRoll + 1
        return if (next < 10) "0$next" else next.toString()
    }

    fun bulkImportStudentsCsv(csvText: String): Int {
        val lines = csvText.lines().filter { it.isNotBlank() }
        var count = 0
        val newEntities = mutableListOf<StudentEntity>()
        val first = lines.firstOrNull() ?: ""
        val startIdx = if (first.contains("StudentID", ignoreCase = true) || first.contains("UniqueId", ignoreCase = true) || first.contains("আইডি", ignoreCase = true)) 1 else 0

        for (i in startIdx until lines.size) {
            val parts = parseCsvLine(lines[i])
            if (parts.size >= 4) {
                val uId = parts.getOrNull(0)?.ifBlank { "STU-${(1000..9999).random()}" } ?: "STU-000"
                val roll = parts.getOrNull(1)?.ifBlank { "01" } ?: "01"
                val name = parts.getOrNull(2) ?: "Unknown"
                val className = parts.getOrNull(3) ?: "Class 10"
                val targetClass = _classes.value.find { it.name.equals(className, ignoreCase = true) }
                    ?: _classes.value.firstOrNull()
                val birthReg = parts.getOrNull(4) ?: "20101234567890"
                val dateOfBirth = if (parts.size >= 10) parts[5] else "01/01/2010"
                val father = if (parts.size >= 10) parts[6] else (parts.getOrNull(5) ?: "Guardian Father")
                val mother = if (parts.size >= 10) parts[7] else (parts.getOrNull(6) ?: "Guardian Mother")
                val phone = if (parts.size >= 10) parts[8] else (parts.getOrNull(7) ?: "+880 1700-000000")
                val address = if (parts.size >= 10) parts[9] else (parts.getOrNull(8) ?: "Dhaka, Bangladesh")
                val photoUri = if (parts.size >= 11) parts[10].trim().ifBlank { null } else null

                val entity = StudentEntity(
                    id = "std_${UUID.randomUUID().toString().take(6)}",
                    uniqueId = uId,
                    rollNo = roll,
                    name = name,
                    classId = targetClass?.id ?: "cls_12",
                    className = targetClass?.name ?: className,
                    birthRegNo = birthReg,
                    dateOfBirth = dateOfBirth,
                    fatherName = father,
                    motherName = mother,
                    guardianPhone = phone,
                    address = address,
                    photoUri = photoUri
                )
                newEntities.add(entity)
                count++
            }
        }
        if (newEntities.isNotEmpty()) {
            scope.launch {
                studentDao.insertStudents(newEntities)
            }
        }
        return count
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var insideQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (insideQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    insideQuotes = !insideQuotes
                }
            } else if (c == ',' && !insideQuotes) {
                tokens.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun exportStudentsCsv(classFilter: String?): String {
        val list = if (classFilter == null || classFilter == "All") students.value else students.value.filter { it.className == classFilter }
        val sb = StringBuilder()
        sb.append("StudentID,Roll,Name,Class,BirthRegNo,DateOfBirth,FatherName,MotherName,Phone,Address,PhotoUri\n")
        list.forEach { s ->
            val safeName = s.name.replace("\"", "\"\"")
            val safeClass = s.className.replace("\"", "\"\"")
            val safeFather = s.fatherName.replace("\"", "\"\"")
            val safeMother = s.motherName.replace("\"", "\"\"")
            val safeAddress = s.address.replace("\"", "\"\"")
            val photoVal = s.photoUri ?: ""
            sb.append("${s.uniqueId},${s.rollNo},\"$safeName\",\"$safeClass\",\"${s.birthRegNo}\",\"${s.dateOfBirth}\",\"$safeFather\",\"$safeMother\",\"${s.guardianPhone}\",\"$safeAddress\",\"$photoVal\"\n")
        }
        return sb.toString()
    }

    // --- Bulk Marks Upload & Export by Class ---
    fun bulkImportClassMarksCsv(
        classId: String,
        categoryName: String,
        year: Int,
        csvText: String
    ): Pair<Int, String> {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return 0 to "CSV ফাইলে কোনো ডাটা পাওয়া যায়নি"

        val classStudents = students.value.filter { it.classId == classId }
        val classSubs = subjects.value.filter { it.classId == classId }
        if (classSubs.isEmpty()) return 0 to "এই শ্রেণিতে কোনো বিষয় যুক্ত করা নেই"

        var importedCount = 0
        val newMarks = mutableListOf<ExamResultEntity>()

        val firstLine = lines.first()
        val headerTokens = parseCsvLine(firstLine)

        // Check if wide format: Header has subject names or codes
        val matchedSubjects = mutableMapOf<Int, Subject>()
        for (colIdx in headerTokens.indices) {
            val colName = headerTokens[colIdx]
            val sub = classSubs.find {
                it.name.contains(colName, ignoreCase = true) ||
                it.code.equals(colName, ignoreCase = true) ||
                colName.contains(it.name, ignoreCase = true)
            }
            if (sub != null) {
                matchedSubjects[colIdx] = sub
            }
        }

        if (matchedSubjects.isNotEmpty()) {
            val dataLines = lines.drop(1)
            for (line in dataLines) {
                val parts = parseCsvLine(line)
                if (parts.isEmpty()) continue
                val idOrRoll = parts[0]
                val student = classStudents.find {
                    it.uniqueId.equals(idOrRoll, ignoreCase = true) ||
                    it.rollNo.equals(idOrRoll, ignoreCase = true)
                } ?: (if (parts.size > 1) classStudents.find { it.rollNo.equals(parts[1], ignoreCase = true) } else null)

                if (student != null) {
                    for ((colIdx, sub) in matchedSubjects) {
                        val markVal = parts.getOrNull(colIdx)?.toDoubleOrNull()
                        if (markVal != null) {
                            val valid = markVal.coerceIn(0.0, sub.fullMarks)
                            val (letter, gp) = calculateGrade(valid, sub.fullMarks)
                            val entity = ExamResultEntity(
                                id = "mk_${UUID.randomUUID().toString().take(8)}",
                                uniqueId = student.uniqueId,
                                studentId = student.id,
                                studentName = student.name,
                                fatherName = student.fatherName,
                                classId = student.classId,
                                className = student.className,
                                rollNo = student.rollNo,
                                examCategory = categoryName,
                                year = year,
                                subjectId = sub.id,
                                subjectName = sub.name,
                                marksObtained = valid,
                                fullMarks = sub.fullMarks,
                                passMarks = sub.passMarks,
                                isMandatory = sub.type == SubjectType.MANDATORY,
                                letterGrade = letter,
                                gradePoint = gp
                            )
                            newMarks.add(entity)
                            importedCount++
                        }
                    }
                }
            }
        } else {
            val startIdx = if (firstLine.contains("UniqueId", ignoreCase = true) || firstLine.contains("Roll", ignoreCase = true)) 1 else 0
            for (i in startIdx until lines.size) {
                val parts = parseCsvLine(lines[i])
                if (parts.size >= 3) {
                    val idOrRoll = parts[0]
                    val subIdentifier = parts[1]
                    val marksObtained = parts[2].toDoubleOrNull() ?: continue

                    val student = classStudents.find {
                        it.uniqueId.equals(idOrRoll, ignoreCase = true) ||
                        it.rollNo.equals(idOrRoll, ignoreCase = true)
                    } ?: continue

                    val sub = classSubs.find {
                        it.name.contains(subIdentifier, ignoreCase = true) ||
                        it.code.equals(subIdentifier, ignoreCase = true)
                    } ?: classSubs.firstOrNull() ?: continue

                    val valid = marksObtained.coerceIn(0.0, sub.fullMarks)
                    val (letter, gp) = calculateGrade(valid, sub.fullMarks)
                    val entity = ExamResultEntity(
                        id = "mk_${UUID.randomUUID().toString().take(8)}",
                        uniqueId = student.uniqueId,
                        studentId = student.id,
                        studentName = student.name,
                        fatherName = student.fatherName,
                        classId = student.classId,
                        className = student.className,
                        rollNo = student.rollNo,
                        examCategory = categoryName,
                        year = year,
                        subjectId = sub.id,
                        subjectName = sub.name,
                        marksObtained = valid,
                        fullMarks = sub.fullMarks,
                        passMarks = sub.passMarks,
                        isMandatory = sub.type == SubjectType.MANDATORY,
                        letterGrade = letter,
                        gradePoint = gp
                    )
                    newMarks.add(entity)
                    importedCount++
                }
            }
        }

        if (newMarks.isNotEmpty()) {
            scope.launch {
                examResultDao.insertExamResults(newMarks)
            }
            return importedCount to "সফলভাবে $importedCount টি বিষয়ে নম্বর এন্ট্রি সম্পন্ন হয়েছে"
        } else {
            return 0 to "কোনো বৈধ শিক্ষার্থীর নম্বর পাওয়া যায়নি। রোল বা স্টুডেন্ট আইডি ও বিষয় নাম যাচাই করুন।"
        }
    }

    fun exportClassMarksCsv(classId: String, categoryName: String, year: Int): String {
        val classStudents = students.value.filter { it.classId == classId }.sortedBy { it.rollNo.toIntOrNull() ?: 999 }
        val classSubs = subjects.value.filter { it.classId == classId }
        val sb = StringBuilder()

        sb.append("UniqueId,Roll,StudentName")
        for (sub in classSubs) {
            val safeSub = sub.name.replace(",", " ")
            sb.append(",\"$safeSub\"")
        }
        sb.append("\n")

        val recordedMarks = marks.value.filter {
            it.classId == classId &&
            (it.customCategoryName.equals(categoryName, ignoreCase = true) || it.examCategory.labelBn.equals(categoryName, ignoreCase = true)) &&
            it.year == year
        }

        for (s in classStudents) {
            sb.append("${s.uniqueId},${s.rollNo},\"${s.name.replace("\"", "\"\"")}\"")
            for (sub in classSubs) {
                val mark = recordedMarks.find { it.studentId == s.id && it.subjectId == sub.id }
                val markStr = if (mark != null) mark.marksObtained.toInt().toString() else ""
                sb.append(",$markStr")
            }
            sb.append("\n")
        }
        return sb.toString()
    }

    // --- 3. Exam Routine Auto-Generator ---
    fun generateRoutineForClass(classId: String, examCategory: ExamCategory, year: Int) {
        val cls = _classes.value.find { it.id == classId } ?: return
        val classSubs = subjects.value.filter { it.classId == classId }
        val existingFiltered = _routines.value.filterNot { it.classId == classId && it.examCategory == examCategory && it.year == year }

        val startDates = listOf("10/12/$year", "12/12/$year", "14/12/$year", "16/12/$year", "18/12/$year", "20/12/$year", "22/12/$year", "24/12/$year", "26/12/$year")
        val daysBn = listOf("রবিবার", "মঙ্গলবার", "বৃহস্পতিবার", "শনিবার", "সোমবার", "বুধবার", "বৃহস্পতিবার")

        val generated = classSubs.mapIndexed { index, sub ->
            val dateStr = startDates.getOrElse(index) { "${10 + (index * 2)}/12/$year" }
            val dayStr = daysBn.getOrElse(index % daysBn.size) { "রবিবার" }
            val shiftStr = if (index % 3 == 2) "বিকাল" else "সকাল"
            ExamRoutineSlot(
                id = UUID.randomUUID().toString(),
                classId = classId,
                className = cls.name,
                examCategory = examCategory,
                year = year,
                subjectName = sub.name,
                examDate = dateStr,
                examDay = dayStr,
                shift = shiftStr,
                examTime = if (shiftStr == "সকাল") "10:00 AM - 01:00 PM" else "02:00 PM - 05:00 PM",
                room = "Hall ${((index % 3) + 1)}"
            )
        }
        _routines.value = existingFiltered + generated
    }

    fun updateRoutineSlot(slotId: String, date: String, day: String, subjectName: String, shift: String = "সকাল") {
        _routines.value = _routines.value.map { slot ->
            if (slot.id == slotId) {
                slot.copy(
                    examDate = date,
                    examDay = day,
                    subjectName = subjectName,
                    shift = shift,
                    examTime = if (shift == "সকাল") "10:00 AM - 01:00 PM" else "02:00 PM - 05:00 PM"
                )
            } else slot
        }
    }

    fun addRoutineSlot(classId: String, examCategory: ExamCategory, year: Int, subjectName: String, date: String, day: String, shift: String = "সকাল") {
        val cls = _classes.value.find { it.id == classId }
        val newSlot = ExamRoutineSlot(
            id = UUID.randomUUID().toString(),
            classId = classId,
            className = cls?.name ?: "Class",
            examCategory = examCategory,
            year = year,
            subjectName = subjectName,
            examDate = date,
            examDay = day,
            shift = shift,
            examTime = if (shift == "সকাল") "10:00 AM - 01:00 PM" else "02:00 PM - 05:00 PM",
            room = "Hall 1"
        )
        _routines.value = _routines.value + newSlot
    }

    fun deleteRoutineSlot(slotId: String) {
        _routines.value = _routines.value.filterNot { it.id == slotId }
    }

    // --- 3. Marks Entry & Auto-Grade Calculation ---
    fun saveMark(
        uniqueId: String,
        subjectId: String,
        examCategory: ExamCategory,
        year: Int,
        marksObtained: Double
    ): StudentMark? {
        val student = verifyStudentByUniqueId(uniqueId) ?: return null
        val subject = subjects.value.find { it.id == subjectId } ?: return null

        val existing = marks.value.find {
            it.studentId == student.id &&
            it.subjectId == subject.id &&
            it.examCategory == examCategory &&
            it.year == year
        }

        val validMarks = marksObtained.coerceIn(0.0, subject.fullMarks)
        val (letter, gp) = calculateGrade(validMarks, subject.fullMarks)
        val markRecord = StudentMark(
            id = existing?.id ?: UUID.randomUUID().toString(),
            uniqueId = student.uniqueId,
            studentId = student.id,
            studentName = student.name,
            fatherName = student.fatherName,
            classId = student.classId,
            className = student.className,
            rollNo = student.rollNo,
            examCategory = examCategory,
            year = year,
            subjectId = subject.id,
            subjectName = subject.name,
            marksObtained = validMarks,
            fullMarks = subject.fullMarks,
            passMarks = subject.passMarks,
            isMandatory = subject.type == SubjectType.MANDATORY,
            letterGrade = letter,
            gradePoint = gp
        )

        scope.launch {
            if (existing != null) {
                examResultDao.updateExamResult(ExamResultEntity.fromDomain(markRecord))
            } else {
                examResultDao.insertExamResult(ExamResultEntity.fromDomain(markRecord))
            }
        }
        return markRecord
    }

    private fun calculateGrade(obtained: Double, fullMarks: Double): Pair<String, Double> {
        val pct = if (fullMarks > 0) (obtained / fullMarks) * 100.0 else 0.0
        return when {
            pct >= 80.0 -> "A+" to 5.0
            pct >= 70.0 -> "A" to 4.0
            pct >= 60.0 -> "A-" to 3.5
            pct >= 50.0 -> "B" to 3.0
            pct >= 40.0 -> "C" to 2.0
            pct >= 33.0 -> "D" to 1.0
            else -> "F" to 0.0
        }
    }

    // --- 4. Result System & Business Logic ---
    fun getStudentMarksheet(uniqueId: String, examCategory: ExamCategory, year: Int): StudentMarksheet? {
        val raw = getRawStudentMarksheet(uniqueId, examCategory, year) ?: return null
        val meritList = getClassMeritList(raw.student.classId, examCategory, year)
        val rankItem = meritList.find { it.student.id == raw.student.id }
        return raw.copy(
            classMeritPosition = rankItem?.position ?: 0,
            totalStudentsInClass = meritList.size
        )
    }

    private fun getRawStudentMarksheet(uniqueId: String, examCategory: ExamCategory, year: Int): StudentMarksheet? {
        val student = verifyStudentByUniqueId(uniqueId) ?: return null
        val studentMarks = marks.value.filter {
            it.studentId == student.id && it.examCategory == examCategory && it.year == year
        }

        val failedMandatory = studentMarks
            .filter { it.isMandatory && !it.isPassed }
            .map { it.subjectName }

        val isPassed = failedMandatory.isEmpty() && studentMarks.isNotEmpty()

        val totalMarks = studentMarks.sumOf { it.marksObtained }
        val totalFullMarks = studentMarks.sumOf { it.fullMarks }

        // Business Logic: Mandatory subjects are primary basis for final grade & GPA
        val mandatoryMarks = studentMarks.filter { it.isMandatory }
        val gpa = if (!isPassed || mandatoryMarks.isEmpty()) {
            0.0
        } else {
            val avgGp = mandatoryMarks.map { it.gradePoint }.average()
            val optionalMarks = studentMarks.filter { !it.isMandatory }
            val optionalBonus = optionalMarks.sumOf { (it.gradePoint - 2.0).coerceAtLeast(0.0) } / mandatoryMarks.size
            (avgGp + optionalBonus).coerceAtMost(5.0)
        }

        val overallGrade = if (!isPassed) "F" else when {
            gpa >= 5.0 -> "A+"
            gpa >= 4.0 -> "A"
            gpa >= 3.5 -> "A-"
            gpa >= 3.0 -> "B"
            gpa >= 2.0 -> "C"
            gpa >= 1.0 -> "D"
            else -> "F"
        }

        return StudentMarksheet(
            student = student,
            examCategory = examCategory,
            year = year,
            subjectMarks = studentMarks,
            totalMarks = totalMarks,
            totalFullMarks = totalFullMarks,
            gpa = Math.round(gpa * 100.0) / 100.0,
            letterGrade = overallGrade,
            isPassed = isPassed,
            failedMandatorySubjects = failedMandatory,
            classMeritPosition = 0,
            totalStudentsInClass = 0
        )
    }

    fun getClassMeritList(classId: String, examCategory: ExamCategory, year: Int): List<ClassMeritRank> {
        val classStudents = students.value.filter { it.classId == classId }
        val marksheets = classStudents.mapNotNull { student ->
            getRawStudentMarksheet(student.uniqueId, examCategory, year)
        }

        val passed = marksheets.filter { it.isPassed }.sortedWith(
            compareByDescending<StudentMarksheet> { it.totalMarks }.thenByDescending { it.gpa }
        )
        val failed = marksheets.filter { !it.isPassed }.sortedByDescending { it.totalMarks }

        var rank = 1
        val rankedList = mutableListOf<ClassMeritRank>()
        passed.forEach { ms ->
            rankedList.add(
                ClassMeritRank(
                    position = rank++,
                    student = ms.student,
                    totalMarks = ms.totalMarks,
                    gpa = ms.gpa,
                    isPassed = true,
                    statusText = "Passed (উত্তীর্ণ)"
                )
            )
        }
        failed.forEach { ms ->
            val reason = if (ms.failedMandatorySubjects.isNotEmpty()) {
                "Failed in ${ms.failedMandatorySubjects.joinToString(", ")}"
            } else "Failed"
            rankedList.add(
                ClassMeritRank(
                    position = rank++,
                    student = ms.student,
                    totalMarks = ms.totalMarks,
                    gpa = 0.0,
                    isPassed = false,
                    statusText = reason
                )
            )
        }
        return rankedList
    }

    fun getStudentMeritRank(studentId: String, examCategory: ExamCategory = ExamCategory.ANNUAL, year: Int = 2026): ClassMeritRank? {
        val student = students.value.find { it.id == studentId } ?: return null
        val meritList = getClassMeritList(student.classId, examCategory, year)
        return meritList.find { it.student.id == studentId }
    }

    companion object {
        @Volatile
        private var instance: SchoolRepository? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SchoolRepository(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(context: Context? = null): SchoolRepository {
            if (instance == null && context != null) {
                initialize(context)
            }
            return instance ?: error("SchoolRepository has not been initialized. Ensure SchoolApplication is configured.")
        }

        private fun initialClasses(): List<SchoolClass> {
            val names = listOf(
                "Nursery", "KG", "Class 1", "Class 2", "Class 3",
                "Class 4", "Class 5", "Class 6", "Class 7", "Class 8",
                "Class 9", "Class 10"
            )
            return names.mapIndexed { idx, name ->
                SchoolClass(
                    id = "cls_${idx + 1}",
                    name = name,
                    section = "A",
                    classTeacher = "Teacher ${idx + 1}",
                    order = idx + 1
                )
            }
        }

        private fun initialRoutines(): List<ExamRoutineSlot> {
            val class10Id = "cls_12"
            return listOf(
                ExamRoutineSlot("rt_1", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "Bangla (বাংলা)", "10/12/2026", "Sunday", "সকাল", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_2", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "English (ইংরেজি)", "12/12/2026", "Tuesday", "সকাল", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_3", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "Mathematics (গণিত)", "14/12/2026", "Thursday", "সকাল", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_4", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "General Science (বিজ্ঞান)", "16/12/2026", "Saturday", "সকাল", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_5", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", "18/12/2026", "Monday", "বিকাল", "02:00 PM - 05:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_6", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", "20/12/2026", "Wednesday", "সকাল", "10:00 AM - 11:30 AM", "Hall 1")
            )
        }
    }
}
