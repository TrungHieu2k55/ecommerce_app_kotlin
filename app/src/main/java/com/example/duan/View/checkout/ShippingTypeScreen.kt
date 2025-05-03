package com.example.duan.View.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.duan.Model.model.ShippingOption
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShippingTypeScreen(
    navController: NavController,
    onShippingSelected: (ShippingOption) -> Unit
) {
    val currentDate = Calendar.getInstance().apply { time = Date() }
    val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("vi", "VN"))

    val shippingOptions = listOf(
        ShippingOption("1", "Giao hàng tiết kiệm", sdf.format(currentDate.apply { add(Calendar.DAY_OF_YEAR, 5) }.time), 20000.0),
        ShippingOption("2", "Giao hàng nhanh", sdf.format(currentDate.apply { add(Calendar.DAY_OF_YEAR, 3) }.time), 35000.0),
        ShippingOption("3", "Giao hàng hỏa tốc", sdf.format(currentDate.apply { add(Calendar.DAY_OF_YEAR, 1) }.time), 50000.0),
    )

    var selectedOption by remember { mutableStateOf<ShippingOption?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn phương thức vận chuyển") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shippingOptions) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = option }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = option.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Dự kiến giao: ${option.estimatedArrival}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (option.address != null) {
                                Text(text = option.address, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        RadioButton(
                            selected = selectedOption == option,
                            onClick = { selectedOption = option }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    selectedOption?.let { option ->
                        val optionMap = mapOf(
                            "id" to option.id,
                            "name" to option.name,
                            "estimatedArrival" to option.estimatedArrival,
                            "deliveryFee" to option.deliveryFee,
                            "address" to option.address
                        )
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedShippingOption", optionMap)
                        onShippingSelected(option)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Áp dụng", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}