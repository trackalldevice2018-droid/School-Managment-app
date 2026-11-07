package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ExamCategory
import com.example.data.model.ExamRoutineSlot
import com.example.data.model.StudentMark
import com.example.ui.SchoolViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamManagementScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.examSubTab.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = activeSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { viewModel.setExamSubTab(0) },
                text = { Text("রুটিন অটো-জেনারেটর") },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.testTag("tab_routine_generator")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { viewModel.setExamSubTab(1) },
                text = { Text("নম্বর এন্ট্রি (ভেরিফাই সহ)") },
                icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                modifier = Modifier.testTag("tab_marks_entry")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeSubTab == 0) {
            RoutineGeneratorTab(viewModel = viewModel)
        } else {
            MarksEntryTab(viewModel = viewModel)
        }
    }
}

// ---------------------------------------------------------------------------------
// SUB-TAB 0: EXAM ROUTINE AUTO-GENERATOR
// ---------------------------------------------------------------------------------
@Composable
fun RoutineGeneratorTab(viewModel: SchoolViewModel) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val routineClassId by viewModel.routineClassId.collectAsStateWithLifecycle()
    val routineCategory by viewModel.routineExamCategory.collectAsStateWithLifecycle()
    val routineYear by viewModel.routineYear.collectAsStateWithLifecycle()
    val routineSlots by viewModel.currentRoutineSlots.collectAsStateWithLifecycle()

    var showFourSlipView by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "স্বয়ংক্রিয় পরীক্ষার রুটিন তৈরি",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("শ্রেণি নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classes) { cls ->
                        FilterChip(
                            selected = routineClassId == cls.id,
                            onClick = { viewModel.setRoutineClassId(cls.id) },
                            label = { Text(cls.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("পরীক্ষার ক্যাটাগরি:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExamCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = routineCategory == cat,
                            onClick = { viewModel.setRoutineExamCategory(cat) },
                            label = { Text(cat.labelBn) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.generateRoutine() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generate_routine_btn")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("রুটিন তৈরি করুন")
                    }

                    OutlinedButton(
                        onClick = { showFourSlipView = !showFourSlipView },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showFourSlipView) "সাধারণ ভিউ" else "A4 ৪-স্লিপ ভিউ")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (routineSlots.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.DateRange,
                title = "কোনো রুটিন তৈরি হয়নি",
                description = "উপরের শ্রেণি সিলেক্ট করে 'রুটিন তৈরি করুন' বাটনে চাপ দিন।",
                modifier = Modifier.height(200.dp)
            )
        } else if (showFourSlipView) {
            // A4 Layout: 4 Routine Slips Set in a grid
            Text(
                text = "একটি A4 পেপারে প্রিন্ট উপযোগী ৪টি রুটিন সেট লেআউট প্রিভিউ:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (slipNum in 1..4) {
                    PrintableRoutineSlipCard(
                        slipNumber = slipNum,
                        category = routineCategory,
                        year = routineYear,
                        className = classes.find { it.id == routineClassId }?.name ?: "Class 10",
                        slots = routineSlots
                    )
                }
            }
        } else {
            // Standard Single Card Routine View
            PrintableRoutineSlipCard(
                slipNumber = 1,
                category = routineCategory,
                year = routineYear,
                className = classes.find { it.id == routineClassId }?.name ?: "Class 10",
                slots = routineSlots
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun PrintableRoutineSlipCard(
    slipNumber: Int,
    category: ExamCategory,
    year: Int,
    className: String,
    slots: List<ExamRoutineSlot>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with School Logo, Title, Address
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_school_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "আইডিয়াল মডেল একাডেমি / Ideal Model Academy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ঠিকানা: ধানমন্ডি, ঢাকা-১২০৫ • ফোন: +৮৮০ ১৭০০-০০০০০০",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${category.labelBn} - $year (পরীক্ষার সময়সূচী)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "স্লিপ #$slipNumber",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Student Metadata Header Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "শ্রেণি: $className",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "রোল: [..........]  ইউনিক আইডি: [..........]",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timetable Rows
            slots.forEach { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slot.subjectName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${slot.examDate} (${slot.examDay})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = slot.examTime,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = slot.room,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Instructions & Principal Signature
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "পরীক্ষার্থীদের প্রয়োজনীয় দিকনির্দেশনা:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "১. পরীক্ষা শুরুর ১৫ মিনিট পূর্বে হলে প্রবেশ করতে হবে।\n২. প্রবেশপত্র ব্যতিত কোনো শিক্ষার্থীকে হলে প্রবেশ করতে দেওয়া হবে না।",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Dr. M. Rahman",
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "প্রধান শিক্ষক (ডিজিটাল স্বাক্ষর)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SUB-TAB 1: MARKS ENTRY SYSTEM WITH VERIFY
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksEntryTab(viewModel: SchoolViewModel) {
    val marksUniqueId by viewModel.marksUniqueIdInput.collectAsStateWithLifecycle()
    val verifiedStudent by viewModel.verifiedStudent.collectAsStateWithLifecycle()
    val marksCategory by viewModel.marksExamCategory.collectAsStateWithLifecycle()
    val marksYear by viewModel.marksExamYear.collectAsStateWithLifecycle()
    val selectedSubjectId by viewModel.marksSelectedSubjectId.collectAsStateWithLifecycle()
    val marksObtained by viewModel.marksObtainedInput.collectAsStateWithLifecycle()

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val marksList by viewModel.marks.collectAsStateWithLifecycle()

    val studentSubjects = if (verifiedStudent != null) {
        subjects.filter { it.classId == verifiedStudent?.classId }
    } else emptyList()

    val currentSubject = studentSubjects.find { it.id == selectedSubjectId }

    val studentRecordedMarks = if (verifiedStudent != null) {
        marksList.filter { it.studentId == verifiedStudent?.id && it.examCategory == marksCategory && it.year == marksYear }
    } else emptyList()

    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Verification Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "শিক্ষার্থী যাচাইকরণ প্রক্রিয়া (Student Verification)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = marksUniqueId,
                        onValueChange = { viewModel.setMarksUniqueIdInput(it) },
                        label = { Text("ইউনিক আইডি ইনপুট দিন") },
                        placeholder = { Text("যেমন: STU-2026-001") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("verify_unique_id_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.verifyStudent() },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("verify_student_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verify")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Auto-filled and locked fields
                if (verifiedStudent != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPresent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "যাচাইকৃত শিক্ষার্থী (স্বয়ংক্রিয় এন্ট্রি - সম্পাদনা যোগ্য নয়):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPresent
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("শিক্ষার্থীর নাম: ${verifiedStudent?.name}", fontWeight = FontWeight.Bold)
                            Text("পিতার নাম: ${verifiedStudent?.fatherName}", style = MaterialTheme.typography.bodySmall)
                            Text("শ্রেণি: ${verifiedStudent?.className} • ক্লাস রোল: ${verifiedStudent?.rollNo}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Text(
                        text = "নম্বর এন্ট্রি করার জন্য প্রথমে ইউনিক আইডি দিয়ে 'Verify' বাটনে ক্লিক করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Marks Entry Form
        if (verifiedStudent != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "নম্বর এন্ট্রি ফর্ম (Marks Entry Form)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("পরীক্ষার ক্যাটাগরি:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ExamCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = marksCategory == cat,
                                onClick = { viewModel.setMarksExamCategory(cat) },
                                label = { Text(cat.labelBn) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject Dropdown
                    Text("বিষয় নির্বাচন (Subject Management থেকে প্রাপ্ত):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentSubject?.let { "${it.name} (${it.type.labelBn}, স্কেল: ${it.fullMarks.toInt()})" } ?: "বিষয় নির্বাচন করুন",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            studentSubjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text("${sub.name} (${sub.type.labelBn} • পূর্ণমান: ${sub.fullMarks.toInt()} • পাস: ${sub.passMarks.toInt()})") },
                                    onClick = {
                                        viewModel.setMarksSelectedSubjectId(sub.id)
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Marks Obtained Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = marksObtained,
                            onValueChange = { viewModel.setMarksObtainedInput(it) },
                            label = { Text("প্রাপ্ত নম্বর (পূর্ণমান: ${currentSubject?.fullMarks?.toInt() ?: 100}) *") },
                            placeholder = { Text("যেমন: 85") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("marks_obtained_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Preview Grade
                        val num = marksObtained.toDoubleOrNull()
                        val full = currentSubject?.fullMarks ?: 100.0
                        val previewLetter = if (num != null) {
                            val pct = (num / full) * 100
                            when {
                                pct >= 80 -> "A+ (5.0)"
                                pct >= 70 -> "A (4.0)"
                                pct >= 60 -> "A- (3.5)"
                                pct >= 50 -> "B (3.0)"
                                pct >= 40 -> "C (2.0)"
                                pct >= 33 -> "D (1.0)"
                                else -> "F (0.0)"
                            }
                        } else "--"

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "গ্রেড: $previewLetter",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.saveStudentMarks() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_student_marks_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("নম্বর সংরক্ষণ করুন (Save Marks)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Recorded marks for this student
            Text(
                text = "${verifiedStudent?.name} এর সংরক্ষিত বিষয়ভিত্তিক নম্বর (${studentRecordedMarks.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            studentRecordedMarks.forEach { mk ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(mk.subjectName, fontWeight = FontWeight.Bold)
                            Text(
                                text = "ধরন: ${if (mk.isMandatory) "আবশ্যিক" else "ঐচ্ছিক"} • পাস মার্ক: ${mk.passMarks.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (mk.isPassed) EmeraldLight else MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "${mk.marksObtained.toInt()} / ${mk.fullMarks.toInt()} (${mk.letterGrade})",
                                fontWeight = FontWeight.Bold,
                                color = if (mk.isPassed) EmeraldPresent else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
