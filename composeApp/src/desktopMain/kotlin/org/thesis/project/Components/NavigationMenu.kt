import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.thesis.project.Model.NiftiView

@Composable
fun navigationButtons(navController: NavController, selected: String) {
    Row(
        modifier = Modifier.height(height = 70.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { navController.navigate("upload") },
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "upload") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White            // White text
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("1. Upload", textAlign = TextAlign.Center)
        }
        Button(
            onClick = { navController.navigate("modelSelect") },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "modelSelect") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("2. Model Select", textAlign = TextAlign.Center)
        }
        Button(
            onClick = { navController.navigate("main") },
            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (selected == "main") Color(0xFF0050A0) else Color(0xFF0050A0), // Blue background
                contentColor = Color.White            // White text
            ),
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("3. Result", textAlign = TextAlign.Center)
        }
    }
}
