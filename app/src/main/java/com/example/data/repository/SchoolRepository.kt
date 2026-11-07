package com.example.data.repository

import com.example.data.model.ClassMeritRank
import com.example.data.model.ExamCategory
import com.example.data.model.ExamRoutineSlot
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentMark
import com.example.data.model.StudentMarksheet
import com.example.data.model.Subject
import com.example.data.model.SubjectType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class SchoolRepository private constructor() {

    private val _classes = MutableStateFlow<List<SchoolClass>>(initialClasses())
    val classes: StateFlow<List<SchoolClass>> = _classes.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(initialSubjects())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _students = MutableStateFlow<List<Student>>(initialStudents())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _routines = MutableStateFlow<List<ExamRoutineSlot>>(initialRoutines())
    val routines: StateFlow<List<ExamRoutineSlot>> = _routines.asStateFlow()

    private val _marks = MutableStateFlow<List<StudentMark>>(initialMarks())
    val marks: StateFlow<List<StudentMark>> = _marks.asStateFlow()

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
        _subjects.value = _subjects.value.filterNot { it.classId == classId }
    }

    fun addSubject(
        classId: String,
        name: String,
        code: String,
        fullMarks: Double,
        passMarks: Double,
        type: SubjectType
    ) {
        val newSubject = Subject(
            id = "sub_${UUID.randomUUID().toString().take(6)}",
            classId = classId,
            name = name,
            code = code,
            fullMarks = fullMarks,
            passMarks = passMarks,
            type = type
        )
        _subjects.value = _subjects.value + newSubject
    }

    fun updateSubject(subject: Subject) {
        _subjects.value = _subjects.value.map { if (it.id == subject.id) subject else it }
    }

    fun deleteSubject(subjectId: String) {
        _subjects.value = _subjects.value.filterNot { it.id == subjectId }
    }

    // --- 2. Student Management ---
    fun addStudent(student: Student) {
        _students.value = listOf(student) + _students.value
    }

    fun updateStudent(student: Student) {
        _students.value = _students.value.map { if (it.id == student.id) student else it }
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filterNot { it.id == studentId }
        _marks.value = _marks.value.filterNot { it.studentId == studentId }
    }

    fun verifyStudentByUniqueId(uniqueId: String): Student? {
        val clean = uniqueId.trim()
        return _students.value.find { it.uniqueId.equals(clean, ignoreCase = true) }
    }

    fun bulkImportStudentsCsv(csvText: String): Int {
        val lines = csvText.lines().filter { it.isNotBlank() }
        var count = 0
        val newStudents = mutableListOf<Student>()
        val startIdx = if (lines.firstOrNull()?.contains("UniqueId", ignoreCase = true) == true) 1 else 0

        for (i in startIdx until lines.size) {
            val parts = lines[i].split(",").map { it.trim() }
            if (parts.size >= 4) {
                val uId = parts.getOrNull(0)?.ifBlank { "STU-${(1000..9999).random()}" } ?: "STU-000"
                val roll = parts.getOrNull(1)?.ifBlank { "01" } ?: "01"
                val name = parts.getOrNull(2) ?: "Unknown"
                val className = parts.getOrNull(3) ?: "Class 10"
                val targetClass = _classes.value.find { it.name.equals(className, ignoreCase = true) }
                    ?: _classes.value.firstOrNull()
                val birthReg = parts.getOrNull(4) ?: "20101234567890"
                val father = parts.getOrNull(5) ?: "Guardian Father"
                val mother = parts.getOrNull(6) ?: "Guardian Mother"
                val phone = parts.getOrNull(7) ?: "+880 1700-000000"
                val address = parts.getOrNull(8) ?: "Dhaka, Bangladesh"

                val student = Student(
                    id = "std_${UUID.randomUUID().toString().take(6)}",
                    uniqueId = uId,
                    rollNo = roll,
                    name = name,
                    classId = targetClass?.id ?: "cls_10",
                    className = targetClass?.name ?: className,
                    birthRegNo = birthReg,
                    fatherName = father,
                    motherName = mother,
                    guardianPhone = phone,
                    address = address
                )
                newStudents.add(student)
                count++
            }
        }
        if (newStudents.isNotEmpty()) {
            _students.value = newStudents + _students.value
        }
        return count
    }

    fun exportStudentsCsv(classId: String?): String {
        val list = if (classId == null || classId == "All") _students.value else _students.value.filter { it.classId == classId }
        val sb = StringBuilder()
        sb.append("UniqueId,Roll,Name,Class,BirthRegNo,FatherName,MotherName,Phone,Address\n")
        list.forEach { s ->
            sb.append("${s.uniqueId},${s.rollNo},\"${s.name}\",\"${s.className}\",\"${s.birthRegNo}\",\"${s.fatherName}\",\"${s.motherName}\",\"${s.guardianPhone}\",\"${s.address}\"\n")
        }
        return sb.toString()
    }

    // --- 3. Exam Routine Auto-Generator ---
    fun generateRoutineForClass(classId: String, examCategory: ExamCategory, year: Int) {
        val cls = _classes.value.find { it.id == classId } ?: return
        val classSubs = _subjects.value.filter { it.classId == classId }
        val existingFiltered = _routines.value.filterNot { it.classId == classId && it.examCategory == examCategory && it.year == year }

        val startDates = listOf("10/12/$year", "12/12/$year", "14/12/$year", "16/12/$year", "18/12/$year", "20/12/$year", "22/12/$year")
        val days = listOf("Sunday", "Tuesday", "Thursday", "Saturday", "Monday", "Wednesday", "Friday")

        val generated = classSubs.mapIndexed { index, sub ->
            val dateStr = startDates.getOrElse(index) { "2${index}/12/$year" }
            val dayStr = days.getOrElse(index % days.size) { "Sunday" }
            ExamRoutineSlot(
                id = UUID.randomUUID().toString(),
                classId = classId,
                className = cls.name,
                examCategory = examCategory,
                year = year,
                subjectName = sub.name,
                examDate = dateStr,
                examDay = dayStr,
                examTime = "10:00 AM - 01:00 PM",
                room = "Hall ${((index % 3) + 1)}"
            )
        }
        _routines.value = existingFiltered + generated
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
        val subject = _subjects.value.find { it.id == subjectId } ?: return null

        val (letter, gp) = calculateGrade(marksObtained, subject.fullMarks)
        val markRecord = StudentMark(
            id = UUID.randomUUID().toString(),
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
            marksObtained = marksObtained,
            fullMarks = subject.fullMarks,
            passMarks = subject.passMarks,
            isMandatory = subject.type == SubjectType.MANDATORY,
            letterGrade = letter,
            gradePoint = gp
        )

        // Replace if already exists for this student, subject, category & year
        val filtered = _marks.value.filterNot {
            it.studentId == student.id && it.subjectId == subject.id && it.examCategory == examCategory && it.year == year
        }
        _marks.value = listOf(markRecord) + filtered
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
        val student = verifyStudentByUniqueId(uniqueId) ?: return null
        val studentMarks = _marks.value.filter {
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
            // Optional subject bonus: points above 2.0 add slightly to GPA
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
            failedMandatorySubjects = failedMandatory
        )
    }

    fun getClassMeritList(classId: String, examCategory: ExamCategory, year: Int): List<ClassMeritRank> {
        val classStudents = _students.value.filter { it.classId == classId }
        val marksheets = classStudents.mapNotNull { student ->
            getStudentMarksheet(student.uniqueId, examCategory, year)
        }

        // Passed students sorted by total marks and GPA descending, then failed students
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

    companion object {
        @Volatile
        private var instance: SchoolRepository? = null

        fun getInstance(): SchoolRepository {
            return instance ?: synchronized(this) {
                instance ?: SchoolRepository().also { instance = it }
            }
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

        private fun initialSubjects(): List<Subject> {
            // Default subjects for Class 10 (cls_12)
            val class10Id = "cls_12" // Class 10
            return listOf(
                Subject("sub_10_1", class10Id, "Bangla (বাংলা)", "BAN-101", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_10_2", class10Id, "English (ইংরেজি)", "ENG-102", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_10_3", class10Id, "Mathematics (গণিত)", "MAT-103", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_10_4", class10Id, "General Science (বিজ্ঞান)", "SCI-104", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_10_5", class10Id, "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", "BGS-105", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_10_6", class10Id, "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", "ICT-106", 50.0, 17.0, SubjectType.MANDATORY),
                Subject("sub_10_7", class10Id, "Higher Math (উচ্চতর গণিত)", "HMT-107", 100.0, 33.0, SubjectType.OPTIONAL),
                Subject("sub_10_8", class10Id, "Agriculture Studies (কৃষি শিক্ষা)", "AGR-108", 100.0, 33.0, SubjectType.OPTIONAL),

                // Class 9 (cls_11)
                Subject("sub_9_1", "cls_11", "Bangla (বাংলা)", "BAN-901", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_9_2", "cls_11", "English (ইংরেজি)", "ENG-902", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_9_3", "cls_11", "Mathematics (গণিত)", "MAT-903", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_9_4", "cls_11", "General Science (বিজ্ঞান)", "SCI-904", 100.0, 33.0, SubjectType.MANDATORY),

                // Class 8 (cls_10)
                Subject("sub_8_1", "cls_10", "Bangla (বাংলা)", "BAN-801", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_8_2", "cls_10", "English (ইংরেজি)", "ENG-802", 100.0, 33.0, SubjectType.MANDATORY),
                Subject("sub_8_3", "cls_10", "Mathematics (গণিত)", "MAT-803", 100.0, 33.0, SubjectType.MANDATORY)
            )
        }

        private fun initialStudents(): List<Student> {
            val class10Id = "cls_12" // Class 10
            return listOf(
                Student(
                    id = "std_101",
                    uniqueId = "STU-2026-001",
                    rollNo = "01",
                    name = "Tanzim Hasan (তানজিম হাসান)",
                    classId = class10Id,
                    className = "Class 10",
                    birthRegNo = "20092692518001234",
                    fatherName = "Md. Rafiqul Islam",
                    motherName = "Nazma Begum",
                    guardianPhone = "+880 1711-234567",
                    address = "House 12, Road 4, Dhanmondi, Dhaka",
                    avatarColorHex = 0xFF1E3A8A
                ),
                Student(
                    id = "std_102",
                    uniqueId = "STU-2026-002",
                    rollNo = "02",
                    name = "Sadia Afrin (সাদিয়া আফরিন)",
                    classId = class10Id,
                    className = "Class 10",
                    birthRegNo = "20092692518001235",
                    fatherName = "Anisur Rahman",
                    motherName = "Shahana Parvin",
                    guardianPhone = "+880 1819-345678",
                    address = "Plot 24, Sector 7, Uttara, Dhaka",
                    avatarColorHex = 0xFF0D9488
                ),
                Student(
                    id = "std_103",
                    uniqueId = "STU-2026-003",
                    rollNo = "03",
                    name = "Abrar Fahim (আবরার ফাহিম)",
                    classId = class10Id,
                    className = "Class 10",
                    birthRegNo = "20092692518001236",
                    fatherName = "Kamrul Hassan",
                    motherName = "Farhana Akter",
                    guardianPhone = "+880 1912-456789",
                    address = "Block C, Bashundhara R/A, Dhaka",
                    avatarColorHex = 0xFF7C3AED
                ),
                Student(
                    id = "std_104",
                    uniqueId = "STU-2026-004",
                    rollNo = "04",
                    name = "Sumaiya Khanom (সুমাইয়া খানম)",
                    classId = class10Id,
                    className = "Class 10",
                    birthRegNo = "20092692518001237",
                    fatherName = "Mokhlesur Rahman",
                    motherName = "Rina Akter",
                    guardianPhone = "+880 1610-567890",
                    address = "Mirpur 10, Dhaka",
                    avatarColorHex = 0xFFDB2777
                ),
                Student(
                    id = "std_105",
                    uniqueId = "STU-2026-005",
                    rollNo = "05",
                    name = "Zubair Ahmed (জুবায়ের আহমেদ)",
                    classId = class10Id,
                    className = "Class 10",
                    birthRegNo = "20092692518001238",
                    fatherName = "Alamgir Hossain",
                    motherName = "Parvin Begum",
                    guardianPhone = "+880 1515-678901",
                    address = "Mohammadpur, Dhaka",
                    avatarColorHex = 0xFFD97706
                )
            )
        }

        private fun initialRoutines(): List<ExamRoutineSlot> {
            val class10Id = "cls_12"
            return listOf(
                ExamRoutineSlot("rt_1", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "Bangla (বাংলা)", "10/12/2026", "Sunday", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_2", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "English (ইংরেজি)", "12/12/2026", "Tuesday", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_3", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "Mathematics (গণিত)", "14/12/2026", "Thursday", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_4", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "General Science (বিজ্ঞান)", "16/12/2026", "Saturday", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_5", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", "18/12/2026", "Monday", "10:00 AM - 01:00 PM", "Hall 1"),
                ExamRoutineSlot("rt_6", class10Id, "Class 10", ExamCategory.ANNUAL, 2026, "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", "20/12/2026", "Wednesday", "10:00 AM - 11:30 AM", "Hall 1")
            )
        }

        private fun initialMarks(): List<StudentMark> {
            val class10Id = "cls_12"
            return listOf(
                // Tanzim Hasan (Top student)
                StudentMark("mk_1", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", ExamCategory.ANNUAL, 2026, "sub_10_1", "Bangla (বাংলা)", 88.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_2", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", ExamCategory.ANNUAL, 2026, "sub_10_2", "English (ইংরেজি)", 84.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_3", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", ExamCategory.ANNUAL, 2026, "sub_10_3", "Mathematics (গণিত)", 96.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_4", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", ExamCategory.ANNUAL, 2026, "sub_10_4", "General Science (বিজ্ঞান)", 92.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_5", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", ExamCategory.ANNUAL, 2026, "sub_10_5", "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", 87.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_6", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", ExamCategory.ANNUAL, 2026, "sub_10_6", "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", 46.0, 50.0, 17.0, true, "A+", 5.0),
                StudentMark("mk_7", "STU-2026-001", "std_101", "Tanzim Hasan", "Md. Rafiqul Islam", class10Id, "Class 10", "01", ExamCategory.ANNUAL, 2026, "sub_10_7", "Higher Math (উচ্চতর গণিত)", 90.0, 100.0, 33.0, false, "A+", 5.0),

                // Sadia Afrin
                StudentMark("mk_8", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", ExamCategory.ANNUAL, 2026, "sub_10_1", "Bangla (বাংলা)", 82.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_9", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", ExamCategory.ANNUAL, 2026, "sub_10_2", "English (ইংরেজি)", 86.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_10", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", ExamCategory.ANNUAL, 2026, "sub_10_3", "Mathematics (গণিত)", 90.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_11", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", ExamCategory.ANNUAL, 2026, "sub_10_4", "General Science (বিজ্ঞান)", 85.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_12", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", ExamCategory.ANNUAL, 2026, "sub_10_5", "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", 81.0, 100.0, 33.0, true, "A+", 5.0),
                StudentMark("mk_13", "STU-2026-002", "std_102", "Sadia Afrin", "Anisur Rahman", class10Id, "Class 10", "02", ExamCategory.ANNUAL, 2026, "sub_10_6", "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", 44.0, 50.0, 17.0, true, "A+", 5.0),

                // Abrar Fahim (Fails in Mandatory Science)
                StudentMark("mk_14", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", ExamCategory.ANNUAL, 2026, "sub_10_1", "Bangla (বাংলা)", 72.0, 100.0, 33.0, true, "A", 4.0),
                StudentMark("mk_15", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", ExamCategory.ANNUAL, 2026, "sub_10_2", "English (ইংরেজি)", 65.0, 100.0, 33.0, true, "A-", 3.5),
                StudentMark("mk_16", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", ExamCategory.ANNUAL, 2026, "sub_10_3", "Mathematics (গণিত)", 55.0, 100.0, 33.0, true, "B", 3.0),
                StudentMark("mk_17", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", ExamCategory.ANNUAL, 2026, "sub_10_4", "General Science (বিজ্ঞান)", 28.0, 100.0, 33.0, true, "F", 0.0), // FAILED!
                StudentMark("mk_18", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", ExamCategory.ANNUAL, 2026, "sub_10_5", "Social Studies (বাংলাদেশ ও বিশ্বপরিচয়)", 60.0, 100.0, 33.0, true, "A-", 3.5),
                StudentMark("mk_19", "STU-2026-003", "std_103", "Abrar Fahim", "Kamrul Hassan", class10Id, "Class 10", "03", ExamCategory.ANNUAL, 2026, "sub_10_6", "ICT (তথ্য ও যোগাযোগ প্রযুক্তি)", 35.0, 50.0, 17.0, true, "A", 4.0)
            )
        }
    }
}
