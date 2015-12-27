package cf.docent.bittorrent.protocol.peer.message

import cf.docent.bittorrent.protocol.peer.PeerMessage

/**
 * Created by docent on 04.12.15.
 */
class PieceMessage implements PeerMessage {

    byte[] bytes

    def pieceIndex() {
        return readFirstInt()
    }

    def offset() {
        return readInt(1)
    }

    byte[] data() {
        return Arrays.copyOfRange(bytes, 16, bytes.length)
    }

    @Override
    byte[] getMessageBytes() {
        return bytes
    }

    @Override
    byte getMessageId() {
        return 7
    }
}
