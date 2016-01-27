package cf.docent.bittorrent.data

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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Piece piece = (Piece) o

        if (index != piece.index) return false
        if (pieceLength != piece.pieceLength) return false
        if (!Arrays.equals(hash, piece.hash)) return false

        return true
    }

    int hashCode() {
        int result
        result = (int) (index ^ (index >>> 32))
        result = 31 * result + (int) (pieceLength ^ (pieceLength >>> 32))
        result = 31 * result + (hash != null ? Arrays.hashCode(hash) : 0)
        return result
    }
}
