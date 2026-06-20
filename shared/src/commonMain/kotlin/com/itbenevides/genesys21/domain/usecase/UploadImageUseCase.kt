package com.itbenevides.genesys21.domain.usecase

import com.itbenevides.genesys21.domain.repository.PageRepository

/**
 * Use case para realizar o upload de imagens.
 */
class UploadImageUseCase(private val repository: PageRepository) {
    suspend operator fun invoke(
        bytes: ByteArray,
        fileName: String,
        token: String,
    ): Result<String> {
        return repository.uploadImage(bytes, fileName, token)
    }
}
