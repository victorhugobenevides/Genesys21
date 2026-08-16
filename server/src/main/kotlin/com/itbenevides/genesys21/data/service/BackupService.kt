package com.itbenevides.genesys21.data.service

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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
            val destination = File(backupFolder, "genesys21_backup_$timestamp.db")

            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
            logger.info("BACKUP: Cópia de segurança criada com sucesso em ${destination.absolutePath}")

            cleanOldBackups()
        } catch (e: Exception) {
            logger.error("BACKUP: Falha ao realizar backup: ${e.message}", e)
        }
    }

    private fun cleanOldBackups() {
        val files = backupFolder.listFiles() ?: return
        if (files.size > 7) { // Mantém apenas os últimos 7 backups
            files.sortedByDescending { it.lastModified() }
                .drop(7)
                .forEach {
                    it.delete()
                    logger.info("BACKUP: Removendo backup antigo: ${it.name}")
                }
        }
    }
}
