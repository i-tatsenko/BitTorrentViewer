package cf.docent.bittorrent

/**
 * Created by docent on 17.11.15.
 */
interface TorrentChunkListener {

    def onTorrentChunkComplete(TorrentChunk chunk)
}
