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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppTab
import com.example.ui.SchoolViewModel
import com.example.ui.components.SchoolTopBar
import com.example.ui.screens.ClassAndSubjectScreen
import com.example.ui.screens.ExamManagementScreen
import com.example.ui.screens.ResultSystemScreen
import com.example.ui.screens.StudentManagementScreen
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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Predictive back / BackHandler
    BackHandler(enabled = selectedStudent != null || currentTab != AppTab.CLASS_SUBJECT) {
        if (selectedStudent != null) {
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
            }
            SchoolTopBar(
                title = title,
                subtitle = "আইডিয়াল মডেল একাডেমি • School Management"
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
                    label = { Text("শ্রেণি ও বিষয়") },
                    modifier = Modifier.testTag("nav_tab_class_subject")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.STUDENTS,
                    onClick = { viewModel.setTab(AppTab.STUDENTS) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = "শিক্ষার্থী") },
                    label = { Text("শিক্ষার্থী") },
                    modifier = Modifier.testTag("nav_tab_students")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.EXAMS_MARKS,
                    onClick = { viewModel.setTab(AppTab.EXAMS_MARKS) },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "পরীক্ষা ও নম্বর") },
                    label = { Text("পরীক্ষা ও নম্বর") },
                    modifier = Modifier.testTag("nav_tab_exams")
                )
                NavigationBarItem(
                    selected = currentTab == AppTab.RESULTS,
                    onClick = { viewModel.setTab(AppTab.RESULTS) },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "রেজাল্ট") },
                    label = { Text("ফলাফল") },
                    modifier = Modifier.testTag("nav_tab_results")
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
            }
        }
    }
}
