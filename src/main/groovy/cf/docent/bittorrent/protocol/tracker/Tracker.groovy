package cf.docent.bittorrent.protocol.tracker

import cf.docent.bittorrent.TorrentData
import cf.docent.bittorrent.conf.Configuration
import cf.docent.bittorrent.protocol.bencode.SimpleBencodeDecoder
import cf.docent.bittorrent.Util
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.apache.commons.codec.net.URLCodec
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Tracker {

    private static final Logger LOGGER = LogManager.getLogger(Tracker)

    private String peerId
    private String keyId
    private Configuration configuration

    Tracker(Configuration configuration) {
        peerId = configuration.peerId
        keyId = configuration.keyId
        this.configuration = configuration
    }

    def requestPeers(TorrentData torrentData) {
        String announceUrl = torrentData.announce.announce
        while (true) {
            try {
                return requestPeers(announceUrl, torrentData)
            } catch (Exception e) {
                LOGGER.error("Some error while obtaining peer list. " + e.message)
                announceUrl = torrentData.announce.getNextAnnounce(announceUrl)
                if (announceUrl == null) {
                    throw new IllegalArgumentException("Can't send request to tracker")
                }
            }
        }

    }

    def requestPeers(String announceUrl, TorrentData torrentData) {
        LOGGER.debug("Request peers for announce url: $announceUrl")
        def request = buildRequestToTracker(announceUrl, torrentData)
        LOGGER.debug "Asking tracker about peers. URI: ${request.toString()}"
        def response = new OkHttpClient().newCall(request).execute()
        LOGGER.debug "Response from tracker, code: ${response.code()}"

        if (!response.isSuccessful()) {
            throw new RuntimeException("Bad response code from tracker")
        }
        def decodedResponse = new SimpleBencodeDecoder().decode(response.body().bytes())

        def peerResponse = new PeerResponse(decodedResponse)
        LOGGER.debug peerResponse.peers()
        return peerResponse
    }

    private  def buildRequestToTracker(String announceUrl, TorrentData torrentData) {
        def url = Util.announceToBuilder(announceUrl)
                .addEncodedQueryParameter("info_hash", new String(new URLCodec().encode(torrentData.infoHash)))
                .addQueryParameter("peer_id", peerId)
                .addQueryParameter("key", keyId)
                .addQueryParameter("port", configuration.listeningPort as String)
                .addQueryParameter("no_peer_id", '1')
                .addQueryParameter("uploaded", "0")
                .addQueryParameter("downloaded", "0")
                .addQueryParameter("left", torrentData.totalSize() as String)
                .addQueryParameter("event", "started")
                .build()

        return new Request.Builder()
                .url(url)
                .get()
                .build()
    }
}
