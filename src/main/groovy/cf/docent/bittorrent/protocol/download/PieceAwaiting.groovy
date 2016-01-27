package cf.docent.bittorrent.protocol.download

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicLong

class PieceAwaiting {
    final CompletableFuture<byte[]> promise = new CompletableFuture<>()
    final AtomicLong chunksCount
    final byte[] data

    public PieceAwaiting(long chunksCount, long pieceLength) {
        data = new byte[pieceLength]
        this.chunksCount = new AtomicLong(chunksCount)
    }

    def boolean completeChunk(int offset, byte[] data) {
        chunksCount.decrementAndGet()
        synchronized (data) {
            System.arraycopy(data, 0, this.data, offset, (int) data.length)
            if (chunksCount.get() == 0) {
                promise.complete(this.data)
            }
        }
        return promise.done
    }
}
