package com.example.peh_goapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.peh_goapp.data.model.CategoryModel

@Composable
fun CategoryGrid(
    categories: List<CategoryModel>,
    onCategoryClick: (CategoryModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp), // Menambah padding horizontal
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Baris pertama dengan 4 kategori (Destination, Hotel, Transportation, Culinary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryItem(category = categories[0], onClick = { onCategoryClick(categories[0]) })
            CategoryItem(category = categories[1], onClick = { onCategoryClick(categories[1]) })
            CategoryItem(category = categories[2], onClick = { onCategoryClick(categories[2]) })
            CategoryItem(category = categories[3], onClick = { onCategoryClick(categories[3]) })
        }

        Spacer(modifier = Modifier.height(16.dp)) // Mengurangi jarak antar baris

        // Baris kedua dengan 2 kategori di tengah (Mall, Souvenir)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Gunakan width yang tetap untuk posisi di tengah - jarak lebih proporsional
            Box(modifier = Modifier.width(160.dp)) { // Mengurangi width
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryItem(category = categories[4], onClick = { onCategoryClick(categories[4]) })
                    CategoryItem(category = categories[5], onClick = { onCategoryClick(categories[5]) })
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(55.dp)  // Mengurangi lebar lagi dari 64dp ke 55dp
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ikon kategori - ukuran lebih kecil lagi
        category.iconResId?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = category.name,
                modifier = Modifier.size(35.dp),  // Mengurangi ukuran icon dari 40dp ke 35dp
                tint = Color.Unspecified
            )
        }
        Spacer(modifier = Modifier.height(2.dp))  // Mengurangi jarak dari 4dp menjadi 2dp
        // Teks kategori
        Text(
            text = category.name,
            fontSize = 10.sp,  // Mengurangi ukuran font dari 11sp ke 10sp
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.Black.copy(alpha = 0.7f) // Mengurangi opacity
        )
    }
}