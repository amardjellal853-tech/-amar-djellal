package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ClassRoom
import com.example.data.model.Gender
import com.example.data.model.ImportedStudentResult
import com.example.data.model.TermType
import com.example.data.ocr.ResultSheetScannerService
import com.example.data.ocr.ScanResult
import com.example.ui.theme.ActionNegative
import com.example.ui.theme.ActionPositive
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportTeal
import com.example.ui.viewmodel.EpsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultSheetScannerDialog(
    viewModel: EpsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val classes by viewModel.classes.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val selectedTerm by viewModel.selectedTerm.collectAsState()

    var targetClassId by remember(selectedClass) { mutableStateOf(selectedClass?.id ?: (classes.firstOrNull()?.id ?: "c1")) }
    var targetTerm by remember(selectedTerm) { mutableStateOf(selectedTerm) }

    // Mode: 0: تصوير/صورة (Camera/Image), 1: لصق/تحميل ملف نصي (Text/File), 2: نماذج كشوف جاهزة (Presets)
    var selectedMethodTab by remember { mutableIntStateOf(0) }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rawTextSheetInput by remember { mutableStateOf("") }

    val extractedItems = remember { mutableStateListOf<ImportedStudentResult>() }

    // Import options
    var importStudentNames by remember { mutableStateOf(true) }
    var updateContinuousScores by remember { mutableStateOf(true) }
    var updateTestScores by remember { mutableStateOf(true) }
    var updateExamScores by remember { mutableStateOf(true) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            isProcessing = true
            errorMessage = null
            coroutineScope.launch {
                val result = ResultSheetScannerService.analyzeImage(bitmap)
                isProcessing = false
                when (result) {
                    is ScanResult.Success -> {
                        extractedItems.clear()
                        extractedItems.addAll(result.items)
                        Toast.makeText(context, "تم استخراج ${result.items.size} تلميذاً بنجاح", Toast.LENGTH_SHORT).show()
                    }
                    is ScanResult.Error -> {
                        errorMessage = result.message
                    }
                }
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    isProcessing = true
                    errorMessage = null
                    val bitmap = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    if (bitmap != null) {
                        selectedBitmap = bitmap
                        val result = ResultSheetScannerService.analyzeImage(bitmap)
                        isProcessing = false
                        when (result) {
                            is ScanResult.Success -> {
                                extractedItems.clear()
                                extractedItems.addAll(result.items)
                                Toast.makeText(context, "تم استخراج ${result.items.size} تلميذاً بنجاح", Toast.LENGTH_SHORT).show()
                            }
                            is ScanResult.Error -> {
                                errorMessage = result.message
                            }
                        }
                    } else {
                        isProcessing = false
                        errorMessage = "تعذر تحميل ملف الصورة."
                    }
                } catch (e: Exception) {
                    isProcessing = false
                    errorMessage = "حدث خطأ أثناء قراءة الصورة: ${e.localizedMessage ?: e.message}"
                }
            }
        }
    }

    // Document / text file picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val fileContent = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                    if (!fileContent.isNullOrBlank()) {
                        rawTextSheetInput = fileContent
                        val parsed = ResultSheetScannerService.parseTextSheet(fileContent)
                        if (parsed.isNotEmpty()) {
                            extractedItems.clear()
                            extractedItems.addAll(parsed)
                            Toast.makeText(context, "تم استخراج ${parsed.size} تلميذاً من الملف", Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = "لم يتم التعرف على بنية كشف نقاط في الملف."
                        }
                    }
                } catch (e: Exception) {
                    errorMessage = "تعذر قراءة الملف: ${e.message}"
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "تسجيل النتائج والأسماء من ورقة كشف النقاط",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 17.sp
                        )
                        Text(
                            text = "تصوير بالكاميرا، تحميل صورة، أو استيراد ملف نصي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_scanner_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Input Method Selector Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedMethodTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedMethodTab == 0,
                        onClick = { selectedMethodTab = 0 },
                        text = { Text("صورة / كاميرا", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_camera_image")
                    )
                    Tab(
                        selected = selectedMethodTab == 1,
                        onClick = { selectedMethodTab = 1 },
                        text = { Text("ملف / نص", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_file_text")
                    )
                    Tab(
                        selected = selectedMethodTab == 2,
                        onClick = { selectedMethodTab = 2 },
                        text = { Text("نماذج جاهزة", fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_presets")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Content Body (Scrollable)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Method Action Area
                    item {
                        when (selectedMethodTab) {
                            0 -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "التقاط أو تحميل ورقة كشف النقاط:",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = { cameraLauncher.launch(null) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("scan_take_photo_btn"),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("تصوير الورقة", fontSize = 13.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    photoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("scan_pick_image_btn")
                                            ) {
                                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("تحميل صورة", fontSize = 13.sp)
                                            }
                                        }

                                        // Image thumbnail if loaded
                                        selectedBitmap?.let { bmp ->
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color.Black.copy(alpha = 0.05f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Image(
                                                        bitmap = bmp.asImageBitmap(),
                                                        contentDescription = "معاينة الورقة",
                                                        modifier = Modifier
                                                            .size(54.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = "تم تحميل صورة ورقة النتائج",
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                        Text(
                                                            text = "${bmp.width} × ${bmp.height} بكسل",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            isProcessing = true
                                                            errorMessage = null
                                                            coroutineScope.launch {
                                                                val result = ResultSheetScannerService.analyzeImage(bmp)
                                                                isProcessing = false
                                                                when (result) {
                                                                    is ScanResult.Success -> {
                                                                        extractedItems.clear()
                                                                        extractedItems.addAll(result.items)
                                                                    }
                                                                    is ScanResult.Error -> {
                                                                        errorMessage = result.message
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Refresh, contentDescription = "إعادة المسح")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "استيراد ملف نصي أو لصق الجدول:",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            OutlinedButton(
                                                onClick = { documentPickerLauncher.launch("*/*") },
                                                modifier = Modifier.testTag("upload_file_btn")
                                            ) {
                                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تحميل ملف (CSV/TXT)", fontSize = 11.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedTextField(
                                            value = rawTextSheetInput,
                                            onValueChange = {
                                                rawTextSheetInput = it
                                                val parsed = ResultSheetScannerService.parseTextSheet(it)
                                                if (parsed.isNotEmpty()) {
                                                    extractedItems.clear()
                                                    extractedItems.addAll(parsed)
                                                }
                                            },
                                            label = { Text("الصق أسطر كشف النقاط (الرقم، اللقب والاسم، علامة التقويم، الفرض...)") },
                                            placeholder = {
                                                Text("مثال:\n1\tبن علي أحمد\t16.5\t15.0\t17.0\n2\tعيساني مريم\t18.0\t17.5\t18.5")
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .testTag("raw_text_sheet_input"),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "يقوم النظام تلقائياً بالتعرف على أسماء التلاميذ والعلامات المفصولة بفواصل أو مسافات.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            2 -> {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "نماذج كشوف نقاط جاهزة للاختبار الفوري:",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "اختر أحد النماذج المعتمدة في الطور الثانوي لتجربة الاستيراد بضغطة زر واحدة:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        val samplePresets = remember { ResultSheetScannerService.getPresetSampleSheets() }
                                        samplePresets.forEach { (title, list) ->
                                            OutlinedButton(
                                                onClick = {
                                                    extractedItems.clear()
                                                    extractedItems.addAll(list)
                                                    errorMessage = null
                                                    Toast.makeText(context, "تم تحميل نموذج: $title (${list.size} تلميذاً)", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .testTag("preset_sheet_${title.take(5)}"),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = SportAmber)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text("${list.size} تلميذاً", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Loading State
                    if (isProcessing) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "جاري قراءة ورقة النتائج واستخراج الأسماء والنقاط بالذكاء الاصطناعي...",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Error Message
                    errorMessage?.let { errorText ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = ActionNegative.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = ActionNegative)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "ملاحظة حول المسح الضوئي:",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ActionNegative
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = errorText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            selectedMethodTab = 2 // Switch to presets
                                        },
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        Text("استخدام النماذج الجاهزة أو اللصق النصي", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Extracted Results Table & Review
                    if (extractedItems.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "معاينة التلاميذ والنقاط المستخرجة (${extractedItems.size} تلميذاً):",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "يمكنك مراجعة وتعديل أي اسم أو نقطة قبل حفظها في القسم",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                extractedItems.add(
                                                    ImportedStudentResult(
                                                        regNumber = extractedItems.size + 1,
                                                        fullName = "تلميذ جديد",
                                                        gender = Gender.MALE,
                                                        continuousScore = 15.0,
                                                        testScore = 14.0,
                                                        examScore = 15.0
                                                    )
                                                )
                                            }
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("إضافة سطر")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Summary stats pills
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val maleCount = extractedItems.count { it.gender == Gender.MALE }
                                        val femaleCount = extractedItems.count { it.gender == Gender.FEMALE }
                                        val exemptCount = extractedItems.count { it.isMedicallyExempt }

                                        Surface(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "المجموع: ${extractedItems.size}",
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                textAlign = TextAlign.Center,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Surface(
                                            color = SportBlue.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "ذكور: $maleCount",
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                textAlign = TextAlign.Center,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SportBlue
                                            )
                                        }
                                        Surface(
                                            color = Color(0xFFC2185B).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "إناث: $femaleCount",
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                textAlign = TextAlign.Center,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC2185B)
                                            )
                                        }
                                        if (exemptCount > 0) {
                                            Surface(
                                                color = ActionNegative.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = "معفى: $exemptCount",
                                                    modifier = Modifier.padding(vertical = 4.dp),
                                                    textAlign = TextAlign.Center,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ActionNegative
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Extracted student items list
                        itemsIndexed(extractedItems) { index, item ->
                            ExtractedStudentRowCard(
                                item = item,
                                index = index,
                                onUpdate = { updated ->
                                    extractedItems[index] = updated
                                },
                                onDelete = {
                                    extractedItems.removeAt(index)
                                }
                            )
                        }

                        // Import Target & Scope Options Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "خيارات الحفظ والاستيراد:",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Target class chips
                                    Text(text = "القسم المستهدف:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        classes.forEach { cls ->
                                            FilterChip(
                                                selected = targetClassId == cls.id,
                                                onClick = { targetClassId = cls.id },
                                                label = { Text(cls.name, fontSize = 11.sp) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Target Term chips
                                    Text(text = "الفصل المستهدف:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        TermType.values().forEach { term ->
                                            FilterChip(
                                                selected = targetTerm == term,
                                                onClick = { targetTerm = term },
                                                label = { Text(term.labelAr, fontSize = 11.sp) }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Checkboxes for what to apply
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { importStudentNames = !importStudentNames }
                                    ) {
                                        Checkbox(
                                            checked = importStudentNames,
                                            onCheckedChange = { importStudentNames = it }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("إضافة/تحديث أسماء التلاميذ في قائمة الفوج", fontSize = 13.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { updateContinuousScores = !updateContinuousScores }
                                    ) {
                                        Checkbox(
                                            checked = updateContinuousScores,
                                            onCheckedChange = { updateContinuousScores = it }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تسجيل علامات التقويم المستمر (من 20)", fontSize = 13.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { updateTestScores = !updateTestScores }
                                    ) {
                                        Checkbox(
                                            checked = updateTestScores,
                                            onCheckedChange = { updateTestScores = it }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تسجيل علامات الفرض الفصلي (من 20)", fontSize = 13.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { updateExamScores = !updateExamScores }
                                    ) {
                                        Checkbox(
                                            checked = updateExamScores,
                                            onCheckedChange = { updateExamScores = it }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تسجيل علامات الاختبار / الرياضة الفردية (من 20)", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (extractedItems.isEmpty()) {
                                Toast.makeText(context, "يرجى مسح أو إدخال ورقة نتائج أولاً", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val targetClassName = classes.find { it.id == targetClassId }?.name ?: ""
                            val count = viewModel.importStudentsAndResults(
                                classId = targetClassId,
                                term = targetTerm,
                                importedList = extractedItems,
                                importStudentsNames = importStudentNames,
                                updateContinuousScore = updateContinuousScores,
                                updateTestScore = updateTestScores,
                                updateExamScore = updateExamScores
                            )
                            Toast.makeText(
                                context,
                                "تم استيراد $count تلميذاً وتحديث نتائجهم في ($targetClassName) بنجاح!",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismiss()
                        },
                        enabled = extractedItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(2f)
                            .testTag("confirm_import_results_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (extractedItems.isNotEmpty()) "تأكيد واستيراد (${extractedItems.size} تلميذاً)" else "تأكيد واستيراد",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExtractedStudentRowCard(
    item: ImportedStudentResult,
    index: Int,
    onUpdate: (ImportedStudentResult) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isMedicallyExempt) ActionNegative.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Index badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${item.regNumber.takeIf { it > 0 } ?: (index + 1)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Student name field
                OutlinedTextField(
                    value = item.fullName,
                    onValueChange = { onUpdate(item.copy(fullName = it)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("اللقب والاسم", fontSize = 10.sp) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Gender Toggle
                IconButton(
                    onClick = {
                        val nextGender = if (item.gender == Gender.MALE) Gender.FEMALE else Gender.MALE
                        onUpdate(item.copy(gender = nextGender))
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (item.gender == Gender.MALE) Icons.Default.Male else Icons.Default.Female,
                        contentDescription = "الجنس",
                        tint = if (item.gender == Gender.MALE) SportBlue else Color(0xFFC2185B)
                    )
                }

                // Exempt Toggle
                IconButton(
                    onClick = {
                        onUpdate(item.copy(isMedicallyExempt = !item.isMedicallyExempt))
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "معفى طبياً",
                        tint = if (item.isMedicallyExempt) ActionNegative else MaterialTheme.colorScheme.outline
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف السطر",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Score inputs row (Continuous /20, Test /20, Exam /20)
            if (!item.isMedicallyExempt) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Continuous score
                    OutlinedTextField(
                        value = item.continuousScore?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "",
                        onValueChange = { text ->
                            val v = text.replace(',', '.').toDoubleOrNull()
                            onUpdate(item.copy(continuousScore = v?.coerceIn(0.0, 20.0)))
                        },
                        label = { Text("تقويم مستمر /20", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Test score
                    OutlinedTextField(
                        value = item.testScore?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "",
                        onValueChange = { text ->
                            val v = text.replace(',', '.').toDoubleOrNull()
                            onUpdate(item.copy(testScore = v?.coerceIn(0.0, 20.0)))
                        },
                        label = { Text("الفرض /20", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    // Exam score
                    OutlinedTextField(
                        value = item.examScore?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "",
                        onValueChange = { text ->
                            val v = text.replace(',', '.').toDoubleOrNull()
                            onUpdate(item.copy(examScore = v?.coerceIn(0.0, 20.0)))
                        },
                        label = { Text("الاختبار /20", fontSize = 9.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Surface(
                    color = ActionNegative.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "التلميذ معفى طبياً من التربية البدنية والرياضية (C.M)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ActionNegative,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }
}
