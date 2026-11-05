package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.NoticePriority
import com.example.data.model.SchoolClass
import com.example.data.model.Student

@Composable
fun AddStudentDialog(
    classes: List<SchoolClass>,
    onDismiss: () -> Unit,
    onConfirm: (
        firstName: String,
        lastName: String,
        rollNo: String,
        grade: String,
        section: String,
        parentName: String,
        parentPhone: String,
        parentEmail: String,
        gender: String
    ) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("Grade 10") }
    var selectedSection by remember { mutableStateOf("A") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }

    val gradeOptions = listOf("Grade 9", "Grade 10", "Grade 11", "Grade 12")
    val sectionOptions = listOf("A", "B")
    val genderOptions = listOf("Male", "Female", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enroll New Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_first_name_input")
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_last_name_input")
                )

                OutlinedTextField(
                    value = rollNo,
                    onValueChange = { rollNo = it },
                    label = { Text("Roll Number (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_roll_no_input")
                )

                Text(
                    text = "Select Grade",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    gradeOptions.forEach { grade ->
                        FilterChip(
                            selected = selectedGrade == grade,
                            onClick = { selectedGrade = grade },
                            label = { Text(grade) }
                        )
                    }
                }

                Text(
                    text = "Select Section",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sectionOptions.forEach { sec ->
                        FilterChip(
                            selected = selectedSection == sec,
                            onClick = { selectedSection = sec },
                            label = { Text("Section $sec") }
                        )
                    }
                }

                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    genderOptions.forEach { gen ->
                        FilterChip(
                            selected = gender == gen,
                            onClick = { gender = gen },
                            label = { Text(gen) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Parent / Guardian Contact",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent / Guardian Name *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_name_input")
                )

                OutlinedTextField(
                    value = parentPhone,
                    onValueChange = { parentPhone = it },
                    label = { Text("Emergency Phone Number *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parent_phone_input")
                )

                OutlinedTextField(
                    value = parentEmail,
                    onValueChange = { parentEmail = it },
                    label = { Text("Parent Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() && parentName.isNotBlank()) {
                        onConfirm(
                            firstName.trim(),
                            lastName.trim(),
                            rollNo.trim(),
                            selectedGrade,
                            selectedSection,
                            parentName.trim(),
                            parentPhone.trim(),
                            parentEmail.trim(),
                            gender
                        )
                    }
                },
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && parentName.isNotBlank(),
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text("Enroll Student")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddTeacherDialog(
    classes: List<SchoolClass>,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        department: String,
        subjects: String,
        assignedClass: String,
        email: String,
        phone: String,
        qualification: String,
        experienceYears: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Mathematics") }
    var subjects by remember { mutableStateOf("") }
    var assignedClass by remember { mutableStateOf("Grade 10-A") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("M.Sc. Education") }
    var experienceYearsText by remember { mutableStateOf("5") }

    val departmentOptions = listOf("Mathematics", "Science", "English Literature", "Social Studies", "Computer Science", "Arts & Athletics")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Faculty Member",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name (with Title) *") },
                    placeholder = { Text("Dr. Jane Doe") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_name_input")
                )

                Text(
                    text = "Department",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    departmentOptions.take(3).forEach { dept ->
                        FilterChip(
                            selected = department == dept,
                            onClick = { department = dept },
                            label = { Text(dept, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    departmentOptions.drop(3).forEach { dept ->
                        FilterChip(
                            selected = department == dept,
                            onClick = { department = dept },
                            label = { Text(dept, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subjects,
                    onValueChange = { subjects = it },
                    label = { Text("Subjects (comma separated) *") },
                    placeholder = { Text("Calculus, Geometry") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = assignedClass,
                    onValueChange = { assignedClass = it },
                    label = { Text("Homeroom Class") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("Highest Degree / Qualification") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = experienceYearsText,
                        onValueChange = { experienceYearsText = it },
                        label = { Text("Experience (Years)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("School Email (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val years = experienceYearsText.toIntOrNull() ?: 3
                        onConfirm(
                            name.trim(),
                            department,
                            subjects.ifBlank { department },
                            assignedClass,
                            email.trim(),
                            phone.trim(),
                            qualification.trim(),
                            years
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_teacher_button")
            ) {
                Text("Add Faculty")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddNoticeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        content: String,
        category: String,
        priority: NoticePriority,
        targetAudience: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Academic") }
    var priority by remember { mutableStateOf(NoticePriority.NORMAL) }
    var targetAudience by remember { mutableStateOf("All Students & Faculty") }

    val categoryOptions = listOf("Academic", "Events", "Sports", "Administrative", "Holidays")
    val priorityOptions = NoticePriority.entries

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Post School Announcement",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Announcement Title *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_title_input")
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryOptions.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priorityOptions.forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = targetAudience,
                    onValueChange = { targetAudience = it },
                    label = { Text("Target Audience") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Announcement Details *") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notice_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title.trim(), content.trim(), category, priority, targetAudience.trim())
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank(),
                modifier = Modifier.testTag("save_notice_button")
            ) {
                Text("Publish Notice")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddGradeDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (
        studentId: String,
        subject: String,
        examName: String,
        marksObtained: Double,
        maxMarks: Double,
        remarks: String
    ) -> Unit
) {
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: "") }
    var subject by remember { mutableStateOf("Mathematics") }
    var examName by remember { mutableStateOf("Mid-Term Exam") }
    var marksObtainedText by remember { mutableStateOf("90") }
    var maxMarksText by remember { mutableStateOf("100") }
    var remarks by remember { mutableStateOf("") }

    val subjectOptions = listOf("Mathematics", "Physics", "English Literature", "World History", "Chemistry", "Computer Science")
    val examOptions = listOf("Mid-Term Exam", "Final Exam", "Unit Test 1", "Practical Lab")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Enter Exam Grade",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Student",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    students.take(6).forEach { std ->
                        FilterChip(
                            selected = selectedStudentId == std.id,
                            onClick = { selectedStudentId = std.id },
                            label = { Text("${std.fullName} (${std.classIdentifier})") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Text(
                    text = "Subject",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjectOptions.take(3).forEach { sub ->
                        FilterChip(
                            selected = subject == sub,
                            onClick = { subject = sub },
                            label = { Text(sub, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjectOptions.drop(3).forEach { sub ->
                        FilterChip(
                            selected = subject == sub,
                            onClick = { subject = sub },
                            label = { Text(sub, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = marksObtainedText,
                        onValueChange = { marksObtainedText = it },
                        label = { Text("Score *") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("marks_obtained_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = maxMarksText,
                        onValueChange = { maxMarksText = it },
                        label = { Text("Max Marks *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Teacher Remarks (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val obtained = marksObtainedText.toDoubleOrNull() ?: 0.0
                    val max = maxMarksText.toDoubleOrNull() ?: 100.0
                    if (selectedStudentId.isNotBlank() && max > 0) {
                        onConfirm(selectedStudentId, subject, examName, obtained, max, remarks.trim())
                    }
                },
                enabled = selectedStudentId.isNotBlank() && marksObtainedText.isNotBlank(),
                modifier = Modifier.testTag("save_grade_button")
            ) {
                Text("Save Grade")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
