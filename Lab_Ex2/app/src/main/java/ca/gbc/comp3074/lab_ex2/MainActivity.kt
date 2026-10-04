package ca.gbc.comp3074.lab_ex2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.lab_ex2.ui.theme.Lab_Ex2Theme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            Lab_Ex2Theme()  { 
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    ActionButtons(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtons(modifier: Modifier) {

    val count = remember {
        mutableStateOf(0)
    }

    val step = remember {
        mutableStateOf(1)
    }

    val greenButton = Color(0xFF5E8C61)
    val resetButton = Color(0xFFD96C8C)
    val stepButton = Color(0xFF7FAF9B)
    val backgroundColor = Color(0xFFF6F3F7)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(
            text = count.value.toString(),
            fontSize = 32.sp
        )

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {

            Button(
                onClick = {
                    count.value = count.value - step.value
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = greenButton
                )
            ) {
                Text("-")
            }

            Button(
                onClick = {
                    count.value = count.value + step.value
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = greenButton
                )
            ) {
                Text("+")
            }
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(40.dp)
        ) {

            Button(
                onClick = {
                    count.value = 0
                    step.value = 1
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = resetButton
                )
            ) {
                Text("Reset")
            }

            Button(
                onClick = {
                    step.value = 2
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = stepButton
                )
            ) {
                Text("Step")
            }
        }
    }
}