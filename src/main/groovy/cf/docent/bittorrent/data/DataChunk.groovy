package cf.docent.bittorrent.data

class DataChunk {

    final Piece piece
    final long offset
    final long length

    DataChunk(Piece piece, long offset, long length) {
        this.piece = piece
        this.offset = offset
        this.length = length
    }

    @Override
    String toString() {
        "DataChunk{piece: $piece offset: $offset length: $length}"
    }
}
