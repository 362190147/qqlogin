package top.yumesekai

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.content.*
import io.ktor.routing.get
import kotlinx.coroutines.launch
import kotlinx.html.dom.document
import net.mamoe.mirai.Bot
import top.yumesekai.bot.Account
import top.yumesekai.bot.BotManager
import java.io.File

var bot: Bot?=null

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    launch {
        BotManager.init()
    }

    install(ContentNegotiation) {
        gson {}
    }

    val client = HttpClient(Apache) {

    }



    routing {
        static {
            //defaultResource("index.html", "web")
            resources("web")
        }

        get("/") {
            //bot?.getFriend(362190147)?.sendMessage("test1");
            var tokenFile=File("token");
            var msg= if(tokenFile.exists()){
                "HELLO WORLD!"
            }else{
                "请登陆"
            }

            call.respondHtml {
                body {
                    h1 { +msg }
                    a("/loginUI"){  +"登陆qq"}
                }
            }
        }

        get("/token") {
            var tokenFile=File("token");
            tokenFile.writeText("test");
        }


        get("/qqLogin"){
            var qqString=call.parameters["qq"];
            var password=call.parameters["password"];
            // 先检测是否已经登陆
            if(qqString==null || password==null) {
                call.respondText("""{"code":0,"msg":“账号和密码都不能为空”}""", ContentType.Application.Json);
                return@get
            }
            var qq= qqString?.toLong() ?: 0
            var bot: Bot? = Bot.getInstanceOrNull(qq)
            var msg= if(bot != null){
                "已经登陆了";
            }else{
                //bot= BotManager.qqLogin(1741546709,"asd8802239");
                bot= BotManager.qqLogin(qq,password);
                BotManager.qqSave();
                "登陆";
            }
            call.respondText("""{"code":0,"msg":$msg}""", ContentType.Application.Json);
        }

        get("/loginUI"){
            // 先检测是否已经登陆
            call.respondHtml {
                body {
                    h1 { +"test" }
                    for(account in BotManager.accounts){
                        div {
                            input( InputType.text, InputFormEncType.applicationXWwwFormUrlEncoded,InputFormMethod.get,"qq"){account.id}
                            input( InputType.text,InputFormEncType.applicationXWwwFormUrlEncoded,InputFormMethod.get,"password")
                            button(name="login"){+"登陆"}
                            script {
                                +"console.log('test')"

                            }
                        }

                    }

                }
            }
        }

        get("/qqlist"){

            // 先检测是否已经登陆
            call.respondHtml {
                body {
                    h1 { +"test" }
                    for(bot in Bot.instances){
                        p{ + ("" + bot.id)}
                    }
                }
            }
        }

        get("/close"){
            var qqString=call.parameters["qq"]
            var msg = "缺少参数"
            if(qqString==null){
                call.respondText { msg }
                return@get
            }

            var bot =Bot.findInstance(qqString.toLong());
            msg=if(bot==null){
                "没有"
            }else{
                bot?.close();
                "退出成功"
            }
            call.respondHtml {
                body {
                    h1 { + msg }
                }
            }
        }



        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
