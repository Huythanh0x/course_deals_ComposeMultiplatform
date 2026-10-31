package com.thanh0x.coursedeals.data.user.di

import java.io.File

actual fun platformDataStoreFilePath(fileName: String): String {
    val dir = System.getProperty("java.io.tmpdir") ?: "."
    return File(dir, fileName).absolutePath
}
