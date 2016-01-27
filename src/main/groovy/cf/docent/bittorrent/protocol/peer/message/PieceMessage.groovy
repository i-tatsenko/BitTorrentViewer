package cf.docent.bittorrent.protocol.peer.message

import cf.docent.bittorrent.protocol.peer.PeerMessage

class PieceMessage implements PeerMessage {

    byte[] bytes

    long pieceIndex() {
        return readFirstInt()
    }

    def offset() {
        return readInt(1)
    }

    byte[] data() {
        return Arrays.copyOfRange(bytes, 8, bytes.length)
    }

    int getDataLength() {
        return bytes.length - 8
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
