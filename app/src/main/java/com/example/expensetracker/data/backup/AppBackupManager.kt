package com.example.expensetracker.data.backup

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.expensetracker.BuildConfig
import com.example.expensetracker.data.db.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
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
    private val database: AppDatabase,
) {
    fun backup(outputStream: OutputStream) {
        checkpointDatabase()

        val databasePath = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val databaseFiles = listOf(
            databasePath,
            File("${databasePath.path}-wal"),
            File("${databasePath.path}-shm"),
        ).filter { it.exists() }

        val preferencesDir = File(context.filesDir.parentFile, DATASTORE_DIR_NAME)
        val preferenceFiles = preferencesDir.listFiles()?.filter { it.isFile }.orEmpty()

        ZipOutputStream(outputStream).use { zip ->
            val metadata = JSONObject().apply {
                put("appVersionCode", BuildConfig.VERSION_CODE)
                put("appVersionName", BuildConfig.VERSION_NAME)
                put("dbVersion", AppDatabase.DB_VERSION)
                put("createdAt", System.currentTimeMillis())
            }
            zip.putNextEntry(ZipEntry(METADATA_FILE_NAME))
            zip.write(metadata.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

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

        val zipBytes = inputStream.readBytes()

        validateBackup(zipBytes)
        database.close()

        clearTargets(
            databaseDir = databaseDir,
            dataStoreDir = dataStoreDir,
        )

        ZipInputStream(zipBytes.inputStream()).use { zip ->
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

    private fun validateBackup(zipBytes: ByteArray) {
        var metadataJson: String? = null

        ZipInputStream(zipBytes.inputStream()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == METADATA_FILE_NAME) {
                    metadataJson = zip.readBytes().toString(Charsets.UTF_8)
                    break
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        // 兼容旧版本备份（没有 metadata.json），直接跳过校验
        val json = metadataJson ?: return

        val metadata = JSONObject(json)
        val backupDbVersion = metadata.optInt("dbVersion", -1)

        if (backupDbVersion > AppDatabase.DB_VERSION) {
            throw IncompatibleBackupException(
                backupDbVersion = backupDbVersion,
                currentDbVersion = AppDatabase.DB_VERSION,
            )
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

    private fun checkpointDatabase() {
        database.query(SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)")).close()
    }

    companion object {
        private const val DATABASE_DIR_NAME = "database"
        private const val DATASTORE_DIR_NAME = "datastore"
        private const val METADATA_FILE_NAME = "metadata.json"
    }
}

class IncompatibleBackupException(
    val backupDbVersion: Int,
    val currentDbVersion: Int,
) : RuntimeException(
    "Backup database version ($backupDbVersion) is newer than current ($currentDbVersion). Please update the app.",
)
