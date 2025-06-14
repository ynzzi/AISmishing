package com.example.sbs.smishingdetector.ai

import android.content.Context
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File

object AiRunner {
    private var module: Module? = null

    // 모델 파일 로드
    fun loadModel(context: Context) {
        if (module == null) {
            val modelPath = assetFilePath(context, "ko_miniLM_mobile.pt")
            module = Module.load(modelPath)
        }
    }

    // 예측 실행 (입력값: input_ids, attention_mask)
    fun predict(inputIds: LongArray, attentionMask: LongArray): FloatArray {
        val inputTensor = Tensor.fromBlob(inputIds, longArrayOf(1, inputIds.size.toLong()))
        val maskTensor = Tensor.fromBlob(attentionMask, longArrayOf(1, attentionMask.size.toLong()))
        val outputTensor = module!!.forward(IValue.from(inputTensor), IValue.from(maskTensor)).toTensor()
        return outputTensor.dataAsFloatArray
    }

    // assets 폴더에서 모델 파일 경로 불러오기
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath

        context.assets.open(assetName).use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    }
}
