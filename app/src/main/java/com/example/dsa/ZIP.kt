package com.example.dsa

import android.content.Context
import android.net.Uri
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

private const val MODE_WRITE = "w"
private const val MODE_READ = "r"

class ZIP{
    fun zip(zipFile: File, files: List<File>) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { outStream ->
            zip(outStream, files)
        }
    }

    fun zip(context: Context, zipFile: Uri, files: List<File>) {
        context.contentResolver.openFileDescriptor(zipFile, MODE_WRITE).use { descriptor ->
            descriptor?.fileDescriptor?.let {
                ZipOutputStream(BufferedOutputStream(FileOutputStream(it))).use { outStream ->
                    zip(outStream, files)
                }
            }
        }
    }

    private fun zip(outStream: ZipOutputStream, files: List<File>) {
        files.forEach { file ->
            outStream.putNextEntry(ZipEntry(file.name))
            BufferedInputStream(FileInputStream(file)).use { inStream ->
                inStream.copyTo(outStream)
            }
        }
    }

    fun unzip(zipFile: File, location: File) {
        ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { inStream ->
            unzip(inStream, location)
        }
    }

    fun unzip(context: Context, zipFile: Uri, location: File) {
        try {
            context.contentResolver.openInputStream(zipFile)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { inStream ->
                    var zipEntry: ZipEntry?
                    var unzipFile: File
                    var unzipParentDir: File?

                    while (inStream.nextEntry.also { zipEntry = it } != null) {
                        unzipFile = File(location.absolutePath + File.separator + zipEntry!!.name)
                        if (zipEntry!!.isDirectory) {
                            if (!unzipFile.isDirectory) unzipFile.mkdirs()
                        } else {
                            unzipParentDir = unzipFile.parentFile
                            if (unzipParentDir != null && !unzipParentDir.isDirectory) {
                                unzipParentDir.mkdirs()
                            }
                            BufferedOutputStream(FileOutputStream(unzipFile)).use { outStream ->
                                inStream.copyTo(outStream)
                            }
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            // Handle the case where the file descriptor could not be opened
        }
    }

    private fun unzip(inStream: ZipInputStream, location: File) {
        if (location.exists() && !location.isDirectory)
            throw IllegalStateException("Location file must be directory or not exist")

        if (!location.isDirectory) location.mkdirs()

        val locationPath = location.absolutePath.let {
            if (!it.endsWith(File.separator)) "$it${File.separator}"
            else it
        }

        var zipEntry: ZipEntry?
        var unzipFile: File
        var unzipParentDir: File?

        while (inStream.nextEntry.also { zipEntry = it } != null) {
            unzipFile = File(locationPath + zipEntry!!.name)
            if (zipEntry!!.isDirectory) {
                if (!unzipFile.isDirectory) unzipFile.mkdirs()
            } else {
                unzipParentDir = unzipFile.parentFile
                if (unzipParentDir != null && !unzipParentDir.isDirectory) {
                    unzipParentDir.mkdirs()
                }
                BufferedOutputStream(FileOutputStream(unzipFile)).use { outStream ->
                    inStream.copyTo(outStream)
                }
            }
        }
    }
}