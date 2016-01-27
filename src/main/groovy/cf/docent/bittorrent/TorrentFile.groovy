package cf.docent.bittorrent

import cf.docent.bittorrent.data.DataChunk
import cf.docent.bittorrent.data.Piece

class TorrentFile {

    long size
    long downloaded
    String fileName
    String md5sum
    List<DataChunk> dataChunks

    static List<TorrentFile> torrentFilesFromTorrentData(TorrentData torrentData) {
        if (torrentData.hasProperty("files")) {
            return extractMultipleFiles(torrentData)
        }
        def info = torrentData.info
        def dataChunks = torrentData.getPieces().collect { new DataChunk(it, 0, torrentData.pieceLength) }
        return [new TorrentFile(
                fileName: info.name,
                downloaded: 0,
                size: info.length,
                md5sum: info.md5sum,
                dataChunks: dataChunks)]
    }

    private static List<TorrentFile> extractMultipleFiles(TorrentData torrentData) {
        def pieces = torrentData.pieces
        Piece currentPiece = pieces.remove(0)
        long currentOffset = 0
        torrentData.info.files.collect {
            String fileName = it.path.join("/")
            long fileLengthLeft = it.length
            def fileDataChunks = []

            def dataLeftInPiece = torrentData.pieceLength - currentOffset
            while(fileLengthLeft > dataLeftInPiece) {
                fileDataChunks << new DataChunk(currentPiece, currentOffset, torrentData.pieceLength)
                currentOffset = 0
                currentPiece = pieces.remove(0)
                fileLengthLeft -= dataLeftInPiece
            }
            if (fileLengthLeft > 0) {
                fileDataChunks << new DataChunk(currentPiece, currentOffset, fileLengthLeft)
                currentOffset += fileLengthLeft
            }

            new TorrentFile(fileName: fileName, downloaded: 0, size: it.length, md5sum: it.md5sum, dataChunks: fileDataChunks)
        }
    }


    @Override
    String toString() {
        "File: $fileName size: $size ready: $downloaded dataChunks: $dataChunks"
    }
}
