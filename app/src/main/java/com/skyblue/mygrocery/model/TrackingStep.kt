package com.skyblue.mygrocery.model

data class TrackingStep(
    val title: String,
    val time: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean // The step currently happening
)