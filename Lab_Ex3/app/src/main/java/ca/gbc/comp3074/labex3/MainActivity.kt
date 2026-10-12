package ca.gbc.comp3074.labex3
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// --------------------------------------------------
// MainActivity: Entry point of the Android app
// --------------------------------------------------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                MessageListScreen()
            }
        }
    }
}


// --------------------------------------------------
// Message data class
// Stores the sender's name and message text
// --------------------------------------------------

data class Message(
    val sender: String,
    val text: String
)


// --------------------------------------------------
// Sample messages
// Repeat the four messages three times
// --------------------------------------------------

val messages = List(3) {
    listOf(
        Message("Joe", "Hi!"),
        Message("Jim", "How are you?"),
        Message("Joe", "Test..1..2...3"),
        Message("Joe", "I hate coding!!!")
    )
}.flatten()


// --------------------------------------------------
// Main screen
// LazyColumn creates a vertically scrollable list
// --------------------------------------------------

@Composable
fun MessageListScreen() {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8FF))
            .statusBarsPadding()
            .navigationBarsPadding(),

        contentPadding = PaddingValues(
            start = 28.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 8.dp
        ),

        verticalArrangement = Arrangement.spacedBy(18.dp)

    ) {

        items(messages) { message ->
            MessageItem(message)
        }
    }
}


// --------------------------------------------------
// Custom Composable for one message
// --------------------------------------------------

@Composable
fun MessageItem(message: Message) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        // Display circular Android avatar
        AndroidAvatar()

        // Space between avatar and message
        Spacer(
            modifier = Modifier.width(8.dp)
        )

        // Sender name and message bubble
        Column(
            modifier = Modifier.padding(top = 2.dp)
        ) {

            // Sender name
            Text(
                text = message.sender,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF676176)
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            // Rounded message bubble
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFFFFAFF),

                border = BorderStroke(
                    width = 0.7.dp,
                    color = Color(0xFFEFE8F1)
                ),

                shadowElevation = 1.dp

            ) {

                Text(
                    text = message.text,
                    fontSize = 17.sp,
                    color = Color(0xFF25222A),

                    modifier = Modifier.padding(
                        horizontal = 7.dp,
                        vertical = 6.dp
                    )
                )
            }
        }
    }
}


// --------------------------------------------------
// Custom Android avatar
// Circular image with red border
// --------------------------------------------------

@Composable
fun AndroidAvatar() {

    Box(
        modifier = Modifier
            .size(50.dp)
            .border(
                width = 1.5.dp,
                color = Color.Red,
                shape = CircleShape
            )
            .clip(CircleShape),

        contentAlignment = Alignment.Center

    ) {

        Canvas(
            modifier = Modifier.size(40.dp)
        ) {

            val w = size.width
            val h = size.height


            // --------------------------------------
            // Grey diagonal shadow
            // --------------------------------------

            val shadow = Path().apply {

                moveTo(w * 0.26f, h * 0.52f)
                lineTo(w * 0.66f, h * 0.92f)
                lineTo(w * 0.91f, h * 0.92f)
                lineTo(w * 0.73f, h * 0.52f)

                close()
            }

            drawPath(
                path = shadow,
                color = Color(0xFFDDD9DF)
            )


            // --------------------------------------
            // Android antennae
            // --------------------------------------

            drawLine(
                color = Color.White,
                start = Offset(w * 0.37f, h * 0.23f),
                end = Offset(w * 0.43f, h * 0.33f),
                strokeWidth = 2.dp.toPx()
            )

            drawLine(
                color = Color.White,
                start = Offset(w * 0.63f, h * 0.23f),
                end = Offset(w * 0.57f, h * 0.33f),
                strokeWidth = 2.dp.toPx()
            )


            // --------------------------------------
            // White Android head
            // --------------------------------------

            val head = Path().apply {

                moveTo(w * 0.26f, h * 0.52f)

                cubicTo(
                    w * 0.26f, h * 0.32f,
                    w * 0.38f, h * 0.27f,
                    w * 0.50f, h * 0.27f
                )

                cubicTo(
                    w * 0.62f, h * 0.27f,
                    w * 0.74f, h * 0.32f,
                    w * 0.74f, h * 0.52f
                )

                close()
            }

            drawPath(
                path = head,
                color = Color.White
            )


            // --------------------------------------
            // Left eye
            // --------------------------------------

            drawCircle(
                color = Color(0xFFD6D2D9),
                radius = w * 0.025f,

                center = Offset(
                    w * 0.40f,
                    h * 0.42f
                )
            )


            // --------------------------------------
            // Right eye
            // --------------------------------------

            drawCircle(
                color = Color(0xFFD6D2D9),
                radius = w * 0.025f,

                center = Offset(
                    w * 0.60f,
                    h * 0.42f
                )
            )
        }
    }
}


// --------------------------------------------------
// Android Studio Preview
// --------------------------------------------------

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun MessageListPreview() {

    MaterialTheme {
        MessageListScreen()
    }
}