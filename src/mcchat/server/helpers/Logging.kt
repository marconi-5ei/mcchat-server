package mcchat.server.helpers

import java.net.InetAddress

fun InetAddress.stripSlash(): String {
    return this.toString().replace("/", "")
}