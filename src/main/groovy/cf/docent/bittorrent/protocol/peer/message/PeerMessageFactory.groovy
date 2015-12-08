package cf.docent.bittorrent.protocol.peer.message
import cf.docent.bittorrent.protocol.peer.PeerMessage
/**
 * Created by docent on 29.11.15.
 */
class PeerMessageFactory {

    public static final int MESSAGE_LENGTH_HEADER_SIZE = 4

    PeerMessage messageFromBytes(byte messageId, byte[] bytes) {
        switch (messageId) {
            case 0 : return new ChokeMessage()
            case 1 : return new UnchokeMessage()
            case 2 : return new InterestedMessage()
            case 3 : return new NotInterestedMessage()
            case 4 : return new HaveMessage(bytes)
            case 5 : return new BitFieldMessage(bytes: bytes)
            case 7 : return new PieceMessage(bytes: bytes)
        }
        return new UnknownMessage()
    }

}
