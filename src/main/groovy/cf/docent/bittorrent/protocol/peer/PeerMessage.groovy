package cf.docent.bittorrent.protocol.peer

import java.nio.ByteBuffer

/**
 * Created by docent on 22.11.15.
 */
trait PeerMessage {

    abstract byte[] getMessageBytes()

    int intFromBytes() {
        ByteBuffer.wrap(messageBytes).getInt()
    }

    @Override
    String toString() {
        "${getClass().simpleName} bytes: [${messageBytes.encodeHex()}]"
    }
}