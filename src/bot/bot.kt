package top.yumesekai.bot

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PAD
import java.io.File
import java.util.*
import kotlin.collections.ArrayList



class BotManager(){
    companion object {
        var gson= Gson()
        suspend fun init(){

            File("config").apply {
                if(!isDirectory){
                    this.mkdir()
                }
            }

            KWebSocket.instance.connect()
            GlobalEventChannel.subscribeAlways<GroupMessageEvent> { event ->
                // this: GroupMessageEvent
                // event: GroupMessageEvent
                //subject.sendMessage("Hello!")
                print(event.message)
            }
            GlobalEventChannel.subscribeAlways<FriendMessageEvent> { event ->
                // this: GroupMessageEvent
                // event: GroupMessageEvent
                val sd= setSD(event,null,"FriendMessageEvent",null)
                KWebSocket.instance.sendJson(gson.toJson(sd))
            }
             GlobalEventChannel.subscribeAlways<MemberJoinEvent> { event ->
                jsonData(event)
            }

            //禁言事件
            GlobalEventChannel.subscribeAlways<MemberMuteEvent> { event ->
                jsonData(event)
            }
            //撤回事件
            GlobalEventChannel.subscribeAlways<MessageRecallEvent> { event ->
                //jsonData(event)
            }
            //
            GlobalEventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> { event ->
                //jsonData(event)
            }

           qqLoad()
            //print(accounts.size)
            for (account in accounts) {
                qqLogin(account.id, Base64.getDecoder().decode(account.password))
            }

        }

        private fun jsonData(event: GroupMemberEvent){
            val bot = event.bot
            val member= event.member
            val sd= SendData(bot.id,bot.nick, member.nameCardOrNick,1, event.toString(), member.id, event.toString(), event.group.id, member.permission.toString(), ArrayList())
            print(event.toString())
            KWebSocket.instance.sendJson(gson.toJson(sd))
        }


        private suspend fun setSD(c: MessageEvent, group: Long?, event: String, permission: String?): SendData {
            val msgs = ArrayList<Msg>()
            c.message.forEach {
                when (it){
                    is PlainText -> {
                        val r1 = Regex("\\s*")
                        if (!r1.matches(it.toString())) msgs.add(Msg("text", it.toString()))
                    }
                    is Image->{
                        msgs.add(Msg("img", it.queryUrl()))
                    }
                    is At ->{
                        msgs.add(Msg("at", it.target.toString()))
                    }
                    is Face->{
                        msgs.add(Msg("face", it.content))
                    }
                }

            }
            return SendData(c.bot.id, c.bot.nick, c.sender.nameCardOrNick,1,c.message.contentToString(),c.sender.id,event,group, permission,msgs)
        }


        var accounts=ArrayList<Account>()

        var qqPath="config/account.txt"

        /**
         * 从文件中读取账号
         * @see qqPath
         */
        fun qqLoad(): ArrayList<Account> {
            var qqFile=File(qqPath)
            if (!qqFile.exists()) {
                print("no file")
                return accounts
            }
            var json=qqFile.readText()
            //print(json)
            accounts = gson.fromJson(json, object : TypeToken<ArrayList<Account>>() {}.type)
            return accounts
        }

        fun qqSave(){
            File(qqPath).writeText(gson.toJson(accounts))
        }


        /**
         * 密码登陆
         */
        suspend fun qqLogin(qq:Long, password:String): Bot? {
            val bot = BotFactory.newBot(qq, password) {
                fileBasedDeviceInfo("config/device.json") // 使用 device.json 存储设备信息
                protocol = ANDROID_PAD // 切换协议
            }.alsoLogin()
            //登陆成功后添加到列表
            val passwordMd5= Base64.getEncoder().encodeToString(md5(password))
            if(accounts.none { it.id == qq }){
                accounts.add(Account(qq, passwordMd5))
            }
            return bot
        }

        /**
         * 密码登陆 MD5 登陆
         */
        suspend fun qqLogin(qq:Long,passwordMd5: ByteArray ): Bot? {
            return BotFactory.newBot(qq, passwordMd5) {
                fileBasedDeviceInfo("config/device.json") // 使用 device.json 存储设备信息
                protocol = ANDROID_PAD // 切换协议
            }.alsoLogin()
        }

        fun closeBot(botId: Long){
            Bot.findInstance(botId)?.close()
        }
    }
}


