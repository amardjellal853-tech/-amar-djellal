package com.example.data.ocr

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.Gender
import com.example.data.model.ImportedStudentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

sealed class ScanResult {
    data class Success(val items: List<ImportedStudentResult>, val source: String) : ScanResult()
    data class Error(val message: String, val canFallback: Boolean = true) : ScanResult()
}

object ResultSheetScannerService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Analyze image of a results/score paper using Gemini 2.5 Flash Vision.
     */
    suspend fun analyzeImage(bitmap: Bitmap): ScanResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext ScanResult.Error(
                message = "لم يتم ضبط مفتاح Gemini API في إعدادات التطبيق. يمكنك استخدام النماذج الجاهزة أو إدخال/لصق ورقة النتائج نصياً.",
                canFallback = true
            )
        }

        try {
            // Resize bitmap if too large to conserve bandwidth and memory
            val scaledBitmap = scaleBitmapIfNeeded(bitmap, maxDimension = 1024)
            val base64Data = bitmapToBase64(scaledBitmap)

            val prompt = """
                أنت خبير في التعرف الضوئي (OCR) على كشوف نقاط وأوراق نتائج مادة التربية البدنية والرياضية (EPS) في الثانويات الجزائرية.
                قم بقراءة ورقة النتائج المرفقة واستخراج جميع التلاميذ والنقاط بدقة.
                
                يجب أن يكون الناتج حصراً بصيغة JSON Array بدون أي نص قبله أو بعده:
                [
                  {
                    "regNumber": 1,
                    "fullName": "اللقب والاسم الكامل",
                    "gender": "MALE",
                    "continuousScore": 15.5,
                    "testScore": 14.0,
                    "examScore": 16.0,
                    "individualScore": 15.0,
                    "isExempt": false,
                    "notes": ""
                  }
                ]
                
                قواعد هامة:
                1. "gender": استخدم "MALE" للذكور و "FEMALE" للإناث.
                2. العلامات تكون أعداداً عشرية من 0 إلى 20. إذا لم توجد علامة معينة في الورقة ضعها null.
                3. إذا كان التلميذ معفى طبياً (معفى أو exempt أو C.M)، اجعل "isExempt": true.
                4. استخرج كل تلميذ في سطر مستقل مع رقم الترتيب إن وجد.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            // Text prompt
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            // Image data
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Data)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                return@withContext ScanResult.Error(
                    message = "خطأ من خادم الذكاء الاصطناعي (${response.code}): $errBody",
                    canFallback = true
                )
            }

            val responseBody = response.body?.string() ?: ""
            val jsonRoot = JSONObject(responseBody)
            val candidates = jsonRoot.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext ScanResult.Error("لم يتم العثور على محتوى من تحليل الصورة.", canFallback = true)
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val textOutput = parts.getJSONObject(0).getString("text")

            val parsedItems = parseJsonResults(textOutput)
            if (parsedItems.isEmpty()) {
                return@withContext ScanResult.Error("لم يتم العثور على تلاميذ أو نقاط واضحة في الصورة.", canFallback = true)
            }

            ScanResult.Success(parsedItems, source = "التعرف الذكي بالذكاء الاصطناعي (Gemini Vision)")
        } catch (e: Exception) {
            e.printStackTrace()
            ScanResult.Error(
                message = "تعذر تحليل الصورة: ${e.localizedMessage ?: e.message}",
                canFallback = true
            )
        }
    }

    /**
     * Parses raw JSON string returned by Gemini.
     */
    fun parseJsonResults(rawJson: String): List<ImportedStudentResult> {
        val result = mutableListOf<ImportedStudentResult>()
        try {
            // Clean markdown code fence if present
            var clean = rawJson.trim()
            if (clean.startsWith("```json")) {
                clean = clean.removePrefix("```json")
            }
            if (clean.startsWith("```")) {
                clean = clean.removePrefix("```")
            }
            if (clean.endsWith("```")) {
                clean = clean.removeSuffix("```")
            }
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

    /**
     * Parses text/CSV/TSV or copy-pasted results paper.
     */
    fun parseTextSheet(text: String): List<ImportedStudentResult> {
        val list = mutableListOf<ImportedStudentResult>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        var index = 1
        for (line in lines) {
            // Ignore common header lines
            if (line.contains("اللقب") || line.contains("الاسم") || line.contains("Nom") ||
                line.contains("المعدل") || line.contains("التقويم") || line.contains("الرقم")
            ) {
                continue
            }

            // Split by tab, comma, semicolon, or vertical bar
            val parts = line.split(Regex("[\t,;|]")).map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isEmpty()) continue

            var regNumber = index
            var name = ""
            var gender = Gender.MALE
            var isExempt = false
            var continuousScore: Double? = null
            var testScore: Double? = null
            var examScore: Double? = null

            // Detect parts
            val numericValues = mutableListOf<Double>()
            for (part in parts) {
                val num = part.replace(',', '.').toDoubleOrNull()
                if (num != null) {
                    numericValues.add(num)
                } else {
                    val pLower = part.lowercase()
                    if (pLower == "أنثى" || pLower == "انثى" || pLower == "f" || pLower == "female") {
                        gender = Gender.FEMALE
                    } else if (pLower == "ذكر" || pLower == "m" || pLower == "male") {
                        gender = Gender.MALE
                    } else if (pLower.contains("معفى") || pLower.contains("عفو") || pLower.contains("exempt")) {
                        isExempt = true
                    } else if (part.length >= 3 && !part.all { it.isDigit() }) {
                        if (name.isBlank()) {
                            name = part
                        } else {
                            name += " $part"
                        }
                    }
                }
            }

            if (name.isBlank()) {
                // If line was separated by spaces
                val spaceTokens = line.split(Regex("\\s+"))
                val nameTokens = mutableListOf<String>()
                for (token in spaceTokens) {
                    val n = token.replace(',', '.').toDoubleOrNull()
                    if (n != null) {
                        numericValues.add(n)
                    } else if (token == "أنثى" || token == "انثى") {
                        gender = Gender.FEMALE
                    } else if (token == "ذكر") {
                        gender = Gender.MALE
                    } else if (token.contains("معفى")) {
                        isExempt = true
                    } else {
                        nameTokens.add(token)
                    }
                }
                name = nameTokens.joinToString(" ")
            }

            if (name.isNotBlank()) {
                // Check if first numeric is reg number
                if (numericValues.isNotEmpty() && numericValues.first() > 0 && numericValues.first() <= 100 && numericValues.first() == numericValues.first().toInt().toDouble()) {
                    regNumber = numericValues.removeAt(0).toInt()
                }

                // Remaining numeric values represent continuous, test, exam
                if (numericValues.isNotEmpty()) continuousScore = numericValues.removeAt(0).coerceIn(0.0, 20.0)
                if (numericValues.isNotEmpty()) testScore = numericValues.removeAt(0).coerceIn(0.0, 20.0)
                if (numericValues.isNotEmpty()) examScore = numericValues.removeAt(0).coerceIn(0.0, 20.0)

                list.add(
                    ImportedStudentResult(
                        id = "text_${System.currentTimeMillis()}_$index",
                        regNumber = regNumber,
                        fullName = name,
                        gender = gender,
                        isMedicallyExempt = isExempt,
                        continuousScore = continuousScore,
                        testScore = testScore,
                        examScore = examScore
                    )
                )
                index++
            }
        }
        return list
    }

    /**
     * Preset official Algerian Secondary School test sheets for immediate testing or demo.
     */
    fun getPresetSampleSheets(): Map<String, List<ImportedStudentResult>> {
        val sheet1 = listOf(
            ImportedStudentResult(regNumber = 1, fullName = "بلحاج محمد أنيس", gender = Gender.MALE, continuousScore = 16.5, testScore = 15.0, examScore = 17.0),
            ImportedStudentResult(regNumber = 2, fullName = "بن عيسى فاطمة الزهراء", gender = Gender.FEMALE, continuousScore = 18.0, testScore = 17.5, examScore = 18.0),
            ImportedStudentResult(regNumber = 3, fullName = "بوعبد الله عبد الرحيم", gender = Gender.MALE, continuousScore = 14.0, testScore = 13.5, examScore = 14.0),
            ImportedStudentResult(regNumber = 4, fullName = "بوزيد مريم خديجة", gender = Gender.FEMALE, continuousScore = 15.0, testScore = 14.0, examScore = 15.5),
            ImportedStudentResult(regNumber = 5, fullName = "جابري صهيب إسلام", gender = Gender.MALE, continuousScore = 17.5, testScore = 16.0, examScore = 18.0),
            ImportedStudentResult(regNumber = 6, fullName = "حطاب ريان إكرام", gender = Gender.FEMALE, continuousScore = 16.0, testScore = 15.0, examScore = 16.5),
            ImportedStudentResult(regNumber = 7, fullName = "داودي زكرياء يونس", gender = Gender.MALE, continuousScore = 13.0, testScore = 12.0, examScore = 13.5),
            ImportedStudentResult(regNumber = 8, fullName = "رحماني شيماء أميرة", gender = Gender.FEMALE, isMedicallyExempt = true, notes = "معفى طبياً لداعي الحساسية"),
            ImportedStudentResult(regNumber = 9, fullName = "سعيدي حسام الدين", gender = Gender.MALE, continuousScore = 15.5, testScore = 14.5, examScore = 16.0),
            ImportedStudentResult(regNumber = 10, fullName = "طاهري أسماء ياسمين", gender = Gender.FEMALE, continuousScore = 17.0, testScore = 16.5, examScore = 17.0),
            ImportedStudentResult(regNumber = 11, fullName = "عمارة عبد القادر", gender = Gender.MALE, continuousScore = 12.5, testScore = 11.0, examScore = 13.0),
            ImportedStudentResult(regNumber = 12, fullName = "قاسيمي منال نور الهدى", gender = Gender.FEMALE, continuousScore = 18.5, testScore = 18.0, examScore = 19.0)
        )

        val sheet2 = listOf(
            ImportedStudentResult(regNumber = 1, fullName = "مداح يحيى وليد", gender = Gender.MALE, continuousScore = 15.0, testScore = 14.0, examScore = 16.0),
            ImportedStudentResult(regNumber = 2, fullName = "ناصري إيناس مروة", gender = Gender.FEMALE, continuousScore = 16.5, testScore = 15.5, examScore = 17.0),
            ImportedStudentResult(regNumber = 3, fullName = "هميسي سيف الإسلام", gender = Gender.MALE, continuousScore = 14.0, testScore = 13.0, examScore = 14.5),
            ImportedStudentResult(regNumber = 4, fullName = "يعقوبي هديل سيرين", gender = Gender.FEMALE, continuousScore = 17.0, testScore = 16.0, examScore = 17.5),
            ImportedStudentResult(regNumber = 5, fullName = "أولاد الحاج أكرم", gender = Gender.MALE, continuousScore = 13.5, testScore = 12.5, examScore = 14.0),
            ImportedStudentResult(regNumber = 6, fullName = "بختي كوثر سلسبيل", gender = Gender.FEMALE, continuousScore = 18.0, testScore = 17.5, examScore = 18.5)
        )

        return mapOf(
            "كشف نقاط رسمي شامل (فصل أول - ثانوي)" to sheet1,
            "قائمة نتائج إضافية سريعة" to sheet2
        )
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) {
            return bitmap
        }
        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
