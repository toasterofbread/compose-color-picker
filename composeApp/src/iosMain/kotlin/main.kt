import androidx.compose.ui.window.ComposeUIViewController
import com.godaddy.app.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
