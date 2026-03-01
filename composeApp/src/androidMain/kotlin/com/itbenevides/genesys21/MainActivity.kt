package com.itbenevides.genesys21

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.itbenevides.genesys21.di.initKoin
import com.itbenevides.genesys21.di.initialDeepLink
import com.itbenevides.genesys21.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handleDeepLink(intent)

        // CORREÇÃO CRÍTICA PARA TESTES:
        // Só inicializa o Koin se ele AINDA NÃO EXISTIR.
        // Nos testes instrumentados, a TestApplication já iniciou o Koin com Mocks.
        // Se tentarmos iniciar aqui novamente, ou ele ignora (ok) ou tenta carregar módulos reais (erro).
        if (GlobalContext.getOrNull() == null) {
            initKoin {
                androidContext(this@MainActivity)
                modules(viewModelModule)
            }
        }
        
        setContent {
            App(initialDeepLink = initialDeepLink)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.toString()?.let {
            initialDeepLink = it
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
