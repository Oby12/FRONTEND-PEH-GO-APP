package com.example.peh_goapp.data.model

data class StatsModel(
    val totalRegisteredUsers: Int,
    val destinationViews: List<DestinationViewsModel>,
    val monthlyLogins: List<MonthlyLoginModel>
)

data class DestinationViewsModel(
    val id: Int,
    val name: String,
    val address: String? = null,
    val coverUrl: String? = null,
    val viewCount: String,
    val categoryName: String? = null
)

data class MonthlyLoginModel(
    val year: Int,
    val month: Int,
    val count: String
)