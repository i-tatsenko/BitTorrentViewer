package cf.docent.bittorrent.protocol

/**
 * Created by docent on 19.11.15.
 */
interface Announce {

    String getAnnounce();

    String getNextAnnounce(String previousAnnounce)


}
