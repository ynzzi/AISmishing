package com.example.sbs.smishingdetector.viewmodel

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbs.smishingdetector.network.*
import com.example.sbs.smishingdetector.model.DetectionApkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipFile
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.min

class FileScanViewModel : ViewModel() {

    companion object {
        private const val BATCH_SIZE = 30
        private const val TAG = "FileScanVM"

        private const val ENTROPY_BLOCK = 64 * 1024            // 64KB
        private const val ENTROPY_MAX_BYTES = 32 * 1024 * 1024 // 32MB
        private const val UPLOAD_TIMEOUT_MS = 5_000L           // 업로드 타임아웃 5초
    }

    data class UiState(
        val phase: Phase = Phase.Preparing,
        val statusText: String = "대기 중...",
        val progress: Float = 0f,
        val currentIndex: Int = 0,
        val total: Int = 0,
        val detections: List<DetectionApkResult> = emptyList()
    )

    enum class Phase { Preparing, Extracting, Uploading, Done, Error }

    private val _ui = MutableStateFlow(UiState())
    val uiState = _ui.asStateFlow()

    /** 설치된 앱 기반 스캔 시작 */
    fun startFullStorageScan(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "startFullStorageScan() called [PackageManager mode]")
            logStoragePermStatus(context)

            val items = collectInstalledApkItems(context)
            Log.d(TAG, "Installed APK count = ${items.size}")

            if (items.isEmpty()) {
                _ui.value = UiState(
                    phase = Phase.Done,
                    statusText = "설치된 앱이 없거나 조회 권한 제한",
                    progress = 1f
                )
            } else {
                startScan(items)
            }
        }
    }

    private data class ApkItem(
        val file: File?,
        val appName: String,
        val packageName: String,
        val requestedPermissions: List<String>,
        val iconBase64: String?
    )

    /** 설치된 앱 수집 */
    private fun collectInstalledApkItems(context: Context): List<ApkItem> {
        val pm = context.packageManager
        val packages: List<PackageInfo> = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            Log.w(TAG, "getInstalledPackages failed: ${e.message}")
            emptyList()
        }

        val result = ArrayList<ApkItem>()
        for (pi in packages) {
            val ai = pi.applicationInfo ?: continue
            if ((ai.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) continue

            val label = try {
                ai.loadLabel(pm)?.toString() ?: pi.packageName
            } catch (_: Throwable) {
                pi.packageName
            }
            val src = ai.sourceDir
            val f = try {
                if (!src.isNullOrBlank()) File(src) else null
            } catch (_: Throwable) {
                null
            }
            val perms = try {
                pi.requestedPermissions?.toList() ?: emptyList()
            } catch (_: Throwable) {
                emptyList()
            }

            val iconBase64 = try {
                val drawable: Drawable = pm.getApplicationIcon(ai)
                drawableToBase64(drawable)
            } catch (e: Exception) {
                Log.w(TAG, "icon load failed: ${e.message}")
                null
            }

            result.add(
                ApkItem(
                    file = f,
                    appName = label,
                    packageName = pi.packageName,
                    requestedPermissions = perms,
                    iconBase64 = iconBase64
                )
            )
        }
        return result
    }

    /** Drawable → Base64 */
    private fun drawableToBase64(drawable: Drawable): String {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /** 실제 스캔 */
    private fun startScan(items: List<ApkItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val total = items.size
                if (total == 0) {
                    _ui.value = UiState(
                        phase = Phase.Done,
                        statusText = "검사할 항목 없음",
                        progress = 1f
                    )
                    return@launch
                }

                _ui.value = _ui.value.copy(
                    phase = Phase.Extracting,
                    total = total,
                    detections = emptyList()
                )

                // --- [1] 피처 추출 단계: 0 ~ 95% ---
                val payloads = items.mapIndexed { idx, item ->
                    val ex = extractFromApkFileSafely(item.file)

                    // ✅ 진행률: 항목별 0 ~ 95%
                    val ratio = (idx + 1).toFloat() / total.toFloat()
                    val scaled = (0.95f * ratio).coerceIn(0f, 0.95f)
                    _ui.value = _ui.value.copy(
                        progress = scaled,
                        currentIndex = idx + 1,
                        total = total
                    )

                    FeaturePayload(
                        userId = "user001",
                        appName = item.appName,
                        packageName = item.packageName,
                        apk_permission_list = item.requestedPermissions,
                        apk_api_list = emptyList(),
                        entropy_mean = ex.entropy_mean,
                        entropy_max = ex.entropy_max,
                        entropy_std = ex.entropy_std,
                        entropy_p95 = ex.entropy_p95,
                        ext_cnt_dex = ex.ext_cnt_dex,
                        ext_cnt_png = ex.ext_cnt_png,
                        ext_cnt_xml = ex.ext_cnt_xml,
                        sha256 = ex.sha256,
                        iconBase64 = item.iconBase64
                    ).also {
                        val ratio = (idx + 1).toFloat() / total.toFloat()
                        val scaled = (0.95f * ratio).coerceIn(0f, 0.95f)

                        _ui.value = _ui.value.copy(
                            phase = Phase.Extracting,
                            statusText = item.appName, // APK명 그대로 유지
                            progress = scaled,
                            currentIndex = idx + 1,
                            total = total
                        )
                    }
                }

                // ✅ 추출 완료 시 정확히 95%
                _ui.value = _ui.value.copy(progress = 0.95f)

                // --- [2] 서버 업로드 단계: 95 ~ 100% ---
                val batches = payloads.chunked(BATCH_SIZE)
                val totalBatches = batches.size
                var doneBatches = 0
                val allDetections = mutableListOf<DetectionApkResult>()

                batches.forEach { batch ->
                    val newDetections: List<DetectionApkResult> = try {
                        withTimeout(UPLOAD_TIMEOUT_MS) {
                            val res = RetrofitClient.api.uploadApkFeatureBatch(batch)
                            if (res.isSuccessful) {
                                res.body()?.results ?: emptyList()
                            } else {
                                emptyList()
                            }
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }

                    allDetections.addAll(newDetections)
                    doneBatches++

                    // ✅ 남은 5%를 균등 분배
                    val serverRatio = doneBatches.toFloat() / totalBatches.toFloat()
                    val p = 0.95f + 0.05f * serverRatio
                    _ui.value = _ui.value.copy(progress = p.coerceIn(0f, 1f))
                }

                // ✅ 최종 완료 → 100%
                _ui.value = _ui.value.copy(
                    phase = Phase.Done,
                    progress = 1f,
                    currentIndex = total,
                    detections = allDetections
                )

            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    phase = Phase.Error,
                    progress = 1f
                )
            }
        }
    }

    /** APK 파일에서 피처 추출 */
    private fun extractFromApkFileSafely(file: File?): ApkExtract {
        if (file == null) return ApkExtract(null, null, null, null, null, null, null, null)
        return try {
            val sha = try { sha256File(file) } catch (e: Throwable) { null }
            val entropy = try { entropyStats(file) } catch (e: Throwable) { null }
            val (dex, xml, png) = try { countEntries(file) } catch (e: Throwable) { Triple(null, null, null) }

            ApkExtract(
                sha256 = sha,
                entropy_mean = entropy?.mean,
                entropy_max = entropy?.max,
                entropy_std = entropy?.std,
                entropy_p95 = entropy?.p95,
                ext_cnt_dex = dex,
                ext_cnt_xml = xml,
                ext_cnt_png = png
            )
        } catch (e: Exception) {
            ApkExtract(null, null, null, null, null, null, null, null)
        }
    }

    private data class ApkExtract(
        val entropy_mean: Double?,
        val entropy_max: Double?,
        val entropy_std: Double?,
        val entropy_p95: Double?,
        val ext_cnt_dex: Int?,
        val ext_cnt_xml: Int?,
        val ext_cnt_png: Int?,
        val sha256: String?
    )

    /** SHA-256 */
    private fun sha256File(file: File): String {
        val buf = ByteArray(8192)
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { ins ->
            while (true) {
                val read = ins.read(buf)
                if (read <= 0) break
                md.update(buf, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /** 엔트로피 통계 */
    private fun entropyStats(file: File): EntropyStats {
        val entropies = ArrayList<Double>(
            max(1, (minOf(file.length(), ENTROPY_MAX_BYTES.toLong()).toInt() / ENTROPY_BLOCK) + 1)
        )
        val buf = ByteArray(ENTROPY_BLOCK)
        var processed = 0L

        file.inputStream().buffered().use { ins ->
            while (true) {
                if (processed >= ENTROPY_MAX_BYTES) break
                val toRead = minOf(ENTROPY_BLOCK.toLong(), ENTROPY_MAX_BYTES - processed).toInt()
                val n = ins.read(buf, 0, toRead)
                if (n <= 0) break
                entropies.add(shannonEntropy(buf, n))
                processed += n
            }
        }
        if (entropies.isEmpty()) return EntropyStats(null, null, null, null)

        val mean = entropies.average()
        val mx = entropies.maxOrNull()!!
        val std = sqrt(entropies.map { (it - mean).pow(2.0) }.average())
        val p95 = percentile(entropies, 95.0)
        return EntropyStats(mean, mx, std, p95)
    }

    private data class EntropyStats(
        val mean: Double?, val max: Double?, val std: Double?, val p95: Double?
    )

    private fun shannonEntropy(data: ByteArray, len: Int): Double {
        val counts = IntArray(256)
        for (i in 0 until len) counts[data[i].toInt() and 0xFF]++
        var entropy = 0.0
        val invN = 1.0 / len
        for (c in counts) {
            if (c == 0) continue
            val p = c * invN
            entropy -= p * log2(p)
        }
        return entropy
    }

    private fun log2(x: Double) = ln(x) / ln(2.0)

    private fun percentile(values: List<Double>, p: Double): Double {
        if (values.isEmpty()) return Double.NaN
        val sorted = values.sorted()
        val rank = (p / 100.0) * (sorted.size - 1)
        val lo = rank.toInt()
        val hi = minOf(lo + 1, sorted.lastIndex)
        val frac = rank - lo
        return sorted[lo] * (1 - frac) + sorted[hi] * frac
    }

    private fun countEntries(file: File): Triple<Int?, Int?, Int?> {
        var dex = 0; var xml = 0; var png = 0
        ZipFile(file).use { zip ->
            val en = zip.entries()
            while (en.hasMoreElements()) {
                val name = en.nextElement().name.lowercase()
                when {
                    name.endsWith(".dex") -> dex++
                    name.endsWith(".xml") -> xml++
                    name.endsWith(".png") -> png++
                }
            }
        }
        return Triple(dex, xml, png)
    }

    /** 디버그용 권한 상태 로깅 */
    private fun logStoragePermStatus(context: Context) {
        val api = Build.VERSION.SDK_INT
        val readGranted = if (api <= Build.VERSION_CODES.S_V2) {
            ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else false
        val allFilesGranted: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else false

        Log.d(TAG, "API=$api, READ_EXTERNAL_STORAGE=$readGranted, MANAGE_EXTERNAL_STORAGE=$allFilesGranted")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Log.d(TAG, "Note: Android 11+에서 모든 설치 앱 열람은 QUERY_ALL_PACKAGES 권한 또는 <queries> 설정 필요")
        }
    }
}
