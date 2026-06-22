package com.urmyfood.user.data.util

import com.google.gson.JsonParseException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Chuyển exception kỹ thuật thành thông báo lỗi rõ ràng bằng tiếng Việt cho người dùng,
 * tránh hiển thị message thô như "Failed to connect to /10.0.2.2:8080" hay "timeout".
 */
fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException ->
        "Không có kết nối mạng. Vui lòng kiểm tra Wi-Fi hoặc dữ liệu di động rồi thử lại."
    is SocketTimeoutException ->
        "Máy chủ phản hồi quá lâu. Vui lòng thử lại."
    is ConnectException ->
        "Không thể kết nối tới máy chủ. Vui lòng thử lại sau."
    is IOException ->
        "Mất kết nối tới máy chủ. Vui lòng kiểm tra mạng và thử lại."
    is JsonParseException ->
        "Dữ liệu nhận được không hợp lệ. Vui lòng thử lại."
    else ->
        "Đã xảy ra lỗi. Vui lòng thử lại."
}
