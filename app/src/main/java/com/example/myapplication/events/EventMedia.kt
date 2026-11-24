package com.example.myapplication.events

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import androidx.core.graphics.createBitmap

// Currently on the free plan, so posters and files are saved as base64 in Firestore.
// If the project moves to the paid plan, upload them to a Firebase Storage bucket instead and save the download link in the same field.

data class PosterUpload(val image: String, val alternativeText: String = "")

enum class FileKind { EPF, ATTACHMENT }

data class FileUpload(
    val kind: FileKind,
    val name: String,
    val mimeType: String,
    val data: String
)

// File or error msg
data class FileResult(val file: FileUpload?, val error: String?)

object EventMedia {

    // reduce the resolution of base64 so we have enough space
    const val MAXIMUM_POSTER_WIDTH = 1080
    const val MAXIMUM_POSTER_HEIGHT = 1920

    // Firestore documents - 1 MB, base64 is bigger, make sure not over storage limit
    const val MAXIMUM_POSTER_BYTES = 600 * 1024
    const val MAXIMUM_FILE_BYTES = 700 * 1024

    const val MAXIMUM_POSTERS = 5
    const val START_QUALITY = 85
    const val QUALITY_STEP = 10
    const val LOWEST_QUALITY = 35

    // true if link, false if base64
    fun isLink(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.startsWith("http://")) {
            return true
        }
        if (trimmed.startsWith("https://")) {
            return true
        }
        return false
    }

    // If event name AI Event, gives AI Event poster
    fun defaultAlternativeText(eventTitle: String, posterNumber: Int): String {
        var title = eventTitle.trim()
        if (title.isEmpty()) {
            title = "Event"
        }
        if (posterNumber <= 1) {
            return "$title poster"
        }
        return "$title poster $posterNumber"
    }

    // Keep image smaller for storage
    fun scaledSize(width: Int, height: Int): Pair<Int, Int> {
        if (width <= 0 || height <= 0) {
            return Pair(0, 0)
        }

        // use scale of side with the smallest scale
        val widthScale = MAXIMUM_POSTER_WIDTH.toDouble() / width
        val heightScale = MAXIMUM_POSTER_HEIGHT.toDouble() / height
        var scale = widthScale
        if (heightScale < scale) {
            scale = heightScale
        }
        if (scale > 1.0) {
            scale = 1.0
        }

        var newWidth = (width * scale).toInt()
        var newHeight = (height * scale).toInt()
        if (newWidth < 1) {
            newWidth = 1
        }
        if (newHeight < 1) {
            newHeight = 1
        }
        return Pair(newWidth, newHeight)
    }

    // Reduce image size and change to base64
    fun posterFromUri(context: Context, uri: Uri, alternativeText: String = ""): PosterUpload? {
        var bitmap: Bitmap? = null
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType == "application/pdf") {
            bitmap = readPdfFirstPage(context, uri)
        } else {
            bitmap = readScaledBitmap(context, uri)
        }
        if (bitmap == null) {
            return null
        }

        var quality = START_QUALITY
        while (quality >= LOWEST_QUALITY) {
            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            val bytes = output.toByteArray()
            if (bytes.size <= MAXIMUM_POSTER_BYTES) {
                val image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                return PosterUpload(image, alternativeText)
            }
            quality -= QUALITY_STEP
        }
        return null
    }

    // Reads a file (EPF or attachment) and turns it into base64
    fun fileFromUri(context: Context, uri: Uri, kind: FileKind): FileResult {
        val resolver = context.contentResolver
        var name = "file"
        var size = -1L

        val cursor = resolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameColumn >= 0) {
                    val fileName = cursor.getString(nameColumn)
                    if (fileName != null) {
                        name = fileName
                    }
                }
                if (sizeColumn >= 0 && !cursor.isNull(sizeColumn)) {
                    size = cursor.getLong(sizeColumn)
                }
            }
            cursor.close()
        }

        val tooLarge = "$name is too large (maximum ${MAXIMUM_FILE_BYTES / 1024} KB)"
        if (size > MAXIMUM_FILE_BYTES) {
            return FileResult(null, tooLarge)
        }

        var bytes: ByteArray? = null
        try {
            val inputStream = resolver.openInputStream(uri)
            if (inputStream != null) {
                bytes = inputStream.readBytes()
                inputStream.close()
            }
        } catch (e: Exception) {
            print("Error: $e")
            bytes = null
        }
        if (bytes == null) {
            return FileResult(null, "Could not read $name")
        }
        if (bytes.size > MAXIMUM_FILE_BYTES) {
            return FileResult(null, tooLarge)
        }

        var mimeType = resolver.getType(uri)
        if (mimeType == null) {
            mimeType = "application/octet-stream"
        }
        val data = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return FileResult(FileUpload(kind, name, mimeType, data), null)
    }

    // Turns base64 text back into a picture
    fun decodeBase64Image(text: String): ImageBitmap? {
        try {
            val bytes = Base64.decode(text, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            return bitmap.asImageBitmap()
        } catch (e: IllegalArgumentException) {
            print("Error: $e")
            return null
        }
    }

    // PDF poster use its first page
    private fun readPdfFirstPage(context: Context, uri: Uri): Bitmap? {
        try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val renderer = PdfRenderer(fileDescriptor)
            if (renderer.pageCount == 0) {
                renderer.close()
                fileDescriptor.close()
                return null
            }

            val page = renderer.openPage(0)
            val widthScale = MAXIMUM_POSTER_WIDTH.toDouble() / page.width
            val heightScale = MAXIMUM_POSTER_HEIGHT.toDouble() / page.height
            var scale = widthScale
            if (heightScale < scale) {
                scale = heightScale
            }
            var width = (page.width * scale).toInt()
            var height = (page.height * scale).toInt()
            if (width < 1) {
                width = 1
            }
            if (height < 1) {
                height = 1
            }

            val bitmap = createBitmap(width, height)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            renderer.close()
            fileDescriptor.close()
            return bitmap
        } catch (e: Exception) {
            print("Error: $e")
            return null
        }
    }

    private fun readScaledBitmap(context: Context, uri: Uri): Bitmap? {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                return ImageDecoder.decodeBitmap(source) { decoder, imageInfo, _ ->
                    val newSize = scaledSize(imageInfo.size.width, imageInfo.size.height)
                    decoder.setTargetSize(newSize.first, newSize.second)
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            }
            return readScaledBitmapOld(context, uri)
        } catch (e: Exception) {
            print("Error: $e")
            return null
        }
    }

    private fun readScaledBitmapOld(context: Context, uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        // read the size
        val sizeOptions = BitmapFactory.Options()
        sizeOptions.inJustDecodeBounds = true
        val sizeStream = resolver.openInputStream(uri) ?: return null
        BitmapFactory.decodeStream(sizeStream, null, sizeOptions)
        sizeStream.close()

        val newSize = scaledSize(sizeOptions.outWidth, sizeOptions.outHeight)
        val width = newSize.first
        val height = newSize.second
        if (width == 0) {
            return null
        }

        // read a smaller copy, and scale it
        var sampleSize = 1
        while (sizeOptions.outWidth / (sampleSize * 2) >= width) {
            sampleSize *= 2
        }
        val readOptions = BitmapFactory.Options()
        readOptions.inSampleSize = sampleSize

        val imageStream = resolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(imageStream, null, readOptions)
        imageStream.close()
        if (bitmap == null) {
            return null
        }
        return bitmap.scale(width, height)
    }
}
