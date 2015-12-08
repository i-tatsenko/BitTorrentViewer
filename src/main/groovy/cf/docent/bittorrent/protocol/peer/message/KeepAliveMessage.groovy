package cf.docent.bittorrent.protocol.peer.message

import cf.docent.bittorrent.protocol.peer.PeerMessage

/**
 * Created by docent on 01.12.15.
 */
class KeepAliveMessage implements PeerMessage {

    private static final byte[] ZERO_BYTES = new byte[4]

    private static final KeepAliveMessage INSTANCE = new KeepAliveMessage()

    public static PeerMessage getKeepAliveMessage() {
        return INSTANCE
    }

    @Override
    byte[] getMessageBytes() {
        return ZERO_BYTES.clone()
    }

    @Override
    byte getMessageId() {
        return -1
    }

    @Override
    byte[] serialize() {
        return new byte[4]
    }

    public static boolean isKeepAliveMessage(byte[] bytes) {
        (0..3).each {
            if (bytes[it] != 0) {
                return false
            }
        }
        return true
    }
}
