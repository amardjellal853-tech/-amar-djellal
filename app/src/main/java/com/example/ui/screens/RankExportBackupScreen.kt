package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Student
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import com.example.ui.components.ResultSheetScannerDialog
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal
import com.example.ui.viewmodel.EpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankExportBackupScreen(
    viewModel: EpsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val students by viewModel.currentClassStudents.collectAsState()
    val termAssessments by viewModel.termAssessments.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: الترتيب, 1: التصدير, 2: النسخ الاحتياطي
    var sortBy by remember { mutableStateOf("gpa") } // "gpa", "annual", "individual", "collective"

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showResetPresetDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }

    fun getAssessment(studentId: String): TermAssessment {
        val key = "${studentId}_${selectedTerm.termNumber}"
        return termAssessments[key] ?: TermAssessment(studentId = studentId, termType = selectedTerm)
    }

    val sortedStudents = remember(students, termAssessments, selectedTerm, sortBy) {
        val list = students.toMutableList()
        when (sortBy) {
            "gpa" -> list.sortedByDescending { getAssessment(it.id).termAverage }
            "annual" -> list.sortedByDescending { viewModel.getStudentAnnualAverage(it.id) }
            "individual" -> list.sortedByDescending { getAssessment(it.id).individualSportTotal20 }
            "collective" -> list.sortedByDescending { getAssessment(it.id).collectiveSportScore20 }
            else -> list.sortedBy { it.regNumber }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("ترتيب النتائج") },
                icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                modifier = Modifier.testTag("tab_ranking")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("مركز التنزيل") },
                icon = { Icon(Icons.Default.Download, contentDescription = null) },
                modifier = Modifier.testTag("tab_export")
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("النسخ الاحتياطي وAPK") },
                icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                modifier = Modifier.testTag("tab_backup")
            )
            Tab(
                selected = selectedSubTab == 3,
                onClick = { selectedSubTab = 3 },
                text = { Text("الكراس والأستاذ") },
                icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                modifier = Modifier.testTag("tab_teacher_logbook")
            )
        }

        when (selectedSubTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "ترتيب التلاميذ في (${selectedTerm.labelAr}):",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = (sortBy == "gpa"),
                                        onClick = { sortBy = "gpa" },
                                        label = { Text("معدل ${selectedTerm.labelAr}") },
                                        modifier = Modifier.testTag("sort_gpa")
                                    )
                                    FilterChip(
                                        selected = (sortBy == "annual"),
                                        onClick = { sortBy = "annual" },
                                        label = { Text("المعدل السنوي") },
                                        modifier = Modifier.testTag("sort_annual")
                                    )
                                    FilterChip(
                                        selected = (sortBy == "individual"),
                                        onClick = { sortBy = "individual" },
                                        label = { Text(selectedTerm.individualSportAr) },
                                        modifier = Modifier.testTag("sort_individual")
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(sortedStudents, key = { _, s -> s.id }) { index, student ->
                        val rank = index + 1
                        val ass = getAssessment(student.id)
                        val annualAvg = viewModel.getStudentAnnualAverage(student.id)

                        RankStudentCard(
                            rank = rank,
                            student = student,
                            assessment = ass,
                            annualAvg = annualAvg,
                            termType = selectedTerm,
                            sortBy = sortBy
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }

            1 -> {
                val reportText = remember(selectedClass, selectedTerm, students, termAssessments) {
                    viewModel.generateClassReportText()
                }
                val sharedAppUrl = "https://ais-pre-ug2opxm55w7ejdsrnovg4w-153639526878.europe-west2.run.app"

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // شريط النسخة الكاملة غير المقيدة
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.LockOpen,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "النسخة الكاملة غير المقيدة 🔓",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "دائمة ومجانية 100% لجميع أساتذة التربية البدنية والرياضية",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "التطبيق غير مقيد نهائياً: يمكنك إضافة أي عدد من الأقسام والتلاميذ بدون حدود، تنزيل وحفظ الكشوفات مباشرة، والعمل أوفلاين 100% بدون إنترنت.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("أقسام غير محدودة ∞", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تنزيل فوري مباشر 📥", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("بدون إنترنت 📴", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // بطاقة تنزيل كشوفات النقاط Excel / CSV للرقمنة
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تنزيل كشوفات النقاط للرقمنة (Excel / CSV)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                val indSport = selectedClass?.getIndividualSport(selectedTerm) ?: selectedTerm.individualSportAr
                                val colSport = selectedClass?.getCollectiveSport(selectedTerm) ?: selectedTerm.collectiveSportAr
                                Text(
                                    text = "ملفات متوافقة مع إكسل ومنصة الرقمنة (ترميز عربي UTF-8 BOM سليم). يتضمن كشف ${selectedClass?.name ?: "القسم"} أنشطة [$indSport] و [$colSport] والتقويم المستمر والمعدل السنوي.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { viewModel.downloadCurrentClassCsv(context) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("download_current_class_csv_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تنزيل كشف القسم الحالي (${selectedClass?.name ?: "القسم"}) بصيغة Excel / CSV 📥", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { viewModel.downloadAllClassesCsv(context) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("download_all_classes_csv_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SportTeal)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تنزيل كشف شامل لجميع الأقسام دفعة واحدة (Excel / CSV) 📦", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { viewModel.shareReport(context, isCsv = true) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("share_csv_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("مشاركة ملف CSV عبر التطبيقات (واتساب، تيليغرام، إيميل)")
                                }
                            }
                        }
                    }

                    // بطاقة تنزيل التقرير الرسمي للطباعة المباشرة كـ PDF
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Print, contentDescription = null, tint = SportAmber, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تنزيل الكشف الرسمي للطباعة المباشرة وحفظه كـ PDF",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ملف كشف وزاري رسمي بتصميم A4 جاهز يحتوي على ترويسة الجمهورية الجزائرية، جدول النتائج، الأختام وتوقيع الأستاذ والمدير، مع زر طباعة فوري وحفظ كـ PDF.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { viewModel.downloadCurrentClassHtml(context, openAfterSave = true) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("download_print_html_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SportAmber)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تنزيل وفتح كشف الطباعة المباشرة / PDF (HTML) 🖨️", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { viewModel.shareReport(context, isCsv = false) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("share_text_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("مشاركة التقرير الفصلي كنص رسمي")
                                }
                            }
                        }
                    }

                    // بطاقة تنزيل ملف التطبيق APK وتثبيته
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تنزيل وتثبيت تطبيق الأندرويد (APK) بدون قيود",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "لتثبيت التطبيق على أي هاتف بصيغة APK دائمة بدون الحاجة لمتجر:\n" +
                                            "1. من شريط الأدوات العلوي في AI Studio (Settings)، اضغط على 'Export APK' أو 'Download APK' لتحميل ملف APK جاهز للتثبيت.\n" +
                                            "2. أو اضغط على الزر أدناه لفتح وتشغيل التطبيق في المتصفح وتثبيته فورياً على الشاشة الرئيسية لأي هاتف.",
                                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sharedAppUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "الرابط: $sharedAppUrl", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).testTag("open_app_browser_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فتح رابط التحميل", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sharedAppUrl))
                                            Toast.makeText(context, "تم نسخ الرابط! يمكنك مشاركته مع أي أستاذ", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("copy_download_link_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نسخ الرابط", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // بطاقة معاينة التقرير النصي
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "معاينة التقرير الرسمي المكتوب:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(reportText))
                                            Toast.makeText(context, "تم نسخ التقرير إلى الحافظة", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ التقرير")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = reportText,
                                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }

            2 -> {
                // Backup & Restore Mode
                val sharedAppUrl = "https://ais-pre-ug2opxm55w7ejdsrnovg4w-153639526878.europe-west2.run.app"

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Android,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "تحميل التطبيق ومشاركته مع أي شخص",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "رابط مباشر للتشغيل والتحميل المباشر لجميع الأساتذة",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    color = Color.White.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "رابط التطبيق المباشر (يمكن لأي شخص فتحه وتحميله):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = sharedAppUrl,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, "تطبيق أستاذ التربية البدنية والرياضية")
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "إليك رابط تطبيق أستاذ التربية البدنية والرياضية (الكراس اليومي، التقويم، ورصد النتائج) المتاح للجميع:\n$sharedAppUrl"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة رابط التطبيق مع الزملاء"))
                                        },
                                        modifier = Modifier.weight(1f).testTag("share_app_link_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("مشاركة الرابط", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sharedAppUrl))
                                            Toast.makeText(context, "تم نسخ الرابط! يمكنك إرساله لأي شخص", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("copy_app_link_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نسخ الرابط", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.downloadBackupJsonFile(context)
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("download_app_data_file_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SportTeal)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تنزيل ملف النسخة الاحتياطية (JSON) في مجلد Downloads 📥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        viewModel.downloadTeacherLogbookFile(context, isHtml = true)
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("download_teacher_logbook_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تنزيل الكراس اليومي وبطاقة الأستاذ (HTML للطباعة) 📘", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.DocumentScanner,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تسجيل النتائج والأسماء من ورقة كشف النقاط",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "استيراد كشف النقاط أو قائمة التلاميذ عبر التقاط صورة بالكاميرا، اختيار صورة من الهاتف، أو تحميل ملف نصي.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showScannerDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_scanner_from_backup_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.DocumentScanner, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("فتح ماسح ورقة النتائج (كاميرا / صورة / ملف)")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "حفظ نسخة احتياطية (Export Backup)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "حفظ كافة بيانات الأقسام والتلاميذ للفصول الثلاثة (ف1، ف2، ف3) في ملف نصي بصيغة JSON آمنة.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val json = viewModel.getBackupJson()
                                        clipboardManager.setText(AnnotatedString(json))
                                        Toast.makeText(context, "تم نسخ النسخة الاحتياطية بنجاح إلى الحافظة!", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("copy_backup_btn")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("نسخ النسخة الاحتياطية (JSON) للحافظة")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "استرجاع نسخة احتياطية (Restore Backup)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "لصق نص JSON محفوظ مسبقاً لاسترجاع كافة الأقسام والتلاميذ فوراً.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedButton(
                                    onClick = { showRestoreDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_restore_dialog_btn")
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("استرجاع نسخة من نص JSON")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ActionNegative.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "إعادة ضبط القسمين والأنشطة للفصول الثلاثة",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ActionNegative
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "استرجاع البيانات النموذجية الكاملة للفوجين (1 و 2) بكامل أنشطة الفصول الثلاثة (ف1: 60م وكرة اليد، ف2: الوثب وكرة السلة، ف3: الجلة وكرة الطائرة).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showResetPresetDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = ActionNegative),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("reset_preset_btn")
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("استرجاع بيانات القسمين والفصول الثلاثة")
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }

            3 -> {
                TeacherLogbookScreen(viewModel = viewModel)
            }
        }
    }

    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("استرجاع النسخة الاحتياطية") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("الصق نص JSON للنسخة الاحتياطية:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("restore_json_input"),
                        placeholder = { Text("{\"exportDate\": ...}") },
                        maxLines = 10
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = viewModel.restoreBackup(restoreJsonInput)
                        if (success) {
                            Toast.makeText(context, "تم استرجاع النسخة الاحتياطية بنجاح!", Toast.LENGTH_LONG).show()
                            showRestoreDialog = false
                        } else {
                            Toast.makeText(context, "خطأ في بنية ملف النسخة الاحتياطية!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("confirm_restore_btn")
                ) {
                    Text("استرجاع")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showResetPresetDialog) {
        AlertDialog(
            onDismissRequest = { showResetPresetDialog = false },
            title = { Text("تأكيد استرجاع أقسام الطور الثانوي") },
            text = { Text("هل تريد بالتأكيد إعادة تحميل أقسام الطور الثانوي الأربعة (1 ثانوي علوم، 1 ثانوي آداب، 2 ثانوي علوم، 3 ثانوي بكالوريا) بكامل تقويمات الفصول الثلاثة وسجل المتابعة اليومية؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToDefaults()
                        Toast.makeText(context, "تم تحميل أقسام الطور الثانوي بنجاح!", Toast.LENGTH_LONG).show()
                        showResetPresetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ActionNegative)
                ) {
                    Text("نعم، استرجاع")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPresetDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showScannerDialog) {
        ResultSheetScannerDialog(
            viewModel = viewModel,
            onDismiss = { showScannerDialog = false }
        )
    }
}

@Composable
private fun RankStudentCard(
    rank: Int,
    student: Student,
    assessment: TermAssessment,
    annualAvg: Double,
    termType: TermType,
    sortBy: String
) {
    val rankBadgeColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rank_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = rankBadgeColor,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (rank <= 3) "#$rank" else "$rank",
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.fullName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "معدل ${termType.labelAr}: ${assessment.termAverage}/20",
                        fontSize = 11.sp,
                        fontWeight = if (sortBy == "gpa") FontWeight.Bold else FontWeight.Normal,
                        color = if (sortBy == "gpa") SportBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "سنوي: $annualAvg/20",
                        fontSize = 11.sp,
                        fontWeight = if (sortBy == "annual") FontWeight.Bold else FontWeight.Normal,
                        color = if (sortBy == "annual") SportAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (sortBy) {
                        "annual" -> "$annualAvg/20"
                        "individual" -> "${assessment.individualSportTotal20}/20"
                        "collective" -> "${assessment.collectiveSportScore20}/20"
                        else -> "${assessment.termAverage}/20"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
