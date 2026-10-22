package com.example.data.repository

import android.content.Context
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.ClassRoom
import com.example.data.model.CollectiveMatchAction
import com.example.data.model.ContinuousAssessment
import com.example.data.model.ContinuousAssessmentScale
import com.example.data.model.CriterionEvaluation
import com.example.data.model.DiagnosticEvaluation
import com.example.data.model.Gender
import com.example.data.model.HandballAction
import com.example.data.model.ImportedStudentResult
import com.example.data.model.IndividualSportScale
import com.example.data.model.Student
import com.example.data.model.TeacherProfile
import com.example.data.model.DailyLogbookEntry
import com.example.data.model.TermAssessment
import com.example.data.model.TermType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class EpsRepository(private val context: Context) {

    private val storageFile = File(context.filesDir, "eps_data_v4.json")

    private val _classes = MutableStateFlow<List<ClassRoom>>(emptyList())
    val classes: StateFlow<List<ClassRoom>> = _classes.asStateFlow()

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    // التقييمات الفصلية المفصلة (مفتاح: "${studentId}_${termNumber}")
    private val _termAssessments = MutableStateFlow<Map<String, TermAssessment>>(emptyMap())
    val termAssessments: StateFlow<Map<String, TermAssessment>> = _termAssessments.asStateFlow()

    // رصد الرياضات الجماعية للفصول الثلاثة (مفتاح: "${studentId}_${termNumber}")
    private val _collectiveActions = MutableStateFlow<Map<String, CollectiveMatchAction>>(emptyMap())
    val collectiveActions: StateFlow<Map<String, CollectiveMatchAction>> = _collectiveActions.asStateFlow()

    private val _attendances = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendances: StateFlow<List<AttendanceRecord>> = _attendances.asStateFlow()

    // معلومات الأستاذ والكراس اليومي التربوي
    private val _teacherProfile = MutableStateFlow(TeacherProfile())
    val teacherProfile: StateFlow<TeacherProfile> = _teacherProfile.asStateFlow()

    private val _logbookEntries = MutableStateFlow<List<DailyLogbookEntry>>(emptyList())
    val logbookEntries: StateFlow<List<DailyLogbookEntry>> = _logbookEntries.asStateFlow()

    // توافقية مع الواجهات القائمة للفصل الأول
    private val _diagnostics = MutableStateFlow<Map<String, DiagnosticEvaluation>>(emptyMap())
    val diagnostics: StateFlow<Map<String, DiagnosticEvaluation>> = _diagnostics.asStateFlow()

    private val _handballActions = MutableStateFlow<Map<String, HandballAction>>(emptyMap())
    val handballActions: StateFlow<Map<String, HandballAction>> = _handballActions.asStateFlow()

    private val _criteria = MutableStateFlow<Map<String, CriterionEvaluation>>(emptyMap())
    val criteria: StateFlow<Map<String, CriterionEvaluation>> = _criteria.asStateFlow()

    private val _continuousAssessments = MutableStateFlow<Map<String, ContinuousAssessment>>(emptyMap())
    val continuousAssessments: StateFlow<Map<String, ContinuousAssessment>> = _continuousAssessments.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        if (storageFile.exists()) {
            try {
                val jsonString = storageFile.readText()
                parseJson(jsonString)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        loadInitialPresetData()
    }

    fun loadInitialPresetData() {
        val class1 = ClassRoom("c1", "1 ج م ع ت 1", "السنة الأولى ثانوي (جذع مشترك علوم وتكنولوجيا)", "2026/2027", "الطور الثانوي")
        val class2 = ClassRoom("c2", "1 ج م آ 1", "السنة الأولى ثانوي (جذع مشترك آداب)", "2026/2027", "الطور الثانوي")
        val class3 = ClassRoom("c3", "2 ع ت 1", "السنة الثانية ثانوي (علوم تجريبية)", "2026/2027", "الطور الثانوي")
        val class4 = ClassRoom("c4", "3 ع ت 1", "السنة الثالثة ثانوي (علوم تجريبية - بكالوريا)", "2026/2027", "الطور الثانوي")
        _classes.value = listOf(class1, class2, class3, class4)

        val namesC1 = listOf(
            "أيوب بن علي" to Gender.MALE,
            "مريم بلقاسم" to Gender.FEMALE,
            "ياسين عماري" to Gender.MALE,
            "أمينة بوزيد" to Gender.FEMALE,
            "عبد الرؤوف دراجي" to Gender.MALE,
            "إيمان خلوفي" to Gender.FEMALE,
            "وليد براهيمي" to Gender.MALE,
            "سارة منصوري" to Gender.FEMALE,
            "محمد لمين زرقي" to Gender.MALE,
            "فاطمة الزهراء قاسمي" to Gender.FEMALE,
            "إسلام مداني" to Gender.MALE,
            "خديجة غربي" to Gender.FEMALE,
            "أكرم زغدود" to Gender.MALE,
            "هبة الله بن عمار" to Gender.FEMALE,
            "يوسف طاهري" to Gender.MALE,
            "شيماء زروقي" to Gender.FEMALE,
            "بلال بولنوار" to Gender.MALE,
            "نهال بوعزيز" to Gender.FEMALE
        )

        val namesC2 = listOf(
            "عبد الجليل حركات" to Gender.MALE,
            "بشرى معمري" to Gender.FEMALE,
            "هيثم بوحفص" to Gender.MALE,
            "سلسبيل شريف" to Gender.FEMALE,
            "رياض محرزي" to Gender.MALE,
            "نورهان قدور" to Gender.FEMALE,
            "أسامة بلقاضي" to Gender.MALE,
            "ريان لعريبي" to Gender.MALE,
            "سلمى بوجمعة" to Gender.FEMALE,
            "زكرياء ساعد" to Gender.MALE,
            "ياسمين بوقرة" to Gender.FEMALE,
            "معاذ مسعودي" to Gender.MALE,
            "هاجر بن ناصر" to Gender.FEMALE,
            "إلياس علوش" to Gender.MALE,
            "مروى رحماني" to Gender.FEMALE,
            "أنس حمداوي" to Gender.MALE,
            "كوثر حليمي" to Gender.FEMALE,
            "حمزة حمو" to Gender.MALE
        )

        val namesC3 = listOf(
            "عبد الرحيم سلطاني" to Gender.MALE,
            "نور الهدى بوشامة" to Gender.FEMALE,
            "حمزة قدري" to Gender.MALE,
            "وئام بن زايد" to Gender.FEMALE,
            "طه بن شيخ" to Gender.MALE,
            "رانية بوكرش" to Gender.FEMALE,
            "عصام شارف" to Gender.MALE,
            "أماني بركات" to Gender.FEMALE,
            "أحمد ياسين زروق" to Gender.MALE,
            "صابرين بوعبد الله" to Gender.FEMALE,
            "صلاح الدين عمور" to Gender.MALE,
            "إيناس عمارة" to Gender.FEMALE,
            "زبير بوعلاق" to Gender.MALE,
            "سندس مقراني" to Gender.FEMALE,
            "فاروق بن سعيد" to Gender.MALE,
            "سماح بوشارب" to Gender.FEMALE
        )

        val namesC4 = listOf(
            "أيمن بن عيسى" to Gender.MALE,
            "ملاك بوجلال" to Gender.FEMALE,
            "سامي مسعوداني" to Gender.MALE,
            "خلود شيباني" to Gender.FEMALE,
            "منصف لعور" to Gender.MALE,
            "دعاء زروقي" to Gender.FEMALE,
            "هيثم بن جدو" to Gender.MALE,
            "يسرى بلحاج" to Gender.FEMALE,
            "حسام الدين دربال" to Gender.MALE,
            "صفاء عيساوي" to Gender.FEMALE,
            "عادل قندوز" to Gender.MALE,
            "عبير بوستة" to Gender.FEMALE,
            "مهدي حميدي" to Gender.MALE,
            "ريتاج بوزيان" to Gender.FEMALE
        )

        val studentList = mutableListOf<Student>()
        val termMap = mutableMapOf<String, TermAssessment>()
        val colMap = mutableMapOf<String, CollectiveMatchAction>()
        val attList = mutableListOf<AttendanceRecord>()

        val diagCompat = mutableMapOf<String, DiagnosticEvaluation>()
        val hbCompat = mutableMapOf<String, HandballAction>()
        val critCompat = mutableMapOf<String, CriterionEvaluation>()
        val contCompat = mutableMapOf<String, ContinuousAssessment>()

        fun populateClassStudents(classId: String, names: List<Pair<String, Gender>>) {
            names.forEachIndexed { index, pair ->
                val id = "${classId}_${index + 1}"
                val isExempt = (index == 16)
                studentList.add(
                    Student(
                        id = id,
                        classId = classId,
                        regNumber = index + 1,
                        fullName = pair.first,
                        gender = pair.second,
                        isMedicallyExempt = isExempt,
                        notes = if (isExempt) "شهادة إعفاء طبي معتمدة" else "تلميذ نشيط"
                    )
                )

                // الفصل الأول: الجري السريع 60م (ثوانٍ) + كرة اليد
                // مقياس الأستاذ: نقطة الاختبار بجمع زمن التقويم التشخيصي مع زمن التحصيلي وقسمتهما على 2 ومنها تحسب النقطة على 14
                // والتطور الحاصل كل 0.5 ثانية يقابلها 1 نقطة (حتى 6 نقاط)
                val t1DiagMeas = if (isExempt) null else if (index == 0) 5.5 else if (index == 1) 8.0 else (5.5 + (index % 6) * 0.5)
                val t1SummMeas = if (isExempt || t1DiagMeas == null) null else if (index == 0) 2.5 else if (index == 1) 7.0 else (t1DiagMeas - (0.8 + (index % 5) * 0.4)).coerceAtLeast(2.5)
                val t1Diag14 = if (isExempt) 0.0 else IndividualSportScale.calculateCombinedScore14(TermType.TERM_1, t1DiagMeas, t1SummMeas)
                val t1Prog6 = if (isExempt) 0.0 else IndividualSportScale.calculateProgressionScore6(TermType.TERM_1, t1DiagMeas, t1SummMeas)
                val t1Col = if (isExempt) 0.0 else (12.0 + (index % 6) * 1.1).coerceIn(9.0, 19.0)
                val t1Test = if (isExempt) 14.5 else (11.0 + (index % 8) * 1.0).coerceIn(8.0, 19.0)
                val t1Ass = TermAssessment(
                    studentId = id,
                    termType = TermType.TERM_1,
                    diagnosticMeasurement = t1DiagMeas,
                    summativeMeasurement = t1SummMeas,
                    individualDiagnosticScore14 = t1Diag14,
                    individualProgressionScore6 = t1Prog6,
                    individualMeasurement = t1DiagMeas,
                    collectiveSportScore20 = t1Col,
                    c1MotorScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    c2EfficiencyScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    c3KnowledgeScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    c4BehaviorScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    manualTestScore = t1Test,
                    absenceScore = if (index == 4) 3.0 else 4.0,
                    sportswearScore = if (index == 2) 2.0 else 3.0,
                    behaviorScore = if (index == 3) 3.0 else 4.0,
                    individualTaskScore = if (index == 1) 2.5 else 3.0,
                    teamworkScore = 3.0,
                    creativityScore = if (index == 0) 3.0 else 2.5,
                    teamPlayContributionScore = 3.0
                )
                termMap["${id}_1"] = t1Ass

                // الفصل الثاني: الوثب الطويل (أمتار) + كرة السلة
                val t2DiagMeas = if (isExempt) null else (3.2 + (index % 5) * 0.25)
                val t2SummMeas = if (isExempt || t2DiagMeas == null) null else (t2DiagMeas + 0.35 + (index % 4) * 0.12)
                val t2Diag14 = if (isExempt) 0.0 else IndividualSportScale.calculateCombinedScore14(TermType.TERM_2, t2DiagMeas, t2SummMeas)
                val t2Prog6 = if (isExempt) 0.0 else IndividualSportScale.calculateProgressionScore6(TermType.TERM_2, t2DiagMeas, t2SummMeas)
                val t2Col = if (isExempt) 0.0 else (12.5 + (index % 5) * 1.15).coerceIn(9.5, 19.0)
                val t2Test = if (isExempt) 15.0 else (11.5 + (index % 7) * 1.1).coerceIn(9.0, 19.5)
                val t2Ass = TermAssessment(
                    studentId = id,
                    termType = TermType.TERM_2,
                    diagnosticMeasurement = t2DiagMeas,
                    summativeMeasurement = t2SummMeas,
                    individualDiagnosticScore14 = t2Diag14,
                    individualProgressionScore6 = t2Prog6,
                    individualMeasurement = t2DiagMeas,
                    collectiveSportScore20 = t2Col,
                    c1MotorScore = (t2Col * 0.25).coerceIn(1.0, 5.0),
                    c2EfficiencyScore = (t2Col * 0.25).coerceIn(1.0, 5.0),
                    c3KnowledgeScore = (t2Col * 0.25).coerceIn(1.0, 5.0),
                    c4BehaviorScore = (t2Col * 0.25).coerceIn(1.0, 5.0),
                    manualTestScore = t2Test,
                    absenceScore = if (index == 2) 3.5 else 4.0,
                    sportswearScore = 3.0,
                    behaviorScore = 4.0,
                    individualTaskScore = 3.0,
                    teamworkScore = 3.0,
                    creativityScore = 2.5,
                    teamPlayContributionScore = 3.0
                )
                termMap["${id}_2"] = t2Ass

                // الفصل الثالث: دفع الجلة (أمتار) + كرة الطائرة
                val t3DiagMeas = if (isExempt) null else (5.5 + (index % 5) * 0.5)
                val t3SummMeas = if (isExempt || t3DiagMeas == null) null else (t3DiagMeas + 0.6 + (index % 4) * 0.25)
                val t3Diag14 = if (isExempt) 0.0 else IndividualSportScale.calculateCombinedScore14(TermType.TERM_3, t3DiagMeas, t3SummMeas)
                val t3Prog6 = if (isExempt) 0.0 else IndividualSportScale.calculateProgressionScore6(TermType.TERM_3, t3DiagMeas, t3SummMeas)
                val t3Col = if (isExempt) 0.0 else (13.0 + (index % 5) * 1.05).coerceIn(9.5, 19.0)
                val t3Test = if (isExempt) 15.0 else (12.0 + (index % 7) * 1.05).coerceIn(9.5, 19.5)
                val t3Ass = TermAssessment(
                    studentId = id,
                    termType = TermType.TERM_3,
                    diagnosticMeasurement = t3DiagMeas,
                    summativeMeasurement = t3SummMeas,
                    individualDiagnosticScore14 = t3Diag14,
                    individualProgressionScore6 = t3Prog6,
                    individualMeasurement = t3DiagMeas,
                    collectiveSportScore20 = t3Col,
                    c1MotorScore = (t3Col * 0.25).coerceIn(1.0, 5.0),
                    c2EfficiencyScore = (t3Col * 0.25).coerceIn(1.0, 5.0),
                    c3KnowledgeScore = (t3Col * 0.25).coerceIn(1.0, 5.0),
                    c4BehaviorScore = (t3Col * 0.25).coerceIn(1.0, 5.0),
                    manualTestScore = t3Test,
                    absenceScore = 4.0,
                    sportswearScore = 3.0,
                    behaviorScore = 4.0,
                    individualTaskScore = 3.0,
                    teamworkScore = 3.0,
                    creativityScore = 3.0,
                    teamPlayContributionScore = 3.0
                )
                termMap["${id}_3"] = t3Ass

                // رصد الرياضات الجماعية للفصول الثلاثة
                val team = if (index % 2 == 0) "الفريق أ" else "الفريق ب"
                TermType.values().forEach { term ->
                    colMap["${id}_${term.termNumber}"] = CollectiveMatchAction(
                        studentId = id,
                        termType = term,
                        team = team,
                        positivePasses = if (isExempt) 0 else (3 + (index * 2) % 5),
                        positiveShots = if (isExempt) 0 else (1 + (index % 4)),
                        positiveInterceptions = if (isExempt) 0 else (1 + (index % 3)),
                        positiveSupport = if (isExempt) 0 else (2 + (index % 3)),
                        negativePasses = if (isExempt) 0 else (1 + (index % 3)),
                        negativeShots = if (isExempt) 0 else (1 + (index % 2)),
                        negativeTechnicalFouls = if (isExempt) 0 else (index % 2),
                        negativePersonalFouls = if (isExempt) 0 else (if (index % 4 == 0) 1 else 0)
                    )
                }

                // الحقول التوافقية للفصل الأول
                diagCompat[id] = DiagnosticEvaluation(
                    studentId = id,
                    sprint60mSeconds = if (isExempt) null else (9.2 + (index % 5) * 0.35),
                    enduranceMeters = if (isExempt) null else (1000 + (index * 40) % 500),
                    longJumpMeters = if (isExempt) null else (2.8 + (index % 4) * 0.3),
                    shotPutMeters = if (isExempt) null else (6.0 + (index % 5) * 0.5),
                    gymnasticsScore = if (isExempt) null else (12.0 + (index % 7)),
                    collectiveSportScore = if (isExempt) null else t1Col,
                    teacherObservation = if (isExempt) "معفى طبياً" else "استعداد بدني جيد",
                    diagnosticScore14 = t1Diag14,
                    progressionScore6 = t1Prog6
                )

                hbCompat[id] = HandballAction(
                    studentId = id,
                    team = team,
                    positivePasses = if (isExempt) 0 else (3 + (index * 2) % 5),
                    positiveShots = if (isExempt) 0 else (1 + (index % 4)),
                    positiveInterceptions = if (isExempt) 0 else (1 + (index % 3)),
                    positiveSupport = if (isExempt) 0 else (2 + (index % 3)),
                    negativePasses = if (isExempt) 0 else (1 + (index % 3)),
                    negativeShots = if (isExempt) 0 else (1 + (index % 2))
                )

                critCompat[id] = CriterionEvaluation(
                    studentId = id,
                    c1MotorScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    c2EfficiencyScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    c3KnowledgeScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    c4BehaviorScore = (t1Col * 0.25).coerceIn(1.0, 5.0),
                    customCollectiveScore = t1Col
                )

                contCompat[id] = ContinuousAssessment(
                    studentId = id,
                    manualTestScore = t1Test,
                    absenceScore = t1Ass.absenceScore,
                    sportswearScore = t1Ass.sportswearScore,
                    behaviorScore = t1Ass.behaviorScore,
                    individualTaskScore = t1Ass.individualTaskScore,
                    teamworkScore = t1Ass.teamworkScore,
                    creativityScore = t1Ass.creativityScore,
                    teamPlayContributionScore = t1Ass.teamPlayContributionScore,
                    individualSportScore = t1Ass.individualSportTotal20,
                    collectiveSportScore = t1Col
                )

                attList.add(
                    AttendanceRecord(
                        id = "att_${id}_1",
                        studentId = id,
                        date = "2024-10-02",
                        status = if (isExempt) AttendanceStatus.MEDICALLY_EXEMPT else AttendanceStatus.PRESENT,
                        hasSportswear = true,
                        goodBehavior = true,
                        individualTaskCompleted = true,
                        teamworkContribution = true,
                        creativityDemonstrated = (index == 0)
                    )
                )
                attList.add(
                    AttendanceRecord(
                        id = "att_${id}_2",
                        studentId = id,
                        date = "2024-10-09",
                        status = when {
                            isExempt -> AttendanceStatus.MEDICALLY_EXEMPT
                            index == 4 -> AttendanceStatus.ABSENT_UNEXCUSED
                            else -> AttendanceStatus.PRESENT
                        },
                        hasSportswear = (index != 2),
                        goodBehavior = (index != 3),
                        individualTaskCompleted = (index != 1),
                        teamworkContribution = true,
                        creativityDemonstrated = (index == 0)
                    )
                )
            }
        }

        populateClassStudents("c1", namesC1)
        populateClassStudents("c2", namesC2)
        populateClassStudents("c3", namesC3)
        populateClassStudents("c4", namesC4)

        _students.value = studentList
        _termAssessments.value = termMap
        _collectiveActions.value = colMap
        _attendances.value = attList

        _diagnostics.value = diagCompat
        _handballActions.value = hbCompat
        _criteria.value = critCompat
        _continuousAssessments.value = contCompat

        _teacherProfile.value = TeacherProfile(
            fullName = "أستاذ(ة) التربية البدنية والرياضية",
            schoolName = "ثانوية الشهيد العربي بن مهيدي",
            grade = "أستاذ التعليم الثانوي",
            academicYear = "2026/2027",
            district = "المقاطعة التفتيشية الأولى - ولاية الجزائر",
            phone = "0660 00 00 00",
            email = "prof.eps@education.dz",
            weeklyHours = "18 ساعة أسبوعياً",
            registrationNumber = "EPS-2026-ALG"
        )

        _logbookEntries.value = listOf(
            DailyLogbookEntry(
                id = "log_1",
                date = "2026-10-11",
                dayOfWeek = "الأحد",
                timeSlot = "08:00 - 10:00",
                classId = "c1",
                className = "1 ج م ع ت 1",
                cycle = "الطور الثانوي",
                activityType = "كرة اليد",
                domain = "الميدان الجماعي",
                sessionNumber = "الحصة 1 (تقويم تشخيصي)",
                objective = "التقويم التشخيصي للمهارات الحركية الأساسية في كرة اليد (التمرير، الاستقبال، والتنطيط)",
                warmUpPhase = "تسخين عام: جري إيقاعي متدرج + حركات تمطيط ومفصلية (15 دقيقة)",
                mainPhase = "مواقف لعب مصغرة 4 ضد 4 لتقييم التمرير والاستقبال السليم وسرعة رد الفعل في الهجوم والدفاع (65 دقيقة)",
                coolDownPhase = "تمارين استرخاء واسترجاع نبض القلب + تقييم وملاحظات الأستاذ مع التلاميذ (10 دقائق)",
                absentCount = 0,
                absentNames = "الجميع حاضر",
                equipment = "10 كرات يد، 12 صدريات بلونين، 15 قمعاً، صافرة وميقاتي",
                observations = "مستوى التنسيق الحركي واعد مع الحاجة لتكثيف التدريب على دقة التمرير أثناء التنقل"
            ),
            DailyLogbookEntry(
                id = "log_2",
                date = "2026-10-11",
                dayOfWeek = "الأحد",
                timeSlot = "10:00 - 12:00",
                classId = "c2",
                className = "1 ج م آ 1",
                cycle = "الطور الثانوي",
                activityType = "السرعة والجري السريع 60م",
                domain = "الميدان البدني",
                sessionNumber = "الحصة 1 (تقويم تشخيصي)",
                objective = "قياس السرعة الانتقالية في مسافة 60 متر وتسجيل زمن الانطلاق ورد الفعل",
                warmUpPhase = "تسخين بدني عام وخاص بالأطراف السفلية + تمارين ABC للركض ورفع الركبتين (20 دقيقة)",
                mainPhase = "تكرارات انطلاق من وضعيات مختلفة + قياس رسمي لزمن سباق 60م بالميقاتي (60 دقيقة)",
                coolDownPhase = "مشي خفيف + إطالة العضلات الخلفية والربلية وجمع الأدوات (10 دقائق)",
                absentCount = 1,
                absentNames = "تلميذ واحد بعذر طبي",
                equipment = "شريط قياس، ميقاتي إلكتروني، أقماع تحديد خطوط الانطلاق والوصول",
                observations = "أداء بدني منتظم وتسجيل كافة أزمنة التشخيص بنجاح في التطبيق"
            ),
            DailyLogbookEntry(
                id = "log_3",
                date = "2026-10-12",
                dayOfWeek = "الإثنين",
                timeSlot = "08:00 - 10:00",
                classId = "c3",
                className = "2 ع ت 1",
                cycle = "الطور الثانوي",
                activityType = "كرة اليد",
                domain = "الميدان الجماعي",
                sessionNumber = "الحصة 2 (تعلمية)",
                objective = "بناء هجمة جماعية بالتمرير والمراوغة والتسديد من وضعية الارتقاء والارتكاز",
                warmUpPhase = "تسخين بالكرة في أزواج: تمرير صدري ومرتد + وثب حركي (15 دقيقة)",
                mainPhase = "وضعيات هجومية 3 ضد 2 نحو المرمى مع اشتراط ثلاث تمريرات قبل التسديد المحكم (65 دقيقة)",
                coolDownPhase = "تمارين مرونة وتهدئة عضلية ومناقشة الأخطاء الميدانية (10 دقائق)",
                absentCount = 0,
                absentNames = "لا يوجد غياب",
                equipment = "كرات، شواخص، أقماع، مرمى كرة اليد",
                observations = "تحسن ملحوظ في سرعة اتخاذ قرار التسديد والتعاون بين الزملاء"
            )
        )

        saveDataToFile()
    }

    private fun saveDataToFile() {
        try {
            val json = serializeToJson()
            storageFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun serializeToJson(): String {
        val root = JSONObject()
        root.put("exportDate", "2024-10-11")
        root.put("appVersion", "4.0")

        // classes
        val classesArray = JSONArray()
        _classes.value.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("level", c.level)
            obj.put("academicYear", c.academicYear)
            obj.put("cycle", c.cycle)
            obj.put("term1IndividualSport", c.term1IndividualSport)
            obj.put("term1CollectiveSport", c.term1CollectiveSport)
            obj.put("term2IndividualSport", c.term2IndividualSport)
            obj.put("term2CollectiveSport", c.term2CollectiveSport)
            obj.put("term3IndividualSport", c.term3IndividualSport)
            obj.put("term3CollectiveSport", c.term3CollectiveSport)
            classesArray.put(obj)
        }
        root.put("classes", classesArray)

        // students
        val studentsArray = JSONArray()
        _students.value.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("classId", s.classId)
            obj.put("regNumber", s.regNumber)
            obj.put("fullName", s.fullName)
            obj.put("gender", s.gender.name)
            obj.put("isMedicallyExempt", s.isMedicallyExempt)
            obj.put("notes", s.notes)
            studentsArray.put(obj)
        }
        root.put("students", studentsArray)

        // term assessments
        val termsArray = JSONArray()
        _termAssessments.value.forEach { (key, ta) ->
            val obj = JSONObject()
            obj.put("key", key)
            obj.put("studentId", ta.studentId)
            obj.put("termNumber", ta.termType.termNumber)
            obj.put("diag14", ta.individualDiagnosticScore14)
            obj.put("prog6", ta.individualProgressionScore6)
            ta.diagnosticMeasurement?.let { obj.put("diagMeasurement", it) }
            ta.summativeMeasurement?.let { obj.put("summMeasurement", it) }
            ta.individualMeasurement?.let { obj.put("measurement", it) }
            obj.put("collectiveScore20", ta.collectiveSportScore20)
            obj.put("c1Score", ta.c1Score)
            obj.put("c2Score", ta.c2Score)
            obj.put("c3Score", ta.c3Score)
            obj.put("c4Score", ta.c4Score)
            obj.put("c5Score", ta.c5Score)
            obj.put("c1", ta.c1Score)
            obj.put("c2", ta.c2Score)
            obj.put("c3", ta.c3Score)
            obj.put("c4", ta.c4Score)
            obj.put("manualTestScore", ta.manualTestScore)
            obj.put("absenceScore", ta.absenceScore)
            obj.put("sportswearScore", ta.sportswearScore)
            obj.put("behaviorScore", ta.behaviorScore)
            obj.put("individualTaskScore", ta.individualTaskScore)
            obj.put("teamworkScore", ta.teamworkScore)
            obj.put("creativityScore", ta.creativityScore)
            obj.put("teamPlayContributionScore", ta.teamPlayContributionScore)
            termsArray.put(obj)
        }
        root.put("termAssessments", termsArray)

        // collective actions
        val colArray = JSONArray()
        _collectiveActions.value.forEach { (key, ca) ->
            val obj = JSONObject()
            obj.put("key", key)
            obj.put("studentId", ca.studentId)
            obj.put("termNumber", ca.termType.termNumber)
            obj.put("team", ca.team)
            obj.put("positiveSkill1", ca.positiveSkill1)
            obj.put("positiveSkill2", ca.positiveSkill2)
            obj.put("positiveSkill3", ca.positiveSkill3)
            obj.put("positiveSkill4", ca.positiveSkill4)
            obj.put("positiveSkill5", ca.positiveSkill5)
            obj.put("negativeSkill1", ca.negativeSkill1)
            obj.put("negativeSkill2", ca.negativeSkill2)
            obj.put("negativeSkill3", ca.negativeSkill3)
            obj.put("negativeSkill4", ca.negativeSkill4)
            obj.put("negativeSkill5", ca.negativeSkill5)
            // compatibility
            obj.put("positivePasses", ca.positiveSkill1)
            obj.put("positiveShots", ca.positiveSkill5)
            obj.put("positiveInterceptions", ca.positiveSkill4)
            obj.put("positiveSupport", ca.positiveSkill3)
            obj.put("negativePasses", ca.negativeSkill1)
            obj.put("negativeShots", ca.negativeSkill5)
            obj.put("negativeTechnicalFouls", ca.negativeSkill4)
            obj.put("negativePersonalFouls", ca.negativeSkill2)
            colArray.put(obj)
        }
        root.put("collectiveActions", colArray)

        // attendances
        val attArray = JSONArray()
        _attendances.value.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("studentId", a.studentId)
            obj.put("date", a.date)
            obj.put("status", a.status.name)
            obj.put("hasSportswear", a.hasSportswear)
            obj.put("goodBehavior", a.goodBehavior)
            obj.put("individualTaskCompleted", a.individualTaskCompleted)
            obj.put("teamworkContribution", a.teamworkContribution)
            obj.put("creativityDemonstrated", a.creativityDemonstrated)
            obj.put("remarks", a.remarks)
            attArray.put(obj)
        }
        root.put("attendances", attArray)

        // teacher profile
        val prof = _teacherProfile.value
        val profObj = JSONObject().apply {
            put("fullName", prof.fullName)
            put("schoolName", prof.schoolName)
            put("grade", prof.grade)
            put("academicYear", prof.academicYear)
            put("district", prof.district)
            put("phone", prof.phone)
            put("email", prof.email)
            put("weeklyHours", prof.weeklyHours)
            put("registrationNumber", prof.registrationNumber)
        }
        root.put("teacherProfile", profObj)

        // logbook entries
        val logArray = JSONArray()
        _logbookEntries.value.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("date", entry.date)
                put("dayOfWeek", entry.dayOfWeek)
                put("timeSlot", entry.timeSlot)
                put("classId", entry.classId)
                put("className", entry.className)
                put("cycle", entry.cycle)
                put("activityType", entry.activityType)
                put("domain", entry.domain)
                put("sessionNumber", entry.sessionNumber)
                put("objective", entry.objective)
                put("warmUpPhase", entry.warmUpPhase)
                put("mainPhase", entry.mainPhase)
                put("coolDownPhase", entry.coolDownPhase)
                put("absentCount", entry.absentCount)
                put("absentNames", entry.absentNames)
                put("equipment", entry.equipment)
                put("observations", entry.observations)
            }
            logArray.put(obj)
        }
        root.put("logbookEntries", logArray)

        return root.toString(2)
    }

    fun parseJson(jsonString: String) {
        val root = JSONObject(jsonString)

        val newClasses = mutableListOf<ClassRoom>()
        if (root.has("classes")) {
            val arr = root.getJSONArray("classes")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val rawName = o.optString("name", "1 ج م ع ت 1")
                val rawLevel = o.optString("level", "السنة الأولى ثانوي (جذع مشترك علوم وتكنولوجيا)")
                val rawCycle = o.optString("cycle", if (rawLevel.contains("متوسط")) "الطور المتوسط" else "الطور الثانوي")
                val year = o.optString("academicYear", "2026/2027")
                newClasses.add(
                    ClassRoom(
                        id = o.optString("id", "c_$i"),
                        name = rawName,
                        level = rawLevel,
                        academicYear = year,
                        cycle = rawCycle,
                        term1IndividualSport = o.optString("term1IndividualSport", "الجري السريع 60م"),
                        term1CollectiveSport = o.optString("term1CollectiveSport", "كرة اليد"),
                        term2IndividualSport = o.optString("term2IndividualSport", "الوثب الطويل"),
                        term2CollectiveSport = o.optString("term2CollectiveSport", "كرة السلة"),
                        term3IndividualSport = o.optString("term3IndividualSport", "دفع الجلة"),
                        term3CollectiveSport = o.optString("term3CollectiveSport", "كرة الطائرة")
                    )
                )
            }
        }

        val newStudents = mutableListOf<Student>()
        if (root.has("students")) {
            val arr = root.getJSONArray("students")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val genderStr = o.optString("gender", "MALE")
                val gender = if (genderStr == "FEMALE") Gender.FEMALE else Gender.MALE
                newStudents.add(
                    Student(
                        id = o.optString("id", "s_$i"),
                        classId = o.optString("classId", "c1"),
                        regNumber = o.optInt("regNumber", i + 1),
                        fullName = o.optString("fullName", "تلميذ"),
                        gender = gender,
                        isMedicallyExempt = o.optBoolean("isMedicallyExempt", false),
                        notes = o.optString("notes", "")
                    )
                )
            }
        }

        val newTerms = mutableMapOf<String, TermAssessment>()
        if (root.has("termAssessments")) {
            val arr = root.getJSONArray("termAssessments")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val sid = o.optString("studentId")
                val tNum = o.optInt("termNumber", 1)
                val termType = when (tNum) {
                    2 -> TermType.TERM_2
                    3 -> TermType.TERM_3
                    else -> TermType.TERM_1
                }
                val key = o.optString("key", "${sid}_$tNum")
                newTerms[key] = TermAssessment(
                    studentId = sid,
                    termType = termType,
                    individualDiagnosticScore14 = o.optDouble("diag14", 0.0),
                    individualProgressionScore6 = o.optDouble("prog6", 0.0),
                    diagnosticMeasurement = if (o.has("diagMeasurement")) o.optDouble("diagMeasurement") else (if (o.has("measurement")) o.optDouble("measurement") else null),
                    summativeMeasurement = if (o.has("summMeasurement")) o.optDouble("summMeasurement") else null,
                    individualMeasurement = if (o.has("measurement")) o.optDouble("measurement") else null,
                    c1Score = o.optDouble("c1Score", o.optDouble("c1", 0.0)),
                    c2Score = o.optDouble("c2Score", o.optDouble("c2", 0.0)),
                    c3Score = o.optDouble("c3Score", o.optDouble("c3", 0.0)),
                    c4Score = o.optDouble("c4Score", o.optDouble("c4", 0.0)),
                    c5Score = o.optDouble("c5Score", 0.0),
                    collectiveSportScore20 = o.optDouble("collectiveScore20", 0.0),
                    manualTestScore = o.optDouble("manualTestScore", 0.0),
                    absenceScore = o.optDouble("absenceScore", 0.0),
                    sportswearScore = o.optDouble("sportswearScore", 0.0),
                    behaviorScore = o.optDouble("behaviorScore", 0.0),
                    individualTaskScore = o.optDouble("individualTaskScore", 0.0),
                    teamworkScore = o.optDouble("teamworkScore", o.optDouble("teamPlayContributionScore", 0.0)),
                    creativityScore = o.optDouble("creativityScore", 0.0),
                    teamPlayContributionScore = o.optDouble("teamPlayContributionScore", 0.0)
                )
            }
        }

        val newCols = mutableMapOf<String, CollectiveMatchAction>()
        if (root.has("collectiveActions")) {
            val arr = root.getJSONArray("collectiveActions")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val sid = o.optString("studentId")
                val tNum = o.optInt("termNumber", 1)
                val termType = when (tNum) {
                    2 -> TermType.TERM_2
                    3 -> TermType.TERM_3
                    else -> TermType.TERM_1
                }
                val key = o.optString("key", "${sid}_$tNum")
                newCols[key] = CollectiveMatchAction(
                    studentId = sid,
                    termType = termType,
                    team = o.optString("team", "الفريق أ"),
                    positiveSkill1 = o.optInt("positiveSkill1", o.optInt("positivePasses", 0)),
                    positiveSkill2 = o.optInt("positiveSkill2", 0),
                    positiveSkill3 = o.optInt("positiveSkill3", o.optInt("positiveSupport", 0)),
                    positiveSkill4 = o.optInt("positiveSkill4", o.optInt("positiveInterceptions", 0)),
                    positiveSkill5 = o.optInt("positiveSkill5", o.optInt("positiveShots", 0)),
                    negativeSkill1 = o.optInt("negativeSkill1", o.optInt("negativePasses", 0)),
                    negativeSkill2 = o.optInt("negativeSkill2", o.optInt("negativePersonalFouls", 0)),
                    negativeSkill3 = o.optInt("negativeSkill3", 0),
                    negativeSkill4 = o.optInt("negativeSkill4", o.optInt("negativeTechnicalFouls", 0)),
                    negativeSkill5 = o.optInt("negativeSkill5", o.optInt("negativeShots", 0))
                )
            }
        }

        val newAtt = mutableListOf<AttendanceRecord>()
        if (root.has("attendances")) {
            val arr = root.getJSONArray("attendances")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val statusStr = o.optString("status", "PRESENT")
                val status = try {
                    AttendanceStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    AttendanceStatus.PRESENT
                }
                newAtt.add(
                    AttendanceRecord(
                        id = o.optString("id", "att_$i"),
                        studentId = o.optString("studentId"),
                        date = o.optString("date", "2024-10-11"),
                        status = status,
                        hasSportswear = o.optBoolean("hasSportswear", true),
                        goodBehavior = o.optBoolean("goodBehavior", true),
                        individualTaskCompleted = o.optBoolean("individualTaskCompleted", true),
                        teamworkContribution = o.optBoolean("teamworkContribution", true),
                        creativityDemonstrated = o.optBoolean("creativityDemonstrated", false),
                        remarks = o.optString("remarks", "")
                    )
                )
            }
        }

        if (root.has("teacherProfile")) {
            val p = root.getJSONObject("teacherProfile")
            _teacherProfile.value = TeacherProfile(
                fullName = p.optString("fullName", "أستاذ(ة) التربية البدنية والرياضية"),
                schoolName = p.optString("schoolName", "ثانوية الشهيد العربي بن مهيدي"),
                grade = p.optString("grade", "أستاذ التعليم الثانوي"),
                academicYear = p.optString("academicYear", "2026/2027"),
                district = p.optString("district", "المقاطعة التفتيشية الأولى - ولاية الجزائر"),
                phone = p.optString("phone", "0660 00 00 00"),
                email = p.optString("email", "prof.eps@education.dz"),
                weeklyHours = p.optString("weeklyHours", "18 ساعة أسبوعياً"),
                registrationNumber = p.optString("registrationNumber", "EPS-2026-ALG")
            )
        }

        if (root.has("logbookEntries")) {
            val arr = root.getJSONArray("logbookEntries")
            val list = mutableListOf<DailyLogbookEntry>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    DailyLogbookEntry(
                        id = o.optString("id", "log_$i"),
                        date = o.optString("date", "2026-10-11"),
                        dayOfWeek = o.optString("dayOfWeek", "الأحد"),
                        timeSlot = o.optString("timeSlot", "08:00 - 10:00"),
                        classId = o.optString("classId", ""),
                        className = o.optString("className", ""),
                        cycle = o.optString("cycle", "الطور الثانوي"),
                        activityType = o.optString("activityType", "كرة اليد"),
                        domain = o.optString("domain", "الميدان الجماعي"),
                        sessionNumber = o.optString("sessionNumber", "الحصة 1"),
                        objective = o.optString("objective", ""),
                        warmUpPhase = o.optString("warmUpPhase", ""),
                        mainPhase = o.optString("mainPhase", ""),
                        coolDownPhase = o.optString("coolDownPhase", ""),
                        absentCount = o.optInt("absentCount", 0),
                        absentNames = o.optString("absentNames", ""),
                        equipment = o.optString("equipment", ""),
                        observations = o.optString("observations", "")
                    )
                )
            }
            _logbookEntries.value = list
        }

        _classes.value = newClasses
        _students.value = newStudents
        _termAssessments.value = newTerms
        _collectiveActions.value = newCols
        _attendances.value = newAtt

        // مزامنة التوافقية للفصل الأول
        syncCompatibilityViews()
        saveDataToFile()
    }

    private fun syncCompatibilityViews() {
        val diag = mutableMapOf<String, DiagnosticEvaluation>()
        val hb = mutableMapOf<String, HandballAction>()
        val crit = mutableMapOf<String, CriterionEvaluation>()
        val cont = mutableMapOf<String, ContinuousAssessment>()

        _students.value.forEach { s ->
            val t1 = _termAssessments.value["${s.id}_1"] ?: TermAssessment(studentId = s.id, termType = TermType.TERM_1)
            val c1 = _collectiveActions.value["${s.id}_1"] ?: CollectiveMatchAction(studentId = s.id, termType = TermType.TERM_1)

            diag[s.id] = DiagnosticEvaluation(
                studentId = s.id,
                diagnosticScore14 = t1.individualDiagnosticScore14,
                progressionScore6 = t1.individualProgressionScore6,
                sprint60mSeconds = t1.individualMeasurement
            )
            hb[s.id] = HandballAction(
                studentId = s.id,
                team = c1.team,
                positivePasses = c1.positivePasses,
                positiveShots = c1.positiveShots,
                positiveInterceptions = c1.positiveInterceptions,
                positiveSupport = c1.positiveSupport,
                negativePasses = c1.negativePasses,
                negativeShots = c1.negativeShots
            )
            crit[s.id] = CriterionEvaluation(
                studentId = s.id,
                c1MotorScore = t1.c1MotorScore,
                c2EfficiencyScore = t1.c2EfficiencyScore,
                c3KnowledgeScore = t1.c3KnowledgeScore,
                c4BehaviorScore = t1.c4BehaviorScore,
                customCollectiveScore = t1.collectiveSportScore20
            )
            cont[s.id] = ContinuousAssessment(
                studentId = s.id,
                manualTestScore = t1.manualTestScore,
                absenceScore = t1.absenceScore,
                sportswearScore = t1.sportswearScore,
                behaviorScore = t1.behaviorScore,
                teamPlayContributionScore = t1.teamPlayContributionScore,
                individualSportScore = t1.individualSportTotal20,
                collectiveSportScore = t1.collectiveSportScore20
            )
        }

        _diagnostics.value = diag
        _handballActions.value = hb
        _criteria.value = crit
        _continuousAssessments.value = cont
    }

    // عمليات التحديث الفصلية
    fun updateTermAssessment(assessment: TermAssessment) {
        val map = _termAssessments.value.toMutableMap()
        val key = "${assessment.studentId}_${assessment.termType.termNumber}"
        map[key] = assessment
        _termAssessments.value = map

        if (assessment.termType == TermType.TERM_1) {
            syncCompatibilityViews()
        }
        saveDataToFile()
    }

    fun updateCollectiveAction(action: CollectiveMatchAction) {
        val map = _collectiveActions.value.toMutableMap()
        val key = "${action.studentId}_${action.termType.termNumber}"
        map[key] = action
        _collectiveActions.value = map

        if (action.termType == TermType.TERM_1) {
            syncCompatibilityViews()
        }
        saveDataToFile()
    }

    fun modifyCollectiveAction(studentId: String, term: TermType, isPositive: Boolean, actionType: String) {
        val key = "${studentId}_${term.termNumber}"
        val current = _collectiveActions.value[key] ?: CollectiveMatchAction(studentId = studentId, termType = term)
        val updated = when (actionType) {
            "skill1", "pass", "serve" -> if (isPositive) current.copy(positiveSkill1 = current.positiveSkill1 + 1)
                      else current.copy(negativeSkill1 = (current.negativeSkill1 + 1).coerceAtLeast(0))
            "skill2", "dribble", "reception" -> if (isPositive) current.copy(positiveSkill2 = current.positiveSkill2 + 1)
                      else current.copy(negativeSkill2 = (current.negativeSkill2 + 1).coerceAtLeast(0))
            "skill3", "support", "teamwork", "setting" -> if (isPositive) current.copy(positiveSkill3 = current.positiveSkill3 + 1)
                      else current.copy(negativeSkill3 = (current.negativeSkill3 + 1).coerceAtLeast(0))
            "skill4", "interception", "transition", "defense", "attack_build" -> if (isPositive) current.copy(positiveSkill4 = current.positiveSkill4 + 1)
                      else current.copy(negativeSkill4 = (current.negativeSkill4 + 1).coerceAtLeast(0))
            "skill5", "shot", "scoring", "smash" -> if (isPositive) current.copy(positiveSkill5 = current.positiveSkill5 + 1)
                      else current.copy(negativeSkill5 = (current.negativeSkill5 + 1).coerceAtLeast(0))
            else -> if (isPositive) current.copy(positiveSkill1 = current.positiveSkill1 + 1)
                    else current.copy(negativeSkill1 = current.negativeSkill1 + 1)
        }
        updateCollectiveAction(updated)
    }

    fun modifyHandballActionField(studentId: String, deltaPositive: Boolean, actionType: String) {
        modifyCollectiveAction(studentId, TermType.TERM_1, deltaPositive, actionType)
    }

    fun resetStudentResults(studentId: String, termType: TermType? = null) {
        val tMap = _termAssessments.value.toMutableMap()
        val cMap = _collectiveActions.value.toMutableMap()
        val termsToReset = if (termType != null) listOf(termType) else TermType.values().toList()

        termsToReset.forEach { term ->
            val key = "${studentId}_${term.termNumber}"
            val existing = tMap[key] ?: TermAssessment(studentId = studentId, termType = term)
            tMap[key] = existing.createZeroed()
            val existingCol = cMap[key] ?: CollectiveMatchAction(studentId = studentId, termType = term)
            cMap[key] = existingCol.createZeroed()
        }

        _termAssessments.value = tMap
        _collectiveActions.value = cMap
        syncCompatibilityViews()
        saveDataToFile()
    }

    fun resetCollectiveForClass(classId: String, term: TermType) {
        val studentsInClass = _students.value.filter { it.classId == classId }.map { it.id }.toSet()
        val map = _collectiveActions.value.toMutableMap()
        studentsInClass.forEach { sid ->
            val key = "${sid}_${term.termNumber}"
            val existing = map[key] ?: CollectiveMatchAction(studentId = sid, termType = term)
            map[key] = existing.createZeroed()
        }
        _collectiveActions.value = map
        syncCompatibilityViews()
        saveDataToFile()
    }

    fun addOrUpdateStudent(student: Student) {
        val list = _students.value.toMutableList()
        val index = list.indexOfFirst { it.id == student.id }
        if (index >= 0) {
            list[index] = student
        } else {
            list.add(student)
            // عند اضافة تلميذ جديد، لا تملأ النقاط من عندك (تصفير كامل)
            val tMap = _termAssessments.value.toMutableMap()
            val cMap = _collectiveActions.value.toMutableMap()
            TermType.values().forEach { term ->
                val key = "${student.id}_${term.termNumber}"
                if (!tMap.containsKey(key)) {
                    tMap[key] = TermAssessment(studentId = student.id, termType = term).createZeroed()
                }
                if (!cMap.containsKey(key)) {
                    cMap[key] = CollectiveMatchAction(studentId = student.id, termType = term).createZeroed()
                }
            }
            _termAssessments.value = tMap
            _collectiveActions.value = cMap
        }
        _students.value = list
        syncCompatibilityViews()
        saveDataToFile()
    }

    fun deleteStudent(studentId: String) {
        _students.value = _students.value.filter { it.id != studentId }
        _termAssessments.value = _termAssessments.value.filterKeys { !it.startsWith("${studentId}_") }
        _collectiveActions.value = _collectiveActions.value.filterKeys { !it.startsWith("${studentId}_") }
        _attendances.value = _attendances.value.filter { it.studentId != studentId }
        syncCompatibilityViews()
        saveDataToFile()
    }

    fun updateDiagnostic(diag: DiagnosticEvaluation) {
        val key = "${diag.studentId}_1"
        val existing = _termAssessments.value[key] ?: TermAssessment(studentId = diag.studentId, termType = TermType.TERM_1)
        updateTermAssessment(existing.copy(
            individualDiagnosticScore14 = diag.diagnosticScore14,
            individualProgressionScore6 = diag.progressionScore6,
            individualMeasurement = diag.sprint60mSeconds
        ))
    }

    fun updateCriteria(criterion: CriterionEvaluation) {
        val key = "${criterion.studentId}_1"
        val existing = _termAssessments.value[key] ?: TermAssessment(studentId = criterion.studentId, termType = TermType.TERM_1)
        updateTermAssessment(existing.copy(
            c1Score = criterion.c1MotorScore,
            c2Score = criterion.c2EfficiencyScore,
            c3Score = criterion.c3KnowledgeScore,
            c4Score = criterion.c4BehaviorScore,
            c5Score = 0.0,
            collectiveSportScore20 = criterion.customCollectiveScore ?: 0.0
        ))
    }

    fun updateContinuousAssessment(assessment: ContinuousAssessment) {
        val key = "${assessment.studentId}_1"
        val existing = _termAssessments.value[key] ?: TermAssessment(studentId = assessment.studentId, termType = TermType.TERM_1)
        updateTermAssessment(existing.copy(
            manualTestScore = assessment.manualTestScore,
            absenceScore = assessment.absenceScore,
            sportswearScore = assessment.sportswearScore,
            behaviorScore = assessment.behaviorScore,
            teamPlayContributionScore = assessment.teamPlayContributionScore,
            collectiveSportScore20 = assessment.collectiveSportScore ?: 0.0
        ))
    }

    fun updateClassAcademicYear(classId: String, newAcademicYear: String) {
        val list = _classes.value.map {
            if (it.id == classId) it.copy(academicYear = newAcademicYear) else it
        }
        _classes.value = list
        saveDataToFile()
    }

    fun updateAllClassesAcademicYear(newAcademicYear: String) {
        val list = _classes.value.map {
            it.copy(academicYear = newAcademicYear)
        }
        _classes.value = list
        saveDataToFile()
    }

    fun updateClassSports(
        classId: String,
        term: TermType,
        individualSport: String,
        collectiveSport: String,
        applyToAllClasses: Boolean = false,
        applyToSameCycle: Boolean = false
    ) {
        val targetClass = _classes.value.find { it.id == classId }
        val targetCycle = targetClass?.cycle

        val updated = _classes.value.map { c ->
            val shouldUpdate = when {
                applyToAllClasses -> true
                applyToSameCycle && targetCycle != null -> c.cycle == targetCycle
                else -> c.id == classId
            }
            if (shouldUpdate) {
                when (term) {
                    TermType.TERM_1 -> c.copy(
                        term1IndividualSport = individualSport.trim(),
                        term1CollectiveSport = collectiveSport.trim()
                    )
                    TermType.TERM_2 -> c.copy(
                        term2IndividualSport = individualSport.trim(),
                        term2CollectiveSport = collectiveSport.trim()
                    )
                    TermType.TERM_3 -> c.copy(
                        term3IndividualSport = individualSport.trim(),
                        term3CollectiveSport = collectiveSport.trim()
                    )
                }
            } else {
                c
            }
        }
        _classes.value = updated
        saveDataToFile()
    }

    fun addClass(
        name: String,
        level: String,
        academicYear: String = "2026/2027",
        cycle: String = "الطور الثانوي",
        term1Ind: String = "الجري السريع 60م",
        term1Col: String = "كرة اليد",
        term2Ind: String = "الوثب الطويل",
        term2Col: String = "كرة السلة",
        term3Ind: String = "دفع الجلة",
        term3Col: String = "كرة الطائرة"
    ) {
        val newClass = ClassRoom(
            id = "c_${System.currentTimeMillis()}",
            name = name,
            level = level,
            academicYear = academicYear,
            cycle = cycle,
            term1IndividualSport = term1Ind,
            term1CollectiveSport = term1Col,
            term2IndividualSport = term2Ind,
            term2CollectiveSport = term2Col,
            term3IndividualSport = term3Ind,
            term3CollectiveSport = term3Col
        )
        _classes.value = _classes.value + newClass
        saveDataToFile()
    }

    fun deleteClass(classId: String) {
        if (_classes.value.size <= 1) return
        val remaining = _classes.value.filter { it.id != classId }
        _classes.value = remaining
        val studentsInClass = _students.value.filter { it.classId == classId }.map { it.id }.toSet()
        _students.value = _students.value.filter { it.classId != classId }
        _termAssessments.value = _termAssessments.value.filterKeys { k -> !studentsInClass.any { sid -> k.startsWith("${sid}_") } }
        _collectiveActions.value = _collectiveActions.value.filterKeys { k -> !studentsInClass.any { sid -> k.startsWith("${sid}_") } }
        _attendances.value = _attendances.value.filter { it.studentId !in studentsInClass }
        syncCompatibilityViews()
        saveDataToFile()
    }

    fun recordAttendance(record: AttendanceRecord) {
        val list = _attendances.value.toMutableList()
        val index = list.indexOfFirst { it.studentId == record.studentId && it.date == record.date }
        if (index >= 0) {
            list[index] = record
        } else {
            list.add(record)
        }
        _attendances.value = list
        saveDataToFile()
    }

    /**
     * استيراد دفعة من التلاميذ والنتائج (من صورة ورقة النتائج أو ملف نصي) إلى القسم
     */
    fun importStudentsAndResults(
        classId: String,
        term: TermType,
        importedList: List<ImportedStudentResult>,
        importStudentsNames: Boolean = true,
        updateContinuousScore: Boolean = true,
        updateTestScore: Boolean = true,
        updateExamScore: Boolean = true
    ): Int {
        if (importedList.isEmpty()) return 0
        val currentStudentsList = _students.value.toMutableList()
        val classStudents = currentStudentsList.filter { it.classId == classId }

        val termMap = _termAssessments.value.toMutableMap()
        val collectiveMap = _collectiveActions.value.toMutableMap()
        var updatedCount = 0

        for ((index, item) in importedList.withIndex()) {
            if (item.fullName.isBlank()) continue

            // البحث عن التلميذ في القسم الحالي بالاسم أو برقم الترتيب
            val normalizedItemName = item.fullName.trim().replace(Regex("\\s+"), " ")
            val matchedStudent = classStudents.find { cs ->
                val normalizedCurrent = cs.fullName.trim().replace(Regex("\\s+"), " ")
                normalizedCurrent.equals(normalizedItemName, ignoreCase = true) ||
                        (cs.regNumber == item.regNumber && item.regNumber > 0)
            }

            val student: Student
            if (matchedStudent != null) {
                // تلميذ موجود مسبقاً، تحديث بياناته إذا لزم الأمر
                student = matchedStudent.copy(
                    isMedicallyExempt = if (item.isMedicallyExempt) true else matchedStudent.isMedicallyExempt,
                    gender = if (matchedStudent.gender != item.gender && item.gender == Gender.FEMALE) item.gender else matchedStudent.gender,
                    notes = if (item.notes.isNotBlank()) item.notes else matchedStudent.notes
                )
                val sIndex = currentStudentsList.indexOfFirst { it.id == matchedStudent.id }
                if (sIndex >= 0) {
                    currentStudentsList[sIndex] = student
                }
            } else if (importStudentsNames) {
                // تلميذ جديد يتم إضافته إلى الفوج
                val newId = "s_${System.currentTimeMillis()}_${index}"
                val maxReg = (classStudents.map { it.regNumber } + importedList.map { it.regNumber }).maxOrNull() ?: 0
                val regNum = if (item.regNumber > 0) item.regNumber else (maxReg + 1)
                student = Student(
                    id = newId,
                    classId = classId,
                    regNumber = regNum,
                    fullName = item.fullName.trim(),
                    gender = item.gender,
                    isMedicallyExempt = item.isMedicallyExempt,
                    notes = item.notes
                )
                currentStudentsList.add(student)
            } else {
                continue
            }

            // التأكد من وجود سجلات التقييم الأساسية للفصول الثلاثة
            TermType.values().forEach { t ->
                val k = "${student.id}_${t.termNumber}"
                if (!termMap.containsKey(k)) {
                    termMap[k] = TermAssessment(studentId = student.id, termType = t)
                }
                if (!collectiveMap.containsKey(k)) {
                    collectiveMap[k] = CollectiveMatchAction(studentId = student.id, termType = t)
                }
            }

            // تطبيق النقاط والنتائج على الفصل المختار
            val key = "${student.id}_${term.termNumber}"
            var assessment = termMap[key] ?: TermAssessment(studentId = student.id, termType = term)

            if (updateContinuousScore && item.continuousScore != null) {
                val score = item.continuousScore.coerceIn(0.0, 20.0)
                // توزيع العلامة بالتناسب على معايير التقويم المستمر الستة (مجموعها 20)
                val s4 = (score * 0.20).coerceIn(0.0, 4.0)
                val s3 = (score * 0.15).coerceIn(0.0, 3.0)
                assessment = assessment.copy(
                    absenceScore = String.format(java.util.Locale.US, "%.1f", s4).toDouble(),
                    behaviorScore = String.format(java.util.Locale.US, "%.1f", s4).toDouble(),
                    sportswearScore = String.format(java.util.Locale.US, "%.1f", s3).toDouble(),
                    individualTaskScore = String.format(java.util.Locale.US, "%.1f", s3).toDouble(),
                    teamworkScore = String.format(java.util.Locale.US, "%.1f", s3).toDouble(),
                    creativityScore = String.format(java.util.Locale.US, "%.1f", s3).toDouble()
                )
            }

            if (updateTestScore && item.testScore != null) {
                assessment = assessment.copy(manualTestScore = item.testScore.coerceIn(0.0, 20.0))
            }

            if (updateExamScore && item.examScore != null) {
                val exam = item.examScore.coerceIn(0.0, 20.0)
                val diag14 = (exam * 14.0 / 20.0).coerceIn(0.0, 14.0)
                val prog6 = (exam * 6.0 / 20.0).coerceIn(0.0, 6.0)
                assessment = assessment.copy(
                    individualDiagnosticScore14 = String.format(java.util.Locale.US, "%.1f", diag14).toDouble(),
                    individualProgressionScore6 = String.format(java.util.Locale.US, "%.1f", prog6).toDouble(),
                    collectiveSportScore20 = exam
                )
            } else if (item.individualScore != null) {
                val ind = item.individualScore.coerceIn(0.0, 20.0)
                val diag14 = (ind * 14.0 / 20.0).coerceIn(0.0, 14.0)
                val prog6 = (ind * 6.0 / 20.0).coerceIn(0.0, 6.0)
                assessment = assessment.copy(
                    individualDiagnosticScore14 = String.format(java.util.Locale.US, "%.1f", diag14).toDouble(),
                    individualProgressionScore6 = String.format(java.util.Locale.US, "%.1f", prog6).toDouble()
                )
            }

            termMap[key] = assessment
            updatedCount++
        }

        _students.value = currentStudentsList
        _termAssessments.value = termMap
        _collectiveActions.value = collectiveMap
        syncCompatibilityViews()
        saveDataToFile()
        return updatedCount
    }

    /**
     * حساب علامات التقويم المستمر لطالب معين من سجل المتابعة اليومية
     */
    fun calculateContinuousScoresForStudent(studentId: String): ContinuousAssessmentScale.CalculatedScores {
        val student = _students.value.firstOrNull { it.id == studentId } ?: Student(id = studentId, classId = "", regNumber = 0, fullName = "", gender = Gender.MALE)
        val records = _attendances.value.filter { it.studentId == studentId }
        return ContinuousAssessmentScale.calculateScoresFromRecords(student, records)
    }

    /**
     * احتساب وتحديث نقاط التقويم المستمر لجميع تلاميذ الفصل انطلاقاً من معايير المتابعة اليومية
     */
    fun recalculateContinuousAssessmentsFromDaily(termType: TermType = TermType.TERM_1) {
        val currentTerms = _termAssessments.value.toMutableMap()
        val allAtts = _attendances.value
        val studentsList = _students.value

        studentsList.forEach { student ->
            val studentAtts = allAtts.filter { it.studentId == student.id }
            val scores = ContinuousAssessmentScale.calculateScoresFromRecords(student, studentAtts)
            val key = "${student.id}_${termType.termNumber}"
            val existing = currentTerms[key] ?: TermAssessment(studentId = student.id, termType = termType)
            currentTerms[key] = existing.copy(
                absenceScore = scores.absenceScore,
                sportswearScore = scores.sportswearScore,
                behaviorScore = scores.behaviorScore,
                individualTaskScore = scores.individualTaskScore,
                teamworkScore = scores.teamworkScore,
                creativityScore = scores.creativityScore,
                teamPlayContributionScore = scores.teamworkScore
            )
        }
        _termAssessments.value = currentTerms
        syncCompatibilityViews()
        saveDataToFile()
    }

    // ==========================================
    // إدارة معلومات الأستاذ والكراس اليومي التربوي
    // ==========================================

    fun updateTeacherProfile(profile: TeacherProfile) {
        _teacherProfile.value = profile
        saveDataToFile()
    }

    fun addOrUpdateLogbookEntry(entry: DailyLogbookEntry) {
        val list = _logbookEntries.value.toMutableList()
        val index = list.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            list[index] = entry
        } else {
            list.add(0, entry)
        }
        _logbookEntries.value = list
        saveDataToFile()
    }

    fun deleteLogbookEntry(id: String) {
        _logbookEntries.value = _logbookEntries.value.filter { it.id != id }
        saveDataToFile()
    }

    fun generatePresetLogbookForWeek() {
        val currentClasses = _classes.value
        val newEntries = mutableListOf<DailyLogbookEntry>()
        val days = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس")
        val timeSlots = listOf("08:00 - 10:00", "10:00 - 12:00", "14:00 - 16:00")

        var dayIdx = 0
        currentClasses.forEachIndexed { i, cls ->
            val day = days[dayIdx % days.size]
            val slot = timeSlots[i % timeSlots.size]
            val isCollective = (i % 2 == 0)
            val sport = if (isCollective) "كرة اليد" else "الجري السريع والسرعة 60م"
            val domain = if (isCollective) "الميدان الجماعي" else "الميدان البدني الطبيعي"

            val attsForClass = _attendances.value.filter { att ->
                _students.value.any { it.id == att.studentId && it.classId == cls.id } && att.status != AttendanceStatus.PRESENT
            }

            newEntries.add(
                DailyLogbookEntry(
                    id = "log_auto_${System.currentTimeMillis()}_$i",
                    date = "2026-10-${11 + dayIdx}",
                    dayOfWeek = day,
                    timeSlot = slot,
                    classId = cls.id,
                    className = cls.name,
                    cycle = cls.cycle,
                    activityType = sport,
                    domain = domain,
                    sessionNumber = "الحصة ${((i + 1) % 4) + 1} (تعلمية)",
                    objective = if (isCollective) "التحكم في التمرير والاستقبال السليم وبناء هجمة سريعة نحو مرمى الخصم"
                               else "تطوير السرعة القصوى الانتقالية وحسن اتخاذ وضعية الانطلاق الصحيحة",
                    warmUpPhase = "جري خفيف متدرج + تسخين المفاصل والعضلات الرئيسية + تمارين تمطيط وتنشيط (15 د)",
                    mainPhase = if (isCollective) "مواقف 3 ضد 2: التمرير والمراوغة وإنهاء الهجمة بالتسديد المركز (65 د)"
                               else "سباقات تكرارية 40م و60م مع قياس زمن رد الفعل والوصول (60 د)",
                    coolDownPhase = "تمارين إطالة واسترخاء خفيف + حوصلة لمجريات الحصة وتسجيل الغيابات وجمع العتاد (10 د)",
                    absentCount = attsForClass.size,
                    absentNames = if (attsForClass.isEmpty()) "لا يوجد غياب" else "مسجل في سجل المتابعة",
                    equipment = if (isCollective) "كرات يد، أقماع، صدريات تمييز، صافرة وميقاتي" else "شريط قياس، أقماع، ميقاتي إلكتروني",
                    observations = "تحقيق مؤشرات الكفاءة بنجاح وانضباط تام للتلاميذ"
                )
            )
            if (i % 2 == 1) dayIdx++
        }

        _logbookEntries.value = newEntries + _logbookEntries.value
        saveDataToFile()
    }

    /**
     * توليد نص رسمي منسق لبطاقة معلومات الأستاذ جاهز للتحميل والطباعة
     */
    fun generateTeacherProfileReport(): String {
        val p = _teacherProfile.value
        val sb = StringBuilder()
        sb.append("╔════════════════════════════════════════════════════════════════════════╗\n")
        sb.append("║           الجمهورية الجزائرية الديمقراطية الشعبية                      ║\n")
        sb.append("║                     وزارة التربية الوطنية                              ║\n")
        sb.append("║          بطاقة معلومات أستاذ التربية البدنية والرياضية                  ║\n")
        sb.append("╚════════════════════════════════════════════════════════════════════════╝\n\n")
        sb.append("• الاسم واللقب: ${p.fullName}\n")
        sb.append("• المؤسسة التعليمية: ${p.schoolName}\n")
        sb.append("• الرتبة المهنية: ${p.grade}\n")
        sb.append("• السنة الدراسية: ${p.academicYear}\n")
        sb.append("• المقاطعة التفتيشية: ${p.district}\n")
        sb.append("• رقم التسجيل الوظيفي: ${p.registrationNumber}\n")
        sb.append("• الحجم الساعي الأسبوعي: ${p.weeklyHours}\n")
        sb.append("• رقم الهاتف: ${p.phone}\n")
        sb.append("• البريد الإلكتروني: ${p.email}\n\n")
        sb.append("────────────────────────────────────────────────────────────────────────\n")
        sb.append("الأفواج المسندة للأستاذ (${_classes.value.size} أفواج):\n")
        _classes.value.forEachIndexed { idx, c ->
            val stdCount = _students.value.count { it.classId == c.id }
            sb.append("  ${idx + 1}. القسم: ${c.name} | المستوى: ${c.level} | التعداد: $stdCount تلميذ\n")
        }
        sb.append("────────────────────────────────────────────────────────────────────────\n")
        sb.append("حرر بتاريخ: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())}\n")
        sb.append("توقيع وختم الأستاذ: ____________________\n")
        return sb.toString()
    }

    /**
     * توليد نص رسمي كامل للكراس اليومي للتحميل والتصدير والمشاركة
     */
    fun generateDailyLogbookReport(targetDate: String? = null): String {
        val p = _teacherProfile.value
        val entries = if (targetDate != null) {
            _logbookEntries.value.filter { it.date == targetDate }
        } else {
            _logbookEntries.value
        }

        val sb = StringBuilder()
        sb.append("╔════════════════════════════════════════════════════════════════════════╗\n")
        sb.append("║                   الكراس اليومي لأستاذ التربية البدنية                 ║\n")
        sb.append("║                 CAHIER JOURNAL DE L'ENSEIGNANT D'EPS                  ║\n")
        sb.append("╚════════════════════════════════════════════════════════════════════════╝\n\n")
        sb.append("الأستاذ: ${p.fullName} | المؤسسة: ${p.schoolName}\n")
        sb.append("السنة الدراسية: ${p.academicYear} | الرتبة: ${p.grade}\n")
        sb.append("المقاطعة التفتيشية: ${p.district}\n")
        sb.append("════════════════════════════════════════════════════════════════════════\n\n")

        if (entries.isEmpty()) {
            sb.append("لا توجد حصص مسجلة في الكراس اليومي لهذا التاريخ.\n")
        } else {
            entries.forEachIndexed { index, entry ->
                sb.append("┌───────────────────────────────────────────────────────────────────────\n")
                sb.append("│ حصة رقم [${index + 1}] • ${entry.dayOfWeek} ${entry.date} (${entry.timeSlot})\n")
                sb.append("│ القسم: ${entry.className} (${entry.cycle}) | الميدان: ${entry.domain}\n")
                sb.append("│ النشاط المبرمج: ${entry.activityType} • ${entry.sessionNumber}\n")
                sb.append("├───────────────────────────────────────────────────────────────────────\n")
                sb.append("│ 🎯 الهدف التعلمي (الكفاءة المستهدفة):\n")
                sb.append("│    ${entry.objective}\n")
                sb.append("│\n")
                sb.append("│ 1. المرحلة التحضيرية:\n")
                sb.append("│    ${entry.warmUpPhase}\n")
                sb.append("│ 2. المرحلة التعلمية الرئيسية:\n")
                sb.append("│    ${entry.mainPhase}\n")
                sb.append("│ 3. المرحلة الختامية والتقويم:\n")
                sb.append("│    ${entry.coolDownPhase}\n")
                sb.append("│\n")
                sb.append("│ 👥 الغيابات: ${entry.absentCount} تلميذ (${entry.absentNames})\n")
                sb.append("│ ⚽ العتاد والميدان: ${entry.equipment}\n")
                sb.append("│ 📝 ملاحظات الأستاذ: ${entry.observations}\n")
                sb.append("└───────────────────────────────────────────────────────────────────────\n\n")
            }
        }

        sb.append("════════════════════════════════════════════════════════════════════════\n")
        sb.append("إجمالي الحصص المسجلة: ${entries.size} حصة\n")
        sb.append("تأشيرة المفتش التربوي / مدير المؤسسة:\n")
        sb.append("تاريخ الاطلاع والتأشير: ___ / ___ / ______  الختم والتوقيع: _________________\n")
        return sb.toString()
    }
}
