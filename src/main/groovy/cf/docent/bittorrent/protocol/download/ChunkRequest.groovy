package cf.docent.bittorrent.protocol.download

import cf.docent.bittorrent.data.DataChunk

import java.time.LocalTime

class ChunkRequest {
    DataChunk chunk
    LocalTime requestSentTime
}
