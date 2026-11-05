# Oakridge Academy - School Management App

A modern, comprehensive Android application built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, and **Material Design 3** to manage school operations, student rosters, academic schedules, daily attendance, grading, and administrative announcements.

## Features

### 1. Dashboard & School Overview
- **Visual Campus Hero Banner**: High-resolution educational campus presentation.
- **Real-Time School Metrics**:
  - Total Enrolled Students
  - Faculty & Teachers Count
  - Active Classrooms & Grade Sections
  - Today's Overall Attendance Percentage
- **Quick Action Bar**:
  - One-tap navigation to take class attendance
  - Quick student enrollment
  - School bulletin announcement publisher
- **Visual Attendance Progress Bar**: Instant ratio of Present, Absent, and Late students today.
- **Today's Class Schedule Preview**: Daily period timings, subjects, and classroom numbers.

### 2. Student Directory & Profile Management
- **Search & Filters**: Instant live filtering across student names, roll numbers, and grade levels (Grade 9 through Grade 12).
- **Student Roster Cards**: Shows student roll number, grade & section, attendance rate, and fee payment status.
- **Detailed Student Profile**:
  - Personal information, email, contact phone, gender, blood group, address
  - Guardian / Emergency contact information with **one-tap direct dial** and **email composer**
  - Complete examination and grade history for each student
  - Option to remove or edit students
- **Enroll New Student**: Dialog form with validation for student and parent details.

### 3. Daily Attendance Tracker
- **Classroom Selector**: Switch between Grade 10-A, Grade 10-B, Grade 9-A, Grade 11-A, Grade 12-A, etc.
- **Date Selector**: Real-time daily log with today's date.
- **Live Attendance Summary**: Present, Absent, Late, and Attendance Rate percentage.
- **Quick "Mark All Present"**: Sets all students in the selected class to Present with a single tap.
- **Interactive Status Toggles**: Quickly set status per student to Present, Absent, Late, or Excused.
- **Save Attendance**: Records session logs and dynamically recalculates student attendance percentages.

### 4. Classrooms, Weekly Timetables & Faculty Directory
- **Classrooms Tab**:
  - Room numbers, assigned homeroom teachers, student capacity utilization
  - Expandable **Weekly Timetable** detailing Periods 1–4 with subject, teacher, and room
- **Faculty Directory Tab**:
  - Teacher profiles with department, highest qualifications, and years of experience
  - Assigned subjects and homeroom classes
  - Direct communication actions (Call and Email faculty members)
  - **Add Faculty Member**: Full dialog form to register new teachers

### 5. School Bulletin, Tuition Fees & Grade Book
- **School Announcements (Notices)**:
  - Filter notices by category: Academic, Events, Sports, Administrative
  - Priority badges: Urgent, Normal, Info
  - Audience targeting (All Students, Parents, Faculty)
  - Post new announcements with title, category, priority, and audience
- **Tuition Fees & Invoicing**:
  - Total billing, collection totals, and outstanding dues
  - Student invoice records with status pills (Paid, Pending, Partial, Overdue)
  - Quick "Mark Paid" action button
- **Academic Grade Book**:
  - Exam results with marks obtained, max marks, percentage, and automatic letter grade calculation (A+, A, B+, B, C, F)
  - Enter new grades per student and subject

## Technical Stack
- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Repository Pattern
- **State Management**: Kotlin Coroutines `StateFlow` and `combine`
- **Build System**: Gradle 9.3.1 with Android Gradle Plugin (AGP) 9.1.1
- **Target SDK**: Android 36 (Android 16 / Vanilla Ice Cream)
- **Launcher Icon**: Custom adaptive Material You launcher icon with gold and navy academy crest
