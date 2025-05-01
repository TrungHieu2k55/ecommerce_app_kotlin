package com.example.duan.View.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.duan.View.components.BottomNavigationBar

@Composable
fun NotificationsScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                NotificationsList()
            }
        }
    }
}

@Composable
fun NotificationsList() {
    val hasNotifications = remember { mutableStateOf(false) }

    if (hasNotifications.value) {
        Column(modifier = Modifier.fillMaxWidth()) {
            NotificationItem(
                "Gilbert, you placed an order. Check your order history for full details."
            )
            Spacer(modifier = Modifier.height(8.dp))
            NotificationItem(
                "Gilbert, thank you for shopping with us. We have canceled order #53456."
            )
            Spacer(modifier = Modifier.height(8.dp))
            NotificationItem(
                "Gilbert, your order #52458 has been confirmed. Check your order history for full details."
            )
        }
    } else {
        EmptyNotificationsView()
    }
}

@Composable
fun NotificationItem(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notification",
                modifier = Modifier.size(24.dp),
                tint = Color.Yellow
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "No Notifications",
            modifier = Modifier.size(80.dp),
            tint = Color.Yellow
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Notification yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* Navigate to Categories */ },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
        ) {
            Text("Explore Categories", color = Color.White)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewNotificationsContent() {
    MaterialTheme {
        NotificationsScreen(navController = rememberNavController())
    }
}