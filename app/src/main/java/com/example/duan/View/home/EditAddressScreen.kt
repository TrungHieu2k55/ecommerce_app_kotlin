package com.example.duan.View.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.duan.Model.model.Address
import com.example.duan.ViewModel.usecase.auth.AddressState
import com.example.duan.ViewModel.usecase.auth.AuthViewModel
import android.widget.Toast
import androidx.compose.ui.draw.clip
import com.example.duan.Model.api.AddressApiClient
import com.example.duan.Model.model.DistrictResponse
import com.example.duan.Model.model.ProvinceResponse
import com.example.duan.Model.model.WardResponse
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val addresses = userProfile?.addresses ?: emptyList()
    var selectedAddress by remember { mutableStateOf(userProfile?.selectedAddress ?: "") }
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var showEditAddressDialog by remember { mutableStateOf<Address?>(null) }
    val context = LocalContext.current

    // Lắng nghe trạng thái địa chỉ
    LaunchedEffect(Unit) {
        authViewModel.addressState.collect { state ->
            when (state) {
                is AddressState.Success -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                is AddressState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shipping Address") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Danh sách địa chỉ
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(addresses) { address ->
                    AddressItem(
                        address = address,
                        isSelected = address.title == selectedAddress,
                        onSelect = {
                            selectedAddress = address.title
                            authViewModel.updateSelectedAddress(address.title)
                        },
                        onEdit = {
                            showEditAddressDialog = address
                        },
                        onDelete = {
                            authViewModel.deleteAddress(address.title)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showAddAddressDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFF4FC3F7),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4FC3F7)
                        )
                    ) {
                        Text(
                            text = "+ Thêm địa chỉ ship mới",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút Apply
            Button(
                onClick = {
                    authViewModel.updateSelectedAddress(selectedAddress)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Apply",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Dialog để thêm địa chỉ mới
    if (showAddAddressDialog) {
        AddAddressDialog(
            onConfirm = { title, province, district, ward, street ->
                val details = "$street, $ward, $district, $province"
                val newAddress = Address(title, details)
                authViewModel.addAddress(newAddress)
                if (addresses.isEmpty()) {
                    selectedAddress = newAddress.title
                }
                showAddAddressDialog = false
            },
            onDismiss = { showAddAddressDialog = false }
        )
    }

    // Dialog để chỉnh sửa địa chỉ
    showEditAddressDialog?.let { address ->
        EditAddressDialog(
            address = address,
            onConfirm = { title, province, district, ward, street ->
                val details = "$street, $ward, $district, $province"
                val updatedAddress = Address(title, details)
                authViewModel.editAddress(address.title, updatedAddress)
                showEditAddressDialog = null
            },
            onDismiss = { showEditAddressDialog = null }
        )
    }
}

@Composable
fun AddressItem(
    address: Address,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onSelect,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF4FC3F7),
                        unselectedColor = Color(0xFF4FC3F7)
                    )
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = address.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = address.details,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color(0xFF4FC3F7)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF4FC3F7)
                    )
                }
            }
        }
    }
}

// Hàm kiểm tra kết nối Internet
fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressDialog(
    onConfirm: (title: String, province: String, district: String, ward: String, street: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var ward by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }

    var provinceExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var wardExpanded by remember { mutableStateOf(false) }

    var isLoadingProvinces by remember { mutableStateOf(false) }
    var isLoadingDistricts by remember { mutableStateOf(false) }
    var isLoadingWards by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var provinces by remember { mutableStateOf<List<ProvinceResponse>>(emptyList()) }
    var districts by remember { mutableStateOf<List<DistrictResponse>>(emptyList()) }
    var wards by remember { mutableStateOf<List<WardResponse>>(emptyList()) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!isNetworkAvailable(context)) {
            errorMessage = "Không có kết nối Internet. Vui lòng kiểm tra lại."
            return@LaunchedEffect
        }
        isLoadingProvinces = true
        errorMessage = null
        try {
            provinces = AddressApiClient.getProvinces()
            if (provinces.isEmpty()) {
                errorMessage = "Không tải được danh sách tỉnh/thành phố. Vui lòng thử lại sau."
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải tỉnh/thành phố: ${e.message}"
        } finally {
            isLoadingProvinces = false
        }
    }

    LaunchedEffect(province) {
        if (province.isNotEmpty()) {
            val provinceCode = provinces.find { it.name == province }?.code
            if (provinceCode != null) {
                isLoadingDistricts = true
                errorMessage = null
                try {
                    districts = AddressApiClient.getDistricts(provinceCode)
                    if (districts.isEmpty()) {
                        errorMessage = "Không tải được danh sách quận/huyện. Vui lòng thử lại sau."
                    }
                } catch (e: Exception) {
                    errorMessage = "Lỗi khi tải quận/huyện: ${e.message}"
                } finally {
                    isLoadingDistricts = false
                }
            }
        } else {
            districts = emptyList()
        }
        ward = ""
    }

    LaunchedEffect(district) {
        if (district.isNotEmpty()) {
            val districtCode = districts.find { it.name == district }?.code
            if (districtCode != null) {
                isLoadingWards = true
                errorMessage = null
                try {
                    wards = AddressApiClient.getWards(districtCode)
                    if (wards.isEmpty()) {
                        errorMessage = "Không tải được danh sách xã/phường. Vui lòng thử lại sau."
                    }
                } catch (e: Exception) {
                    errorMessage = "Lỗi khi tải xã/phường: ${e.message}"
                } finally {
                    isLoadingWards = false
                }
            }
        } else {
            wards = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Address") },
        text = {
            Column {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., Home, Office)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = provinceExpanded,
                    onExpandedChange = { provinceExpanded = !provinceExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (isLoadingProvinces) "Đang tải..." else province,
                        onValueChange = { },
                        label = { Text("Tỉnh/Thành phố") },
                        trailingIcon = {
                            if (isLoadingProvinces) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoadingProvinces,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = provinceExpanded,
                        onDismissRequest = { provinceExpanded = false }
                    ) {
                        if (provinces.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có dữ liệu") },
                                onClick = { provinceExpanded = false }
                            )
                        } else {
                            provinces.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        province = item.name
                                        provinceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = districtExpanded,
                    onExpandedChange = { districtExpanded = !districtExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (isLoadingDistricts) "Đang tải..." else district,
                        onValueChange = { },
                        label = { Text("Quận/Huyện") },
                        trailingIcon = {
                            if (isLoadingDistricts) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        enabled = province.isNotEmpty() && !isLoadingDistricts,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        if (districts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có dữ liệu") },
                                onClick = { districtExpanded = false }
                            )
                        } else {
                            districts.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        district = item.name
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = wardExpanded,
                    onExpandedChange = { wardExpanded = !wardExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (isLoadingWards) "Đang tải..." else ward,
                        onValueChange = { },
                        label = { Text("Xã/Phường/Thị trấn") },
                        trailingIcon = {
                            if (isLoadingWards) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = wardExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        enabled = district.isNotEmpty() && !isLoadingWards,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = wardExpanded,
                        onDismissRequest = { wardExpanded = false }
                    ) {
                        if (wards.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có dữ liệu") },
                                onClick = { wardExpanded = false }
                            )
                        } else {
                            wards.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        ward = item.name
                                        wardExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Tên đường, số nhà") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && province.isNotEmpty() && district.isNotEmpty() && ward.isNotEmpty() && street.isNotEmpty()) {
                        onConfirm(title, province, district, ward, street)
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressDialog(
    address: Address,
    onConfirm: (title: String, province: String, district: String, ward: String, street: String) -> Unit,
    onDismiss: () -> Unit
) {
    // Phân tích địa chỉ hiện tại để điền sẵn vào các trường
    val parts = address.details.split(", ")
    val initialStreet = if (parts.size == 4) parts[0] else ""
    val initialWard = if (parts.size == 4) parts[1] else ""
    val initialDistrict = if (parts.size == 4) parts[2] else ""
    val initialProvince = if (parts.size == 4) parts[3] else ""

    var title by remember { mutableStateOf(address.title) }
    var province by remember { mutableStateOf(initialProvince) }
    var district by remember { mutableStateOf(initialDistrict) }
    var ward by remember { mutableStateOf(initialWard) }
    var street by remember { mutableStateOf(initialStreet) }

    var provinceExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }
    var wardExpanded by remember { mutableStateOf(false) }

    // Trạng thái loading và lỗi
    var isLoadingProvinces by remember { mutableStateOf(false) }
    var isLoadingDistricts by remember { mutableStateOf(false) }
    var isLoadingWards by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Lấy dữ liệu từ API
    var provinces by remember { mutableStateOf<List<ProvinceResponse>>(emptyList()) }
    var districts by remember { mutableStateOf<List<DistrictResponse>>(emptyList()) }
    var wards by remember { mutableStateOf<List<WardResponse>>(emptyList()) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!isNetworkAvailable(context)) {
            errorMessage = "Không có kết nối Internet. Vui lòng kiểm tra lại."
            return@LaunchedEffect
        }
        isLoadingProvinces = true
        errorMessage = null
        try {
            provinces = AddressApiClient.getProvinces()
            if (provinces.isEmpty()) {
                errorMessage = "Không tải được danh sách tỉnh/thành phố"
            }
        } catch (e: Exception) {
            errorMessage = "Lỗi khi tải tỉnh/thành phố: ${e.message}"
        } finally {
            isLoadingProvinces = false
        }
    }

    LaunchedEffect(province) {
        if (province.isNotEmpty()) {
            val provinceCode = provinces.find { it.name == province }?.code
            if (provinceCode != null) {
                isLoadingDistricts = true
                errorMessage = null
                try {
                    districts = AddressApiClient.getDistricts(provinceCode)
                    if (districts.isEmpty()) {
                        errorMessage = "Không tải được danh sách quận/huyện"
                    }
                } catch (e: Exception) {
                    errorMessage = "Lỗi khi tải quận/huyện: ${e.message}"
                } finally {
                    isLoadingDistricts = false
                }
            }
        } else {
            districts = emptyList()
        }
        if (!districts.any { it.name == district }) {
            district = ""
            ward = ""
        }
    }

    LaunchedEffect(district) {
        if (district.isNotEmpty()) {
            val districtCode = districts.find { it.name == district }?.code
            if (districtCode != null) {
                isLoadingWards = true
                errorMessage = null
                try {
                    wards = AddressApiClient.getWards(districtCode)
                    if (wards.isEmpty()) {
                        errorMessage = "Không tải được danh sách xã/phường"
                    }
                } catch (e: Exception) {
                    errorMessage = "Lỗi khi tải xã/phường: ${e.message}"
                } finally {
                    isLoadingWards = false
                }
            }
        } else {
            wards = emptyList()
        }
        if (!wards.any { it.name == ward }) {
            ward = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Address") },
        text = {
            Column {
                // Hiển thị thông báo lỗi nếu có
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., Home, Office)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown Tỉnh/Thành phố
                ExposedDropdownMenuBox(
                    expanded = provinceExpanded,
                    onExpandedChange = { provinceExpanded = !provinceExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (isLoadingProvinces) "Đang tải..." else province,
                        onValueChange = { },
                        label = { Text("Tỉnh/Thành phố") },
                        trailingIcon = {
                            if (isLoadingProvinces) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinceExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        enabled = !isLoadingProvinces,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = provinceExpanded,
                        onDismissRequest = { provinceExpanded = false }
                    ) {
                        if (provinces.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có dữ liệu") },
                                onClick = { provinceExpanded = false }
                            )
                        } else {
                            provinces.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        province = item.name
                                        provinceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown Quận/Huyện
                ExposedDropdownMenuBox(
                    expanded = districtExpanded,
                    onExpandedChange = { districtExpanded = !districtExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (isLoadingDistricts) "Đang tải..." else district,
                        onValueChange = { },
                        label = { Text("Quận/Huyện") },
                        trailingIcon = {
                            if (isLoadingDistricts) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        enabled = province.isNotEmpty() && !isLoadingDistricts,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = districtExpanded,
                        onDismissRequest = { districtExpanded = false }
                    ) {
                        if (districts.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có dữ liệu") },
                                onClick = { districtExpanded = false }
                            )
                        } else {
                            districts.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        district = item.name
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown Xã/Phường
                ExposedDropdownMenuBox(
                    expanded = wardExpanded,
                    onExpandedChange = { wardExpanded = !wardExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (isLoadingWards) "Đang tải..." else ward,
                        onValueChange = { },
                        label = { Text("Xã/Phường/Thị trấn") },
                        trailingIcon = {
                            if (isLoadingWards) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = wardExpanded)
                            }
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        enabled = district.isNotEmpty() && !isLoadingWards,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = wardExpanded,
                        onDismissRequest = { wardExpanded = false }
                    ) {
                        if (wards.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Không có dữ liệu") },
                                onClick = { wardExpanded = false }
                            )
                        } else {
                            wards.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.name) },
                                    onClick = {
                                        ward = item.name
                                        wardExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Tên đường, số nhà") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && province.isNotEmpty() && district.isNotEmpty() && ward.isNotEmpty() && street.isNotEmpty()) {
                        onConfirm(title, province, district, ward, street)
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}