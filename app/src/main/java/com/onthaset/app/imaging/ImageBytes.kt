package com.onthaset.app.imaging

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import kotlin.math.max

private const val MAX_DIMENSION = 1600
private const val JPEG_QUALITY = 85

/**
 * Reads a content Uri (e.g. from the photo picker), respects EXIF rotation, downscales
 * the long edge to MAX_DIMENSION, and returns JPEG bytes ready for upload.
 */
fun ContentResolver.toCompressedJpeg(uri: Uri): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val sample = sampleSize(bounds.outWidth, bounds.outHeight)

    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    val raw = openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        ?: error("Could not decode image")

    val rotated = applyExifOrientation(uri, raw)
    val scaled = downscaleIfNeeded(rotated)

    val out = ByteArrayOutputStream()
    scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
    if (scaled !== raw) raw.recycle()
    if (rotated !== raw && rotated !== scaled) rotated.recycle()
    scaled.recycle()
    return out.toByteArray()
}

private fun sampleSize(width: Int, height: Int): Int {
    if (width <= 0 || height <= 0) return 1
    var sample = 1
    var w = width
    var h = height
    while (w / 2 >= MAX_DIMENSION && h / 2 >= MAX_DIMENSION) {
        w /= 2; h /= 2; sample *= 2
    }
    return sample
}

private fun ContentResolver.applyExifOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
    val orientation = openInputStream(uri)?.use { ExifInterface(it).getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
    ) } ?: ExifInterface.ORIENTATION_NORMAL

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
        else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun downscaleIfNeeded(bitmap: Bitmap): Bitmap {
    val longest = max(bitmap.width, bitmap.height)
    if (longest <= MAX_DIMENSION) return bitmap
    val scale = MAX_DIMENSION.toFloat() / longest
    val w = (bitmap.width * scale).toInt()
    val h = (bitmap.height * scale).toInt()
    return Bitmap.createScaledBitmap(bitmap, w, h, true)
}
