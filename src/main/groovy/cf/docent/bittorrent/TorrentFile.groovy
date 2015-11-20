package cf.docent.bittorrent

/**
 * Created by docent on 17.11.15.
 */
class TorrentFile {

    long size
    long downloaded
    String fileName
    String md5sum

    static List<TorrentFile> torrentFilesFromTorrentData(TorrentData torrentData) {
        if (torrentData.hasProperty("files")) {
            return extractMultipleFiles(torrentData)
        }
        def info = torrentData.info

        return [new TorrentFile([fileName: info.name, downloaded: 0, size: info.length, md5sum: info.md5sum])]
    }

    private static List<TorrentFile> extractMultipleFiles(TorrentData torrentData) {
        torrentData.info.files.collect {
            String fileName = it.path.join("/")
            new TorrentFile(fileName: fileName, downloaded: 0, size: it.length, md5sum: it.md5sum)
        }
    }

    @Override
    String toString() {
        "File: $fileName size: $size ready: $downloaded"
    }
}
