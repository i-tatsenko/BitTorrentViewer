package cf.docent.bittorrent.protocol

/**
 * Created by docent on 19.11.15.
 */
class SingleAnnounce implements Announce {

    private String announceString

    public SingleAnnounce(String announce) {
        this.announceString = announce
    }

    @Override
    String getAnnounce() {
        return announceString
    }

    @Override
    String getNextAnnounce(String previousAnnounce) {
        return announce
    }

    @Override
    String toString() {
        announce
    }
}
