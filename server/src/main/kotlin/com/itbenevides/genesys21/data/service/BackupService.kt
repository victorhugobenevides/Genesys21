package com.itbenevides.genesys21.data.service

import com.google.firebase.cloud.StorageClient
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*

object BackupService {
    private val logger = LoggerFactory.getLogger(BackupService::class.java)
    private val backupFolder = File("backups")

    fun performBackup(dbPath: String) {
        try {
            if (!backupFolder.exists()) backupFolder.mkdirs()

            val source = File(dbPath)
            if (!source.exists()) {
                logger.warn("BACKUP: Arquivo de banco de dados não encontrado em $dbPath")
                return
            }

            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val zipFileName = "genesys21_backup_$timestamp.zip"
            val zipFile = File(backupFolder, zipFileName)

            FileOutputStream(zipFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    val entry = ZipEntry(source.name)
                    zos.putNextEntry(entry)
                    FileInputStream(source).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }

            logger.info("BACKUP: Cópia de segurança comprimida (ZIP) criada em ${zipFile.absolutePath}")

            // TIER 2: Upload para o Firebase Storage (Off-site)
            uploadToCloud(zipFile)

            cleanOldBackups()
        } catch (e: Exception) {
            logger.error("BACKUP: Falha ao realizar backup ZIP: ${e.message}", e)
        }
    }

    private fun uploadToCloud(file: File) {
        try {
            val bucket = StorageClient.getInstance().bucket()
            if (bucket == null) {
                logger.warn("BACKUP: Firebase Storage não disponível para upload off-site.")
                return
            }

            val blobPath = "backups/${file.name}"
            val blob = bucket.create(blobPath, file.readBytes(), "application/zip")

            logger.info("✅ BACKUP: Upload off-site concluído: ${blob.name}")
        } catch (e: Exception) {
            logger.error("🚨 BACKUP: Falha no upload para a nuvem: ${e.message}")
        }
    }

    private fun cleanOldBackups() {
        val files = backupFolder.listFiles() ?: return
        if (files.size > 30) { // Mantém os últimos 30 dias (Tier 1)
            files.sortedByDescending { it.lastModified() }
                .drop(30)
                .forEach {
                    it.delete()
                    logger.info("BACKUP: Removendo backup antigo: ${it.name}")
                }
        }
    }
}
