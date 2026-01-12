package ksnd.webviewplayground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ksnd.webviewplayground.ui.MainScreen
import ksnd.webviewplayground.ui.theme.WebViewPlaygroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebViewPlaygroundTheme {
                MainScreen()
            }
        }
    }
}
