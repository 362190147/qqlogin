package top.yumesekai.bot

/**
 *
 */
data class Msg(var type: String, var msg: String)

data class Account(var id:Long,var password:String)

data class ReceiveData(
    var bot: Long,
    var cmd: String,
    var msg: String,
    var code: Int,
    var img: String?,
    var qq: Long,
    var group: Long?,
    var at: Long,
    var meta: String,
    var msgs: ArrayList<Msg>,
)

data class SendData(
    var bot: Long,
    var botname: String,
    var nickname: String = "",
    var code: Int = 1,
    var msg: String = "",
    var qq: Long,
    var event: String = "",
    var group: Long? = null,
    var permission: String? = null,
    var msgs:ArrayList<Msg>,
)