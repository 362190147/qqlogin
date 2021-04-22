package top.yumesekai.bot

import com.github.salomonbrys.kotson.byString
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.ServiceMessage
import net.mamoe.mirai.message.data.at
import okhttp3.*
import okio.ByteString
import java.net.URL

object BotLog{
    val log:ArrayList<String> = ArrayList()
    var isDebug=true
    fun d(msg:String){
        if(log.size>100){
            for(i in 0..50){
                log.removeFirst()
            }

        }
        log.add(msg)
        println(msg)
    }
}

class KWebSocket() {
    companion object {
        val instance: KWebSocket by lazy {
            KWebSocket()
        }

        private val client by lazy {
            OkHttpClient()
        }
    }


    var isConnected = false
    private var mSocket: WebSocket? = null
    //private var contactMessage: MessageEvent? = null

    private fun init() {
    }

    fun send(text: String) {
        mSocket?.send(text)
    }

    fun sendJson(json: String) {
        if (mSocket!=null) {
            send(json)
            println(json)
        } else {
            connect()
        }
    }

    var url = "ws://localhost:8001"
    private var startTime = 0L
    fun connect() {
        //重连间隔不能少于10秒
        if ((System.currentTimeMillis() - startTime) > 10000) {
            startTime = System.currentTimeMillis()
            val request = Request.Builder().url(url).build()
            client.newWebSocket(request, listener)
            client.dispatcher.executorService.shutdown()
        }
    }


    private val listener: WebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            mSocket = webSocket
            //webSocket.send("hello world")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            mSocket = null
            println("onClosed$reason")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("onClosing$reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            mSocket = null
            println("onFailure")
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            //println("onMessage")
            throw  Exception("aaa")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            BotLog.d("onMessage$text")

            GlobalScope.launch {
                try {
                    val gson = Gson()
                    val rd = gson.fromJson<ReceiveData>(text, object : TypeToken<ReceiveData>() {}.type)
                    //println("test"+rd.qq)
                    val bot: Bot? = Bot.findInstance(rd.bot) ?: return@launch
                    var g: Group? = null
                    //为了避免同时对话造成contactMessage冲突，直接利用qq号和群号获取
                    val subject: Contact?
                    if (rd.group != null) {
                        g = bot?.getGroup(rd.group!!)
                        subject = bot?.getGroup(rd.group!!)
                    } else {
                        subject = bot?.getFriend(rd.qq)
                    }
                    var msg: Message = PlainText("")
                    when (rd.cmd) {
                        "img" -> {
                            if (rd.meta != "") {
                                //msg += getImgByUrl(rd.meta, subject)
                                if (rd.msg != "") {
                                    msg += PlainText(rd.msg)
                                }
                                subject?.sendMessage(msg)//sendMessage(msg)
                            }
                        }
                        "mute" -> {
                            rd.msg?.toLong()?.let { g?.members?.get(it)?.mute(60) }
                        }
                        "song" -> {
                            //var sm: ServiceMessage = net.mamoe.mirai.message.data.buildXmlMessage(1, rd.meta)
                            subject?.sendMessage(PlainText("test"))
                        }
                        "at" -> {
                            if (rd.group != null) {
                                var ats = rd.meta.split(",".toRegex())
                                var members = bot?.getGroup(rd.group!!)?.members
                                ats.forEach() {
                                    println(it)
                                    if (it != "") msg += members?.get(it.toLong())?.at()!!
                                }
                            }
                            subject?.sendMessage(msg + rd.msg)
                        }
                        "chat" -> {
                            BotLog.d(rd.msg)
                            subject?.sendMessage(rd.msg)
                        }
                        "msgs" -> {
                            println(rd.msgs)
                            rd.msgs.forEach() {
                                when (it.type) {
                                    "img" -> {
                                        msg += getImgByUrl(it.msg, subject!!)
                                    }
                                    "text" -> {
                                        msg += PlainText(it.msg)
                                    }
                                    "at" -> {
                                        val members = bot?.getGroup(rd.group!!)?.members
                                        msg += members?.get(it.msg.toLong())?.at()!!
                                    }
                                    "xml" -> {
                                        msg += net.mamoe.mirai.message.data.SimpleServiceMessage(1, it.msg)
                                    }
                                }
                            }
                            subject?.sendMessage(msg)
                        }
                        else->{
                            BotLog.d("cmd:" + rd.cmd)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    /**
    override fun onOpen(webSocket: WebSocket, response: Response) {
    mSocket = webSocket
    isConnected = true
    //webSocket.send("hello world")
    //webSocket.send(ByteString.decodeHex("adef"))
    //webSocket.close(1000, "再见")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    isConnected = false
    println("onClosed$reason")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    println("onClosing$reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    isConnected = false
    println("onFailure")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    println("onMessage$bytes")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
    println("onMessage$text")
    runBlocking {
    try {
    var resultType = object : TypeToken<ReceiveData>() {}.type
    var gson = Gson()
    var rd = gson.fromJson<ReceiveData>(text, resultType)
    //println("test"+rd.qq)
    var bot: Bot? = Bot.findInstance(rd.bot)?: return@runBlocking
    var g: Group? = null
    //为了避免同时对话造成contactMessage冲突，直接利用qq号和群号获取
    var subject: Contact?
    if (rd.group != null) {
    g = bot?.getGroup(rd.group!!)
    subject= bot?.getGroup(rd.group!!)
    } else {
    subject=bot?.getFriend(rd.qq!!)
    }
    var msg: Message = PlainText("")
    when (rd.cmd) {
    "img" -> {
    if (rd.meta != "") {
    //msg += getImgByUrl(rd.meta, subject)
    if (rd.msg != null && rd.msg != "") {
    msg += PlainText(rd.msg)
    }
    subject?.sendMessage(msg)//sendMessage(msg)
    }
    }
    "mute" -> {
    rd.msg?.toLong()?.let { g?.members?.get(it)?.mute(60) }
    }
    "song" -> {
    //var sm: ServiceMessage = net.mamoe.mirai.message.data.buildXmlMessage(1, rd.meta)
    subject?.sendMessage( PlainText("test"))
    }
    "at" -> {
    if (rd.group != null) {
    var ats = rd.meta.split(",".toRegex())
    var members = bot?.getGroup(rd.group!!)?.members
    ats.forEach() {
    println(it)
    if (it != "") msg += members?.get(it.toLong())?.at()!!
    }
    }
    subject?.sendMessage(msg + rd.msg)
    }
    "chat" -> {
    print(rd.msg)
    subject?.sendMessage(rd.msg)
    }
    "msgs" -> {
    println(rd.msgs)
    rd.msgs?.forEach() {
    if (it.type == "img") {
    msg += getImgByUrl(it.msg, subject!!)
    }
    if (it.type == "text") {
    msg += PlainText(it.msg)
    }
    if (it.type == "at") {
    var members = bot?.getGroup(rd.group!!)?.members
    msg += members?.get(it.msg.toLong())?.at()!!
    }
    if (it.type == "xml") {
    msg += net.mamoe.mirai.message.data.SimpleServiceMessage(1, it.msg)
    }
    }
    subject?.sendMessage(msg)
    }
    }
    println("cmd:" + rd.cmd)

    } catch (e: Exception) {
    e.printStackTrace()
    }
    }
    }
     **/
}
