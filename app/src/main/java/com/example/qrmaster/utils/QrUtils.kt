package com.example.qrmaster.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatWriter
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.common.GlobalHistogramBinarizer
import androidx.camera.core.ImageProxy
import com.google.zxing.MultiFormatReader
import java.nio.ByteBuffer

object QrUtils {
    
    // Generate QR Bitmap
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size
            )
            val w = bitMatrix.width
            val h = bitMatrix.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, w, 0, 0, w, h)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Decode ImageProxy for Scanning
    fun decodeImageProxy(image: ImageProxy): String? {
        val plane = image.planes[0]
        val buffer: ByteBuffer = plane.buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        
        // Setup Luminance Source (supports rotation logic roughly, simplified here)
        // CameraX images are typically YUV_420_888
        val source = PlanarYUVLuminanceSource(
            data,
            image.width,
            image.height,
            0,
            0,
            image.width,
            image.height,
            false
        )
        
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        return try {
            val result = MultiFormatReader().decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            // Try GlobalHistogramBinarizer as fallback
            try {
                val globalBitmap = BinaryBitmap(GlobalHistogramBinarizer(source))
                val result = MultiFormatReader().decode(globalBitmap)
                result.text
            } catch (e2: Exception) {
                null
            }
        }
    }
}