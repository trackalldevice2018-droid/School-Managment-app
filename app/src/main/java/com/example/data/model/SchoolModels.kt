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
    val dateOfBirth: String = "01/01/2010",
    val fatherName: String,
    val motherName: String,
    val guardianPhone: String,
    val address: String,
    val avatarColorHex: Long = 0xFF1E3A8A,
    val photoUri: String? = null
)

data class ExamRoutineSlot(
    val id: String,
    val classId: String,
    val className: String,
    val examCategory: ExamCategory = ExamCategory.ANNUAL,
    val year: Int,
    val subjectName: String,
    val examDate: String,
    val examDay: String,
    val shift: String = "সকাল",
    val examTime: String = "10:00 AM - 01:00 PM",
    val room: String = "Hall A",
    val customCategoryName: String = ""
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
    val examCategory: ExamCategory = ExamCategory.ANNUAL,
    val year: Int,
    val subjectId: String,
    val subjectName: String,
    val marksObtained: Double,
    val fullMarks: Double,
    val passMarks: Double,
    val isMandatory: Boolean,
    val letterGrade: String,
    val gradePoint: Double,
    val customCategoryName: String = ""
) {
    val isPassed: Boolean get() = marksObtained >= passMarks
}

data class StudentMarksheet(
    val student: Student,
    val examCategory: ExamCategory = ExamCategory.ANNUAL,
    val year: Int,
    val subjectMarks: List<StudentMark>,
    val totalMarks: Double,
    val totalFullMarks: Double,
    val gpa: Double,
    val letterGrade: String,
    val isPassed: Boolean,
    val failedMandatorySubjects: List<String>,
    val classMeritPosition: Int = 0,
    val totalStudentsInClass: Int = 0,
    val customCategoryName: String = ""
)

data class ClassMeritRank(
    val position: Int,
    val student: Student,
    val totalMarks: Double,
    val gpa: Double,
    val isPassed: Boolean,
    val statusText: String
)

enum class AuthMode(val labelBn: String, val labelEn: String) {
    GOOGLE_ACCOUNT("গুগল অ্যাকাউন্ট / Google Account API", "Google Account API"),
    BIOMETRIC_OR_PIN("বায়োমেট্রিক ও পিন উভয়ই (সুপারিশকৃত)", "Biometric with PIN Fallback"),
    BIOMETRIC_ONLY("শুধু ফিঙ্গারপ্রিন্ট / ফেস আনলক", "Biometric Only"),
    PIN_ONLY("শুধু পিন কোড", "PIN Only")
}

data class SchoolSettings(
    val schoolName: String = "আইডিয়াল মডেল একাডেমি / Ideal Model Academy",
    val schoolAddress: String = "ধানমন্ডি, ঢাকা-১২০৫ • বাংলাদেশ",
    val eiinNumber: String = "EIIN: 134567",
    val contactPhone: String = "+৮৮০ ১৭০০-০০০০০০",
    val contactEmail: String = "info@idealmodelacademy.edu.bd",
    val principalName: String = "Dr. M. Rahman",
    val principalTitle: String = "প্রধান শিক্ষক (অধ্যক্ষ)",
    val customLogoUri: String? = null,
    val themeColorHex: Long = 0xFF1E3A8A,
    val showWatermark: Boolean = true,
    val watermarkOpacity: Float = 0.08f,
    val routineInstructions: String = "১. পরীক্ষা শুরুর ১৫ মিনিট পূর্বে হলে প্রবেশ করতে হবে।\n২. প্রবেশপত্র ব্যতিত কোনো শিক্ষার্থীকে হলে প্রবেশ করতে দেওয়া হবে না।\n৩. পরীক্ষা কক্ষে মোবাইল ফোন বা যে কোনো ডিজিটাল ডিভাইস আনা সম্পূর্ণ নিষিদ্ধ।",
    val isSecurityLockEnabled: Boolean = true,
    val authMode: AuthMode = AuthMode.BIOMETRIC_OR_PIN,
    val autoLockOnBackground: Boolean = true,
    val isGoogleAuthEnabled: Boolean = true,
    val googleAccountEmail: String = "trackalldevice2018@gmail.com",
    val googleAccountName: String = "মাদ্রাসা এডমিন (Google User)",
    val isGoogleUserSignedIn: Boolean = false,
    val securityPin: String = "1234",
    val securityQuestion: String = "আপনার প্রিয় বিষয় কোনটি?",
    val securityAnswer: String = "Mathematics"
)
