package cf.docent.bittorrent.data

import groovy.transform.CompileStatic

@CompileStatic
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

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        DataChunk dataChunk = (DataChunk) o

        if (length != dataChunk.length) return false
        if (offset != dataChunk.offset) return false
        if (piece != dataChunk.piece) return false

        return true
    }

    int hashCode() {
        int result
        result = (piece != null ? piece.hashCode() : 0)
        result = 31 * result + (int) (offset ^ (offset >>> 32))
        result = 31 * result + (int) (length ^ (length >>> 32))
        return result
    }
}
