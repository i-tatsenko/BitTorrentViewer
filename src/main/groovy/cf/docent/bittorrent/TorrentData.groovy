package cf.docent.bittorrent

import cf.docent.bittorrent.protocol.bencode.BString
import cf.docent.bittorrent.protocol.bencode.BencodeDecoder
import cf.docent.bittorrent.protocol.Announce
import cf.docent.bittorrent.protocol.MultipleAnnounce
import cf.docent.bittorrent.protocol.SingleAnnounce
import groovy.transform.PackageScope

/**
 * Created by docent on 17.11.15.
 */
@PackageScope
class TorrentData {

    private static final int PIECE_HASH_BYTES_LENGTH = 20
    def decodedData

    TorrentData(File file, BencodeDecoder bencodeDecoder) {
        decodedData = bencodeDecoder.decode(file)
    }

    Announce getAnnounce() {
        if (decodedData.'announce-list') {
            return new MultipleAnnounce(decodedData.'announce-list')
        }
        return new SingleAnnounce(decodedData.announce)
    }

    def getName() {
        return decodedData.name
    }

    def pieceLength() {
        return decodedData.info.'piece length'
    }

    def getInfo() {
        return decodedData.info
    }

    byte[] getInfoHash() {
        return decodedData['info_hash']
    }

    def propertyMissing(String name) {
        return decodedData[name]
    }

    def totalSize() {
        torrentFiles.collect({it.size}).sum()
    }

    def List<byte[]> pieces(){
        BString pieces = decodedData.info.pieces
        byte[] piecesData = pieces.bytes
        def result = []
        for (int i = 0; i < piecesData.length / PIECE_HASH_BYTES_LENGTH; i++) {
            result << Arrays.copyOfRange(piecesData, i * PIECE_HASH_BYTES_LENGTH, (i + 1) * PIECE_HASH_BYTES_LENGTH)
        }
        return result
    }

    List<TorrentFile> getTorrentFiles() {
        TorrentFile.torrentFilesFromTorrentData(this)
    }
}