package com.example.expensetracker.data.backup

import android.content.Context
import com.example.expensetracker.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun backup(outputStream: OutputStream) {
        val databasePath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val databaseFiles = listOf(
            databasePath,
            File("${databasePath.path}-wal"),
            File("${databasePath.path}-shm"),
        ).filter { it.exists() }

        val preferencesDir = File(context.filesDir.parentFile, DATASTORE_DIR_NAME)
        val preferenceFiles = preferencesDir.listFiles()?.filter { it.isFile }.orEmpty()

        ZipOutputStream(outputStream).use { zip ->
            databaseFiles.forEach { file ->
                zip.putNextEntry(ZipEntry("$DATABASE_DIR_NAME/${file.name}"))
                file.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }

            preferenceFiles.forEach { file ->
                zip.putNextEntry(ZipEntry("$DATASTORE_DIR_NAME/${file.name}"))
                file.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    fun restore(inputStream: InputStream) {
        val databaseDir = context.getDatabasePath(AppDatabase.DATABASE_NAME).parentFile
            ?: error("Database directory is missing")
        val dataStoreDir = File(context.filesDir.parentFile, DATASTORE_DIR_NAME)

        databaseDir.mkdirs()
        dataStoreDir.mkdirs()

        clearTargets(
            databaseDir = databaseDir,
            dataStoreDir = dataStoreDir,
        )

        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    when {
                        entry.name.startsWith("$DATABASE_DIR_NAME/") -> {
                            val target = File(databaseDir, entry.name.removePrefix("$DATABASE_DIR_NAME/"))
                            target.outputStream().use { output -> zip.copyTo(output) }
                        }

                        entry.name.startsWith("$DATASTORE_DIR_NAME/") -> {
                            val target = File(dataStoreDir, entry.name.removePrefix("$DATASTORE_DIR_NAME/"))
                            target.outputStream().use { output -> zip.copyTo(output) }
                        }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
    }

    private fun clearTargets(
        databaseDir: File,
        dataStoreDir: File,
    ) {
        databaseDir.listFiles()
            ?.filter { it.name.startsWith(AppDatabase.DATABASE_NAME) }
            ?.forEach { it.delete() }

        dataStoreDir.listFiles()
            ?.forEach { it.delete() }
    }

    companion object {
        private const val DATABASE_DIR_NAME = "database"
        private const val DATASTORE_DIR_NAME = "datastore"
    }
}
