package com.example.util

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.model.ClassMeritRank
import com.example.data.model.ExamCategory
import com.example.data.model.ExamRoutineSlot
import com.example.data.model.SchoolSettings
import com.example.data.model.Student
import com.example.data.model.StudentMarksheet

object PrintUtils {

    fun printHtml(context: Context, htmlContent: String, jobName: String) {
        try {
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    try {
                        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                        if (printManager != null) {
                            val printAdapter = webView.createPrintDocumentAdapter(jobName)
                            val printAttributes = PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                .build()
                            printManager.print(jobName, printAdapter, printAttributes)
                        } else {
                            android.widget.Toast.makeText(context, "প্রিন্ট সেবা এই ডিভাইসে পাওয়া যায়নি", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, "প্রিন্ট করতে সমস্যা হয়েছে: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "প্রিন্ট প্রক্রিয়া শুরু করা যায়নি: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun getLogoBase64(settings: SchoolSettings): String? {
        val uriStr = settings.customLogoUri ?: return null
        return try {
            val file = java.io.File(uriStr)
            if (file.exists()) {
                val bytes = file.readBytes()
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                "data:image/png;base64,$base64"
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun generateMarksheetHtml(settings: SchoolSettings, marksheet: StudentMarksheet): String {
        val logoBase64 = getLogoBase64(settings)
        val logoImgTag = if (logoBase64 != null) {
            """<img src="$logoBase64" style="height: 52px; width: 52px; object-fit: contain; vertical-align: middle; margin-right: 12px; border-radius: 6px;">"""
        } else ""

        val watermarkHtml = if (settings.showWatermark) {
            if (logoBase64 != null) {
                """<div class="watermark"><img src="$logoBase64" style="width: 280px; opacity: ${settings.watermarkOpacity};"></div>"""
            } else {
                """<div class="watermark" style="font-size: 46px; font-weight: 900; color: #1e3a8a; opacity: ${settings.watermarkOpacity}; width: 90%;">${settings.schoolName}</div>"""
            }
        } else ""

        val rowsHtml = StringBuilder()
        marksheet.subjectMarks.forEach { mk ->
            val statusColor = if (mk.isPassed) "#000000" else "#dc2626"
            rowsHtml.append(
                """
                <tr>
                    <td style="padding: 7px 10px; border: 1px solid #cbd5e1; font-weight: 600;">${mk.subjectName}</td>
                    <td style="padding: 7px 10px; border: 1px solid #cbd5e1; text-align: center;">${if (mk.isMandatory) "আবশ্যিক" else "ঐচ্ছিক"}</td>
                    <td style="padding: 7px 10px; border: 1px solid #cbd5e1; text-align: center;">${mk.fullMarks.toInt()}</td>
                    <td style="padding: 7px 10px; border: 1px solid #cbd5e1; text-align: center; color: $statusColor; font-weight: bold;">${mk.marksObtained.toInt()}</td>
                    <td style="padding: 7px 10px; border: 1px solid #cbd5e1; text-align: center; font-weight: bold;">${mk.letterGrade}</td>
                    <td style="padding: 7px 10px; border: 1px solid #cbd5e1; text-align: center;">${mk.gradePoint}</td>
                </tr>
                """.trimIndent()
            )
        }

        val passStatusText = if (marksheet.isPassed) "উত্তীর্ণ (PASSED)" else "অনুত্তীর্ণ (FAILED)"
        val passStatusColor = if (marksheet.isPassed) "#059669" else "#dc2626"
        val examTitle = marksheet.customCategoryName.ifBlank { marksheet.examCategory.labelBn }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Academic Transcript - ${marksheet.student.name}</title>
            <style>
                @page { size: A4 portrait; margin: 8mm; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    color: #1e293b;
                    margin: 0;
                    padding: 0;
                }
                .container {
                    border: 2.5px solid #1e3a8a;
                    padding: 20px;
                    border-radius: 8px;
                    position: relative;
                    min-height: 275mm;
                    box-sizing: border-box;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                }
                .watermark {
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    pointer-events: none;
                    user-select: none;
                    text-align: center;
                    z-index: 0;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #1e3a8a;
                    padding-bottom: 10px;
                    margin-bottom: 12px;
                }
                .header-flex {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-bottom: 6px;
                }
                .school-name {
                    font-size: 22px;
                    font-weight: bold;
                    color: #1e3a8a;
                    margin: 0 0 2px 0;
                }
                .school-sub {
                    font-size: 11.5px;
                    color: #64748b;
                    margin: 0 0 4px 0;
                }
                .doc-title {
                    display: inline-block;
                    background: #e0e7ff;
                    padding: 4px 18px;
                    border-radius: 4px;
                    font-size: 14px;
                    font-weight: bold;
                    color: #1e3a8a;
                    letter-spacing: 0.5px;
                    margin-top: 4px;
                }
                .meta-box {
                    background: #f8fafc;
                    border: 1px solid #cbd5e1;
                    border-radius: 6px;
                    padding: 10px 14px;
                    margin-bottom: 14px;
                }
                .meta-table { width: 100%; border-collapse: collapse; }
                .meta-table td { padding: 3px 6px; font-size: 12.5px; }
                .meta-label { color: #64748b; font-weight: 600; width: 18%; }
                .meta-val { font-weight: bold; color: #0f172a; width: 32%; }
                .data-table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-bottom: 14px;
                    font-size: 12.5px;
                }
                .data-table th {
                    background: #1e3a8a;
                    color: #ffffff;
                    padding: 7px;
                    border: 1px solid #1e3a8a;
                    font-size: 12px;
                }
                .summary-table {
                    width: 100%;
                    border: 1.5px solid #cbd5e1;
                    background: #f8fafc;
                    border-radius: 6px;
                    text-align: center;
                    margin-bottom: 16px;
                    padding: 8px;
                }
                .summary-table td {
                    font-size: 12px;
                    color: #475569;
                    padding: 4px;
                }
                .summary-table b {
                    display: block;
                    font-size: 15px;
                    margin-top: 2px;
                }
                .signatures {
                    margin-top: auto;
                    display: table;
                    width: 100%;
                    padding-top: 20px;
                }
                .sig-cell {
                    display: table-cell;
                    width: 50%;
                    text-align: center;
                    vertical-align: bottom;
                }
                .sig-line {
                    display: inline-block;
                    border-top: 1px dashed #64748b;
                    width: 180px;
                    padding-top: 4px;
                    font-size: 12px;
                    font-weight: 600;
                }
            </style>
        </head>
        <body>
            <div class="container">
                $watermarkHtml
                <div style="position: relative; z-index: 1;">
                    <div class="header">
                        <div class="header-flex">
                            $logoImgTag
                            <div>
                                <h1 class="school-name">${settings.schoolName}</h1>
                                <p class="school-sub">${settings.schoolAddress} • EIIN: ${settings.eiinNumber} • হেল্পলাইন: ${settings.contactPhone}</p>
                            </div>
                        </div>
                        <div class="doc-title">অফিসিয়াল একাডেমিক ট্রান্সক্রিপ্ট / মার্কশীট (ACADEMIC TRANSCRIPT)</div>
                    </div>

                    <div class="meta-box">
                        <table class="meta-table">
                            <tr>
                                <td class="meta-label">শিক্ষার্থীর নাম:</td>
                                <td class="meta-val">${marksheet.student.name}</td>
                                <td class="meta-label">স্টুডেন্ট আইডি:</td>
                                <td class="meta-val">${marksheet.student.uniqueId}</td>
                            </tr>
                            <tr>
                                <td class="meta-label">শ্রেণি ও রোল:</td>
                                <td class="meta-val">${marksheet.student.className} (রোল: ${marksheet.student.rollNo})</td>
                                <td class="meta-label">পরীক্ষার নাম:</td>
                                <td class="meta-val">$examTitle (${marksheet.year})</td>
                            </tr>
                            <tr>
                                <td class="meta-label">পিতার নাম:</td>
                                <td class="meta-val">${marksheet.student.fatherName}</td>
                                <td class="meta-label">মাতার নাম:</td>
                                <td class="meta-val">${marksheet.student.motherName}</td>
                            </tr>
                            <tr>
                                <td class="meta-label">জন্ম নিবন্ধন নম্বর:</td>
                                <td class="meta-val">${marksheet.student.birthRegNo}</td>
                                <td class="meta-label">জন্ম তারিখ:</td>
                                <td class="meta-val">${marksheet.student.dateOfBirth}</td>
                            </tr>
                            <tr>
                                <td class="meta-label">ঠিকানা:</td>
                                <td class="meta-val" colspan="3">${marksheet.student.address}</td>
                            </tr>
                        </table>
                    </div>

                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>বিষয় (Subject)</th>
                                <th>ধরন</th>
                                <th>পূর্ণমান</th>
                                <th>প্রাপ্ত নম্বর</th>
                                <th>লেটার গ্রেড</th>
                                <th>গ্রেড পয়েন্ট</th>
                            </tr>
                        </thead>
                        <tbody>
                            $rowsHtml
                        </tbody>
                    </table>

                    <div style="border: 1px solid #cbd5e1; background: #f8fafc; border-radius: 6px; padding: 10px; margin-bottom: 12px;">
                        <table style="width: 100%; text-align: center;">
                            <tr>
                                <td style="font-size: 11.5px; color: #64748b;">মোট প্রাপ্ত নম্বর: <b style="font-size: 14.5px; color: #000;">${marksheet.totalMarks.toInt()} / ${marksheet.totalFullMarks.toInt()}</b></td>
                                <td style="font-size: 11.5px; color: #64748b;">জিপিএ (GPA): <b style="font-size: 15px; color: $passStatusColor;">${marksheet.gpa}</b></td>
                                <td style="font-size: 11.5px; color: #64748b;">চূড়ান্ত গ্রেড: <b style="font-size: 15px; color: $passStatusColor;">${marksheet.letterGrade}</b></td>
                                <td style="font-size: 11.5px; color: #64748b;">মেধা অবস্থান (Rank): <b style="font-size: 15px; color: #1e3a8a;">${if (marksheet.classMeritPosition > 0) "${marksheet.classMeritPosition}ম স্থান" else "--"}</b></td>
                                <td style="font-size: 11.5px; color: #64748b;">ফলাফল: <b style="font-size: 14.5px; color: $passStatusColor;">$passStatusText</b></td>
                            </tr>
                        </table>
                    </div>

                    ${if (!marksheet.isPassed && marksheet.failedMandatorySubjects.isNotEmpty()) """
                    <div style="color: #dc2626; font-size: 11px; font-weight: bold; margin-bottom: 12px; background: #fee2e2; padding: 6px 10px; border-radius: 4px;">
                        * অনুত্তীর্ণের কারণ: আবশ্যিক বিষয় (${marksheet.failedMandatorySubjects.joinToString(", ")}) এ পাশ নম্বর অর্জিত হয়নি।
                    </div>
                    """ else ""}

                    <div style="font-size: 10px; color: #475569; margin-top: 6px;">
                        <b>নির্দেশনা ও নিয়মাবলী:</b> ৫০ নম্বরের বিষয়ে ১৭ এবং ১০০ নম্বরের বিষয়ে ৩৩ পাশ মার্কস হিসেবে গণ্য। ফলাফল সংক্রান্ত যেকোনো তথ্যের জন্য প্রধান শিক্ষকের কার্যালয়ে যোগাযোগ করুন।
                    </div>

                    <div class="signatures">
                        <div class="sig-cell">
                            <div class="sig-line">শ্রেণি শিক্ষকের স্বাক্ষর</div>
                        </div>
                        <div class="sig-cell">
                            <div style="font-style: italic; font-weight: bold; color: #1e3a8a; margin-bottom: 2px;">${settings.principalName}</div>
                            <div class="sig-line">${settings.principalTitle}<br><span style="font-size: 9.5px; color: #64748b;">${settings.schoolName}</span></div>
                        </div>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateClassMeritListHtml(
        settings: SchoolSettings,
        className: String,
        categoryName: String,
        year: Int,
        meritList: List<ClassMeritRank>
    ): String {
        val logoBase64 = getLogoBase64(settings)
        val logoImgTag = if (logoBase64 != null) {
            """<img src="$logoBase64" style="height: 44px; width: 44px; object-fit: contain; vertical-align: middle; margin-right: 10px;">"""
        } else ""

        val watermarkHtml = if (settings.showWatermark) {
            if (logoBase64 != null) {
                """<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); opacity: ${settings.watermarkOpacity}; pointer-events: none; z-index: 0;"><img src="$logoBase64" style="width: 260px;"></div>"""
            } else {
                """<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); opacity: ${settings.watermarkOpacity}; pointer-events: none; z-index: 0; font-size: 50px; font-weight: bold; color: #1e3a8a;">${settings.schoolName}</div>"""
            }
        } else ""

        val rows = StringBuilder()
        meritList.forEach { r ->
            val statusColor = if (r.isPassed) "#059669" else "#dc2626"
            rows.append(
                """
                <tr>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center; font-weight: bold;">${r.position}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center; font-weight: bold;">${r.student.rollNo}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; font-weight: 600;">${r.student.name}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center;">${r.student.uniqueId}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center; font-weight: bold;">${r.totalMarks.toInt()}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center; font-weight: bold; color: $statusColor;">${r.gpa}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center; color: $statusColor; font-weight: bold;">${r.statusText}</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Class Merit List - $className</title>
            <style>
                @page { size: A4 landscape; margin: 8mm; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    color: #1e293b;
                    margin: 0; padding: 0;
                }
                .container {
                    min-height: 190mm;
                    box-sizing: border-box;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                    position: relative;
                }
                .header { text-align: center; border-bottom: 2px solid #1e3a8a; padding-bottom: 8px; margin-bottom: 12px; }
                .title { font-size: 20px; font-weight: bold; color: #1e3a8a; margin: 0; }
                .sub { font-size: 12px; color: #64748b; margin: 3px 0; }
                .doc { display: inline-block; background: #e0e7ff; color: #1e3a8a; font-weight: bold; padding: 3px 14px; border-radius: 4px; font-size: 13px; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 16px; }
                th { background: #1e3a8a; color: white; padding: 6px; border: 1px solid #1e3a8a; }
            </style>
        </head>
        <body>
            <div class="container">
                $watermarkHtml
                <div style="position: relative; z-index: 1;">
                    <div class="header">
                        <div style="display: flex; align-items: center; justify-content: center;">
                            $logoImgTag
                            <div>
                                <div class="title">${settings.schoolName}</div>
                                <div class="sub">${settings.schoolAddress} • EIIN: ${settings.eiinNumber}</div>
                            </div>
                        </div>
                        <div class="doc" style="margin-top: 4px;">শ্রেণিভিত্তিক ফলাফল ও মেধা তালিকা (OFFICIAL MERIT LIST)</div>
                        <p style="font-size: 12px; margin: 4px 0 0 0; font-weight: bold;">শ্রেণি: $className | পরীক্ষা: $categoryName ($year) | মোট পরীক্ষার্থী: ${meritList.size} জন</p>
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th style="width: 50px;">মেধাক্রম</th>
                                <th style="width: 60px;">রোল</th>
                                <th>শিক্ষার্থীর নাম</th>
                                <th style="width: 120px;">স্টুডেন্ট আইডি</th>
                                <th style="width: 90px;">মোট নম্বর</th>
                                <th style="width: 80px;">জিপিএ (GPA)</th>
                                <th style="width: 140px;">ফলাফল স্ট্যাটাস</th>
                            </tr>
                        </thead>
                        <tbody>
                            $rows
                        </tbody>
                    </table>

                    <table style="width: 100%; margin-top: auto; border: none;">
                        <tr style="border: none;">
                            <td style="border: none; text-align: center; width: 33%;">
                                <div style="border-top: 1px dashed #64748b; display: inline-block; width: 160px; padding-top: 4px; font-weight: bold; font-size: 11px;">শ্রেণি শিক্ষক</div>
                            </td>
                            <td style="border: none; text-align: center; width: 33%;">
                                <div style="border-top: 1px dashed #64748b; display: inline-block; width: 160px; padding-top: 4px; font-weight: bold; font-size: 11px;">পরীক্ষা নিয়ন্ত্রক</div>
                            </td>
                            <td style="border: none; text-align: center; width: 33%;">
                                <div style="font-style: italic; font-weight: bold; color: #1e3a8a; font-size: 12px;">${settings.principalName}</div>
                                <div style="border-top: 1px dashed #64748b; display: inline-block; width: 160px; padding-top: 4px; font-weight: bold; font-size: 11px;">${settings.principalTitle}</div>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateExamRoutineHtml(
        settings: SchoolSettings,
        className: String,
        categoryName: String,
        year: Int,
        routineSlots: List<ExamRoutineSlot>,
        slipsPerPage: Int = 4
    ): String {
        val logoBase64 = getLogoBase64(settings)
        val logoImgTag = if (logoBase64 != null) {
            """<img src="$logoBase64" style="height: 32px; width: 32px; object-fit: contain; vertical-align: middle; margin-right: 6px;">"""
        } else ""

        val watermarkHtml = if (settings.showWatermark) {
            if (logoBase64 != null) {
                """<img src="$logoBase64" style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 140px; opacity: ${settings.watermarkOpacity}; pointer-events: none; z-index: 0;">"""
            } else {
                """<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-size: 30px; color: rgba(30,58,138,${settings.watermarkOpacity}); font-weight: 900; pointer-events: none; z-index: 0; text-align: center; width: 90%;">${settings.schoolName}</div>"""
            }
        } else ""

        val slotRows = StringBuilder()
        routineSlots.forEach { s ->
            slotRows.append(
                """
                <tr>
                    <td style="padding: 4px 6px; border: 1px solid #94a3b8; font-size: 9px; font-weight: bold; text-align: center;">${s.examDate}</td>
                    <td style="padding: 4px 6px; border: 1px solid #94a3b8; font-size: 9px; text-align: center;">${s.examDay}</td>
                    <td style="padding: 4px 6px; border: 1px solid #94a3b8; font-size: 9px; text-align: center; font-weight: 600; color: #0284c7;">${s.shift}</td>
                    <td style="padding: 4px 6px; border: 1px solid #94a3b8; font-size: 9px; font-weight: bold; color: #1e3a8a;">${s.subjectName}</td>
                </tr>
                """.trimIndent()
            )
        }

        val cardHeight = if (slipsPerPage == 6) "88mm" else "134mm"

        val routineBulletsHtml = settings.routineInstructions
            .lines()
            .map { it.trim().removePrefix("•").removePrefix("-").trim() }
            .filter { it.isNotBlank() }
            .joinToString("") { "<li style='margin-bottom: 2px;'>$it</li>" }

        val slipCard = { idNum: Int ->
            """
            <div class="card" style="height: $cardHeight;">
                $watermarkHtml
                <div style="position: relative; z-index: 1; height: 100%; display: flex; flex-direction: column; justify-content: space-between;">
                    <div>
                        <div style="text-align: center; border-bottom: 1.5px solid #1e3a8a; padding-bottom: 3px; margin-bottom: 4px;">
                            <div>
                                $logoImgTag
                                <span style="font-size: 12.5px; font-weight: bold; color: #1e3a8a; vertical-align: middle;">${settings.schoolName}</span>
                            </div>
                            <div style="font-size: 8.5px; color: #475569;">${settings.schoolAddress} • ${settings.eiinNumber}</div>
                            <div style="font-size: 9.5px; font-weight: bold; color: #b45309; margin: 2px 0;">
                                $categoryName - $year
                            </div>
                            <div style="display: inline-block; background: #1e3a8a; color: #ffffff; font-weight: bold; padding: 1px 12px; border-radius: 10px; font-size: 9.5px;">
                                শ্রেণি : $className
                            </div>
                        </div>

                        <table style="width: 100%; border-collapse: collapse; margin-bottom: 4px; background: rgba(255,255,255,0.92);">
                            <thead>
                                <tr style="background: #1e3a8a; color: #ffffff;">
                                    <th style="padding: 2.5px; font-size: 8.5px; width: 25%;">তারিখ</th>
                                    <th style="padding: 2.5px; font-size: 8.5px; width: 20%;">বার</th>
                                    <th style="padding: 2.5px; font-size: 8.5px; width: 18%;">শিফট</th>
                                    <th style="padding: 2.5px; font-size: 8.5px; width: 37%;">বিষয়</th>
                                </tr>
                            </thead>
                            <tbody>
                                $slotRows
                            </tbody>
                        </table>

                        <div style="font-size: 7.5px; color: #334155; margin-bottom: 4px; line-height: 1.3; background: #f8fafc; padding: 3px 5px; border-left: 2.5px solid #1e3a8a; border-radius: 2px;">
                            <b>পরীক্ষার্থীদের দিকনির্দেশনা:</b>
                            <ul style="margin: 2px 0 2px 14px; padding: 0;">$routineBulletsHtml</ul>
                        </div>
                    </div>

                    <table style="width: 100%; border: none; margin-top: auto;">
                        <tr style="border: none;">
                            <td style="border: none; font-size: 8px; text-align: left; vertical-align: bottom;">
                                রোল নং: _________
                            </td>
                            <td style="border: none; text-align: right; vertical-align: bottom;">
                                <span style="font-size: 8.5px; font-style: italic; color: #1e3a8a; font-weight: bold;">${settings.principalName}</span><br>
                                <span style="border-top: 1px dashed #64748b; font-size: 7px; padding-top: 1px;">${settings.principalTitle}</span>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            """.trimIndent()
        }

        val totalSlips = if (slipsPerPage == 6) 6 else 4
        val cardsHtml = StringBuilder()
        for (i in 1..totalSlips) {
            cardsHtml.append(slipCard(i))
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Exam Routine - $slipsPerPage Slips A4</title>
            <style>
                @page { size: A4 portrait; margin: 6mm; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    margin: 0; padding: 0;
                    color: #0f172a;
                }
                .grid {
                    width: 100%;
                    min-height: 280mm;
                    height: 280mm;
                    display: flex;
                    flex-wrap: wrap;
                    justify-content: space-between;
                    align-content: space-between;
                    box-sizing: border-box;
                }
                .card {
                    width: 49%;
                    box-sizing: border-box;
                    border: 1.5px solid #1e3a8a;
                    border-radius: 8px;
                    padding: 8px;
                    position: relative;
                    background: #ffffff;
                    page-break-inside: avoid;
                }
            </style>
        </head>
        <body>
            <div class="grid">
                $cardsHtml
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    fun generateAdmitCardsHtml(
        settings: SchoolSettings,
        className: String,
        categoryName: String,
        year: Int,
        students: List<Student>,
        cardsPerPage: Int = 4
    ): String {
        val logoBase64 = getLogoBase64(settings)
        val logoImgTag = if (logoBase64 != null) {
            """<img src="$logoBase64" style="height: 36px; width: 36px; object-fit: contain; vertical-align: middle; margin-right: 6px;">"""
        } else ""

        val watermarkHtml = if (settings.showWatermark) {
            if (logoBase64 != null) {
                """<img src="$logoBase64" style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 140px; opacity: ${settings.watermarkOpacity}; pointer-events: none; z-index: 0;">"""
            } else {
                """<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-size: 32px; color: rgba(30,58,138,${settings.watermarkOpacity}); font-weight: 900; pointer-events: none; z-index: 0; text-align: center; width: 90%;">${settings.schoolName}</div>"""
            }
        } else ""

        val cardHeight = if (cardsPerPage == 6) "88mm" else "134mm"

        val admitBulletsHtml = settings.routineInstructions
            .lines()
            .map { it.trim().removePrefix("•").removePrefix("-").trim() }
            .filter { it.isNotBlank() }
            .joinToString("") { "<li style='margin-bottom: 2px;'>$it</li>" }

        fun renderCard(student: Student): String {
            return """
            <div class="card" style="height: $cardHeight;">
                $watermarkHtml
                <div style="position: relative; z-index: 1; height: 100%; display: flex; flex-direction: column; justify-content: space-between;">
                    <div>
                        <div style="text-align: center; border-bottom: 1.5px solid #1e3a8a; padding-bottom: 4px; margin-bottom: 6px;">
                            <div>
                                $logoImgTag
                                <span style="font-size: 13px; font-weight: bold; color: #1e3a8a; vertical-align: middle;">${settings.schoolName}</span>
                            </div>
                            <div style="font-size: 8.5px; color: #475569; margin-top: 1px;">${settings.schoolAddress} • EIIN: ${settings.eiinNumber}</div>
                            <div style="font-size: 9.5px; font-weight: bold; color: #b45309; margin-top: 2px;">$categoryName - $year</div>
                            <div style="display: inline-block; background: #fef3c7; color: #92400e; border: 1px solid #f59e0b; padding: 1px 16px; border-radius: 4px; font-size: 11px; font-weight: bold; margin-top: 2px; letter-spacing: 0.5px;">
                                প্রবেশপত্র
                            </div>
                        </div>

                        <div style="font-size: 11px; line-height: 1.7; margin-bottom: 6px; color: #0f172a;">
                            <div><b>নাম :</b> <span style="border-bottom: 1px dotted #94a3b8; display: inline-block; width: 75%; color: #1e3a8a;"><b><i>${student.name}</i></b></span></div>
                            <div><b>পিতার নাম :</b> <span style="border-bottom: 1px dotted #94a3b8; display: inline-block; width: 68%;"><b><i>${student.fatherName}</i></b></span></div>
                            <div style="display: flex; justify-content: space-between; margin-top: 2px;">
                                <span style="width: 52%;"><b>শ্রেণি :</b> <span style="border-bottom: 1px dotted #94a3b8; display: inline-block; width: 55%;"><b><i>$className</i></b></span></span>
                                <span style="width: 45%;"><b>রোল :</b> <span style="border-bottom: 1px dotted #94a3b8; display: inline-block; width: 55%; color: #b45309;"><b><i>${student.rollNo}</i></b></span></span>
                            </div>
                        </div>

                        <div style="font-size: 7.5px; color: #334155; margin-bottom: 6px; line-height: 1.3; background: #f8fafc; padding: 3px 5px; border-left: 2.5px solid #1e3a8a; border-radius: 2px;">
                            <b>প্রবেশপত্রের নির্দেশনাবলী:</b>
                            <ul style="margin: 2px 0 2px 14px; padding: 0;">$admitBulletsHtml</ul>
                        </div>
                    </div>

                    <table style="width: 100%; border: none; margin-top: auto;">
                        <tr style="border: none;">
                            <td style="border: none; font-size: 9px; text-align: left; vertical-align: bottom; width: 50%;">
                                <div style="border-top: 1px dashed #64748b; display: inline-block; width: 125px; padding-top: 2px; font-weight: bold;"><b><i>শ্রেণি শিক্ষকের স্বাক্ষর</i></b></div>
                            </td>
                            <td style="border: none; text-align: right; vertical-align: bottom; width: 50%;">
                                <span style="font-size: 9px; font-style: italic; color: #1e3a8a; font-weight: bold;">${settings.principalName}</span><br>
                                <span style="border-top: 1px dashed #64748b; font-size: 7.5px; padding-top: 1px;">${settings.principalTitle}</span>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            """.trimIndent()
        }

        val pagesHtml = StringBuilder()
        val chunked = students.chunked(cardsPerPage)
        chunked.forEachIndexed { pageIndex, pageStudents ->
            val isLast = pageIndex == chunked.size - 1
            pagesHtml.append(
                """
                <div class="page" style="${if (!isLast) "page-break-after: always;" else ""}">
                    <div class="grid">
                        ${pageStudents.joinToString("\n") { renderCard(it) }}
                    </div>
                </div>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Bulk Admit Cards - $className</title>
            <style>
                @page { size: A4 portrait; margin: 6mm; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    margin: 0; padding: 0;
                    color: #0f172a;
                }
                .page {
                    width: 100%;
                    min-height: 280mm;
                    height: 280mm;
                    box-sizing: border-box;
                    display: flex;
                    flex-direction: column;
                }
                .grid {
                    width: 100%;
                    height: 100%;
                    display: flex;
                    flex-wrap: wrap;
                    justify-content: space-between;
                    align-content: space-between;
                    box-sizing: border-box;
                }
                .card {
                    width: 49%;
                    box-sizing: border-box;
                    border: 1.5px dashed #1e3a8a;
                    border-radius: 8px;
                    padding: 8px;
                    position: relative;
                    background: #ffffff;
                    page-break-inside: avoid;
                }
            </style>
        </head>
        <body>
            $pagesHtml
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Generates a complete full A4 PDF printable table for student list
     */
    fun generateStudentDirectoryHtml(
        settings: SchoolSettings,
        className: String,
        students: List<Student>
    ): String {
        val logoBase64 = getLogoBase64(settings)
        val logoImgTag = if (logoBase64 != null) {
            """<img src="$logoBase64" style="height: 44px; width: 44px; object-fit: contain; vertical-align: middle; margin-right: 10px;">"""
        } else ""

        val watermarkHtml = if (settings.showWatermark) {
            if (logoBase64 != null) {
                """<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); opacity: ${settings.watermarkOpacity}; pointer-events: none; z-index: 0;"><img src="$logoBase64" style="width: 260px;"></div>"""
            } else {
                """<div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); opacity: ${settings.watermarkOpacity}; pointer-events: none; z-index: 0; font-size: 48px; font-weight: bold; color: #1e3a8a;">${settings.schoolName}</div>"""
            }
        } else ""

        val rows = StringBuilder()
        students.forEach { s ->
            rows.append(
                """
                <tr>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center; font-weight: bold;">${s.rollNo}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; font-weight: bold; color: #1e3a8a;">${s.name}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center;">${s.uniqueId}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1;">${s.fatherName}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center;">${s.birthRegNo}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center;">${s.dateOfBirth}</td>
                    <td style="padding: 6px 8px; border: 1px solid #cbd5e1; text-align: center;">${s.guardianPhone}</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>Student Directory - $className</title>
            <style>
                @page { size: A4 portrait; margin: 8mm; }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    color: #1e293b;
                    margin: 0; padding: 0;
                }
                .container {
                    min-height: 275mm;
                    box-sizing: border-box;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                    position: relative;
                }
                .header { text-align: center; border-bottom: 2px solid #1e3a8a; padding-bottom: 8px; margin-bottom: 12px; }
                .title { font-size: 20px; font-weight: bold; color: #1e3a8a; margin: 0; }
                .sub { font-size: 11.5px; color: #64748b; margin: 2px 0; }
                .doc { display: inline-block; background: #e0e7ff; color: #1e3a8a; font-weight: bold; padding: 3px 14px; border-radius: 4px; font-size: 13px; }
                table { width: 100%; border-collapse: collapse; font-size: 11.5px; margin-bottom: 16px; }
                th { background: #1e3a8a; color: white; padding: 6px; border: 1px solid #1e3a8a; font-size: 11px; }
            </style>
        </head>
        <body>
            <div class="container">
                $watermarkHtml
                <div style="position: relative; z-index: 1;">
                    <div class="header">
                        <div style="display: flex; align-items: center; justify-content: center;">
                            $logoImgTag
                            <div>
                                <div class="title">${settings.schoolName}</div>
                                <div class="sub">${settings.schoolAddress} • EIIN: ${settings.eiinNumber}</div>
                            </div>
                        </div>
                        <div class="doc" style="margin-top: 4px;">শিক্ষার্থীদের তালিকা (STUDENT DIRECTORY)</div>
                        <p style="font-size: 11.5px; margin: 4px 0 0 0; font-weight: bold;">শ্রেণি: $className | মোট শিক্ষার্থী: ${students.size} জন</p>
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th style="width: 50px;">রোল</th>
                                <th>শিক্ষার্থীর নাম</th>
                                <th style="width: 100px;">স্টুডেন্ট আইডি</th>
                                <th>পিতার নাম</th>
                                <th style="width: 120px;">জন্ম নিবন্ধন নং</th>
                                <th style="width: 90px;">জন্ম তারিখ</th>
                                <th style="width: 110px;">যোগাযোগ ফোন</th>
                            </tr>
                        </thead>
                        <tbody>
                            $rows
                        </tbody>
                    </table>

                    <table style="width: 100%; margin-top: auto; border: none; padding-top: 20px;">
                        <tr style="border: none;">
                            <td style="border: none; text-align: left; width: 50%;">
                                <div style="font-size: 11px; color: #64748b;">মুদ্রণ তারিখ: ${java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}</div>
                            </td>
                            <td style="border: none; text-align: right; width: 50%;">
                                <div style="font-style: italic; font-weight: bold; color: #1e3a8a; font-size: 12px;">${settings.principalName}</div>
                                <div style="border-top: 1px dashed #64748b; display: inline-block; width: 160px; padding-top: 3px; font-weight: bold; font-size: 11px;">${settings.principalTitle}</div>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
