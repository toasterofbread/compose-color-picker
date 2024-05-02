import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.godaddy.app.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow("color-picker-update") {
        App()
    }
}
