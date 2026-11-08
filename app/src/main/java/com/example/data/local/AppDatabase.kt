package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ExamResultDao
import com.example.data.local.dao.StudentDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.entity.ExamResultEntity
import com.example.data.local.entity.StudentEntity
import com.example.data.local.entity.SubjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        SubjectEntity::class,
        ExamResultEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun examResultDao(): ExamResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "school_management.db"
                )
                    .addCallback(DatabasePrepopulateCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabasePrepopulateCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        prepopulateDatabase(database)
                    }
                }
            }
        }

        private suspend fun prepopulateDatabase(db: AppDatabase) {
            // Initial Subjects (Class 10 - cls_12)
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

                // Class 9
                SubjectEntity("sub_9_1", "cls_11", "Bangla (বাংলা)", "BAN-901", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_9_2", "cls_11", "English (ইংরেজি)", "ENG-902", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_9_3", "cls_11", "Mathematics (গণিত)", "MAT-903", 100.0, 33.0, "MANDATORY"),
                SubjectEntity("sub_9_4", "cls_11", "General Science (বিজ্ঞান)", "SCI-904", 100.0, 33.0, "MANDATORY")
            )
            db.subjectDao().insertSubjects(initialSubjects)

            // Initial Students
            val initialStudents = listOf(
                StudentEntity("std_101", "STU-2026-001", "01", "Tanzim Hasan (তানজিম হাসান)", class10Id, "Class 10", "20092692518001234", "Md. Rafiqul Islam", "Nazma Begum", "+880 1711-234567", "House 12, Road 4, Dhanmondi, Dhaka", 0xFF1E3A8A),
                StudentEntity("std_102", "STU-2026-002", "02", "Sadia Afrin (সাদিয়া আফরিন)", class10Id, "Class 10", "20092692518001235", "Anisur Rahman", "Shahana Parvin", "+880 1819-345678", "Plot 24, Sector 7, Uttara, Dhaka", 0xFF0D9488),
                StudentEntity("std_103", "STU-2026-003", "03", "Abrar Fahim (আবরার ফাহিম)", class10Id, "Class 10", "20092692518001236", "Kamrul Hassan", "Farhana Akter", "+880 1912-456789", "Block C, Bashundhara R/A, Dhaka", 0xFF7C3AED),
                StudentEntity("std_104", "STU-2026-004", "04", "Sumaiya Khanom (সুমাইয়া খানম)", class10Id, "Class 10", "20092692518001237", "Mokhlesur Rahman", "Rina Akter", "+880 1610-567890", "Mirpur 10, Dhaka", 0xFFDB2777),
                StudentEntity("std_105", "STU-2026-005", "05", "Zubair Ahmed (জুবায়ের আহমেদ)", class10Id, "Class 10", "20092692518001238", "Alamgir Hossain", "Parvin Begum", "+880 1515-678901", "Mohammadpur, Dhaka", 0xFFD97706)
            )
            db.studentDao().insertStudents(initialStudents)

            // Initial Marks
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
            db.examResultDao().insertExamResults(initialMarks)
        }
    }
}
