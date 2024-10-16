package dev.evanchang.somnia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.evanchang.somnia.ui.scaffold.MainScaffold
import dev.evanchang.somnia.ui.theme.SomniaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            SomniaTheme {
                MainScaffold()
            }
        }
    }
}