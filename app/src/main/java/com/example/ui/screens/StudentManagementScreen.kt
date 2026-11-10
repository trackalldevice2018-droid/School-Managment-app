package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import com.example.util.BackupUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.SchoolViewModel
import com.example.ui.components.AsyncPhotoAvatar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PhotoPickerControl

@Composable
fun StudentManagementScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val students by viewModel.filteredStudents.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
    val classFilter by viewModel.studentClassFilter.collectAsStateWithLifecycle()
    val selectedStudent by viewModel.selectedStudent.collectAsStateWithLifecycle()
    val editingStudent by viewModel.editingStudent.collectAsStateWithLifecycle()
    val showAddStudent by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()
    val showBulkUpload by viewModel.showBulkUploadDialog.collectAsStateWithLifecycle()
    val selectedStudentIds by viewModel.selectedStudentIds.collectAsStateWithLifecycle()
    val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsStateWithLifecycle()

    var showExportDialog by remember { mutableStateOf(false) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    val filterOptions = listOf("All") + classes.map { it.name }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (!isMultiSelectMode) {
                FloatingActionButton(
                    onClick = { viewModel.openAddStudentDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("enroll_student_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "New Student Entry")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar (Unique ID, Name, Roll, Phone)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setStudentSearchQuery(it) },
                placeholder = { Text("স্টুডেন্ট আইডি, নাম, রোল বা ফোন নম্বর দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setStudentSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_box")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Class Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { opt ->
                    FilterChip(
                        selected = classFilter == opt,
                        onClick = { viewModel.setStudentClassFilter(opt) },
                        label = { Text(opt) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Bulk Upload, Export & Multi-Select Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট শিক্ষার্থী: ${students.size} জন",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = isMultiSelectMode,
                        onClick = { viewModel.setMultiSelectMode(!isMultiSelectMode) },
                        label = { Text(if (isMultiSelectMode) "নির্বাচন সমাপ্ত" else "একাধিক নির্বাচন") }
                    )

                    OutlinedButton(
                        onClick = { viewModel.openBulkUploadDialog() },
                        modifier = Modifier.testTag("bulk_upload_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("বাল্ক আপলোড", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("export_data_button")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এক্সপোর্ট", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Multi-Select Action Bar (when active)
            if (isMultiSelectMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedStudentIds.size} জন শিক্ষার্থী নির্বাচিত",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(
                                onClick = {
                                    if (selectedStudentIds.size == students.size) {
                                        viewModel.clearStudentSelection()
                                    } else {
                                        viewModel.selectAllStudents(students)
                                    }
                                }
                            ) {
                                Text(if (selectedStudentIds.size == students.size) "সব বাতিল" else "সব নির্বাচন")
                            }

                            Button(
                                onClick = { showBulkDeleteConfirm = true },
                                enabled = selectedStudentIds.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("মুছুন (${selectedStudentIds.size})")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (students.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.School,
                    title = "কোনো শিক্ষার্থী পাওয়া যায়নি",
                    description = "সার্চ ফিল্টার চেক করুন অথবা নতুন শিক্ষার্থী নিবন্ধন করুন।",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        val meritRank = viewModel.getStudentMeritRank(student.id)
                        val isSelected = selectedStudentIds.contains(student.id)
                        StudentCardItem(
                            student = student,
                            meritRank = meritRank,
                            isSelectionMode = isMultiSelectMode,
                            isSelected = isSelected,
                            onToggleSelect = { viewModel.toggleStudentSelection(student.id) },
                            onClick = {
                                if (isMultiSelectMode) {
                                    viewModel.toggleStudentSelection(student.id)
                                } else {
                                    viewModel.selectStudent(student)
                                }
                            },
                            onEditClick = { viewModel.openEditStudentDialog(student) },
                            onDeleteClick = { studentToDelete = student }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Single Student Delete Confirmation Dialog
    studentToDelete?.let { targetStudent ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("শিক্ষার্থী মুছে ফেলতে চান?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে '${targetStudent.name}' (রোল: ${targetStudent.rollNo}, শ্রেণি: ${targetStudent.className}) এর সকল তথ্য ও পরীক্ষার ফলাফল ডাটাবেজ থেকে মুছে ফেলতে চান? এটি আর ফিরিয়ে আনা যাবে না।"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(targetStudent.id)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("হ্যাঁ, মুছে ফেলুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { studentToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Bulk Delete Confirmation Dialog
    if (showBulkDeleteConfirm) {
        val count = selectedStudentIds.size
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("একাধিক শিক্ষার্থী মুছে ফেলা", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে নির্বাচিত $count জন শিক্ষার্থীর তথ্য এবং তাদের সকল পরীক্ষার ফলাফল স্থায়ীভাবে মুছে ফেলতে চান?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSelectedStudents()
                        showBulkDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("হ্যাঁ, সব মুছে ফেলুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // New Student Entry Dialog (with Photo Picker)
    if (showAddStudent) {
        AddNewStudentDialog(
            classes = classes,
            isRollDuplicate = { clsId, roll -> viewModel.isRollNumberTaken(clsId, roll) },
            suggestedNextRoll = { clsId -> viewModel.getNextRollNumber(clsId) },
            onDismiss = { viewModel.closeAddStudentDialog() },
            onConfirm = { name, uId, roll, classId, birthReg, dateOfBirth, father, mother, phone, address, photoUri ->
                viewModel.addStudent(name, uId, roll, classId, birthReg, dateOfBirth, father, mother, phone, address, photoUri)
            }
        )
    }

    // Edit Existing Student Dialog
    editingStudent?.let { studentToEdit ->
        EditStudentDialog(
            student = studentToEdit,
            classes = classes,
            isRollDuplicate = { clsId, roll -> viewModel.isRollNumberTaken(clsId, roll, studentToEdit.id) },
            onDismiss = { viewModel.closeEditStudentDialog() },
            onConfirm = { id, name, uId, roll, classId, birthReg, dateOfBirth, father, mother, phone, address, photoUri, _ ->
                viewModel.updateStudentById(
                    id = id,
                    name = name,
                    uniqueId = uId,
                    rollNo = roll,
                    classId = classId,
                    birthRegNo = birthReg,
                    dateOfBirth = dateOfBirth,
                    fatherName = father,
                    motherName = mother,
                    guardianPhone = phone,
                    address = address,
                    photoUri = photoUri
                )
            }
        )
    }

    // Bulk Upload Dialog
    if (showBulkUpload) {
        BulkUploadDialog(
            onDismiss = { viewModel.closeBulkUploadDialog() },
            onConfirm = { csv -> viewModel.bulkUploadCsv(csv) }
        )
    }

    // Student Detail Profile Dialog (with Edit option)
    selectedStudent?.let { student ->
        val meritRank = viewModel.getStudentMeritRank(student.id)
        StudentFullProfileDialog(
            student = student,
            meritRank = meritRank,
            onDismiss = { viewModel.selectStudent(null) },
            onEdit = {
                viewModel.openEditStudentDialog(student)
            },
            onDelete = {
                viewModel.deleteStudent(student.id)
                viewModel.selectStudent(null)
            }
        )
    }

    // Export Data Dialog
    if (showExportDialog) {
        val csvData = viewModel.exportStudentsCsv()
        val clipboardManager = LocalClipboardManager.current
        val ctx = LocalContext.current
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("শিক্ষার্থীদের ডাটা ডাউনলোড / এক্সপোর্ট", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CSV এবং সম্পূর্ণ A4 সাইজ PDF ডাউনলোড ও গুগল ড্রাইভ/শীট ব্যাকআপ সুবিধা:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // PDF Print / Download Button
                    Button(
                        onClick = {
                            viewModel.printStudentDirectory(ctx, classFilter, students)
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📄 সম্পূর্ণ পেজ A4 সাইজ PDF প্রিন্ট / সেভ")
                    }

                    // Google Drive & Sheets Backup Button
                    Button(
                        onClick = {
                            val cleanClassName = if (classFilter == "All") "All_Students" else classFilter.replace(" ", "_")
                            BackupUtils.shareCsvToDriveOrSheets(
                                context = ctx,
                                fileName = "students_${cleanClassName}.csv",
                                csvContent = csvData,
                                title = "Google Drive / Sheets ব্যাকআপ ও সেভ"
                            )
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Icon(Icons.Default.DriveFileMove, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("☁️ Google Drive ও Sheets এ সরাসরি ব্যাকআপ")
                    }

                    // Copy CSV Button
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(csvData))
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📋 CSV টেক্সট ক্লিপবোর্ডে কপি করুন")
                    }

                    Text(
                        text = "এক্সেল/সিএসভি (Excel/CSV) ফরম্যাট ডাটা প্রিভিউ:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = csvData,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showExportDialog = false }) { Text("বন্ধ করুন") }
            }
        )
    }
}

@Composable
fun StudentCardItem(
    student: Student,
    meritRank: com.example.data.model.ClassMeritRank? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                androidx.compose.material3.Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            AsyncPhotoAvatar(
                photoUri = student.photoUri,
                name = student.name,
                colorHex = student.avatarColorHex,
                size = 50,
                shapeCorner = 12
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "রোল: ${student.rollNo}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (meritRank != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = com.example.ui.theme.AmberGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "মেধাক্রম: #${meritRank.position}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.AmberGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "স্টুডেন্ট আইডি: ${student.uniqueId} • শ্রেণি: ${student.className}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "পিতা: ${student.fatherName} • ফোন: ${student.guardianPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isSelectionMode) {
                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag("edit_student_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Student Data",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag("delete_student_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Student",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${student.guardianPhone}")
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Guardian",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewStudentDialog(
    classes: List<SchoolClass>,
    isRollDuplicate: (classId: String, rollNo: String) -> Boolean = { _, _ -> false },
    suggestedNextRoll: (classId: String) -> String = { "01" },
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        birthRegNo: String,
        dateOfBirth: String,
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String,
        photoUri: String?
    ) -> Unit
) {
    val initialClassId = classes.firstOrNull()?.id ?: ""
    var name by remember { mutableStateOf("") }
    var uniqueId by remember { mutableStateOf("STU-2026-${(100..999).random()}") }
    var selectedClassId by remember { mutableStateOf(initialClassId) }
    var rollNo by remember { mutableStateOf(suggestedNextRoll(initialClassId)) }
    var birthRegNo by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("01/01/2010") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }

    val isDuplicateRoll = rollNo.isNotBlank() && isRollDuplicate(selectedClassId, rollNo)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন ছাত্র/ছাত্রী নিবন্ধন (New Student Entry)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo Picker for Student
                PhotoPickerControl(
                    selectedUri = photoUri,
                    onPhotoSelected = { photoUri = it },
                    label = "শিক্ষার্থীর পাসপোর্ট সাইজ ছবি"
                )

                Text("ব্যক্তিগত তথ্য (Personal Info):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পূর্ণ নাম (Full Name) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = birthRegNo,
                    onValueChange = { birthRegNo = it },
                    label = { Text("জন্ম নিবন্ধন নম্বর (Birth Reg No) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("জন্ম তারিখ (Date of Birth - DD/MM/YYYY) *") },
                    placeholder = { Text("যেমন: 15/03/2010") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("পিতার নাম (Father's Name) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = motherName,
                    onValueChange = { motherName = it },
                    label = { Text("মাতার নাম (Mother's Name) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text("অভিভাবকের ফোন নম্বর (Phone) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা (Address)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("একাডেমিক তথ্য (Academic Info):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Text("শ্রেণি নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classes) { cls ->
                        FilterChip(
                            selected = selectedClassId == cls.id,
                            onClick = {
                                selectedClassId = cls.id
                                rollNo = suggestedNextRoll(cls.id)
                            },
                            label = { Text(cls.name) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uniqueId,
                        onValueChange = { uniqueId = it },
                        label = { Text("স্টুডেন্ট আইডি (Student ID)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it },
                        label = { Text("ক্লাস রোল (Roll) *") },
                        isError = isDuplicateRoll,
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (isDuplicateRoll) {
                    Text(
                        text = "⚠️ এই শ্রেণিতে রোল '$rollNo' ইতোমধ্যে অন্য শিক্ষার্থীর জন্য বরাদ্দ আছে!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && rollNo.isNotBlank() && fatherName.isNotBlank() && !isDuplicateRoll) {
                        onConfirm(
                            name.trim(),
                            uniqueId.trim(),
                            rollNo.trim(),
                            selectedClassId,
                            birthRegNo.trim(),
                            dateOfBirth.trim(),
                            fatherName.trim(),
                            motherName.trim(),
                            guardianPhone.trim(),
                            address.trim(),
                            photoUri
                        )
                    }
                },
                enabled = name.isNotBlank() && rollNo.isNotBlank() && fatherName.isNotBlank() && !isDuplicateRoll
            ) {
                Text("নিবন্ধন সম্পন্ন করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun EditStudentDialog(
    student: Student,
    classes: List<SchoolClass>,
    isRollDuplicate: (classId: String, rollNo: String) -> Boolean = { _, _ -> false },
    onDismiss: () -> Unit,
    onConfirm: (
        id: String,
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        birthRegNo: String,
        dateOfBirth: String,
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String,
        photoUri: String?,
        avatarColorHex: Long
    ) -> Unit
) {
    var name by remember(student) { mutableStateOf(student.name) }
    var uniqueId by remember(student) { mutableStateOf(student.uniqueId) }
    var rollNo by remember(student) { mutableStateOf(student.rollNo) }
    var selectedClassId by remember(student) { mutableStateOf(student.classId) }
    var birthRegNo by remember(student) { mutableStateOf(student.birthRegNo) }
    var dateOfBirth by remember(student) { mutableStateOf(student.dateOfBirth) }
    var fatherName by remember(student) { mutableStateOf(student.fatherName) }
    var motherName by remember(student) { mutableStateOf(student.motherName) }
    var guardianPhone by remember(student) { mutableStateOf(student.guardianPhone) }
    var address by remember(student) { mutableStateOf(student.address) }
    var photoUri by remember(student) { mutableStateOf(student.photoUri) }

    val isDuplicateRoll = rollNo.isNotBlank() && isRollDuplicate(selectedClassId, rollNo)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("শিক্ষার্থীর তথ্য সংশোধন (Edit Student)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Photo Picker for Edit
                PhotoPickerControl(
                    selectedUri = photoUri,
                    onPhotoSelected = { photoUri = it },
                    label = "শিক্ষার্থীর ছবি পরিবর্তন / আপলোড"
                )

                Text("ব্যক্তিগত তথ্য সংশোধন:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("পূর্ণ নাম (Full Name) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_student_name_input")
                )

                OutlinedTextField(
                    value = birthRegNo,
                    onValueChange = { birthRegNo = it },
                    label = { Text("জন্ম নিবন্ধন নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_student_birth_reg_input")
                )

                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text("জন্ম তারিখ (Date of Birth - DD/MM/YYYY) *") },
                    placeholder = { Text("যেমন: 15/03/2010") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_student_dob_input")
                )

                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("পিতার নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_student_father_input")
                )

                OutlinedTextField(
                    value = motherName,
                    onValueChange = { motherName = it },
                    label = { Text("মাতার নাম *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_student_mother_input")
                )

                OutlinedTextField(
                    value = guardianPhone,
                    onValueChange = { guardianPhone = it },
                    label = { Text("অভিভাবকের ফোন নম্বর *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_student_phone_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("ঠিকানা") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_student_address_input")
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("একাডেমিক তথ্য:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Text("শ্রেণি পরিবর্তন:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classes) { cls ->
                        FilterChip(
                            selected = selectedClassId == cls.id,
                            onClick = { selectedClassId = cls.id },
                            label = { Text(cls.name) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uniqueId,
                        onValueChange = { uniqueId = it },
                        label = { Text("স্টুডেন্ট আইডি") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("edit_student_unique_id_input")
                    )
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it },
                        label = { Text("ক্লাস রোল *") },
                        isError = isDuplicateRoll,
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("edit_student_roll_input")
                    )
                }

                if (isDuplicateRoll) {
                    Text(
                        text = "⚠️ এই শ্রেণিতে রোল '$rollNo' ইতোমধ্যে অন্য শিক্ষার্থীর জন্য বরাদ্দ আছে!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && rollNo.isNotBlank() && !isDuplicateRoll) {
                        onConfirm(
                            student.id,
                            name.trim(),
                            uniqueId.trim(),
                            rollNo.trim(),
                            selectedClassId,
                            birthRegNo.trim(),
                            dateOfBirth.trim(),
                            fatherName.trim(),
                            motherName.trim(),
                            guardianPhone.trim(),
                            address.trim(),
                            photoUri,
                            student.avatarColorHex
                        )
                    }
                },
                enabled = name.isNotBlank() && rollNo.isNotBlank() && !isDuplicateRoll,
                modifier = Modifier.testTag("edit_student_save_button")
            ) {
                Text("আপডেট করুন (Save Changes)")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("edit_student_cancel_button")
            ) { Text("বাতিল") }
        }
    )
}

@Composable
fun BulkUploadDialog(
    onDismiss: () -> Unit,
    onConfirm: (csv: String) -> Unit
) {
    val sampleCsv = """
UniqueId,Roll,Name,Class,BirthRegNo,DateOfBirth,FatherName,MotherName,Phone,Address
STU-2026-010,10,Tanvir Ahmed,Class 10,20092692518001240,15/03/2009,Anwar Hossain,Nasrin Akter,+880 1711-998877,Dhanmondi Dhaka
STU-2026-011,11,Nusrat Jahan,Class 10,20092692518001241,20/07/2009,Shafiqul Islam,Rubina Begum,+880 1819-887766,Gulshan Dhaka
STU-2026-012,12,Mahir Faisal,Class 10,20092692518001242,10/11/2009,Zakir Hossain,Farida Yasmin,+880 1912-776655,Mirpur Dhaka
    """.trimIndent()

    var csvText by remember { mutableStateOf(sampleCsv) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val content = inputStream?.bufferedReader(Charsets.UTF_8)?.use { reader -> reader.readText() }
                if (!content.isNullOrBlank()) {
                    csvText = content
                    val cursor = context.contentResolver.query(it, null, null, null, null)
                    cursor?.use { c ->
                        if (c.moveToFirst()) {
                            val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                            if (nameIdx != -1) {
                                selectedFileName = c.getString(nameIdx)
                            }
                        }
                    }
                    if (selectedFileName == null) {
                        selectedFileName = it.lastPathSegment ?: "student_list.csv"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("এক্সেল / CSV সরাসরি ফাইল আপলোড (Bulk Upload)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Direct File Upload Button
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📂 সরাসরি (fileName.csv) ফাইল সিলেক্ট করুন")
                }

                if (selectedFileName != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("নির্বাচিত ফাইল: $selectedFileName", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                Text("রেকর্ড সংখ্যা: ${csvText.lines().filter { it.isNotBlank() }.size - 1} জন", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Text(
                    text = "সিএসভি ফাইল প্রিভিউ (ফরম্যাট: UniqueId, Roll, Name, Class, BirthRegNo, DateOfBirth, FatherName, MotherName, Phone, Address):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    minLines = 5,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (csvText.isNotBlank()) onConfirm(csvText) },
                enabled = csvText.isNotBlank()
            ) {
                Text("আপলোড সম্পন্ন করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun StudentFullProfileDialog(
    student: Student,
    meritRank: com.example.data.model.ClassMeritRank? = null,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.testTag("edit_student_profile_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("তথ্য সংশোধন")
                }
                OutlinedButton(onClick = onDismiss) { Text("বন্ধ করুন") }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ডিলিট")
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("শিক্ষার্থী প্রোফাইল", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncPhotoAvatar(
                        photoUri = student.photoUri,
                        name = student.name,
                        colorHex = student.avatarColorHex,
                        size = 64,
                        shapeCorner = 14
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("শ্রেণি: ${student.className} • রোল: ${student.rollNo}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("স্টুডেন্ট আইডি: ${student.uniqueId}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProfileField(
                    label = "শ্রেণিভিত্তিক মেধা অবস্থান (Class Rank)",
                    value = if (meritRank != null) "${meritRank.position}ম স্থান (${meritRank.statusText} • GPA: ${meritRank.gpa})" else "ফলাফল অপেক্ষমাণ"
                )
                ProfileField("জন্ম নিবন্ধন নম্বর", student.birthRegNo)
                ProfileField("জন্ম তারিখ", student.dateOfBirth)
                ProfileField("পিতার নাম", student.fatherName)
                ProfileField("মাতার নাম", student.motherName)
                ProfileField("অভিভাবকের ফোন", student.guardianPhone)
                ProfileField("ঠিকানা", student.address)

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${student.guardianPhone}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("অভিভাবককে কল করুন")
                }
            }
        }
    )
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value.ifBlank { "N/A" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
