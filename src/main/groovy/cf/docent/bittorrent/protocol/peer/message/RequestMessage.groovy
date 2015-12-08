package cf.docent.bittorrent.protocol.peer.message

import cf.docent.bittorrent.protocol.peer.PeerMessage

import java.nio.ByteBuffer

/**
 * Created by docent on 05.12.15.
 */
class RequestMessage implements PeerMessage {

    byte[] bytes

    RequestMessage(int pieceIndex, int offset, int length) {
        def result = ByteBuffer.allocate(3 * 4)
        result.putInt(pieceIndex)
        result.putInt(offset)
        result.putInt(length)
        bytes = result.array()
    }

    @Override
    byte[] getMessageBytes() {
        return bytes
    }

    @Override
    byte getMessageId() {
        return 6
    }
}
