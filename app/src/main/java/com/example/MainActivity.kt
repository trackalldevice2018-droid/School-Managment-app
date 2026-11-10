package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.SchoolViewModel
import com.example.ui.components.SchoolTopBar
import com.example.ui.screens.ClassAndSubjectScreen
import com.example.ui.screens.ExamManagementScreen
import com.example.ui.screens.ResultSystemScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SecurityLockScreen
import com.example.ui.screens.StudentManagementScreen
import com.example.ui.theme.SchoolManagementTheme

import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.data.repository.SchoolRepository.initialize(applicationContext)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            SchoolManagementTheme(primaryColor = Color(settings.themeColorHex)) {
                SchoolApp(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Ensure the app remains locked when not in use (e.g. app moved to background or screen turned off)
        if (viewModel.settings.value.isSecurityLockEnabled && viewModel.settings.value.autoLockOnBackground) {
            viewModel.lockApp()
        }
    }
}

@Composable
fun SchoolApp(viewModel: SchoolViewModel) {
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val editingStudent by viewModel.editingStudent.collectAsStateWithLifecycle()
    val showAboutUs by viewModel.showAboutUsDialog.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (settings.isSecurityLockEnabled && !isAppUnlocked) {
        SecurityLockScreen(viewModel = viewModel)
        return
    }

    // Predictive back / BackHandler
    BackHandler(enabled = selectedStudent != null || editingStudent != null || currentTab != AppTab.CLASS_SUBJECT) {
        if (editingStudent != null) {
            viewModel.closeEditStudentDialog()
        } else if (selectedStudent != null) {
            viewModel.selectStudent(null)
        } else if (currentTab != AppTab.CLASS_SUBJECT) {
            viewModel.setTab(AppTab.CLASS_SUBJECT)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val title = when (currentTab) {
                AppTab.CLASS_SUBJECT -> "১. শ্রেণি ও বিষয় ব্যবস্থাপনা"
                AppTab.STUDENTS -> "২. শিক্ষার্থী ব্যবস্থাপনা"
                AppTab.EXAMS_MARKS -> "৩. পরীক্ষা ও নম্বর এন্ট্রি"
                AppTab.RESULTS -> "৪. রেজাল্ট ও মার্কশীট সিস্টেম"
                AppTab.SETTINGS -> "৫. কেন্দ্রীয় সেটিংস ও থিম"
            }
            SchoolTopBar(
                title = title,
                subtitle = "${settings.schoolName} • ${settings.eiinNumber}",
                logoUri = settings.customLogoUri,
                onAboutClick = { viewModel.openAboutUsDialog() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.CLASS_SUBJECT,
                    onClick = { viewModel.setTab(AppTab.CLASS_SUBJECT) },
                    icon = { Icon(Icons.Default.Class, contentDescription = "শ্রেণি ও বিষয়") },
                    label = { Text("শ্রেণি/বিষয়", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_class_subject")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.STUDENTS,
                    onClick = { viewModel.setTab(AppTab.STUDENTS) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "শিক্ষার্থী") },
                    label = { Text("শিক্ষার্থী", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_students")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.EXAMS_MARKS,
                    onClick = { viewModel.setTab(AppTab.EXAMS_MARKS) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "পরীক্ষা ও নম্বর") },
                    label = { Text("পরীক্ষা/নম্বর", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_exams")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.RESULTS,
                    onClick = { viewModel.setTab(AppTab.RESULTS) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "রেজাল্ট") },
                    label = { Text("ফলাফল", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_results")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.SETTINGS,
                    onClick = { viewModel.setTab(AppTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "সেটিংস") },
                    label = { Text("সেটিংস", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_tab_settings")
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
                AppTab.CLASS_SUBJECT -> ClassAndSubjectScreen(viewModel = viewModel)
                AppTab.STUDENTS -> StudentManagementScreen(viewModel = viewModel)
                AppTab.EXAMS_MARKS -> ExamManagementScreen(viewModel = viewModel)
                AppTab.RESULTS -> ResultSystemScreen(viewModel = viewModel)
                AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }

    if (showAboutUs) {
        com.example.ui.components.AboutUsDialog(
            settings = settings,
            onDismiss = { viewModel.closeAboutUsDialog() }
        )
    }
}
