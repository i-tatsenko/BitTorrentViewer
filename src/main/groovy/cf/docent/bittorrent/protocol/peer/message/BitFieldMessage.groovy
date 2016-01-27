package cf.docent.bittorrent.protocol.peer.message;

import cf.docent.bittorrent.protocol.peer.PeerMessage;

public class BitFieldMessage implements PeerMessage {

    byte[] bytes

    @Override
    public byte[] getMessageBytes() {
        return bytes
    }

    @Override
    byte getMessageId() {
        return 5
    }
}
