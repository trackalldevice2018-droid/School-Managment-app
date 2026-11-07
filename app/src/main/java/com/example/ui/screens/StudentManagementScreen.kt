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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Student
import com.example.ui.SchoolViewModel
import com.example.ui.components.AvatarInitials
import com.example.ui.components.EmptyStateView

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
    val showAddStudent by viewModel.showAddStudentDialog.collectAsStateWithLifecycle()
    val showBulkUpload by viewModel.showBulkUploadDialog.collectAsStateWithLifecycle()

    var showExportDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All") + classes.map { it.name }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddStudentDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("enroll_student_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "New Student Entry")
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
                placeholder = { Text("ইউনিক আইডি, নাম, রোল বা ফোন নম্বর দিয়ে খুঁজুন...") },
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

            // Action Row: Bulk Upload & Export
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        StudentCardItem(
                            student = student,
                            onClick = { viewModel.selectStudent(student) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // New Student Entry Dialog
    if (showAddStudent) {
        AddNewStudentDialog(
            classes = classes,
            onDismiss = { viewModel.closeAddStudentDialog() },
            onConfirm = { name, uId, roll, classId, birthReg, father, mother, phone, address ->
                viewModel.addStudent(name, uId, roll, classId, birthReg, father, mother, phone, address)
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

    // Student Detail Profile Dialog
    selectedStudent?.let { student ->
        StudentFullProfileDialog(
            student = student,
            onDismiss = { viewModel.selectStudent(null) },
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
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("শিক্ষার্থীদের ডাটা ডাউনলোড / এক্সপোর্ট", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "এক্সেল/সিএসভি (Excel/CSV) ফরম্যাট ডাটা প্রিভিউ:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = csvData,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    clipboardManager.setText(AnnotatedString(csvData))
                    showExportDialog = false
                }) {
                    Text("ক্লিপবোর্ডে কপি করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExportDialog = false }) { Text("বন্ধ করুন") }
            }
        )
    }
}

@Composable
fun StudentCardItem(
    student: Student,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 48)

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
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "ইউনিক আইডি: ${student.uniqueId} • শ্রেণি: ${student.className}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "পিতা: ${student.fatherName} • ফোন: ${student.guardianPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddNewStudentDialog(
    classes: List<com.example.data.model.SchoolClass>,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        uniqueId: String,
        rollNo: String,
        classId: String,
        birthRegNo: String,
        fatherName: String,
        motherName: String,
        guardianPhone: String,
        address: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var uniqueId by remember { mutableStateOf("STU-2026-${(100..999).random()}") }
    var rollNo by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "") }
    var birthRegNo by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var guardianPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

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
                            onClick = { selectedClassId = cls.id },
                            label = { Text(cls.name) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uniqueId,
                        onValueChange = { uniqueId = it },
                        label = { Text("ইউনিক আইডি (Unique ID)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rollNo,
                        onValueChange = { rollNo = it },
                        label = { Text("ক্লাস রোল (Roll) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && rollNo.isNotBlank() && fatherName.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            uniqueId.trim(),
                            rollNo.trim(),
                            selectedClassId,
                            birthRegNo.trim(),
                            fatherName.trim(),
                            motherName.trim(),
                            guardianPhone.trim(),
                            address.trim()
                        )
                    }
                },
                enabled = name.isNotBlank() && rollNo.isNotBlank() && fatherName.isNotBlank()
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
fun BulkUploadDialog(
    onDismiss: () -> Unit,
    onConfirm: (csv: String) -> Unit
) {
    val sampleCsv = """
UniqueId,Roll,Name,Class,BirthRegNo,FatherName,MotherName,Phone,Address
STU-2026-010,10,Tanvir Ahmed,Class 10,20092692518001240,Anwar Hossain,Nasrin Akter,+880 1711-998877,Dhanmondi Dhaka
STU-2026-011,11,Nusrat Jahan,Class 10,20092692518001241,Shafiqul Islam,Rubina Begum,+880 1819-887766,Gulshan Dhaka
STU-2026-012,12,Mahir Faisal,Class 10,20092692518001242,Zakir Hossain,Farida Yasmin,+880 1912-776655,Mirpur Dhaka
    """.trimIndent()

    var csvText by remember { mutableStateOf(sampleCsv) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("এক্সেল / CSV বাল্ক আপলোড (Bulk Upload)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "নিচের বক্সে CSV ফরম্যাটে শিক্ষার্থীদের ডাটা পেস্ট করুন (UniqueId, Roll, Name, Class, BirthRegNo, FatherName, MotherName, Phone, Address):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    minLines = 6,
                    maxLines = 10,
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
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) { Text("বন্ধ করুন") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ডিলিট করুন")
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
                    AvatarInitials(name = student.name, colorHex = student.avatarColorHex, size = 56)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("শ্রেণি: ${student.className} • রোল: ${student.rollNo}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("ইউনিক আইডি: ${student.uniqueId}", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ProfileField("জন্ম নিবন্ধন নম্বর", student.birthRegNo)
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
