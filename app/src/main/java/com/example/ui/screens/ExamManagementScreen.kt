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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.example.util.BackupUtils
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ExamCategory
import com.example.data.model.ExamRoutineSlot
import com.example.data.model.SchoolSettings
import com.example.data.model.StudentMark
import com.example.data.model.Subject
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

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = activeSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { viewModel.setExamSubTab(0) },
                text = { Text("১. পরীক্ষার রুটিন") },
                icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier.testTag("tab_routine_generator")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { viewModel.setExamSubTab(1) },
                text = { Text("২. প্রবেশপত্র জেনারেট") },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                modifier = Modifier.testTag("tab_admit_card_generator")
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { viewModel.setExamSubTab(2) },
                text = { Text("৩. নম্বর এন্ট্রি") },
                icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                modifier = Modifier.testTag("tab_marks_entry")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (activeSubTab) {
            0 -> RoutineGeneratorTab(viewModel = viewModel)
            1 -> AdmitCardGeneratorTab(viewModel = viewModel)
            else -> MarksEntryTab(viewModel = viewModel)
        }
    }
}

// ---------------------------------------------------------------------------------
// SUB-TAB 0: EXAM ROUTINE AUTO-GENERATOR (EDITABLE & 4/6 SLIPS PRINT)
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineGeneratorTab(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val routineClassId by viewModel.routineClassId.collectAsStateWithLifecycle()
    val routineCategory by viewModel.routineExamCategory.collectAsStateWithLifecycle()
    val routineCategoryText by viewModel.routineExamCategoryText.collectAsStateWithLifecycle()
    val routineYear by viewModel.routineYear.collectAsStateWithLifecycle()
    val routineSlots by viewModel.currentRoutineSlots.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var viewMode by remember { mutableStateOf(0) } // 0: Edit & Single Card, 1: 4 Slips Preview, 2: 6 Slips Preview
    var showAddSlotDialog by remember { mutableStateOf(false) }

    val currentClass = classes.find { it.id == routineClassId }
    val currentClassName = currentClass?.name ?: "Class"
    val classSubjects = subjects.filter { it.classId == routineClassId }

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
                    text = "স্বয়ংক্রিয় পরীক্ষার রুটিন তৈরি ও সম্পাদনা",
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

                Text("পরীক্ষার ক্যাটাগরি (সম্পাদন যোগ্য):", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = routineCategoryText,
                    onValueChange = { viewModel.setRoutineExamCategoryText(it) },
                    placeholder = { Text("পরীক্ষার নাম লিখুন বা নিচের অপশন নির্বাচন করুন") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("routine_category_text_field")
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("বার্ষিক পরীক্ষা", "অর্ধবার্ষিক পরীক্ষা", "১ম সাময়িক", "২য় সাময়িক", "মডেল টেস্ট", "নির্বাচনী পরীক্ষা")) { preset ->
                        FilterChip(
                            selected = routineCategoryText == preset,
                            onClick = {
                                viewModel.setRoutineExamCategoryText(preset)
                                ExamCategory.entries.find { it.labelBn == preset }?.let { viewModel.setRoutineExamCategory(it) }
                            },
                            label = { Text(preset, fontSize = 11.5.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Auto-Generate, Add Row, Print
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.generateRoutine() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("generate_routine_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("স্বয়ংক্রিয় রুটিন তৈরি")
                    }

                    OutlinedButton(
                        onClick = { showAddSlotDialog = true },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ বিষয় যোগ")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Print Buttons: 4 or 6 Slips per A4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.printRoutineWithLayout(context, slipsPerPage = 4) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("A4 এ ৪টি রুটিন প্রিন্ট", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.printRoutineWithLayout(context, slipsPerPage = 6) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("A4 এ ৬টি রুটিন প্রিন্ট", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (routineSlots.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.DateRange,
                title = "কোনো রুটিন তৈরি করা হয়নি",
                description = "শ্রেণি ও পরীক্ষার ধরন নির্বাচন করে 'স্বয়ংক্রিয় রুটিন তৈরি' বাটনে ক্লিক করুন।",
                modifier = Modifier.height(240.dp)
            )
        } else {
            // View Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "রুটিন তালিকা (${routineSlots.size} টি বিষয়)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        label = { Text("এডিটেবল রুটিন") }
                    )
                    FilterChip(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        label = { Text("৪টি স্লিপ") }
                    )
                    FilterChip(
                        selected = viewMode == 2,
                        onClick = { viewMode = 2 },
                        label = { Text("৬টি স্লিপ") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (viewMode) {
                0 -> EditableRoutineCardView(
                    settings = settings,
                    className = currentClassName,
                    category = routineCategory,
                    year = routineYear,
                    slots = routineSlots,
                    classSubjects = classSubjects,
                    onUpdateSlot = { slotId, date, day, sub, shift ->
                        viewModel.updateRoutineSlot(slotId, date, day, sub, shift)
                    },
                    onDeleteSlot = { slotId ->
                        viewModel.deleteRoutineSlot(slotId)
                    }
                )
                1 -> SlipsRoutineA4View(
                    settings = settings,
                    className = currentClassName,
                    category = routineCategory,
                    year = routineYear,
                    routineSlots = routineSlots,
                    slipsCount = 4
                )
                else -> SlipsRoutineA4View(
                    settings = settings,
                    className = currentClassName,
                    category = routineCategory,
                    year = routineYear,
                    routineSlots = routineSlots,
                    slipsCount = 6
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Add New Slot Dialog
    if (showAddSlotDialog) {
        var inputDate by remember { mutableStateOf("28/12/$routineYear") }
        var inputDay by remember { mutableStateOf("রবিবার") }
        var inputShift by remember { mutableStateOf("সকাল") }
        var selectedSubjectName by remember { mutableStateOf(classSubjects.firstOrNull()?.name ?: "সাধারণ বিষয়") }
        var subjectDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddSlotDialog = false },
            title = { Text("রুটিনে নতুন বিষয় যুক্ত করুন", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("শ্রেণি: $currentClassName • সন: $routineYear", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = inputDate,
                        onValueChange = { inputDate = it },
                        label = { Text("পরীক্ষার তারিখ (Date) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputDay,
                        onValueChange = { inputDay = it },
                        label = { Text("পরীক্ষার বার (Day) *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column {
                        Text("শিফট নির্বাচন করুন (Shift) *", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("সকাল", "বিকাল").forEach { sft ->
                                FilterChip(
                                    selected = inputShift == sft,
                                    onClick = { inputShift = sft },
                                    label = { Text(sft) }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSubjectName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("শ্রেণির বিষয় নির্বাচন করুন *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            classSubjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(sub.name) },
                                    onClick = {
                                        selectedSubjectName = sub.name
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addRoutineSlot(selectedSubjectName, inputDate.trim(), inputDay.trim(), inputShift)
                        showAddSlotDialog = false
                    },
                    enabled = inputDate.isNotBlank() && inputDay.isNotBlank() && selectedSubjectName.isNotBlank()
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddSlotDialog = false }) { Text("বাতিল") }
            }
        )
    }
}

// ---------------------------------------------------------------------------------
// EDITABLE ROUTINE CARD VIEW (WITH 3 COLUMNS: DATE, DAY, SUBJECT DROPDOWN)
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableRoutineCardView(
    settings: SchoolSettings,
    className: String,
    category: ExamCategory,
    year: Int,
    slots: List<ExamRoutineSlot>,
    classSubjects: List<Subject>,
    onUpdateSlot: (slotId: String, date: String, day: String, subjectName: String, shift: String) -> Unit,
    onDeleteSlot: (slotId: String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        com.example.ui.components.WatermarkContainer(
            settings = settings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header: School Name, Address, Category & Year
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.example.ui.components.SchoolLogoView(
                        logoUri = settings.customLogoUri,
                        size = 50,
                        shapeCorner = 12
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = settings.schoolName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${settings.schoolAddress} • ${settings.eiinNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${category.labelBn} - $year",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Centered Bold Shape / Pill Badge for Class
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = "শ্রেণি : $className",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 1.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Table Header: 4 Columns (তারিখ, বার, শিফট, বিষয়)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("তারিখ (Date)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.1f))
                        Text("বার (Day)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.9f))
                        Text("শিফট (Shift)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.9f))
                        Text("বিষয় (Subject)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1.5f))
                        Spacer(modifier = Modifier.width(36.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Editable Table Rows
                slots.forEach { slot ->
                    var editDate by remember(slot.id, slot.examDate) { mutableStateOf(slot.examDate) }
                    var editDay by remember(slot.id, slot.examDay) { mutableStateOf(slot.examDay) }
                    var editShift by remember(slot.id, slot.shift) { mutableStateOf(slot.shift) }
                    var editSubject by remember(slot.id, slot.subjectName) { mutableStateOf(slot.subjectName) }
                    var subMenuExpanded by remember { mutableStateOf(false) }
                    var shiftMenuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 1. Date Field (Editable)
                        OutlinedTextField(
                            value = editDate,
                            onValueChange = {
                                editDate = it
                                onUpdateSlot(slot.id, it, editDay, editSubject, editShift)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.weight(1.1f)
                        )

                        // 2. Day Field (Editable)
                        OutlinedTextField(
                            value = editDay,
                            onValueChange = {
                                editDay = it
                                onUpdateSlot(slot.id, editDate, it, editSubject, editShift)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            modifier = Modifier.weight(0.9f)
                        )

                        // 3. Shift Field (Dropdown: সকাল / বিকাল)
                        Box(modifier = Modifier.weight(0.9f)) {
                            ExposedDropdownMenuBox(
                                expanded = shiftMenuExpanded,
                                onExpandedChange = { shiftMenuExpanded = !shiftMenuExpanded }
                            ) {
                                OutlinedTextField(
                                    value = editShift,
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftMenuExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = shiftMenuExpanded,
                                    onDismissRequest = { shiftMenuExpanded = false }
                                ) {
                                    listOf("সকাল", "বিকাল").forEach { sft ->
                                        DropdownMenuItem(
                                            text = { Text(sft, fontSize = 11.sp) },
                                            onClick = {
                                                editShift = sft
                                                shiftMenuExpanded = false
                                                onUpdateSlot(slot.id, editDate, editDay, editSubject, sft)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Subject Field (Dropdown from class subjects)
                        Box(modifier = Modifier.weight(1.5f)) {
                            ExposedDropdownMenuBox(
                                expanded = subMenuExpanded,
                                onExpandedChange = { subMenuExpanded = !subMenuExpanded }
                            ) {
                                OutlinedTextField(
                                    value = editSubject,
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subMenuExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = subMenuExpanded,
                                    onDismissRequest = { subMenuExpanded = false }
                                ) {
                                    classSubjects.forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub.name, fontSize = 11.5.sp) },
                                            onClick = {
                                                editSubject = sub.name
                                                subMenuExpanded = false
                                                onUpdateSlot(slot.id, editDate, editDay, sub.name, editShift)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Delete Slot Row
                        IconButton(
                            onClick = { onDeleteSlot(slot.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Row",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Routine Instructions & Principal Signature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val bullets = settings.routineInstructions
                        .lines()
                        .map { it.trim().removePrefix("•").removePrefix("-").trim() }
                        .filter { it.isNotBlank() }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "পরীক্ষার্থীদের প্রয়োজনীয় দিকনির্দেশনা:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        bullets.forEach { b ->
                            Row(
                                modifier = Modifier.padding(vertical = 1.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = b,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = settings.principalName,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = settings.principalTitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SLIPS ROUTINE A4 PREVIEW (4 SLIPS OR 6 SLIPS)
// ---------------------------------------------------------------------------------
@Composable
fun SlipsRoutineA4View(
    settings: SchoolSettings,
    className: String,
    category: ExamCategory,
    year: Int,
    routineSlots: List<ExamRoutineSlot>,
    slipsCount: Int = 4
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "A4 সাইজে সম্পূর্ণ $slipsCount টি স্লিপ প্রিন্ট লেআউট প্রিভিউ:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val rows = if (slipsCount == 6) 3 else 2
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val idx1 = (r * 2) + 1
                    val idx2 = (r * 2) + 2
                    Box(modifier = Modifier.weight(1f)) {
                        MiniRoutineSlipCard(settings, className, category, year, routineSlots, idx1)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        MiniRoutineSlipCard(settings, className, category, year, routineSlots, idx2)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun MiniRoutineSlipCard(
    settings: SchoolSettings,
    className: String,
    category: ExamCategory,
    year: Int,
    routineSlots: List<ExamRoutineSlot>,
    slipNo: Int
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = settings.schoolName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${category.labelBn} - $year • শ্রেণি: $className (স্লিপ #$slipNo)",
                fontSize = 8.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            routineSlots.take(4).forEach { s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${s.examDate} (${s.examDay} • ${s.shift})", fontSize = 7.5.sp)
                    Text(s.subjectName.take(14), fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("রোল: ____", fontSize = 8.sp)
                Text(
                    text = settings.principalTitle,
                    fontSize = 7.5.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SUB-TAB 1: AUTOMATIC ADMIT CARDS GENERATOR (BULK & 4/6 SLIPS PRINT)
// ---------------------------------------------------------------------------------
@Composable
fun AdmitCardGeneratorTab(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val admitClassId by viewModel.admitCardClassId.collectAsStateWithLifecycle()
    val admitCategory by viewModel.admitCardExamCategory.collectAsStateWithLifecycle()
    val admitCategoryText by viewModel.admitCardExamCategoryText.collectAsStateWithLifecycle()
    val admitYear by viewModel.admitCardYear.collectAsStateWithLifecycle()
    val studentsInClass by viewModel.studentsForAdmitCard.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var previewMode by remember { mutableStateOf(0) } // 0: Single Preview, 1: 4 Cards Grid, 2: 6 Cards Grid

    val currentClass = classes.find { it.id == admitClassId }
    val currentClassName = currentClass?.name ?: "Class"

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
                    text = "স্বয়ংক্রিয় প্রবেশপত্র জেনারেট (Admit Cards)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("শ্রেণি নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classes) { cls ->
                        FilterChip(
                            selected = admitClassId == cls.id,
                            onClick = { viewModel.setAdmitCardClassId(cls.id) },
                            label = { Text(cls.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("পরীক্ষার ক্যাটাগরি (সম্পাদন যোগ্য):", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = admitCategoryText,
                    onValueChange = { viewModel.setAdmitCardExamCategoryText(it) },
                    placeholder = { Text("পরীক্ষার নাম লিখুন বা নিচের অপশন নির্বাচন করুন") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admit_category_text_field")
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("বার্ষিক পরীক্ষা", "অর্ধবার্ষিক পরীক্ষা", "১ম সাময়িক", "২য় সাময়িক", "মডেল টেস্ট", "নির্বাচনী পরীক্ষা")) { preset ->
                        FilterChip(
                            selected = admitCategoryText == preset,
                            onClick = {
                                viewModel.setAdmitCardExamCategoryText(preset)
                                ExamCategory.entries.find { it.labelBn == preset }?.let { viewModel.setAdmitCardExamCategory(it) }
                            },
                            label = { Text(preset, fontSize = 11.5.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info banner: Student count
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "এই শ্রেণির মোট শিক্ষার্থী: ${studentsInClass.size} জন (স্বয়ংক্রিয় বাল্ক ডাটা ফিলাপ হবে)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bulk Print Options: 4 or 6 per A4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.printAdmitCards(context, cardsPerPage = 4) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("A4 এ ৪টি প্রবেশপত্র প্রিন্ট", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = { viewModel.printAdmitCards(context, cardsPerPage = 6) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("A4 এ ৬টি প্রবেশপত্র প্রিন্ট", fontSize = 11.5.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (studentsInClass.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.School,
                title = "কোনো শিক্ষার্থী পাওয়া যায়নি",
                description = "প্রথমে শিক্ষার্থী ব্যবস্থাপনা থেকে এই শ্রেণিতে শিক্ষার্থী নিবন্ধন বা ইমপোর্ট করুন।",
                modifier = Modifier.height(240.dp)
            )
        } else {
            // Preview Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "প্রবেশপত্র প্রিভিউ (${studentsInClass.size} জন)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = previewMode == 0,
                        onClick = { previewMode = 0 },
                        label = { Text("একক প্রিভিউ") }
                    )
                    FilterChip(
                        selected = previewMode == 1,
                        onClick = { previewMode = 1 },
                        label = { Text("৪টি লেআউট") }
                    )
                    FilterChip(
                        selected = previewMode == 2,
                        onClick = { previewMode = 2 },
                        label = { Text("৬টি লেআউট") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (previewMode) {
                0 -> {
                    // Single Student Admit Card Preview
                    val firstStudent = studentsInClass.first()
                    SingleAdmitCardView(
                        settings = settings,
                        className = currentClassName,
                        category = admitCategory,
                        year = admitYear,
                        student = firstStudent
                    )
                }
                1 -> {
                    // 4 Slips on A4 Preview
                    AdmitCardGridPreview(
                        settings = settings,
                        className = currentClassName,
                        category = admitCategory,
                        year = admitYear,
                        students = studentsInClass.take(4),
                        cardsPerPage = 4
                    )
                }
                else -> {
                    // 6 Slips on A4 Preview
                    AdmitCardGridPreview(
                        settings = settings,
                        className = currentClassName,
                        category = admitCategory,
                        year = admitYear,
                        students = studentsInClass.take(6),
                        cardsPerPage = 6
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SingleAdmitCardView(
    settings: SchoolSettings,
    className: String,
    category: ExamCategory,
    year: Int,
    student: com.example.data.model.Student
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        com.example.ui.components.WatermarkContainer(
            settings = settings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header: Madrasah/School Name, Address, Category & Year
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    com.example.ui.components.SchoolLogoView(
                        logoUri = settings.customLogoUri,
                        size = 54,
                        shapeCorner = 12
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = settings.schoolName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${settings.schoolAddress} • ${settings.eiinNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${category.labelBn} - $year",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = com.example.ui.theme.AmberGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.AmberGold)
                    ) {
                        Text(
                            text = "প্রবেশপত্র",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Auto-filled Fields (নাম, পিতার নাম, শ্রেণি, রোল)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("নাম : ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("পিতার নাম : ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = student.fatherName,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Text("শ্রেণি : ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = className,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row {
                            Text("রোল : ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = student.rollNo,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bulleted Instructions for Admit Card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "প্রবেশপত্রের নির্দেশনাবলী:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val admitBullets = settings.routineInstructions
                            .lines()
                            .map { it.trim().removePrefix("•").removePrefix("-").trim() }
                            .filter { it.isNotBlank() }
                        admitBullets.forEach { b ->
                            Row(modifier = Modifier.padding(vertical = 1.dp), verticalAlignment = Alignment.Top) {
                                Text("• ", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = b,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Signatures: Class Teacher & Principal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "................................................",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "শ্রেণি শিক্ষকের স্বাক্ষর",
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = settings.principalName,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = settings.principalTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdmitCardGridPreview(
    settings: SchoolSettings,
    className: String,
    category: ExamCategory,
    year: Int,
    students: List<com.example.data.model.Student>,
    cardsPerPage: Int
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "A4 সাইজে সম্পূর্ণ $cardsPerPage টি প্রবেশপত্র প্রিন্ট লেআউট প্রিভিউ:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val rows = if (cardsPerPage == 6) 3 else 2
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val s1 = students.getOrNull(r * 2)
                    val s2 = students.getOrNull((r * 2) + 1)
                    Box(modifier = Modifier.weight(1f)) {
                        if (s1 != null) {
                            MiniAdmitCardSlip(settings, className, category, year, s1)
                        } else {
                            Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.height(100.dp)) {}
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (s2 != null) {
                            MiniAdmitCardSlip(settings, className, category, year, s2)
                        } else {
                            Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.height(100.dp)) {}
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun MiniAdmitCardSlip(
    settings: SchoolSettings,
    className: String,
    category: ExamCategory,
    year: Int,
    student: com.example.data.model.Student
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = settings.schoolName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "প্রবেশপত্র • ${category.labelBn} - $year",
                fontSize = 8.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "নাম: ${student.name}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "শ্রেণি: $className", fontSize = 8.sp)
                Text(text = "রোল: ${student.rollNo}", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("স্বাক্ষর: ________", fontSize = 7.5.sp)
                Text(settings.principalTitle.take(12), fontSize = 7.5.sp, fontStyle = FontStyle.Italic)
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SUB-TAB 1: MARKS ENTRY SYSTEM WITH VERIFY & DUPLICATE PREVENTION
// ---------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksEntryTab(viewModel: SchoolViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val marksUniqueId by viewModel.marksUniqueIdInput.collectAsStateWithLifecycle()
    val verifiedStudent by viewModel.verifiedStudent.collectAsStateWithLifecycle()
    val marksCategory by viewModel.marksExamCategory.collectAsStateWithLifecycle()
    val marksCategoryText by viewModel.marksExamCategoryText.collectAsStateWithLifecycle()
    val marksYear by viewModel.marksExamYear.collectAsStateWithLifecycle()
    val selectedSubjectId by viewModel.marksSelectedSubjectId.collectAsStateWithLifecycle()
    val marksObtained by viewModel.marksObtainedInput.collectAsStateWithLifecycle()

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val marksList by viewModel.marks.collectAsStateWithLifecycle()
    val unenteredSubjects by viewModel.unenteredSubjectsForVerifiedStudent.collectAsStateWithLifecycle()

    var selectedBulkClassId by remember { mutableStateOf(classes.firstOrNull()?.id ?: "cls_01") }
    var selectedMarksFileName by remember { mutableStateOf<String?>(null) }
    var showSampleCsvDialog by remember { mutableStateOf(false) }

    val marksFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val csvText = stream.bufferedReader().use { it.readText() }
                    val fileName = uri.lastPathSegment ?: "marks.csv"
                    selectedMarksFileName = fileName
                    viewModel.bulkUploadClassMarks(selectedBulkClassId, csvText)
                }
            } catch (e: Exception) {
                viewModel.showMessage("ফাইল পড়ার সময় ত্রুটি হয়েছে: ${e.message}")
            }
        }
    }

    val selectedBulkClass = classes.find { it.id == selectedBulkClassId }
    val bulkClassName = selectedBulkClass?.name ?: "Class"
    val allStudents = viewModel.students.collectAsStateWithLifecycle().value
    val classStudentsCount = allStudents.count { it.classId == selectedBulkClassId }
    val classSubjects = subjects.filter { it.classId == selectedBulkClassId }

    val studentClassSubjects = if (verifiedStudent != null) {
        subjects.filter { it.classId == verifiedStudent?.classId }
    } else emptyList()

    val currentSubject = studentClassSubjects.find { it.id == selectedSubjectId }

    val studentRecordedMarks = if (verifiedStudent != null) {
        marksList.filter { it.studentId == verifiedStudent?.id && it.year == marksYear }
    } else emptyList()

    val isSubjectAlreadyEntered = studentRecordedMarks.any { it.subjectId == selectedSubjectId }
    val existingSubjectMark = studentRecordedMarks.find { it.subjectId == selectedSubjectId }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Master Exam Category & Year
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "পরীক্ষার ক্যাটাগরি ও শিক্ষাবর্ষ নির্ধারণ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("পরীক্ষার ক্যাটাগরি (সম্পাদন যোগ্য):", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = marksCategoryText,
                    onValueChange = { viewModel.setMarksExamCategoryText(it) },
                    placeholder = { Text("পরীক্ষার নাম লিখুন বা নিচের অপশন নির্বাচন করুন") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("marks_category_text_field")
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf("বার্ষিক পরীক্ষা", "অর্ধবার্ষিক পরীক্ষা", "১ম সাময়িক", "২য় সাময়িক", "মডেল টেস্ট", "নির্বাচনী পরীক্ষা")) { preset ->
                        FilterChip(
                            selected = marksCategoryText == preset,
                            onClick = {
                                viewModel.setMarksExamCategoryText(preset)
                                ExamCategory.entries.find { it.labelBn == preset }?.let { viewModel.setMarksExamCategory(it) }
                            },
                            label = { Text(preset, fontSize = 11.5.sp) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Class-based Bulk Marks Upload (DIRECTLY UNDER EXAM CATEGORY)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "শ্রেণি ভিত্তিক বাল্ক মার্কস আপলোড (Bulk Marks Upload)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "পরীক্ষা: $marksCategoryText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("শ্রেণি নির্বাচন করুন:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classes) { cls ->
                        FilterChip(
                            selected = selectedBulkClassId == cls.id,
                            onClick = { selectedBulkClassId = cls.id },
                            label = { Text(cls.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "শ্রেণি: $bulkClassName • শিক্ষার্থী: $classStudentsCount জন • বিষয়: ${classSubjects.size} টি",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                if (selectedMarksFileName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldPresent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "আপলোডকৃত ফাইল: $selectedMarksFileName",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPresent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Direct File Upload & Google Drive Import Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { marksFilePickerLauncher.launch("*/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("bulk_marks_csv_file_upload_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📂 সরাসরি CSV আপলোড", fontSize = 11.5.sp)
                    }

                    Button(
                        onClick = { marksFilePickerLauncher.launch("*/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("google_drive_marks_import_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("☁️ ড্রাইভ থেকে ইম্পোর্ট", fontSize = 11.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Export to CSV & Google Drive Backup Button
                    Button(
                        onClick = {
                            val csvData = viewModel.exportClassMarksCsv(selectedBulkClassId)
                            val fileName = "marks_${bulkClassName}_${marksCategoryText.replace(" ", "_")}.csv"
                            BackupUtils.shareCsvToDriveOrSheets(
                                context = context,
                                fileName = fileName,
                                csvContent = csvData,
                                title = "গুগল ড্রাইভ বা শিটসে ব্যাকআপ ($bulkClassName)"
                            )
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("☁️ ড্রাইভ ও শিটসে ব্যাকআপ", fontSize = 11.5.sp)
                    }

                    // View Sample CSV format
                    OutlinedButton(
                        onClick = { showSampleCsvDialog = true },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নমুনা CSV ফরম্যাট", fontSize = 11.5.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Verification Section (Single Student)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "একক শিক্ষার্থী যাচাই ও নম্বর এন্ট্রি (Individual Verification)",
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
                        label = { Text("স্টুডেন্ট আইডি ইনপুট দিন") },
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
                        text = "একক নম্বর এন্ট্রি করার জন্য প্রথমে স্টুডেন্ট আইডি দিয়ে 'Verify' বাটনে ক্লিক করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Marks Entry Form for Verified Student
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

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("পরীক্ষার ক্যাটাগরি: $marksCategoryText", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(10.dp))

                    // All subjects completed indicator
                    if (unenteredSubjects.isEmpty() && studentClassSubjects.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সকল বিষয়ের নম্বর এন্ট্রি সম্পন্ন হয়েছে! কোনো নতুন বিষয় বাকি নেই। প্রয়োজনে নিচে তালিকা থেকে নম্বর আপডেট করতে পারেন।",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Duplicate Warning / Update Info
                    if (isSubjectAlreadyEntered && existingSubjectMark != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "সতর্কতা: '${existingSubjectMark.subjectName}' এর নম্বর ইতোমধ্যে এন্ট্রি রয়েছে (${existingSubjectMark.marksObtained.toInt()})। নতুন নম্বর দিলে পূর্বের নম্বর আপডেট হয়ে যাবে। ডুপ্লিকেট এন্ট্রি হবে না।",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Subject Dropdown
                    Text("বিষয় নির্বাচন (ডুপ্লিকেট এন্ট্রি রোধ সহ):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = subjectDropdownExpanded,
                        onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentSubject?.let { sub ->
                                val status = if (studentRecordedMarks.any { it.subjectId == sub.id }) " [ইতোমধ্যে এন্ট্রি করা]" else " [নতুন এন্ট্রি]"
                                "${sub.name} (${sub.type.labelBn}, স্কেল: ${sub.fullMarks.toInt()})$status"
                            } ?: "বিষয় নির্বাচন করুন",
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
                            studentClassSubjects.forEach { sub ->
                                val alreadyEntered = studentRecordedMarks.any { it.subjectId == sub.id }
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${sub.name} (${sub.type.labelBn} • পূর্ণমান: ${sub.fullMarks.toInt()})")
                                            if (alreadyEntered) {
                                                Text(" (এন্ট্রি সম্পন্ন)", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            } else {
                                                Text(" (বাকি আছে)", color = EmeraldPresent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setMarksSelectedSubjectId(sub.id)
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val full = currentSubject?.fullMarks ?: 100.0
                    val num = marksObtained.toDoubleOrNull()
                    val isValidMarks = num != null && num in 0.0..full

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
                            isError = marksObtained.isNotBlank() && !isValidMarks,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("marks_obtained_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Preview Grade
                        val previewLetter = if (num != null) {
                            if (num < 0 || num > full) {
                                "অবৈধ"
                            } else {
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
                            }
                        } else "--"

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isValidMarks || marksObtained.isBlank()) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "গ্রেড: $previewLetter",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isValidMarks || marksObtained.isBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    if (marksObtained.isNotBlank() && !isValidMarks) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ নম্বর অবশ্যই ০ থেকে ${full.toInt()} এর মধ্যে হতে হবে!",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.saveStudentMarks() },
                        enabled = isValidMarks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_student_marks_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSubjectAlreadyEntered) "নম্বর আপডেট করুন (Update Marks)" else "নম্বর সংরক্ষণ করুন (Save Marks)")
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mk.subjectName, fontWeight = FontWeight.Bold)
                            Text(
                                text = "ধরন: ${if (mk.isMandatory) "আবশ্যিক" else "ঐচ্ছিক"} • পাস মার্ক: ${mk.passMarks.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
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

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = {
                                    viewModel.setMarksSelectedSubjectId(mk.subjectId)
                                    viewModel.setMarksObtainedInput(mk.marksObtained.toInt().toString())
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Marks", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showSampleCsvDialog) {
        val sampleFormat = "UniqueId,Roll,Bangla,English,Mathematics,Science,Islam\n" +
                "STU-2026-001,1,85,78,92,88,95\n" +
                "STU-2026-002,2,75,82,88,80,90\n\n" +
                "# অথবা কলাম ফরম্যাট:\n" +
                "# UniqueId/Roll, SubjectName, MarksObtained\n" +
                "STU-2026-001,বাংলা,85\n" +
                "STU-2026-001,ইংরেজি,78"

        AlertDialog(
            onDismissRequest = { showSampleCsvDialog = false },
            title = { Text("নমুনা মার্কস CSV ফাইল ফরম্যাট", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "শ্রেণি ভিত্তিক বাল্ক মার্কস আপলোডের জন্য নিচের যেকোনো একটি ফরম্যাটে .csv ফাইল তৈরি করুন:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sampleFormat,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "টিপস:\n১. ফাইলের নামের শেষে .csv থাকতে হবে।\n২. প্রথম লাইনে কলাম নাম (বিষয়ের নাম বা কোড) উল্লেখ থাকতে পারে।\n৩. রোল নম্বর অথবা স্টুডেন্ট আইডি দিয়ে স্বয়ংক্রিয়ভাবে শিক্ষার্থী চিহ্নিত হবে।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(sampleFormat))
                        viewModel.showMessage("নমুনা CSV ক্লিপবোর্ডে কপি হয়েছে")
                        showSampleCsvDialog = false
                    }
                ) {
                    Text("ক্লিপবোর্ডে কপি করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSampleCsvDialog = false }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
}
