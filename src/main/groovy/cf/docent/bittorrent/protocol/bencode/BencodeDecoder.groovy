package cf.docent.bittorrent.protocol.bencode

/**
 * Created by docent on 17.11.15.
 */
trait BencodeDecoder {

    abstract def decode(byte[] data);

    def decode(File file) {
        decode file.bytes
    }
}
