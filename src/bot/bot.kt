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
            connect()
            var listener= GlobalEventChannel.subscribeAlways<GroupMessageEvent> { event ->
                // this: GroupMessageEvent
                // event: GroupMessageEvent
                //subject.sendMessage("Hello!")
                print(event.message)
            }
            var listener2= GlobalEventChannel.subscribeAlways<FriendMessageEvent> { event ->
                // this: GroupMessageEvent
                // event: GroupMessageEvent
                var sd= setSD(this,null,"FriendMessageEvent",null)
                sendJson(gson.toJson(sd))
            }
            var listener3=   GlobalEventChannel.subscribeAlways<MemberJoinEvent> { event ->
                jsonData(event)
            }

            //禁言事件
            var listener4=   GlobalEventChannel.subscribeAlways<MemberMuteEvent> { event ->
                jsonData(event)
            }
            //撤回事件
            var listener5=   GlobalEventChannel.subscribeAlways<MessageRecallEvent> { event ->
                //jsonData(event)
            }
            //
            var listener6=   GlobalEventChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> { event ->
                //jsonData(event)
            }

           qqLoad()
            //print(accounts.size)
            for (account in accounts) {
                qqLogin(account.id, Base64.getDecoder().decode(account.password))
            }

        }

        private fun jsonData(event: GroupMemberEvent){
            var bot = event.bot
            var member= event.member
            var sd= SendData(bot.id,bot.nick, member.nameCardOrNick,1, event.toString(), member.id, event.toString(), event.group.id, member.permission.toString(), ArrayList())
            print(event.toString())
            sendJson(gson.toJson(sd))
        }


        private suspend fun setSD(c: MessageEvent, group: Long?, event: String, permission: String?): SendData {
            var msgs = ArrayList<Msg>()
            c.message.forEach {
                when (it){
                    is PlainText -> {
                        var r1 = Regex("\\s*")
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

        var qqPath="account.txt"

        /**
         * 从文件中读取账号
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
            var bot = BotFactory.newBot(qq, password) {
                fileBasedDeviceInfo("device.json") // 使用 device.json 存储设备信息
                protocol = ANDROID_PAD // 切换协议
            }.alsoLogin()
            //登陆成功后添加到列表
            var passwordMd5= Base64.getEncoder().encodeToString(md5(password))
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
                fileBasedDeviceInfo("device.json") // 使用 device.json 存储设备信息
                protocol = ANDROID_PAD // 切换协议
            }.alsoLogin()
        }

        fun closeBot(botId: Long){
            Bot.findInstance(botId)?.close()
        }
    }
}


