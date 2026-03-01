package com.itbenevides.genesys21

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

class GenesysTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        // CORREÇÃO: Usa TestApplication para isolar o ambiente e evitar crashes
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}
