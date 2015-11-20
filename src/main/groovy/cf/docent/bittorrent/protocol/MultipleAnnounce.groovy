package cf.docent.bittorrent.protocol

/**
 * Created by docent on 19.11.15.
 */
class MultipleAnnounce implements Announce {

    Map<Integer, List<String>> announces = [:]
    Map<String, Integer> announceToGroup = [:]

    MultipleAnnounce(def announce) {
        int orderNum = 0
        announce.each {annList->
            def announceList = new ArrayList<String>(annList.collect {it.toString()})
            announceList.each {announceToGroup[it as String] = orderNum}
            announces[orderNum++] = announceList
        }
    }

    @Override
    String getAnnounce() {
        return announces[0].get(0)
    }

    @Override
    String getNextAnnounce(String previousAnnounce) {
        Integer group = announceToGroup.get previousAnnounce
        def prevAnnounceIndex = announces[group].indexOf(previousAnnounce)
        if (prevAnnounceIndex == announces[group].size() - 1) {
            group++
            prevAnnounceIndex = -1
        }
        return announces[group]?.getAt(prevAnnounceIndex + 1)
    }

    @Override
    String toString() {
        return announces
    }
}
