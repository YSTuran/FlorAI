package yusufs.turan.florai.ui.prediction

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import yusufs.turan.florai.domain.prediction.SelectedImage
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

private const val MAX_UPLOAD_IMAGE_SIDE_PX = 1280
private const val JPEG_UPLOAD_QUALITY = 85

internal data class PreparedImage(
    val selectedImage: SelectedImage,
    val previewBitmap: ImageBitmap
)

internal fun Context.prepareSelectedImage(uri: Uri): PreparedImage? {
    val bitmap = decodeUploadBitmap(
        ImageDecoder.createSource(contentResolver, uri)
    ) ?: return null
    val bytes = bitmap.toJpegBytes() ?: return null
    val fileName = contentResolver.getDisplayName(uri)
        ?.toJpegFileName()
        ?: "flower-image.jpg"

    return PreparedImage(
        selectedImage = SelectedImage(
            fileName = fileName,
            mimeType = "image/jpeg",
            bytes = bytes
        ),
        previewBitmap = bitmap.asImageBitmap()
    )
}

internal fun Context.prepareCapturedImage(file: File): PreparedImage? {
    if (!file.exists()) return null

    val bitmap = decodeUploadBitmap(
        ImageDecoder.createSource(file)
    ) ?: return null
    val bytes = bitmap.toJpegBytes() ?: return null

    return PreparedImage(
        selectedImage = SelectedImage(
            fileName = file.name.toJpegFileName(),
            mimeType = "image/jpeg",
            bytes = bytes
        ),
        previewBitmap = bitmap.asImageBitmap()
    )
}

private fun decodeUploadBitmap(source: ImageDecoder.Source): Bitmap? {
    return runCatching {
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val width = info.size.width
            val height = info.size.height
            val longestSide = maxOf(width, height)
            if (longestSide > MAX_UPLOAD_IMAGE_SIDE_PX) {
                val scale = MAX_UPLOAD_IMAGE_SIDE_PX.toFloat() / longestSide.toFloat()
                decoder.setTargetSize(
                    maxOf(1, (width * scale).roundToInt()),
                    maxOf(1, (height * scale).roundToInt())
                )
            }
        }
    }.getOrNull()
}

private fun Bitmap.toJpegBytes(): ByteArray? {
    val output = ByteArrayOutputStream()
    return if (compress(Bitmap.CompressFormat.JPEG, JPEG_UPLOAD_QUALITY, output)) {
        output.toByteArray()
    } else {
        null
    }
}

private fun String.toJpegFileName(): String {
    val baseName = substringBeforeLast(
        delimiter = ".",
        missingDelimiterValue = this
    ).ifBlank { "flower-image" }
    return "$baseName.jpg"
}

private fun ContentResolver.getDisplayName(uri: Uri): String? {
    return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}
