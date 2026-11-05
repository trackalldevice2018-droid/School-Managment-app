package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.data.model.Student
import com.example.ui.SchoolViewModel
import com.example.ui.components.AvatarInitials
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldLight
import com.example.ui.theme.CrimsonAbsent
import com.example.ui.theme.CrimsonLight
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPresent
import com.example.ui.theme.VioletLate
import com.example.ui.theme.VioletLight

@Composable
fun AttendanceScreen(
    viewModel: SchoolViewModel,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val allStudents by viewModel.students.collectAsStateWithLifecycle()
    val selectedClassId by viewModel.selectedAttendanceClassId.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedAttendanceDate.collectAsStateWithLifecycle()
    val sessionAttendance by viewModel.currentSessionAttendance.collectAsStateWithLifecycle()

    val currentClass = classes.find { it.id == selectedClassId }
    val classStudents = allStudents.filter {
        if (currentClass != null) it.grade == currentClass.grade && it.section == currentClass.section else true
    }

    val presentCount = classStudents.count { (sessionAttendance[it.id] ?: AttendanceStatus.PRESENT) == AttendanceStatus.PRESENT }
    val absentCount = classStudents.count { (sessionAttendance[it.id] ?: AttendanceStatus.PRESENT) == AttendanceStatus.ABSENT }
    val lateCount = classStudents.count { (sessionAttendance[it.id] ?: AttendanceStatus.PRESENT) == AttendanceStatus.LATE }
    val totalCount = classStudents.size
    val rate = if (totalCount > 0) ((presentCount + lateCount) * 100) / totalCount else 100

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Class Selection Row
        Text(
            text = "Select Classroom",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(classes) { cls ->
                FilterChip(
                    selected = selectedClassId == cls.id,
                    onClick = { viewModel.setAttendanceClass(cls.id) },
                    label = { Text(cls.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("class_chip_${cls.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date and Summary Metrics Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Date: $selectedDate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.markAllPresent() },
                        modifier = Modifier.testTag("mark_all_present_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("All Present", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatPill(count = presentCount, label = "Present", color = EmeraldPresent, bgColor = EmeraldLight)
                    StatPill(count = absentCount, label = "Absent", color = CrimsonAbsent, bgColor = CrimsonLight)
                    StatPill(count = lateCount, label = "Late", color = VioletLate, bgColor = VioletLight)
                    StatPill(count = rate, label = "Rate %", color = MaterialTheme.colorScheme.primary, bgColor = MaterialTheme.colorScheme.surfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student Roster
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentClass?.name ?: "Class"} Roster (${classStudents.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap status to change",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (classStudents.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.HowToReg,
                title = "No Students in this Class",
                description = "Enroll students to ${currentClass?.name} to start taking daily attendance.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(classStudents, key = { it.id }) { student ->
                    val status = sessionAttendance[student.id] ?: AttendanceStatus.PRESENT
                    AttendanceStudentRow(
                        student = student,
                        status = status,
                        onStatusSelected = { newStatus ->
                            viewModel.setSingleStudentStatus(student.id, newStatus)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Save Attendance Floating Action Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Button(
                onClick = { viewModel.saveAttendance() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_attendance_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Attendance Record (${classStudents.size} Students)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    count: Int,
    label: String,
    color: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun AttendanceStudentRow(
    student: Student,
    status: AttendanceStatus,
    onStatusSelected: (AttendanceStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarInitials(
                    name = student.fullName,
                    colorHex = student.avatarColorHex,
                    size = 40
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Roll #${student.rollNo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Status selection pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttendanceOptionButton(
                    label = "Present",
                    isSelected = status == AttendanceStatus.PRESENT,
                    selectedColor = EmeraldPresent,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.PRESENT) }
                )
                AttendanceOptionButton(
                    label = "Absent",
                    isSelected = status == AttendanceStatus.ABSENT,
                    selectedColor = CrimsonAbsent,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.ABSENT) }
                )
                AttendanceOptionButton(
                    label = "Late",
                    isSelected = status == AttendanceStatus.LATE,
                    selectedColor = VioletLate,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.LATE) }
                )
                AttendanceOptionButton(
                    label = "Excused",
                    isSelected = status == AttendanceStatus.EXCUSED,
                    selectedColor = AmberGold,
                    modifier = Modifier.weight(1f),
                    onClick = { onStatusSelected(AttendanceStatus.EXCUSED) }
                )
            }
        }
    }
}

@Composable
private fun AttendanceOptionButton(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
