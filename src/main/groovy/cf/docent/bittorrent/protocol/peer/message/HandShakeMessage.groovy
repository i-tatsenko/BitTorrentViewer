package cf.docent.bittorrent.protocol.peer.message

import cf.docent.bittorrent.protocol.peer.PeerMessage

/**
 * Created by docent on 22.11.15.
 */
class HandShakeMessage implements PeerMessage {

    private static final byte[] protocolBytes = 'BitTorrent protocol'.bytes
    private static final int HANDSHAKE_MESSAGE_LENGTH = 68

    byte[] data = new byte[HANDSHAKE_MESSAGE_LENGTH]

    public HandShakeMessage(byte[] infoHash, byte[] peerId) {
        data[0] = 19
        System.arraycopy(protocolBytes, 0, data, 1, protocolBytes.length)
        System.arraycopy(infoHash, 0, data, 28, 20)
        System.arraycopy(peerId, 0, data, 48, 20)
    }

    public static HandShakeMessage handShakeMessageFromPeerBytes(byte[] bytes) {
        new HandShakeMessage(Arrays.copyOfRange(bytes, 28, 20), Arrays.copyOfRange(bytes, 48, 20))
    }

    @Override
    byte[] getMessageBytes() {
        return data
    }

    static boolean isHandshakeMessage(byte[] bytes) {
        if (bytes.length < 4) {
            throw new IllegalArgumentException("Need at least 4 bytes to determine if message is handshake\n19, $protocolBytes expected" +
                    "\n $bytes received")
        }
        return (bytes[0] == (Byte)19) &&
                bytes[1] == protocolBytes[0] &&
                bytes[2] == protocolBytes[1] &&
                bytes[3] == protocolBytes[2]
    }

    static int messageLength() {
        return HANDSHAKE_MESSAGE_LENGTH
    }

    @Override
    byte[] serialize() {
        return messageBytes
    }

    @Override
    byte getMessageId() {
        return -1
    }

    @Override
    String toString() {
        return "HandshakePeerMessage"
    }
}
