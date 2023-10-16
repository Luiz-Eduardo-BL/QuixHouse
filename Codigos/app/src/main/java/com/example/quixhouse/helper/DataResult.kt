package com.example.quixhouse.helper

data class DataResult<T>(val status: Status, val data: T?, val message: String?) {
    enum class Status { LOADING, SUCCESS, ERROR }

    companion object {
        fun <T> loading(): DataResult<T> = DataResult(Status.LOADING, null, null)
        fun <T> success(data: T): DataResult<T> = DataResult(Status.SUCCESS, data, null)
        fun <T> error(message: String?): DataResult<T> = DataResult(Status.ERROR, null, message)
    }
}
