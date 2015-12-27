package cf.docent.bittorrent.data

/**
 * Created by docent on 08.12.15.
 */
class Piece {

    final long index
    final long pieceLength
    final byte[] hash

    Piece(long index, long pieceLength, byte[] hash) {
        this.index = index
        this.pieceLength = pieceLength
        this.hash = hash
    }

    @Override
    String toString() {
        "Piece{index: $index}"
    }


}
