package cf.docent.bittorrent.protocol.peer

import java.nio.ByteBuffer

/**
 * Created by docent on 22.11.15.
 */
trait PeerMessage {

    abstract byte[] getMessageBytes()

    abstract byte getMessageId()

    int readFirstInt() {
        readInt(0)
    }

    int readInt(int position) {
        ByteBuffer.wrap(messageBytes).getInt(position * Integer.BYTES)
    }

    byte[] serialize() {
        ByteBuffer result = ByteBuffer.allocate(4 + 1 + messageBytes.length)
        result.putInt(messageBytes.length + 1)
        result.put(messageId)
        result.put(messageBytes)
        def array = result.array()
        return array
    }

    @Override
    String toString() {
        "${getClass().simpleName} bytes: [${messageBytes.encodeHex()}]"
    }
}