package com.itbenevides.genesys21.data.service

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
            val zipFile = File(backupFolder, "genesys21_backup_$timestamp.zip")

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

            cleanOldBackups()
        } catch (e: Exception) {
            logger.error("BACKUP: Falha ao realizar backup ZIP: ${e.message}", e)
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
