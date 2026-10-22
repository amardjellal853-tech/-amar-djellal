package com.example.util

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileDownloadHelper {

    /**
     * حفظ وتحميل أي ملف نصي أو CSV أو HTML أو JSON مباشرة في مجلد التنزيلات بالجهاز (Downloads)
     * بدون أي قيود ودون الحاجة لأذونات معقدة
     */
    fun saveFileToDownloads(
        context: Context,
        baseFileName: String,
        extension: String,
        mimeType: String,
        content: String,
        prependUtf8Bom: Boolean = false,
        openAfterSave: Boolean = false
    ): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        val sanitizedName = baseFileName.replace("[\\\\/:*?\"<>|\\s]+".toRegex(), "_")
        val fullFileName = "${sanitizedName}_$timeStamp.$extension"

        var fileUri: Uri? = null

        try {
            val bytes = if (prependUtf8Bom) {
                // UTF-8 BOM helps Excel / Numbers correctly display Arabic text without corrupting encoding
                byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + content.toByteArray(Charsets.UTF_8)
            } else {
                content.toByteArray(Charsets.UTF_8)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { os ->
                        os.write(bytes)
                        os.flush()
                    }
                    fileUri = uri
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val destFile = File(downloadsDir, fullFileName)
                FileOutputStream(destFile).use { os ->
                    os.write(bytes)
                    os.flush()
                }
                fileUri = Uri.fromFile(destFile)
            }

            Toast.makeText(
                context,
                "تم التنزيل بنجاح في مجلد Downloads:\n$fullFileName 📥",
                Toast.LENGTH_LONG
            ).show()

            if (openAfterSave && fileUri != null) {
                openFile(context, fileUri, mimeType)
            }

            return fileUri
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "تعذر التنزيل المباشر: ${e.localizedMessage ?: "خطأ غير متوقع"}",
                Toast.LENGTH_SHORT
            ).show()
            // Fallback: trigger standard Android share intent
            shareTextContent(context, fullFileName, content, mimeType)
            return null
        }
    }

    /**
     * فتح الملف المحمّل مباشرة باستخدام التطبيق المناسب في الهاتف (المتصفح، Excel، عارض النصوص، إلخ)
     */
    fun openFile(context: Context, uri: Uri, mimeType: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "عرض الملف المحمّل"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "تم حفظ الملف بنجاح في مجلد التنزيلات (Downloads)", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * مشاركة المحتوى النصي مع أي تطبيق آخر
     */
    fun shareTextContent(context: Context, title: String, content: String, mimeType: String = "text/plain") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "مشاركة / حفظ $title"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * إنشاء تقرير HTML رسمي كامل ومنسق بتصميم وزاري عالي الدقة وجاهز للطباعة المباشرة كـ PDF
     */
    fun generatePrintableHtmlReport(
        className: String,
        academicYear: String,
        termLabel: String,
        indSport: String,
        colSport: String,
        teacherName: String,
        schoolName: String,
        studentsData: List<HtmlStudentRow>,
        classAverage: Double,
        passRate: Double
    ): String {
        val rowsBuilder = StringBuilder()
        studentsData.forEach { s ->
            val statusColor = if (s.isExempt) "#d32f2f" else "#2e7d32"
            val avgColor = if (s.termAverage >= 10.0) "#1b5e20" else "#b71c1c"
            rowsBuilder.append(
                """
                <tr>
                    <td class="text-center font-bold">${s.regNumber}</td>
                    <td class="font-bold">${s.fullName}</td>
                    <td class="text-center">${s.gender}</td>
                    <td class="text-center" style="color: $statusColor; font-weight: bold;">${s.status}</td>
                    <td class="text-center">${s.diagScore14}</td>
                    <td class="text-center">${s.progScore6}</td>
                    <td class="text-center font-bold">${s.indTotal20}</td>
                    <td class="text-center font-bold">${s.colScore20}</td>
                    <td class="text-center">${s.testScore}</td>
                    <td class="text-center font-bold">${s.examScore}</td>
                    <td class="text-center font-bold">${s.continuousScore}</td>
                    <td class="text-center font-bold" style="color: $avgColor; font-size: 1.1em; background: #f0fdf4;">${s.termAverage}</td>
                    <td class="text-center font-bold">${s.annualAverage}</td>
                    <td class="text-center font-bold" style="color: #0284c7;">${s.rank}</td>
                    <td>${s.appreciation}</td>
                </tr>
                """.trimIndent()
            )
        }

        return """
        <!DOCTYPE html>
        <html lang="ar" dir="rtl">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>كشف نقاط التربية البدنية - $className - $termLabel</title>
            <style>
                @page {
                    size: A4 landscape;
                    margin: 8mm;
                }
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    direction: rtl;
                    text-align: right;
                    background-color: #f8fafc;
                    margin: 0;
                    padding: 16px;
                    color: #0f172a;
                }
                .action-bar {
                    background: #1e293b;
                    color: white;
                    padding: 12px 20px;
                    border-radius: 12px;
                    margin-bottom: 20px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
                }
                .btn {
                    background: #0284c7;
                    color: white;
                    border: none;
                    padding: 8px 18px;
                    font-size: 14px;
                    font-weight: bold;
                    border-radius: 8px;
                    cursor: pointer;
                    display: inline-flex;
                    align-items: center;
                    gap: 6px;
                }
                .btn:hover { background: #0369a1; }
                .sheet-container {
                    background: white;
                    padding: 24px;
                    border-radius: 12px;
                    border: 1px solid #cbd5e1;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
                }
                .header-title {
                    text-align: center;
                    margin-bottom: 16px;
                    border-bottom: 2px solid #0284c7;
                    padding-bottom: 12px;
                }
                .header-title h1 {
                    font-size: 18px;
                    margin: 2px 0;
                    color: #0f172a;
                }
                .header-title h2 {
                    font-size: 15px;
                    margin: 4px 0;
                    color: #0369a1;
                }
                .info-grid {
                    display: grid;
                    grid-template-columns: repeat(4, 1fr);
                    gap: 12px;
                    background: #f1f5f9;
                    padding: 12px;
                    border-radius: 8px;
                    margin-bottom: 16px;
                    font-size: 13px;
                }
                .info-item {
                    display: flex;
                    flex-direction: column;
                }
                .info-label {
                    font-size: 11px;
                    color: #64748b;
                }
                .info-val {
                    font-weight: bold;
                    color: #0f172a;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    font-size: 11px;
                    margin-bottom: 16px;
                }
                th, td {
                    border: 1px solid #94a3b8;
                    padding: 6px 4px;
                }
                th {
                    background: #e2e8f0;
                    color: #1e293b;
                    font-weight: bold;
                    text-align: center;
                }
                tr:nth-child(even) {
                    background-color: #f8fafc;
                }
                .text-center { text-align: center; }
                .font-bold { font-weight: bold; }
                .stats-box {
                    display: flex;
                    justify-content: space-around;
                    background: #f0fdf4;
                    border: 1px solid #86efac;
                    padding: 12px;
                    border-radius: 8px;
                    margin-top: 14px;
                    font-weight: bold;
                    font-size: 13px;
                }
                .signatures {
                    margin-top: 30px;
                    display: flex;
                    justify-content: space-between;
                    padding: 0 40px;
                    font-weight: bold;
                    font-size: 13px;
                }
                @media print {
                    body {
                        background: white;
                        padding: 0;
                    }
                    .action-bar { display: none; }
                    .sheet-container {
                        border: none;
                        padding: 0;
                        box-shadow: none;
                    }
                    table { font-size: 10px; }
                    th, td { padding: 4px 2px; }
                }
            </style>
        </head>
        <body>
            <div class="action-bar">
                <div>
                    <strong>📄 كشف النقاط الرسمي للتربية البدنية والرياضية (جاهز للطباعة والحفظ PDF)</strong>
                </div>
                <button class="btn" onclick="window.print()">🖨️ طباعة الكشف / حفظ كـ PDF</button>
            </div>

            <div class="sheet-container">
                <div class="header-title">
                    <h1>الجمهورية الجزائرية الديمقراطية الشعبية</h1>
                    <h1>وزارة التربية الوطنية</h1>
                    <h2>كشف التقييم الفصلي لمادة التربية البدنية والرياضية ($termLabel)</h2>
                </div>

                <div class="info-grid">
                    <div class="info-item">
                        <span class="info-label">المؤسسة التعليمية:</span>
                        <span class="info-val">${schoolName.ifEmpty { "ثانوية الرقمنة والامتياز" }}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">السنة الدراسية:</span>
                        <span class="info-val">$academicYear</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">الأستاذ المشرف:</span>
                        <span class="info-val">${teacherName.ifEmpty { "أستاذ المادة" }}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">الفوج التربوي (القسم):</span>
                        <span class="info-val">$className</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">النشاط البدني الفردي:</span>
                        <span class="info-val">$indSport</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">النشاط البدني الجماعي:</span>
                        <span class="info-val">$colSport</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">طبيعة الكشف:</span>
                        <span class="info-val">نسخة كاملة غير مقيدة للرقمنة</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">تاريخ الاستخراج:</span>
                        <span class="info-val">${SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())}</span>
                    </div>
                </div>

                <table>
                    <thead>
                        <tr>
                            <th rowspan="2">الرقم</th>
                            <th rowspan="2" style="min-width: 140px;">اللقب والاسم</th>
                            <th rowspan="2">الجنس</th>
                            <th rowspan="2">الحالة</th>
                            <th colspan="3">النشاط الفردي ($indSport)</th>
                            <th rowspan="2">جماعي ($colSport) [20]</th>
                            <th rowspan="2">الفرض [20]</th>
                            <th rowspan="2">الاختبار [20]</th>
                            <th rowspan="2">التقويم المستمر [20]</th>
                            <th rowspan="2" style="background: #bbf7d0;">معدل الفصل [20]</th>
                            <th rowspan="2">المعدل السنوي [20]</th>
                            <th rowspan="2">الترتيب</th>
                            <th rowspan="2">الملاحظة البيداغوجية</th>
                        </tr>
                        <tr>
                            <th>تشخيص (14)</th>
                            <th>تطور (6)</th>
                            <th>المجموع (20)</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rowsBuilder
                    </tbody>
                </table>

                <div class="stats-box">
                    <span>👥 إجمالي التلاميذ: ${studentsData.size}</span>
                    <span>🏃‍♂️ الممارسون: ${studentsData.count { !it.isExempt }}</span>
                    <span>🏥 المعفون طبياً: ${studentsData.count { it.isExempt }}</span>
                    <span>📊 معدل القسم العام: ${String.format(Locale.US, "%.2f", classAverage)} / 20</span>
                    <span>🎯 نسبة النجاح: ${String.format(Locale.US, "%.1f", passRate)}%</span>
                </div>

                <div class="signatures">
                    <div>
                        <p>توقيع وختم أستاذ المادة:</p>
                        <br><br>
                        <p>.......................................</p>
                    </div>
                    <div>
                        <p>تأشيرة وختم مدير المؤسسة التعليمية:</p>
                        <br><br>
                        <p>.......................................</p>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}

data class HtmlStudentRow(
    val regNumber: Int,
    val fullName: String,
    val gender: String,
    val status: String,
    val isExempt: Boolean,
    val diagScore14: Double,
    val progScore6: Double,
    val indTotal20: Double,
    val colScore20: Double,
    val testScore: Double,
    val examScore: Double,
    val continuousScore: Double,
    val termAverage: Double,
    val annualAverage: Double,
    val rank: Int,
    val appreciation: String
)
