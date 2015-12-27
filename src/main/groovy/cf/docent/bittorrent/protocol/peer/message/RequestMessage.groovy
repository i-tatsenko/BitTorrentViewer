package cf.docent.bittorrent.protocol.peer.message

import cf.docent.bittorrent.protocol.peer.PeerMessage

import java.nio.ByteBuffer

/**
 * Created by docent on 05.12.15.
 */
class RequestMessage implements PeerMessage {

    byte[] bytes

    RequestMessage(long pieceIndex, long offset, long length) {
        def result = ByteBuffer.allocate(12)
        result.putInt(pieceIndex.intValue())
        result.putInt(offset.intValue())
        result.putInt(length.intValue())
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
