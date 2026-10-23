package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.model.Gender
import com.example.data.model.ImportedStudentResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "GeminiAiService"

sealed class AiResult<out T> {
    data class Success<out T>(val data: T) : AiResult<T>()
    data class Error(val message: String, val canFallback: Boolean = true) : AiResult<Nothing>()
}

object GeminiAiService {

    // Lazy initialization mandated by Firebase AI Logic
    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val jsonModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )
    }

    /**
     * مسح وتحليل ورقة نتائج التلاميذ وصور الكشوفات باستخدام Gemini 3.8 Flash Vision
     */
    suspend fun analyzeResultSheet(bitmap: Bitmap): AiResult<List<ImportedStudentResult>> = withContext(Dispatchers.IO) {
        try {
            val scaledBitmap = scaleBitmapIfNeeded(bitmap, maxDimension = 1200)

            val prompt = """
                أنت خبير في التعرف الضوئي (OCR) على كشوف نقاط وأوراق نتائج مادة التربية البدنية والرياضية (EPS) في الثانويات الجزائرية.
                قم بقراءة ورقة النتائج واستخراج جميع التلاميذ والنقاط بدقة تامة.
                
                يجب أن يكون الناتج حصراً بصيغة JSON Array بدون أي نص قبله أو بعده:
                [
                  {
                    "regNumber": 1,
                    "fullName": "اللقب والاسم الكامل",
                    "gender": "MALE",
                    "continuousScore": 16.0,
                    "testScore": 14.0,
                    "examScore": 15.0,
                    "individualScore": 15.0,
                    "isExempt": false,
                    "notes": ""
                  }
                ]
                
                قواعد هامة:
                1. "gender": استخدم "MALE" للذكور و "FEMALE" للإناث.
                2. العلامات تكون أعداداً عشرية من 0 إلى 20.
                3. إذا كان التلميذ معفى طبياً (معفى أو exempt أو C.M)، اجعل "isExempt": true.
                4. استخرج كل تلميذ في سطر مستقل مع رقم الترتيب إن وجد.
            """.trimIndent()

            val content = Content(
                parts = listOf(
                    ImagePart(scaledBitmap),
                    TextPart(prompt)
                )
            )

            val response = withTimeout(60_000L) {
                jsonModel.generateContent(content)
            }

            val text = response.text
            if (text.isNullOrBlank()) {
                return@withContext AiResult.Error("لم يُرجع نموذج الذكاء الاصطناعي أي نتيجة للصورة المرفقة.")
            }

            val results = parseStudentResultsJson(text)
            if (results.isEmpty()) {
                return@withContext AiResult.Error("تعذر استخراج بيانات التلاميذ من الصورة، يرجى التأكد من وضوح الخط والإضاءة.")
            }

            AiResult.Success(results)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini OCR timed out", e)
            AiResult.Error("استغرقت معالجة الصورة أكثر من 60 ثانية. يرجى إعادة المحاولة مع تقليل دقة الصورة أو التحقق من الاتصال.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini OCR failed", e)
            AiResult.Error(e.localizedMessage ?: "فشل تحليل ورقة النتائج عبر الذكاء الاصطناعي.")
        }
    }

    /**
     * إعداد تحليل بيداغوجي ذكي وتوصيات لمفتش المادة وإدارة المؤسسة عبر Gemini
     */
    suspend fun generatePedagogicalAnalysis(
        className: String,
        academicYear: String,
        termName: String,
        indSport: String,
        colSport: String,
        teacherName: String,
        schoolName: String,
        classAverage: Double,
        passRate: Double,
        studentsCount: Int,
        exemptCount: Int,
        studentsDataSnippet: String
    ): AiResult<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                أنت خبير ومفتش بيداغوجي لمادة التربية البدنية والرياضية في المنظومة التربوية الجزائرية (وزارة التربية الوطنية).
                بناءً على نتائج الفوج التربوي الآتية، قم بإعداد تقرير بيداغوجي تحليلي رسمي مفصل للأستاذ:
                
                - المؤسسة: $schoolName
                - الأستاذ المشرف: $teacherName
                - الفوج التربوي (القسم): $className
                - السنة الدراسية: $academicYear
                - الفصل الدراسي: $termName
                - النشاط الفردي المبرمج: $indSport
                - النشاط الجماعي المبرمج: $colSport
                - إجمالي التلاميذ: $studentsCount (المعفون طبياً: $exemptCount)
                - معدل القسم العام: $classAverage / 20
                - نسبة النجاح: $passRate%
                
                عينة من نتائج التلاميذ:
                $studentsDataSnippet
                
                المطلوب صياغة التقرير بالعناصر البيداغوجية الرسمية الآتية:
                1. قراءة تشخيصية لمستوى التحصيل الحركي والبدني في النشاط الفردي ($indSport) والنشاط الجماعي ($colSport).
                2. تحليل مواطن القوة الحركية ونقاط الضعف الملاحظة لدى المتعلمين.
                3. التوصيات البيداغوجية الميدانية لتحسين الأداء وتدارك النقائص في الفصل الموالي.
                4. خطة عمل لمعالجة التلاميذ ذوي التحصيل دون المتوسط ورعاية المتفوقين.
                5. كلمة توجيهية رسمية من المفتشية البيداغوجية للأستاذ.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                generalModel.generateContent(prompt)
            }

            val text = response.text
            if (text.isNullOrBlank()) {
                AiResult.Error("لم يُرجع الذكاء الاصطناعي تقريراً مكتملاً.")
            } else {
                AiResult.Success(text)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini pedagogical analysis timed out", e)
            AiResult.Error("استغرق التحليل البيداغوجي وقتاً طويلاً. يرجى المحاولة مرة أخرى.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini pedagogical analysis failed", e)
            AiResult.Error(e.localizedMessage ?: "تعذر توليد التحليل البيداغوجي عبر الذكاء الاصطناعي.")
        }
    }

    /**
     * توليد مذكرة بيداغوجية نموذجية لحصة تربية بدنية ورياضية
     */
    suspend fun generateLessonPlan(
        sportName: String,
        gradeLevel: String,
        sessionNumber: Int,
        topic: String,
        fieldGoal: String
    ): AiResult<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                أنت خبير في إعداد المذكرات البيداغوجية لمادة التربية البدنية والرياضية في الطور الثانوي بالجزائر وفق منهاج الجيل الثاني.
                قم بإعداد مذكرة بيداغوجية نموذجية كاملة للحصة رقم $sessionNumber:
                - النشاط الرياضي: $sportName
                - المستوى الدراسي: $gradeLevel
                - عنوان الحصة / الموضوع: $topic
                - الهدف الإجرائي / الكفاءة الختامية: $fieldGoal
                
                المطلوب صياغة المذكرة متضمنة الجداول والعناصر الرسمية:
                1. الكفاءة الختامية والهدف الإجرائي للحصة.
                2. الجزء التحضيري (10-15د): التسخين العام والخاص والتهيئة النفسية الحركية.
                3. الجزء التعلمي / الرئيسي (35-40د): 
                   - الموقف التعليمي الأول: التشكيل، التوجيهات، معايير ومؤشرات النجاح.
                   - الموقف التعليمي الثاني: الإدماج الحركي وتطبيق القواعد.
                4. الجزء الختامي (5-10د): الرجوع إلى حالة الهدوء والتقييم الذاتي للحصة.
                5. التوجيهات الأمنية والوسائل البيداغوجية المستعملة.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                generalModel.generateContent(prompt)
            }

            val text = response.text
            if (text.isNullOrBlank()) {
                AiResult.Error("لم يُرجع الذكاء الاصطناعي مذكرة مكتملة.")
            } else {
                AiResult.Success(text)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini lesson plan timed out", e)
            AiResult.Error("استغرق إعداد المذكرة وقتاً طويلاً. يرجى إعادة المحاولة.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini lesson plan failed", e)
            AiResult.Error(e.localizedMessage ?: "تعذر توليد المذكرة البيداغوجية.")
        }
    }

    private fun parseStudentResultsJson(rawJson: String): List<ImportedStudentResult> {
        val result = mutableListOf<ImportedStudentResult>()
        try {
            var clean = rawJson.trim()
            if (clean.startsWith("```json")) clean = clean.removePrefix("```json")
            if (clean.startsWith("```")) clean = clean.removePrefix("```")
            if (clean.endsWith("```")) clean = clean.removeSuffix("```")
            clean = clean.trim()

            val jsonArray = if (clean.startsWith("[")) {
                JSONArray(clean)
            } else if (clean.startsWith("{")) {
                val obj = JSONObject(clean)
                obj.optJSONArray("students") ?: obj.optJSONArray("results") ?: JSONArray()
            } else {
                JSONArray()
            }

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val name = item.optString("fullName", "").ifBlank {
                    item.optString("name", "تلميذ ${i + 1}")
                }
                val genderStr = item.optString("gender", "MALE")
                val gender = if (genderStr.equals("FEMALE", ignoreCase = true) || genderStr.contains("أنثى")) {
                    Gender.FEMALE
                } else {
                    Gender.MALE
                }

                val continuous = if (item.has("continuousScore") && !item.isNull("continuousScore")) {
                    item.optDouble("continuousScore")
                } else null

                val test = if (item.has("testScore") && !item.isNull("testScore")) {
                    item.optDouble("testScore")
                } else null

                val exam = if (item.has("examScore") && !item.isNull("examScore")) {
                    item.optDouble("examScore")
                } else null

                val individual = if (item.has("individualScore") && !item.isNull("individualScore")) {
                    item.optDouble("individualScore")
                } else null

                val exempt = item.optBoolean("isExempt", false)
                val regNum = item.optInt("regNumber", i + 1)
                val notes = item.optString("notes", "")

                result.add(
                    ImportedStudentResult(
                        id = "temp_${System.currentTimeMillis()}_$i",
                        regNumber = if (regNum > 0) regNum else (i + 1),
                        fullName = name.trim(),
                        gender = gender,
                        isMedicallyExempt = exempt,
                        continuousScore = continuous,
                        testScore = test,
                        examScore = exam,
                        individualScore = individual,
                        notes = notes
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
