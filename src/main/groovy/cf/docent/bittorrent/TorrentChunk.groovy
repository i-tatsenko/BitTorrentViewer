package cf.docent.bittorrent

import groovy.transform.Immutable

/**
 * Created by docent on 17.11.15.
 */
@Immutable
class TorrentChunk {

    long offset
    long size
    byte[] data
}
