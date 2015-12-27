package cf.docent.bittorrent.protocol.peer.message

import cf.docent.bittorrent.protocol.peer.PeerMessage

/**
 * Created by docent on 05.12.15.
 */
class UnknownMessage implements PeerMessage {

    private byte[] bytes

    UnknownMessage(byte[] bytes) {

        this.bytes = bytes
    }

    @Override
    byte[] getMessageBytes() {
        return bytes
    }

    @Override
    byte getMessageId() {
        return -1
    }

    @Override
    byte[] serialize() {
        throw new UnsupportedOperationException()
    }
}
