package com.example.data.model

data class SportCriterionInfo(
    val id: String,
    val labelAr: String,
    val maxScore: Double = 4.0,
    val descriptionAr: String = ""
)

object SportCriteriaDefinitions {
    // 1. معايير كرة اليد (5 معايير × 4 نقاط = 20 نقطة)
    val HANDBALL_CRITERIA = listOf(
        SportCriterionInfo("c1", "التمرير والاستقبال", 4.0, "دقة التمرير وسرعة الاستقبال والتحكم في الكرة"),
        SportCriterionInfo("c2", "التنطيط والمراوغة", 4.0, "التحكم في الكرة أثناء التنقل وتجاوز المدافع"),
        SportCriterionInfo("c3", "العمل الجماعي والإبداع", 4.0, "التعاون مع الزملاء وتقديم حلول مبتكرة في اللعب"),
        SportCriterionInfo("c4", "بناء دفاع وهجوم", 4.0, "التموقع السليم وسرعة الانتقال بين الدفاع والهجوم"),
        SportCriterionInfo("c5", "تهديف", 4.0, "دقة التسديد واختيار زاوية التهديف المناسبة")
    )

    // 2. معايير كرة السلة (5 معايير × 4 نقاط = 20 نقطة)
    val BASKETBALL_CRITERIA = listOf(
        SportCriterionInfo("c1", "التمرير والمحاورة", 4.0, "التمرير الصدري والمرتد وحماية الكرة أثناء المحاورة"),
        SportCriterionInfo("c2", "التنطيط وتغيير الاتجاه", 4.0, "المراوغة السريعة مع التحكم بالكرة باليدين"),
        SportCriterionInfo("c3", "العمل الجماعي والإبداع", 4.0, "صناعة اللعب والتمركز الذكي بدون كرة"),
        SportCriterionInfo("c4", "بناء دفاع وهجوم ومتابعة", 4.0, "التغطية الدفاعية والارتداد الهجومي السريع"),
        SportCriterionInfo("c5", "تهديف وتصويب على السلة", 4.0, "الرميات الدقيقة والارتقاء نحو السلة وحسم النقاط")
    )

    val HANDBALL_AND_BASKETBALL_CRITERIA = HANDBALL_CRITERIA

    // 3. معايير كرة الطائرة (5 معايير × 4 نقاط = 20 نقطة)
    val VOLLEYBALL_CRITERIA = listOf(
        SportCriterionInfo("c1", "الإرسال", 4.0, "إتقان الإرسال وتوجيه الكرة داخل الملعب"),
        SportCriterionInfo("c2", "الاستقبال", 4.0, "استقبال إرسال الخصم وتوجيهه بدقة نحو الرفاع"),
        SportCriterionInfo("c3", "التمرير", 4.0, "تمرير الكرة بالأصابع أو الساعدين بدقة ومرونة"),
        SportCriterionInfo("c4", "بناء هجمة وتغطية", 4.0, "التنسيق والتحضير الجماعي لإعداد كرة السحق"),
        SportCriterionInfo("c5", "تهديف وسحق", 4.0, "حسم النقطة بالسحق والضربات الساقطة الذكية")
    )

    // 4. معايير كرة القدم المدرسية (5 معايير × 4 نقاط = 20 نقطة)
    val FOOTBALL_CRITERIA = listOf(
        SportCriterionInfo("c1", "السيطرة والتمرير", 4.0, "ترويض الكرة والتمرير القصير والطويل بدقة للزملاء"),
        SportCriterionInfo("c2", "الجري بالكرة والمراوغة", 4.0, "قيادة الكرة بسلاسة وتخطي المنافس بمهارة"),
        SportCriterionInfo("c3", "العمل الجماعي والمساندة", 4.0, "فتح زوايا التمرير والتعاون الهجومي المشترك"),
        SportCriterionInfo("c4", "التمركز الدفاعي والهجومي", 4.0, "التغطية، قطع الكرات، والارتداد السريع"),
        SportCriterionInfo("c5", "التهديف والتسديد نحو المرمى", 4.0, "دقة التسديد من مسافات وزوايا مختلفة")
    )

    // 5. معايير الركبي المدرسي / ألعاب المطاردة (5 معايير × 4 نقاط = 20 نقطة)
    val RUGBY_CRITERIA = listOf(
        SportCriterionInfo("c1", "التمرير للخلف والاستقبال", 4.0, "تمرير الكرة للخلف بدقة وسرعة التقاطها"),
        SportCriterionInfo("c2", "التقدم وتفادي الخصم", 4.0, "الجري في المساحات الحرة وتغيير المسار"),
        SportCriterionInfo("c3", "الدعم والمساندة الجماعية", 4.0, "مرافقة حامل الكرة وتوفير خيارات مستمرة"),
        SportCriterionInfo("c4", "التنظيم الدفاعي واحتلال المساحات", 4.0, "المحاذاة الدفاعية ومنع تقدم المنافس"),
        SportCriterionInfo("c5", "تسجيل المحاولة (التهديف)", 4.0, "وضع الكرة وراء خط النهاية بنجاح")
    )

    // 6. معايير الجمباز والحركات الأرضية والعروض (5 معايير × 4 نقاط = 20 نقطة)
    val GYMNASTICS_CRITERIA = listOf(
        SportCriterionInfo("c1", "وضعيات الاتزان والتحكم", 4.0, "الثبات، التوازن على قدم واحدة أو اليدين"),
        SportCriterionInfo("c2", "الدحرجة والحركات الأساسية", 4.0, "الدحرجة الأمامية والخلفية بانسيابية"),
        SportCriterionInfo("c3", "التوافق والرشاقة والإبداع", 4.0, "تناسق أجزاء الجسم والابتكار الحركي"),
        SportCriterionInfo("c4", "الربط والتسلسل الحركي", 4.0, "تسلسل حركات متناسقة دون توقف مفاجئ"),
        SportCriterionInfo("c5", "الوضعية النهائية والجمالية", 4.0, "دقة الهبوط، الانتصاب، والجانب الجمالي")
    )

    fun getCriteriaForSport(sportName: String): List<SportCriterionInfo> {
        val name = sportName.trim().lowercase()
        return when {
            name.contains("طائرة") || name.contains("volley") -> VOLLEYBALL_CRITERIA
            name.contains("سلة") || name.contains("basket") -> BASKETBALL_CRITERIA
            name.contains("قدم") || name.contains("foot") -> FOOTBALL_CRITERIA
            name.contains("ركبي") || name.contains("rugby") -> RUGBY_CRITERIA
            name.contains("جمباز") || name.contains("gym") || name.contains("حركات") -> GYMNASTICS_CRITERIA
            name.contains("يد") || name.contains("hand") -> HANDBALL_CRITERIA
            else -> HANDBALL_CRITERIA
        }
    }

    fun getCriteriaForClassAndTerm(classRoom: ClassRoom?, termType: TermType): List<SportCriterionInfo> {
        val sportName = classRoom?.getCollectiveSport(termType) ?: termType.collectiveSportAr
        return getCriteriaForSport(sportName)
    }

    fun getCriteriaForTerm(termType: TermType): List<SportCriterionInfo> {
        return when (termType) {
            TermType.TERM_1 -> HANDBALL_CRITERIA
            TermType.TERM_2 -> BASKETBALL_CRITERIA
            TermType.TERM_3 -> VOLLEYBALL_CRITERIA
        }
    }
}

data class CriterionEvaluation(
    val studentId: String,
    // المعايير الخمسة للرياضة الجماعية (من 4 نقاط لكل معيار = مجموع 20)
    // لا يتم ملء النقاط تلقائياً (تبدأ من 0.0 حتى يضع الأستاذ علامته الفعلية)
    val c1Score: Double = 0.0, // التمرير والاستقبال (أو الإرسال للطائرة)
    val c2Score: Double = 0.0, // التنطيط والمراوغة (أو الاستقبال للطائرة)
    val c3Score: Double = 0.0, // العمل الجماعي والإبداع (أو التمرير للطائرة)
    val c4Score: Double = 0.0, // بناء دفاع وهجوم (أو بناء هجمة للطائرة)
    val c5Score: Double = 0.0, // تهديف (أو تهديف/سحق للطائرة)
    
    // إمكانية الضبط اليدوي للعلامة الناتجة عن المستوى أو تركها محسوبة تلقائياً
    val customCollectiveScore: Double? = null,

    // حقول التوافق مع الإصدارات السابقة
    val c1MotorScore: Double = c1Score,
    val c2EfficiencyScore: Double = c2Score,
    val c3KnowledgeScore: Double = c3Score,
    val c4BehaviorScore: Double = c4Score
) {
    // مجموع المعايير الخمسة (من 20)
    val criteriaSum: Double
        get() = String.format(
            java.util.Locale.US,
            "%.2f",
            (c1Score + c2Score + c3Score + c4Score + c5Score).coerceIn(0.0, 20.0)
        ).toDouble()

    // علامة الرياضة الجماعية من 20
    val collectiveSportScore20: Double
        get() = customCollectiveScore ?: criteriaSum

    val percentage: Double
        get() = String.format(java.util.Locale.US, "%.1f", (collectiveSportScore20 / 20.0) * 100.0).toDouble()

    // تحديد المستوى من بين المستويات الأربعة للمعايير
    val masteryLevel: MasteryLevel
        get() = when {
            collectiveSportScore20 >= 16.0 -> MasteryLevel.MAXIMAL     // المستوى 4: تحكم أقصى (16 - 20)
            collectiveSportScore20 >= 12.0 -> MasteryLevel.ACCEPTABLE  // المستوى 3: تحكم مقبول (12 - 15.9)
            collectiveSportScore20 >= 8.0 -> MasteryLevel.PARTIAL      // المستوى 2: تحكم جزئي (8 - 11.9)
            else -> MasteryLevel.LIMITED                               // المستوى 1: تحكم محدود (< 8)
        }

    val totalScore: Double
        get() = collectiveSportScore20

    val isCriterionAchieved: Boolean
        get() = collectiveSportScore20 >= 10.0
}

enum class MasteryLevel(
    val levelNumber: Int,
    val labelAr: String,
    val scoreRangeAr: String,
    val minScore: Double,
    val defaultScore: Double
) {
    MAXIMAL(4, "المستوى 4: تحكم أقصى", "16 إلى 20", 16.0, 18.0),
    ACCEPTABLE(3, "المستوى 3: تحكم مقبول", "12 إلى 15.5", 12.0, 14.0),
    PARTIAL(2, "المستوى 2: تحكم جزئي", "8 إلى 11.5", 8.0, 10.0),
    LIMITED(1, "المستوى 1: تحكم محدود", "دون 8", 0.0, 6.0)
}
