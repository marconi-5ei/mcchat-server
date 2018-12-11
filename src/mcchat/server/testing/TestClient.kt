package mcchat.server.testing

import mcchat.packets.*
import java.io.IOException
import java.net.Socket

fun main(args: Array<String>) {
    try {
        Socket("localhost", 1502).use { socket ->
            val parser = Parser(socket.getInputStream())
            val out = socket.getOutputStream()

            println("Version: ${(parser.next() as InfoPacket).version}")

            out.write(SubscriptionPacket("test").serialize())
            out.write(TopicListRequestPacket().serialize())

            println((parser.next() as TopicListPacket).topics.contentToString())

            out.write(MessagePacket("test", "tester", "testing").serialize())

            println((parser.next() as MessagePacket).run {
                "Message from $username on topic $topic: $message"
            })
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
