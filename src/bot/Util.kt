package top.yumesekai.bot

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import sun.misc.BASE64Decoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO

fun decodeToImage(imageString: String?): BufferedImage? {
    var image: BufferedImage? = null
    val imageByte: ByteArray
    try {
        val decoder = BASE64Decoder()
        imageByte = decoder.decodeBuffer(imageString)
        val bis = ByteArrayInputStream(imageByte)
        image = ImageIO.read(bis)
        bis.close()
    } catch (e: Exception) {
        e.printStackTrace();
    }
    return image
}


suspend fun getImgByUrl(url: String, subject: Contact): Image {
    val r1 = Regex("data:image/png;base64,")
    var miraiImage:Image;
    if(r1.containsMatchIn(url)) {
        var img = r1.replace(url, "");
        var istream = BASE64Decoder().decodeBuffer(img)
        miraiImage = subject.uploadImage(istream.toExternalResource())
        istream.clone()
    } else{
        var istream = URL(url).openStream()
        miraiImage = subject.uploadImage(istream.toExternalResource())
        istream.close();
    }
    return miraiImage;
}