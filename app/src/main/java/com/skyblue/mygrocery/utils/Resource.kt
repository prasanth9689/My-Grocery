package com.skyblue.mygrocery.utils

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val isNetworkError: Boolean = false) : Resource<Nothing>()
    object Empty : Resource<Nothing>()
    class Loading2<T> : Resource<T>()
}