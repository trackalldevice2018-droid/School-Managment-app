package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.SchoolViewModel
import com.example.ui.components.SchoolTopBar
import com.example.ui.dialogs.AddGradeDialog
import com.example.ui.dialogs.AddNoticeDialog
import com.example.ui.dialogs.AddStudentDialog
import com.example.ui.dialogs.AddTeacherDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.ClassesAndStaffScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NoticesAndFeesScreen
import com.example.ui.screens.StudentDetailDialog
import com.example.ui.screens.StudentsScreen
import com.example.ui.theme.SchoolManagementTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SchoolManagementTheme {
                SchoolApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SchoolApp(viewModel: SchoolViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val showAddStudent by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()
    val showAddTeacher by viewModel.showAddTeacherDialog.collectAsStateWithLifecycle()
    val showAddNotice by viewModel.showAddNoticeDialog.collectAsStateWithLifecycle()
    val showAddGrade by viewModel.showAddGradeDialog.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Predictive back / BackHandler
    BackHandler(enabled = selectedStudent != null || currentTab != AppTab.DASHBOARD) {
        if (selectedStudent != null) {
            viewModel.selectStudent(null)
        } else if (currentTab != AppTab.DASHBOARD) {
            viewModel.setTab(AppTab.DASHBOARD)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val title = when (currentTab) {
                AppTab.DASHBOARD -> "Oakridge Academy"
                AppTab.STUDENTS -> "Student Directory"
                AppTab.ATTENDANCE -> "Daily Attendance"
                AppTab.CLASSES -> "Classrooms & Staff"
                AppTab.NOTICES -> "School Bulletin & Fees"
            }
            SchoolTopBar(
                title = title,
                onNotificationClick = {
                    viewModel.setTab(AppTab.NOTICES)
                    viewModel.setNoticesSubTab(0)
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                    label = { Text("Overview") },
                    modifier = Modifier.testTag("nav_tab_overview")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.STUDENTS,
                    onClick = { viewModel.setTab(AppTab.STUDENTS) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "Students") },
                    label = { Text("Students") },
                    modifier = Modifier.testTag("nav_tab_students")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.ATTENDANCE,
                    onClick = { viewModel.setTab(AppTab.ATTENDANCE) },
                    icon = { Icon(Icons.Default.HowToReg, contentDescription = "Attendance") },
                    label = { Text("Attendance") },
                    modifier = Modifier.testTag("nav_tab_attendance")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.CLASSES,
                    onClick = { viewModel.setTab(AppTab.CLASSES) },
                    icon = { Icon(Icons.Default.Class, contentDescription = "Classes") },
                    label = { Text("Classes") },
                    modifier = Modifier.testTag("nav_tab_classes")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.NOTICES,
                    onClick = { viewModel.setTab(AppTab.NOTICES) },
                    icon = { Icon(Icons.Default.Campaign, contentDescription = "Bulletin") },
                    label = { Text("Bulletin") },
                    modifier = Modifier.testTag("nav_tab_bulletin")
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppTab.STUDENTS -> StudentsScreen(viewModel = viewModel)
                AppTab.ATTENDANCE -> AttendanceScreen(viewModel = viewModel)
                AppTab.CLASSES -> ClassesAndStaffScreen(viewModel = viewModel)
                AppTab.NOTICES -> NoticesAndFeesScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Dialogs
    selectedStudent?.let { student ->
        StudentDetailDialog(
            student = student,
            viewModel = viewModel,
            onDismiss = { viewModel.selectStudent(null) }
        )
    }

    if (showAddStudent) {
        AddStudentDialog(
            classes = classes,
            onDismiss = { viewModel.closeAddStudentDialog() },
            onConfirm = { firstName, lastName, rollNo, grade, section, parentName, parentPhone, parentEmail, gender ->
                viewModel.addStudent(
                    firstName, lastName, rollNo, grade, section, parentName, parentPhone, parentEmail, gender
                )
            }
        )
    }

    if (showAddTeacher) {
        AddTeacherDialog(
            classes = classes,
            onDismiss = { viewModel.closeAddTeacherDialog() },
            onConfirm = { name, department, subjects, assignedClass, email, phone, qualification, expYears ->
                viewModel.addTeacher(
                    name, department, subjects, assignedClass, email, phone, qualification, expYears
                )
            }
        )
    }

    if (showAddNotice) {
        AddNoticeDialog(
            onDismiss = { viewModel.closeAddNoticeDialog() },
            onConfirm = { title, content, category, priority, audience ->
                viewModel.addNotice(title, content, category, priority, audience)
            }
        )
    }

    if (showAddGrade) {
        AddGradeDialog(
            students = students,
            onDismiss = { viewModel.closeAddGradeDialog() },
            onConfirm = { studentId, subject, examName, marksObtained, maxMarks, remarks ->
                viewModel.addGrade(studentId, subject, examName, marksObtained, maxMarks, remarks)
            }
        )
    }
}
