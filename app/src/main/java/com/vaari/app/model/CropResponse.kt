package com.vaari.app.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CropResponse(
    val productName: String,
    val totalWater: Double,
    val greenWater: Double,
    val blueWater: Double,
    val greyWater: Double,
    val waterSavingTip: String,
    val irrigationType: String,
    val climate: String,
    val waterScarcity: String,
    val harvestSeason: String,
    val unit: String
) : Parcelable