package cf.docent.bittorrent.protocol

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
