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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ClassMeritRank
import com.example.data.model.ExamCategory
import com.example.data.model.StudentMarksheet
import com.example.ui.SchoolViewModel
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent

@Composable
fun ResultSystemScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val activeSubTab by viewModel.resultSubTab.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = activeSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { viewModel.setResultSubTab(0) },
                text = { Text("ইন্ডিভিজুয়াল মার্কশীট") },
                icon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null) },
                modifier = Modifier.testTag("tab_individual_marksheet")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { viewModel.setResultSubTab(1) },
                text = { Text("শ্রেণিভিত্তিক মেধাতালিকা") },
                icon = { Icon(Icons.Default.Leaderboard, contentDescription = null) },
                modifier = Modifier.testTag("tab_class_merit_list")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (activeSubTab == 0) {
            IndividualMarksheetSubTab(viewModel = viewModel)
        } else {
            ClassMeritListSubTab(viewModel = viewModel)
        }
    }
}

// ---------------------------------------------------------------------------------
// SUB-TAB 0: INDIVIDUAL MARKSHEET (A4 WITH WATERMARK)
// ---------------------------------------------------------------------------------
@Composable
fun IndividualMarksheetSubTab(viewModel: SchoolViewModel) {
    val searchUniqueId by viewModel.resultSearchUniqueId.collectAsStateWithLifecycle()
    val examCategory by viewModel.resultExamCategory.collectAsStateWithLifecycle()
    val currentMarksheet by viewModel.currentMarksheet.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Search Box Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ফলাফল অনুসন্ধান (Search Marksheet)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("পরীক্ষার ক্যাটাগরি:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExamCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = examCategory == cat,
                            onClick = { viewModel.setResultExamCategory(cat) },
                            label = { Text(cat.labelBn) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchUniqueId,
                        onValueChange = { viewModel.setResultSearchUniqueId(it) },
                        label = { Text("শিক্ষার্থীর ইউনিক আইডি") },
                        placeholder = { Text("STU-2026-001") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("marksheet_search_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { viewModel.searchIndividualMarksheet() },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("সার্চ")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (currentMarksheet == null) {
            EmptyStateView(
                icon = Icons.Default.Grade,
                title = "কোনো মার্কশীট প্রদর্শিত হচ্ছে না",
                description = "ইউনিক আইডি ও পরীক্ষার ক্যাটাগরি দিয়ে সার্চ করুন।",
                modifier = Modifier.height(200.dp)
            )
        } else {
            OfficialA4MarksheetView(marksheet = currentMarksheet!!)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun OfficialA4MarksheetView(marksheet: StudentMarksheet) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // CENTRAL WATERMARK (প্রতিষ্ঠানের লোগোর জলছাপ)
            Image(
                painter = painterResource(id = R.drawable.ic_school_logo),
                contentDescription = "Watermark",
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.Center)
                    .alpha(0.08f) // Subtle institutional watermark
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Institution Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_school_logo),
                        contentDescription = "School Logo",
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "আইডিয়াল মডেল একাডেমি / Ideal Model Academy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "EIIN: 134567 • ধানমন্ডি, ঢাকা-১২০৫ • বাংলাদেশ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "একাডেমিক ট্রান্সক্রিপ্ট / মার্কশীট (MARKSHEET)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                // Exam & Student Metadata Table Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("পরীক্ষা: ${marksheet.examCategory.labelBn} (${marksheet.year})", fontWeight = FontWeight.Bold)
                            Text("ইউনিক আইডি: ${marksheet.student.uniqueId}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("শিক্ষার্থীর নাম: ${marksheet.student.name}", style = MaterialTheme.typography.bodySmall)
                            Text("শ্রেণি: ${marksheet.student.className} • রোল: ${marksheet.student.rollNo}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("পিতার নাম: ${marksheet.student.fatherName}", style = MaterialTheme.typography.bodySmall)
                            Text("মাতার নাম: ${marksheet.student.motherName}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("জন্ম নিবন্ধন: ${marksheet.student.birthRegNo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subject-wise Marks Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("বিষয় (Subject)", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(2f))
                    Text("ধরন", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text("মোট", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("প্রাপ্ত", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("গ্রেড", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("জিপি", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                }

                // Marks Rows
                marksheet.subjectMarks.forEach { mk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mk.subjectName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                        Text(if (mk.isMandatory) "আবশ্যিক" else "ঐচ্ছিক", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text("${mk.fullMarks.toInt()}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text(
                            text = "${mk.marksObtained.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (mk.isPassed) MaterialTheme.colorScheme.onSurface else CrimsonAbsent,
                            modifier = Modifier.weight(0.7f),
                            textAlign = TextAlign.Center
                        )
                        Text(mk.letterGrade, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                        Text("${mk.gradePoint}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Final Summary Box (Total, GPA, Grade, Pass/Fail)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (marksheet.isPassed) EmeraldLight else MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মোট প্রাপ্ত নম্বর", style = MaterialTheme.typography.labelSmall)
                            Text("${marksheet.totalMarks.toInt()} / ${marksheet.totalFullMarks.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("গ্রেড পয়েন্ট (GPA)", style = MaterialTheme.typography.labelSmall)
                            Text("${marksheet.gpa}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (marksheet.isPassed) EmeraldPresent else MaterialTheme.colorScheme.error)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("চূড়ান্ত গ্রেড", style = MaterialTheme.typography.labelSmall)
                            Text(marksheet.letterGrade, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (marksheet.isPassed) EmeraldPresent else MaterialTheme.colorScheme.error)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ফলাফল স্ট্যাটাস", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (marksheet.isPassed) "উত্তীর্ণ (PASSED)" else "অনুত্তীর্ণ (FAILED)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (marksheet.isPassed) EmeraldPresent else CrimsonAbsent
                            )
                        }
                    }
                }

                if (!marksheet.isPassed && marksheet.failedMandatorySubjects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ফলাফল অনুত্তীর্ণের কারণ: আবশ্যিক বিষয় (${marksheet.failedMandatorySubjects.joinToString(", ")}) এ প্রয়োজনীয় পাস নম্বর অর্জিত হয়নি।",
                        style = MaterialTheme.typography.labelSmall,
                        color = CrimsonAbsent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Signatures
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("শ্রেণি শিক্ষকের স্বাক্ষর", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("------------------------", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Dr. M. Rahman",
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("প্রধান শিক্ষক (অধ্যক্ষ)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("আইডিয়াল মডেল একাডেমি", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// SUB-TAB 1: CLASS-WISE MERIT LIST (মেধাতালিকা)
// ---------------------------------------------------------------------------------
@Composable
fun ClassMeritListSubTab(viewModel: SchoolViewModel) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val meritClassId by viewModel.meritClassId.collectAsStateWithLifecycle()
    val meritCategory by viewModel.meritExamCategory.collectAsStateWithLifecycle()
    val meritYear by viewModel.meritYear.collectAsStateWithLifecycle()
    val meritList by viewModel.classMeritList.collectAsStateWithLifecycle()

    val currentClass = classes.find { it.id == meritClassId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "শ্রেণিভিত্তিক ফলাফল ও মেধা তালিকা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("শ্রেণি নির্বাচন:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(classes) { cls ->
                        FilterChip(
                            selected = meritClassId == cls.id,
                            onClick = { viewModel.setMeritClassId(cls.id) },
                            label = { Text(cls.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("পরীক্ষা:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ExamCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = meritCategory == cat,
                            onClick = { viewModel.setMeritExamCategory(cat) },
                            label = { Text(cat.labelBn) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentClass?.name ?: "শ্রেণি"} - মেধাতালিকা (${meritList.size} জন)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "${meritCategory.labelBn} • $meritYear",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (meritList.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.EmojiEvents,
                title = "কোনো ফলাফল পাওয়া যায়নি",
                description = "এই শ্রেণির জন্য নম্বর এন্ট্রি সম্পন্ন হলে স্বয়ংক্রিয়ভাবে মেধা তালিকা তৈরি হবে।",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(meritList, key = { it.student.id }) { rankItem ->
                    MeritRankCard(rank = rankItem)
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun MeritRankCard(rank: ClassMeritRank) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when (rank.position) {
                    1 -> AmberGold
                    2 -> Color(0xFF94A3B8)
                    3 -> Color(0xFFD97706)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "${rank.position}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (rank.position <= 3) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rank.student.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "রোল: ${rank.student.rollNo} • আইডি: ${rank.student.uniqueId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = rank.statusText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (rank.isPassed) EmeraldPresent else CrimsonAbsent
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "GPA: ${rank.gpa}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (rank.isPassed) EmeraldPresent else CrimsonAbsent
                )
                Text(
                    text = "মোট নম্বর: ${rank.totalMarks.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
