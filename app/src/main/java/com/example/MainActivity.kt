package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import com.example.data.model.TermType
import com.example.ui.components.SmartSpeedStopwatchDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddClassDialog
import com.example.ui.components.ClassSelectorHeader
import com.example.ui.components.ResultSheetScannerDialog
import com.example.ui.screens.AssessmentAttendanceScreen
import com.example.ui.screens.CriteriaScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.HandballTrackerScreen
import com.example.ui.screens.RankExportBackupScreen
import com.example.ui.screens.RosterScreen
import com.example.ui.screens.TeacherLogbookScreen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SportAmber
import com.example.ui.theme.SportAmberContainer
import com.example.ui.theme.SportAmberDark
import com.example.ui.theme.SportBlue
import com.example.ui.theme.SportBlueDark
import com.example.ui.theme.SportGold
import com.example.ui.viewmodel.EpsViewModel

enum class MainDestination(val titleAr: String, val icon: ImageVector, val tag: String) {
    ROSTER("التلاميذ", Icons.Default.People, "nav_roster"),
    DIAGNOSTIC("التشخيص", Icons.Default.DirectionsRun, "nav_diagnostic"),
    HANDBALL("كرة اليد", Icons.Default.SportsHandball, "nav_handball"),
    CRITERIA("المعايير", Icons.Default.PieChart, "nav_criteria"),
    ASSESSMENT("التقويم", Icons.Default.AssignmentTurnedIn, "nav_assessment"),
    RANK_BACKUP("التنزيل والترتيب", Icons.Default.Download, "nav_rank_backup")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: EpsViewModel = viewModel()
                    EpsMainScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpsMainScreen(viewModel: EpsViewModel) {
    var currentDestination by remember { mutableStateOf(MainDestination.ROSTER) }
    var showAddClassDialog by remember { mutableStateOf(false) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var showTeacherLogbook by remember { mutableStateOf(false) }
    var showChangeSportsDialog by remember { mutableStateOf(false) }
    var showStopwatchDialog by remember { mutableStateOf(false) }

    val classes by viewModel.classes.collectAsState()
    val selectedClassId by viewModel.selectedClassId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Handle back button: return to Roster screen if in subscreen, or close logbook
    BackHandler(enabled = showTeacherLogbook || currentDestination != MainDestination.ROSTER) {
        if (showTeacherLogbook) {
            showTeacherLogbook = false
        } else {
            currentDestination = MainDestination.ROSTER
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top App Bar - تصميم ناصع ومريح للقراءة تحت أشعة الشمس المباشرة
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.eps_shortcut_icon_1792115255311),
                                    contentDescription = "شعار أستاذ التربية البدنية والرياضية",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "أستاذ التربية البدنية والرياضية",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "التقويم، رصد المقابلات والغيابات، والنسخ الاحتياطي",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { currentDestination = MainDestination.RANK_BACKUP },
                            modifier = Modifier.testTag("top_bar_download_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "مركز التنزيل والتحميل المباشر",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showStopwatchDialog = true },
                            modifier = Modifier.testTag("top_bar_stopwatch_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "الميقاتي الذكي لحساب السرعة والنتائج آلياً",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showTeacherLogbook = true },
                            modifier = Modifier.testTag("top_bar_teacher_logbook_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = "الكراس اليومي وبطاقة الأستاذ",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showScannerDialog = true },
                            modifier = Modifier.testTag("top_bar_scan_sheet_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = "تسجيل النتائج والأسماء بمسح الورقة",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Class selector chips & search row
                ClassSelectorHeader(
                    classes = classes,
                    selectedClassId = selectedClassId,
                    onSelectClass = { viewModel.selectClass(it) },
                    onAddClassClicked = { showAddClassDialog = true },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onUpdateAcademicYear = { classId, yr -> viewModel.updateClassAcademicYear(classId, yr) },
                    onUpdateAllAcademicYears = { yr -> viewModel.updateAllClassesAcademicYear(yr) }
                )

                // Term selector chips (Trimester 1, 2, 3)
                val selectedTerm by viewModel.selectedTerm.collectAsState()
                val currentClass = classes.firstOrNull { it.id == selectedClassId }
                com.example.ui.components.TermSelectorHeader(
                    selectedTerm = selectedTerm,
                    onSelectTerm = { viewModel.selectTerm(it) },
                    selectedClass = currentClass,
                    onChangeSports = { showChangeSportsDialog = true }
                )
            }
        },
        bottomBar = {
            val selectedTerm by viewModel.selectedTerm.collectAsState()
            val currentClass = classes.firstOrNull { it.id == selectedClassId }
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                MainDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    val currentCollectiveSport = currentClass?.getCollectiveSport(selectedTerm) ?: selectedTerm.collectiveSportAr
                    val label = if (destination == MainDestination.HANDBALL) {
                        currentCollectiveSport
                    } else {
                        destination.titleAr
                    }
                    val icon = if (destination == MainDestination.HANDBALL) {
                        when {
                            currentCollectiveSport.contains("قدم") -> Icons.Default.SportsSoccer
                            currentCollectiveSport.contains("سلة") -> Icons.Default.SportsBasketball
                            currentCollectiveSport.contains("طائرة") -> Icons.Default.SportsVolleyball
                            else -> Icons.Default.SportsHandball
                        }
                    } else {
                        destination.icon
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(destination.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    MainDestination.ROSTER -> RosterScreen(viewModel = viewModel)
                    MainDestination.DIAGNOSTIC -> DiagnosticScreen(viewModel = viewModel)
                    MainDestination.HANDBALL -> HandballTrackerScreen(viewModel = viewModel)
                    MainDestination.CRITERIA -> CriteriaScreen(viewModel = viewModel)
                    MainDestination.ASSESSMENT -> AssessmentAttendanceScreen(viewModel = viewModel)
                    MainDestination.RANK_BACKUP -> RankExportBackupScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showAddClassDialog) {
        AddClassDialog(
            onDismiss = { showAddClassDialog = false },
            onAddClass = { name, level, academicYear, cycle ->
                viewModel.addClass(name, level, academicYear, cycle)
                showAddClassDialog = false
            }
        )
    }

    if (showScannerDialog) {
        ResultSheetScannerDialog(
            viewModel = viewModel,
            onDismiss = { showScannerDialog = false }
        )
    }

    if (showTeacherLogbook) {
        TeacherLogbookScreen(
            viewModel = viewModel,
            onClose = { showTeacherLogbook = false }
        )
    }

    if (showStopwatchDialog) {
        SmartSpeedStopwatchDialog(
            viewModel = viewModel,
            onDismiss = { showStopwatchDialog = false }
        )
    }

    val activeClassForDialog = classes.firstOrNull { it.id == selectedClassId }
    val activeTermForDialog by viewModel.selectedTerm.collectAsState()
    if (showChangeSportsDialog && activeClassForDialog != null) {
        com.example.ui.components.ChangeSportsDialog(
            currentClass = activeClassForDialog,
            initialTerm = activeTermForDialog,
            onDismiss = { showChangeSportsDialog = false },
            onSave = { term, ind, col, all, sameCycle ->
                viewModel.updateClassSports(
                    classId = activeClassForDialog.id,
                    term = term,
                    individualSport = ind,
                    collectiveSport = col,
                    applyToAllClasses = all,
                    applyToSameCycle = sameCycle
                )
                showChangeSportsDialog = false
            }
        )
    }
}
