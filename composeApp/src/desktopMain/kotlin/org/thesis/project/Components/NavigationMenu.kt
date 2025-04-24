import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.thesis.project.Components.LocalAppColors

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun navigationButtons(navController: NavController, selected: String) {

    var isHoveredUpload by remember { mutableStateOf(false) }
    var isHoveredResult by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.height(height = 70.dp)
            .width(300.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(

            onClick = { navController.navigate("upload") },
            //shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor  = when {
                    selected == "upload" -> LocalAppColors.current.buttonPressedColor.copy(alpha = 0.7f)
                    isHoveredUpload -> LocalAppColors.current.buttonColor.copy(alpha = 0.7f)
                    else -> LocalAppColors.current.buttonColor
                },
                contentColor = LocalAppColors.current.buttonTextColor
            ),
            modifier = Modifier
                .onPointerEvent(PointerEventType.Enter) { isHoveredUpload = true }
                .onPointerEvent(PointerEventType.Exit) { isHoveredUpload = false }
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("Upload", textAlign = TextAlign.Center)
        }


//        Button(
//            onClick = { navController.navigate("modelSelect") },
//            shape = RectangleShape,
//            colors = ButtonDefaults.buttonColors(
//                backgroundColor = if (selected == "modelSelect") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
//                contentColor = Color.White
//            ),
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxHeight()
//        ) {
//            Text("2. Model Select", textAlign = TextAlign.Center)
//        }
        Button(
            onClick = { navController.navigate("main") },
            //shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            shape = RoundedCornerShape(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor  = when {
                    selected == "main" -> LocalAppColors.current.buttonPressedColor.copy(alpha = 0.7f)
                    isHoveredResult -> LocalAppColors.current.buttonColor.copy(alpha = 0.7f)
                    else -> LocalAppColors.current.buttonColor
                },
                contentColor = LocalAppColors.current.buttonTextColor
            ),
            modifier = Modifier
                .onPointerEvent(PointerEventType.Enter) { isHoveredResult = true }
                .onPointerEvent(PointerEventType.Exit) { isHoveredResult = false }
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("Result", textAlign = TextAlign.Center)
        }
    }
}
