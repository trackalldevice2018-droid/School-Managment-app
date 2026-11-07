package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Subject
import com.example.data.model.SubjectType
import com.example.ui.SchoolViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AcademyBluePrimary
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.VioletLight

@Composable
fun ClassAndSubjectScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedClassId.collectAsStateWithLifecycle()
    val subjects by viewModel.currentClassSubjects.collectAsStateWithLifecycle()
    val showAddClass by viewModel.showAddClassDialog.collectAsStateWithLifecycle()
    val showAddSubject by viewModel.showAddSubjectDialog.collectAsStateWithLifecycle()

    val currentClass = classes.find { it.id == selectedClassId } ?: classes.firstOrNull()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddSubjectDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_subject_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
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

            // Class Header & Add Class Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "১. শ্রেণি তালিকা (Class List)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = { viewModel.openAddClassDialog() },
                    modifier = Modifier.testTag("add_class_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন শ্রেণি", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Classes Selector Chips (Nursery to Class 10)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(classes, key = { it.id }) { cls ->
                    FilterChip(
                        selected = cls.id == selectedClassId,
                        onClick = { viewModel.setSelectedClassId(cls.id) },
                        label = { Text(cls.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("class_tab_${cls.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Business Logic Notice Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "বিশেষ নিয়ম (Business Logic): কোনো বিষয় 'আবশ্যিক' হলে শিক্ষার্থীকে অবশ্যই সেই বিষয়ে পাশ করতে হবে। চূড়ান্ত ফলাফল ও জিপিএ গণনায় আবশ্যিক বিষয় প্রধান ভিত্তি।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subject Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentClass?.name ?: "শ্রেণি"} এর বিষয়সমূহ (${subjects.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "টোটাল ও পাস মার্কিং স্কেল",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (subjects.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Book,
                    title = "কোনো বিষয় পাওয়া যায়নি",
                    description = "নিচের '+' বাটনে ট্যাপ করে এই শ্রেণির জন্য বিষয় ও মার্কিং স্কেল যুক্ত করুন।",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(subjects, key = { it.id }) { subject ->
                        SubjectItemCard(
                            subject = subject,
                            onDelete = { viewModel.deleteSubject(subject.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddClass) {
        AddClassDialog(
            onDismiss = { viewModel.closeAddClassDialog() },
            onConfirm = { name, section -> viewModel.addClass(name, section) }
        )
    }

    if (showAddSubject) {
        AddSubjectDialog(
            className = currentClass?.name ?: "Class",
            onDismiss = { viewModel.closeAddSubjectDialog() },
            onConfirm = { name, code, fullMarks, passMarks, type ->
                viewModel.addSubject(name, code, fullMarks, passMarks, type)
            }
        )
    }
}

@Composable
fun SubjectItemCard(
    subject: Subject,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (subject.type == SubjectType.MANDATORY) EmeraldLight else VioletLight
                    ) {
                        Text(
                            text = subject.type.labelBn,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (subject.type == SubjectType.MANDATORY) EmeraldPresent else Color(0xFF7C3AED),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "কোড: ${subject.code} • মোট নম্বর: ${subject.fullMarks.toInt()} • পাস নম্বর: ${subject.passMarks.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Subject",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddClassDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, section: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("A") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন শ্রেণি যোগ করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("শ্রেণির নাম (যেমন: Class 11, Alim ইত্যাদি)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("শাখা / সেকশন (যেমন: A)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), section.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("যুক্ত করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}

@Composable
fun AddSubjectDialog(
    className: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, fullMarks: Double, passMarks: Double, type: SubjectType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var selectedScale by remember { mutableStateOf(100.0) } // 100 or 50
    var passMarksText by remember { mutableStateOf("33") }
    var selectedType by remember { mutableStateOf(SubjectType.MANDATORY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$className এ বিষয় যোগ করুন", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("বিষয়ের নাম (Subject Name) *") },
                    placeholder = { Text("যেমন: গণিত / Mathematics") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("বিষয় কোড (Subject Code)") },
                    placeholder = { Text("MAT-101") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "পূর্ণমান স্কেল (Full Marks Scale):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedScale == 100.0,
                        onClick = {
                            selectedScale = 100.0
                            passMarksText = "33"
                        },
                        label = { Text("১০০ নম্বর স্কেল") }
                    )
                    FilterChip(
                        selected = selectedScale == 50.0,
                        onClick = {
                            selectedScale = 50.0
                            passMarksText = "17"
                        },
                        label = { Text("৫০ নম্বর স্কেল") }
                    )
                }

                OutlinedTextField(
                    value = passMarksText,
                    onValueChange = { passMarksText = it },
                    label = { Text("পাস নম্বর (Pass Marks)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "বিষয়ের ধরন (Mandatory / Optional):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == SubjectType.MANDATORY,
                        onClick = { selectedType = SubjectType.MANDATORY },
                        label = { Text("আবশ্যিক (Mandatory)") }
                    )
                    FilterChip(
                        selected = selectedType == SubjectType.OPTIONAL,
                        onClick = { selectedType = SubjectType.OPTIONAL },
                        label = { Text("ঐচ্ছিক (Optional)") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pass = passMarksText.toDoubleOrNull() ?: if (selectedScale == 100.0) 33.0 else 17.0
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), code.ifBlank { "SUB-${(100..999).random()}" }, selectedScale, pass, selectedType)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("বাতিল") }
        }
    )
}
