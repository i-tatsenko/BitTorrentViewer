package cf.docent.bittorrent.protocol.peer.message;

import cf.docent.bittorrent.protocol.peer.PeerMessage;

/**
 * Created by docent on 04.12.15.
 */
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
