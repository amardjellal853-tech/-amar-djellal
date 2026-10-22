package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.res.painterResource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.DailyLogbookEntry
import com.example.data.model.TeacherProfile
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal
import com.example.ui.viewmodel.EpsViewModel

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TeacherLogbookScreen(
    viewModel: EpsViewModel,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val teacherProfile by viewModel.teacherProfile.collectAsState()
    val logbookEntries by viewModel.logbookEntries.collectAsState()
    val classes by viewModel.classes.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: الكراس اليومي, 1: معلومات الأستاذ, 2: تحميل التطبيق (APK)

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<DailyLogbookEntry?>(null) }
    var isAddingNewEntry by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<DailyLogbookEntry?>(null) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SportBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = SportBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الكراس اليومي ومعلومات الأستاذ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${teacherProfile.fullName} • ${teacherProfile.schoolName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onClose != null) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }
            }
        }

        // Tabs
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("الكراس اليومي (${logbookEntries.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_logbook")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("معلومات الأستاذ", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_teacher_profile")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("تحميل التطبيق (APK)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_download_apk")
            )
        }

        when (selectedTab) {
            // ----------------------------------------------------
            // Tab 0: الكراس اليومي التربوي (Daily Logbook)
            // ----------------------------------------------------
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Action Bar Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "الكراس اليومي للتربية البدنية والرياضية",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "تسجيل الأهداف التعلمية، الحصص، والملاحظات التربوية",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "${logbookEntries.size} حصة",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quick Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { isAddingNewEntry = true },
                                        modifier = Modifier.weight(1f).testTag("add_logbook_entry_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إضافة حصة +", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.generatePresetLogbookForWeek()
                                            Toast.makeText(context, "تم توليد حصص الأسبوع آلياً بنجاح!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).testTag("generate_week_logbook_btn")
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("توليد حصص أسبوع", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Download & Export Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val text = viewModel.getDailyLogbookReportText()
                                            viewModel.downloadOrShareReport(
                                                context = context,
                                                title = "الكراس اليومي لأستاذ التربية البدنية - ${teacherProfile.fullName}",
                                                content = text,
                                                fileName = "الكراس_اليومي_${teacherProfile.academicYear.replace("/", "-")}"
                                            )
                                        },
                                        modifier = Modifier.weight(1.2f).testTag("download_logbook_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SportTeal)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تحميل وتنزيل الكراس 📥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val text = viewModel.getDailyLogbookReportText()
                                            clipboardManager.setText(AnnotatedString(text))
                                            Toast.makeText(context, "تم نسخ الكراس اليومي كاملاً إلى الحافظة!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(0.8f).testTag("copy_logbook_btn")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("نسخ", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (logbookEntries.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("لا توجد حصص مسجلة في الكراس اليومي بعد", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "اضغط على 'إضافة حصة +' لتسجيل حصة جديدة، أو 'توليد حصص أسبوع' لتعبئة الحصص المقترحة آلياً.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(logbookEntries, key = { it.id }) { entry ->
                            LogbookEntryCard(
                                entry = entry,
                                onEdit = { entryToEdit = entry },
                                onDelete = { entryToDelete = entry },
                                onDownloadSingle = {
                                    val singleText = viewModel.getDailyLogbookReportText(entry.date)
                                    viewModel.downloadOrShareReport(
                                        context = context,
                                        title = "جذاذة حصة: ${entry.className} - ${entry.activityType} (${entry.date})",
                                        content = singleText,
                                        fileName = "حصة_${entry.className}_${entry.date}"
                                    )
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }

            // ----------------------------------------------------
            // Tab 1: بطاقة ومعلومات الأستاذ (Teacher Profile)
            // ----------------------------------------------------
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Official Algerian Teacher Card Layout
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Header Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "الجمهورية الجزائرية الديمقراطية الشعبية",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "وزارة التربية الوطنية • بطاقة الأستاذ المهنية",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = SportBlue.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = teacherProfile.academicYear,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SportBlue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Profile Name Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(id = com.example.R.drawable.eps_shortcut_icon_1792115255311),
                                        contentDescription = "شعار الأستاذ",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = teacherProfile.fullName,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = teacherProfile.grade,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Details Fields
                                ProfileFieldRow(label = "المؤسسة التعليمية", value = teacherProfile.schoolName, icon = Icons.Default.School)
                                ProfileFieldRow(label = "المقاطعة التفتيشية", value = teacherProfile.district, icon = Icons.Default.Badge)
                                ProfileFieldRow(label = "رقم التسجيل الوظيفي", value = teacherProfile.registrationNumber, icon = Icons.Default.Badge)
                                ProfileFieldRow(label = "الحجم الساعي الأسبوعي", value = teacherProfile.weeklyHours, icon = Icons.Default.CalendarMonth)
                                ProfileFieldRow(label = "رقم الهاتف", value = teacherProfile.phone, icon = Icons.Default.Person)
                                ProfileFieldRow(label = "البريد الإلكتروني", value = teacherProfile.email, icon = Icons.Default.Person)

                                Spacer(modifier = Modifier.height(14.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showEditProfileDialog = true },
                                        modifier = Modifier.weight(1f).testTag("edit_teacher_profile_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تعديل المعلومات", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val text = viewModel.getTeacherProfileReportText()
                                            viewModel.downloadOrShareReport(
                                                context = context,
                                                title = "بطاقة معلومات أستاذ التربية البدنية - ${teacherProfile.fullName}",
                                                content = text,
                                                fileName = "بطاقة_الأستاذ_${teacherProfile.fullName.replace(" ", "_")}"
                                            )
                                        },
                                        modifier = Modifier.weight(1.2f).testTag("download_teacher_profile_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SportTeal)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تحميل البطاقة 📥", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        val text = viewModel.getTeacherProfileReportText()
                                        clipboardManager.setText(AnnotatedString(text))
                                        Toast.makeText(context, "تم نسخ بطاقة الأستاذ إلى الحافظة بنجاح!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("copy_teacher_profile_btn")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("نسخ بطاقة الأستاذ للحافظة", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Assigned Classes Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "الأفواج التربوية المسندة (${classes.size} أفواج):",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                classes.forEach { cls ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "• ${cls.name}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(text = "${cls.level} (${cls.cycle})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }

            // ----------------------------------------------------
            // Tab 2: تحميل التطبيق حالا آليا (Download APK & Data)
            // ----------------------------------------------------
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = com.example.R.drawable.eps_shortcut_icon_1792115255311),
                                        contentDescription = "أيقونة التطبيق",
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "تحميل وتثبيت التطبيق على هاتفك (APK)",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "تثبيت النسخة الرسمية لأستاذ التربية البدنية فوراً",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Surface(
                                    color = Color.White.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "📌 خطوات تنزيل ملف الـ APK فوراً من منصة AI Studio:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "1. انقر على قائمة الإعدادات أو زر التصدير (Export / Settings ⚙️) الموجود في أعلى شريط منصة Google AI Studio.\n" +
                                                   "2. اختر الخيار: 'Download APK' (أو 'Export Project as ZIP').\n" +
                                                   "3. سيبدأ تحميل ملف التطبيق مباشرة إلى جهازك لتثبيته بنقرة واحدة على أي هاتف Android.\n" +
                                                   "4. يمكنك استخدام التطبيق أوفلاين دون الحاجة للإنترنت بعد التثبيت.",
                                            fontSize = 12.sp,
                                            lineHeight = 20.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        val backupJson = viewModel.getBackupJson()
                                        viewModel.downloadOrShareReport(
                                            context = context,
                                            title = "نسخة احتياطية كاملة لتطبيق التربية البدنية (JSON)",
                                            content = backupJson,
                                            fileName = "نسخة_تطبيق_التربية_البدنية_${teacherProfile.academicYear.replace("/", "-")}"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("download_app_data_backup_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تحميل حزمة بيانات التطبيق الكاملة (Backup File) 📥", fontWeight = FontWeight.Bold)
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
                                    text = "تحميل وتنزيل التقارير الرسمية الشاملة:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        val fullReport = StringBuilder().apply {
                                            append(viewModel.getTeacherProfileReportText())
                                            append("\n\n")
                                            append(viewModel.getDailyLogbookReportText())
                                            append("\n\n")
                                            append(viewModel.generateClassReportText())
                                        }.toString()

                                        viewModel.downloadOrShareReport(
                                            context = context,
                                            title = "الملف الإداري والبيداغوجي الشامل لأستاذ التربية البدنية",
                                            content = fullReport,
                                            fileName = "الملف_الشامل_لأستاذ_التربية_البدنية"
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("download_all_reports_combined_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SportTeal)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تحميل الملف البيداغوجي الكامل (كراس + بطاقة + نتائج)", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // Edit Teacher Profile Dialog
    // ----------------------------------------------------
    if (showEditProfileDialog) {
        var fullName by remember { mutableStateOf(teacherProfile.fullName) }
        var schoolName by remember { mutableStateOf(teacherProfile.schoolName) }
        var grade by remember { mutableStateOf(teacherProfile.grade) }
        var academicYear by remember { mutableStateOf(teacherProfile.academicYear) }
        var district by remember { mutableStateOf(teacherProfile.district) }
        var phone by remember { mutableStateOf(teacherProfile.phone) }
        var email by remember { mutableStateOf(teacherProfile.email) }
        var weeklyHours by remember { mutableStateOf(teacherProfile.weeklyHours) }
        var regNum by remember { mutableStateOf(teacherProfile.registrationNumber) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text("تعديل معلومات وبطاقة الأستاذ", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم واللقب") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { schoolName = it },
                        label = { Text("المؤسسة التعليمية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("الرتبة المهنية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("السنة الدراسية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("المقاطعة التفتيشية / الولاية") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = regNum,
                        onValueChange = { regNum = it },
                        label = { Text("رقم التسجيل الوظيفي") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weeklyHours,
                        onValueChange = { weeklyHours = it },
                        label = { Text("الحجم الساعي الأسبوعي") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTeacherProfile(
                            TeacherProfile(
                                fullName = fullName.trim(),
                                schoolName = schoolName.trim(),
                                grade = grade.trim(),
                                academicYear = academicYear.trim(),
                                district = district.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                weeklyHours = weeklyHours.trim(),
                                registrationNumber = regNum.trim()
                            )
                        )
                        showEditProfileDialog = false
                        Toast.makeText(context, "تم حفظ معلومات الأستاذ بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حفظ المعلومات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ----------------------------------------------------
    // Add / Edit Daily Logbook Entry Dialog
    // ----------------------------------------------------
    if (isAddingNewEntry || entryToEdit != null) {
        val editing = entryToEdit
        var date by remember { mutableStateOf(editing?.date ?: "2026-10-18") }
        var dayOfWeek by remember { mutableStateOf(editing?.dayOfWeek ?: "الأحد") }
        var timeSlot by remember { mutableStateOf(editing?.timeSlot ?: "08:00 - 10:00") }
        var selectedClassId by remember { mutableStateOf(editing?.classId ?: (classes.firstOrNull()?.id ?: "")) }
        var activityType by remember { mutableStateOf(editing?.activityType ?: "كرة اليد") }
        var domain by remember { mutableStateOf(editing?.domain ?: "الميدان الجماعي") }
        var sessionNumber by remember { mutableStateOf(editing?.sessionNumber ?: "الحصة 2 (تعلمية)") }
        var objective by remember { mutableStateOf(editing?.objective ?: "التحكم في التمرير والاستقبال وبناء هجمة نحو الهدف") }
        var warmUp by remember { mutableStateOf(editing?.warmUpPhase ?: "تسخين عام وخاص بالكرة (15 د)") }
        var mainPhase by remember { mutableStateOf(editing?.mainPhase ?: "مواقف لعب 3 ضد 2: التمرير والتنطيط والتسديد (65 د)") }
        var coolDown by remember { mutableStateOf(editing?.coolDownPhase ?: "استرخاء وتهدئة عضلية وتقييم ختامي (10 د)") }
        var absentCountStr by remember { mutableStateOf((editing?.absentCount ?: 0).toString()) }
        var absentNames by remember { mutableStateOf(editing?.absentNames ?: "") }
        var equipment by remember { mutableStateOf(editing?.equipment ?: "كرات يد، أقماع، صدريات تمييز") }
        var observations by remember { mutableStateOf(editing?.observations ?: "تحقيق أهداف الحصة وتفاعل ممتاز من التلاميذ") }

        val currentCls = classes.firstOrNull { it.id == selectedClassId } ?: classes.firstOrNull()

        AlertDialog(
            onDismissRequest = {
                isAddingNewEntry = false
                entryToEdit = null
            },
            title = {
                Text(if (editing == null) "إضافة حصة جديدة للكراس اليومي" else "تعديل حصة الكراس اليومي", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Date & Day
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dayOfWeek,
                            onValueChange = { dayOfWeek = it },
                            label = { Text("اليوم") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("التاريخ") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                    }

                    // Time Slot
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = { timeSlot = it },
                        label = { Text("التوقيت (مثال: 08:00 - 10:00)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Class Picker Chips
                    Text("اختر القسم / الفوج:", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        classes.forEach { c ->
                            FilterChip(
                                selected = (selectedClassId == c.id),
                                onClick = { selectedClassId = c.id },
                                label = { Text(c.name) }
                            )
                        }
                    }

                    // Activity & Domain
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = activityType,
                            onValueChange = { activityType = it },
                            label = { Text("النشاط المبرمج") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = domain,
                            onValueChange = { domain = it },
                            label = { Text("الميدان") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = sessionNumber,
                        onValueChange = { sessionNumber = it },
                        label = { Text("نوع ورقم الحصة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = objective,
                        onValueChange = { objective = it },
                        label = { Text("الهدف التعلمي / الكفاءة المستهدفة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = warmUp,
                        onValueChange = { warmUp = it },
                        label = { Text("المرحلة التحضيرية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = mainPhase,
                        onValueChange = { mainPhase = it },
                        label = { Text("المرحلة التعلمية الرئيسية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = coolDown,
                        onValueChange = { coolDown = it },
                        label = { Text("المرحلة الختامية والتقويم") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = absentCountStr,
                            onValueChange = { absentCountStr = it },
                            label = { Text("عدد الغائبين") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = absentNames,
                            onValueChange = { absentNames = it },
                            label = { Text("أسماء/ملاحظات الغياب") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("العتاد الرياضي المستخدم") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = observations,
                        onValueChange = { observations = it },
                        label = { Text("ملاحظات الأستاذ وتقييم الإنجاز") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cls = currentCls ?: classes.firstOrNull()
                        val newEntry = DailyLogbookEntry(
                            id = editing?.id ?: "log_${System.currentTimeMillis()}",
                            date = date.trim(),
                            dayOfWeek = dayOfWeek.trim(),
                            timeSlot = timeSlot.trim(),
                            classId = cls?.id ?: "",
                            className = cls?.name ?: "",
                            cycle = cls?.cycle ?: "الطور الثانوي",
                            activityType = activityType.trim(),
                            domain = domain.trim(),
                            sessionNumber = sessionNumber.trim(),
                            objective = objective.trim(),
                            warmUpPhase = warmUp.trim(),
                            mainPhase = mainPhase.trim(),
                            coolDownPhase = coolDown.trim(),
                            absentCount = absentCountStr.toIntOrNull() ?: 0,
                            absentNames = absentNames.trim(),
                            equipment = equipment.trim(),
                            observations = observations.trim()
                        )
                        viewModel.addOrUpdateLogbookEntry(newEntry)
                        isAddingNewEntry = false
                        entryToEdit = null
                        Toast.makeText(context, "تم حفظ الحصة في الكراس اليومي!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حفظ الحصة")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isAddingNewEntry = false
                        entryToEdit = null
                    }
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    // ----------------------------------------------------
    // Delete Logbook Entry Confirmation Dialog
    // ----------------------------------------------------
    if (entryToDelete != null) {
        val entry = entryToDelete!!
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("حذف حصة من الكراس اليومي") },
            text = { Text("هل تريد بالتأكيد حذف حصة ${entry.className} (${entry.activityType} - ${entry.date}) من الكراس اليومي؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLogbookEntry(entry.id)
                        entryToDelete = null
                        Toast.makeText(context, "تم حذف الحصة بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ProfileFieldRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "$label: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun LogbookEntryCard(
    entry: DailyLogbookEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDownloadSingle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("logbook_entry_card_${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SportBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${entry.dayOfWeek} ${entry.date}",
                            color = SportBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.timeSlot,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onDownloadSingle, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Download, contentDescription = "تحميل جذاذة الحصة", tint = SportTeal, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Class & Sport
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${entry.className} • ${entry.activityType}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    color = SportAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = entry.sessionNumber,
                        color = SportAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Objective
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "🎯 الهدف التعلمي:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = entry.objective,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Phases & Details
            Text(text = "1. تحضيرية: ${entry.warmUpPhase}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "2. تعلمية: ${entry.mainPhase}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "3. ختامية: ${entry.coolDownPhase}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الغياب: ${if (entry.absentCount == 0) "لا يوجد" else "${entry.absentCount} تلميذ"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (entry.absentCount == 0) ActionPositive else ActionNegative
                )
                Text(
                    text = "العتاد: ${entry.equipment}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
