package top.yumesekai.bot

import java.security.MessageDigest

fun md5(text: String): ByteArray {
    //对字符串加密，返回字节数组
    val digest:ByteArray = MessageDigest.getInstance("MD5").digest(text.toByteArray())
    return digest;
}