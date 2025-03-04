package org.thesis.project.Model

import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import javax.imageio.ImageIO
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Decompress a gzipped file and return the uncompressed bytes.
fun decompressGzip(file: File): ByteArray {
    FileInputStream(file).use { fis ->
        GZIPInputStream(fis).use { gzis ->
            return gzis.readBytes()
        }
    }
}

/**
 * Parses a NIfTI-1 file (optionally gzipped) that uses data type 64 (double) and returns a Triple:
 * - Axial images: each image is (width x height)
 * - Coronal images: each image is (width x slices)
 * - Sagittal images: each image is (height x slices)
 */
fun parseNifti(file: File): Triple<List<BufferedImage>, List<BufferedImage>, List<BufferedImage>> {
    // Determine if the file is gzipped and obtain its bytes.
    val isCompressed = file.name.lowercase().endsWith(".nii.gz")
    val bytes: ByteArray = if (isCompressed) decompressGzip(file) else file.readBytes()
    require(bytes.size >= 348) { "File too small to be a valid NIfTI file." }

    // --- Determine Endianness using the header size ---
    val headerBytes = bytes.copyOfRange(0, 348)
    // Read header size as little-endian.
    val headerBufferLE = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
    val headerSizeLE = headerBufferLE.int
    // Reset and read header size as big-endian.
    headerBufferLE.rewind()
    val headerBufferBE = ByteBuffer.wrap(headerBytes).order(ByteOrder.BIG_ENDIAN)
    val headerSizeBE = headerBufferBE.int

    // Determine which byte order yields the correct header size (348).
    val byteOrder = when {
        headerSizeLE == 348 -> ByteOrder.LITTLE_ENDIAN
        headerSizeBE == 348 -> ByteOrder.BIG_ENDIAN
        else -> throw IllegalArgumentException("Invalid header size in both LE ($headerSizeLE) and BE ($headerSizeBE)")
    }
    // Now create a headerBuffer with the correct order.
    val headerBuffer = ByteBuffer.wrap(headerBytes).order(byteOrder)

    // --- Read dimensions ---
    headerBuffer.position(40)
    val dim = ShortArray(8) { headerBuffer.short }
    val width = dim[1].toInt()
    val height = dim[2].toInt()
    val slices = dim[3].toInt()
    println("Width: $width, Height: $height, Slices: $slices")

    // --- Read data type (offset 70) ---
    headerBuffer.position(70)
    val dataType = headerBuffer.short.toInt()
    require(dataType == 64) { "Unsupported data type: $dataType. Expected 64 (double)." }

    // --- Image Data ---
    // The data starts at byte offset 348.
    val dataBytes = bytes.copyOfRange(348, bytes.size)
    val dataBuffer = ByteBuffer.wrap(dataBytes).order(byteOrder)

    val numPixels = width * height * slices
    // For data type 64, each pixel is 8 bytes.
    val expectedDataSize = numPixels * 8
    val actualDataSize = dataBytes.size
    println("Expected data size: $expectedDataSize, Actual data size: $actualDataSize")
    if (expectedDataSize != actualDataSize) {
        println("Warning: Data size mismatch.")
    }

    // Read pixel data as doubles.
    val pixels = DoubleArray(numPixels) { dataBuffer.double }

    // Normalize double pixel values to [0, 255].
    val minValue = pixels.minOrNull() ?: 0.0
    val maxValue = pixels.maxOrNull() ?: 1.0
    fun pixelTo8Bit(value: Double): Int {
        val normalized = if (maxValue != minValue) (value - minValue) / (maxValue - minValue) else 0.0
        return (normalized * 255).toInt().coerceIn(0, 255)
    }

    // --- Create Axial Images ---
    // Each axial image: width x height, one image per slice.
    val axialImages = mutableListOf<BufferedImage>()
    for (slice in 0 until slices) {
        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
        for (j in 0 until height) {
            for (i in 0 until width) {
                val index = slice * width * height + j * width + i
                val value = pixelTo8Bit(pixels[index])
                val rgb = (value shl 16) or (value shl 8) or value
                image.setRGB(i, j, rgb)
            }
        }
        axialImages.add(image)
    }

    // --- Create Coronal Images ---
    // Each coronal image: width x slices, one image per row.
    val coronalImages = mutableListOf<BufferedImage>()
    for (row in 0 until height) {
        val image = BufferedImage(width, slices, BufferedImage.TYPE_BYTE_GRAY)
        for (slice in 0 until slices) {
            for (i in 0 until width) {
                val index = slice * width * height + row * width + i
                val value = pixelTo8Bit(pixels[index])
                val rgb = (value shl 16) or (value shl 8) or value
                image.setRGB(i, slice, rgb)
            }
        }
        coronalImages.add(image)
    }

    // --- Create Sagittal Images ---
    // Each sagittal image: height x slices, one image per column.
    val sagittalImages = mutableListOf<BufferedImage>()
    for (col in 0 until width) {
        val image = BufferedImage(height, slices, BufferedImage.TYPE_BYTE_GRAY)
        for (slice in 0 until slices) {
            for (j in 0 until height) {
                val index = slice * width * height + j * width + col
                val value = pixelTo8Bit(pixels[index])
                val rgb = (value shl 16) or (value shl 8) or value
                image.setRGB(j, slice, rgb)
            }
        }
        sagittalImages.add(image)
    }

    return Triple(axialImages, coronalImages, sagittalImages)
}
