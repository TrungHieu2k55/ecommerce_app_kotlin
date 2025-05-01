package com.example.duan.View.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.duan.Model.model.FilterState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBrand by remember { mutableStateOf("ALL") }
    var selectedGender by remember { mutableStateOf("ALL") }
    var selectedSortBy by remember { mutableStateOf("Popular") }
    var priceRange by remember { mutableStateOf(Pair(2.0, 150.0)) }
    var selectedRating by remember { mutableStateOf<Double?>(4.5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter") },
        text = {
            Column {
                // Brands
                Text("Brands", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "Nike", "Adidas", "Puma").forEach { brand ->
                        FilterButton(
                            text = brand,
                            isSelected = selectedBrand == brand,
                            onClick = { selectedBrand = brand }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gender
                Text("Gender", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "Men", "Women").forEach { gender ->
                        FilterButton(
                            text = gender,
                            isSelected = selectedGender == gender,
                            onClick = { selectedGender = gender }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sort By
                Text("Sort by", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Most Recent", "Popular", "Price High").forEach { sort ->
                        FilterButton(
                            text = sort,
                            isSelected = selectedSortBy == sort,
                            onClick = { selectedSortBy = sort }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pricing Range
                Text("Pricing Range", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                RangeSlider(
                    value = priceRange.first.toFloat()..priceRange.second.toFloat(),
                    onValueChange = { range ->
                        priceRange = Pair(range.start.toDouble(), range.endInclusive.toDouble())
                    },
                    valueRange = 2f..150f,
                    steps = 148,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4FC3F7),
                        activeTrackColor = Color(0xFF4FC3F7),
                        inactiveTrackColor = Color.LightGray
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$${priceRange.first.toInt()}")
                    Text("$${priceRange.second.toInt()}+")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reviews
                Text("Reviews", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Column {
                    listOf(
                        Pair("4.5 and above", 4.5),
                        Pair("4.0 - 4.5", 4.0),
                        Pair("3.5 - 4.0", 3.5),
                        Pair("3.0 - 3.5", 3.0),
                        Pair("2.5 - 3.0", 2.5)
                    ).forEach { (label, rating) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedRating == rating,
                                    onClick = {
                                        selectedRating = if (selectedRating == rating) null else rating
                                    }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedRating == rating,
                                onClick = {
                                    selectedRating = if (selectedRating == rating) null else rating
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF4FC3F7),
                                    unselectedColor = Color.Gray
                                )
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(
                        FilterState(
                            brand = selectedBrand,
                            gender = selectedGender,
                            sortBy = selectedSortBy,
                            priceRange = priceRange,
                            minRating = selectedRating
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7),
                    contentColor = Color.White
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    selectedBrand = "ALL"
                    selectedGender = "ALL"
                    selectedSortBy = "Popular"
                    priceRange = Pair(2.0, 150.0)
                    selectedRating = null
                    onApply(
                        FilterState(
                            brand = selectedBrand,
                            gender = selectedGender,
                            sortBy = selectedSortBy,
                            priceRange = priceRange,
                            minRating = selectedRating
                        )
                    )
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF4FC3F7)
                )
            ) {
                Text("Reset Filter")
            }
        }
    )
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4FC3F7) else Color.LightGray,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Text(text, fontSize = 14.sp)
    }
}