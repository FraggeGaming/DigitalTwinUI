import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun navigationButtons(navController: NavController, selected: String) {
    Row(
        modifier = Modifier.height(height = 70.dp)
            .width(300.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { navController.navigate("upload") },
            shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "upload") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White            // White text
            ),
            modifier = Modifier
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
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "main") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White            // White text
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("Result", textAlign = TextAlign.Center)
        }
    }
}
