package com.urmyfood.shared.util

import android.content.Context
import android.net.Uri
import com.urmyfood.shared.domain.model.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

object ChatImageMultipartBuilder {
    private const val MAX_SIZE_BYTES = 5L * 1024L * 1024L
    private const val PART_NAME = "file"

    private val extensionsByMimeType = mapOf(
        "image/jpeg" to "jpg",
        "image/png" to "png",
        "image/webp" to "webp"
    )

    fun build(context: Context, uri: Uri): Result<MultipartBody.Part> {
        val resolver = context.contentResolver
        val bytes = try {
            resolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            return Result.Error("Không thể mở ảnh đã chọn")
        } ?: return Result.Error("Không thể mở ảnh đã chọn")

        if (bytes.isEmpty()) {
            return Result.Error("File ảnh không được để trống")
        }

        if (bytes.size > MAX_SIZE_BYTES) {
            return Result.Error("Ảnh vượt quá 5MB")
        }

        val mimeType = normalizeMimeType(resolver.getType(uri))
            ?: sniffMimeType(bytes)
            ?: return Result.Error("Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP")
        val extension = extensionsByMimeType[mimeType]
            ?: return Result.Error("Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP")

        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val fileName = "chat_image.$extension"
        return Result.Success(MultipartBody.Part.createFormData(PART_NAME, fileName, requestBody))
    }

    private fun normalizeMimeType(mimeType: String?): String? {
        val normalized = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?: return null

        return when (normalized) {
            "image/jpg" -> "image/jpeg"
            "image/jpeg", "image/png", "image/webp" -> normalized
            else -> null
        }
    }

    private fun sniffMimeType(bytes: ByteArray): String? {
        return when {
            bytes.isJpeg() -> "image/jpeg"
            bytes.isPng() -> "image/png"
            bytes.isWebp() -> "image/webp"
            else -> null
        }
    }

    private fun ByteArray.isJpeg(): Boolean =
        size >= 3 &&
            this[0] == 0xFF.toByte() &&
            this[1] == 0xD8.toByte() &&
            this[2] == 0xFF.toByte()

    private fun ByteArray.isPng(): Boolean =
        size >= 8 &&
            this[0] == 0x89.toByte() &&
            this[1] == 0x50.toByte() &&
            this[2] == 0x4E.toByte() &&
            this[3] == 0x47.toByte() &&
            this[4] == 0x0D.toByte() &&
            this[5] == 0x0A.toByte() &&
            this[6] == 0x1A.toByte() &&
            this[7] == 0x0A.toByte()

    private fun ByteArray.isWebp(): Boolean =
        size >= 12 &&
            this[0] == 'R'.code.toByte() &&
            this[1] == 'I'.code.toByte() &&
            this[2] == 'F'.code.toByte() &&
            this[3] == 'F'.code.toByte() &&
            this[8] == 'W'.code.toByte() &&
            this[9] == 'E'.code.toByte() &&
            this[10] == 'B'.code.toByte() &&
            this[11] == 'P'.code.toByte()
}
