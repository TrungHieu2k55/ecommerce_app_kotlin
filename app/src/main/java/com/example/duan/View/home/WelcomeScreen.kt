package com.example.duan.View.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.duan.R

@Composable
fun WelcomeScreen(
    onStartShopping: () -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val gradientColors = listOf(
        Color(0xFF1A237E),  // Deep indigo
        Color(0xFF3949AB),  // Indigo
        Color(0xFF3F51B5),  // Primary indigo
        Color(0xFF5C6BC0)   // Light indigo
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(colors = gradientColors))
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset((-60).dp, 40.dp)
                .shadow(20.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3F51B5).copy(alpha = 0.7f),
                            Color(0xFF303F9F).copy(alpha = 0.6f)
                        )
                    )
                )
                .zIndex(0f)
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.CenterEnd)
                .offset(80.dp, 20.dp)
                .shadow(20.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF5C6BC0).copy(alpha = 0.8f),
                            Color(0xFF3F51B5).copy(alpha = 0.7f)
                        )
                    )
                )
                .zIndex(0f)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomStart)
                .offset(20.dp, (-40).dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7986CB).copy(alpha = 0.9f),
                            Color(0xFF5C6BC0).copy(alpha = 0.8f)
                        )
                    )
                )
                .zIndex(0f)
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.TopEnd)
                .offset((-30).dp, 80.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7986CB).copy(alpha = 0.9f),
                            Color(0xFF5C6BC0).copy(alpha = 0.8f)
                        )
                    )
                )
                .zIndex(0f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.TopStart)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFE91E63), Color(0xFFC2185B))
                            )
                        )
                ) {
                    Image(
                        painter = painterResource(R.drawable.demo1),
                        contentDescription = "Person Shopping",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Card(
                    modifier = Modifier
                        .padding(start = 100.dp, top = 100.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .zIndex(2f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Shopping",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterEnd)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Image(
                        painter = painterResource(R.drawable.demo2),
                        contentDescription = "Person with mobile device shopping",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Card(
                    modifier = Modifier
                        .padding(end = 40.dp, top = 100.dp)
                        .align(Alignment.CenterEnd)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .zIndex(2f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = "Deals",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Welcome to HKAI",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Your One-Stop Shopping Destination",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Sign in to discover exclusive deals, personalized recommendations, and lightning-fast checkout. New user? Register now for a seamless shopping experience!",
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Button(
                    onClick = onStartShopping, // Gọi callback khi nhấn "Start Shopping"
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Start Shopping",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                TextButton(
                    onClick = onSignInClick, // Gọi callback khi nhấn "Sign In"
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "Already have an account? Sign in",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}