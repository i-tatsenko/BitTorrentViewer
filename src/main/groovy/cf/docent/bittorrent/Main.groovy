package cf.docent.bittorrent

import cf.docent.bittorrent.protocol.peer.PeerConnectionStatusListener
import cf.docent.bittorrent.protocol.peer.SeedConnection

/**
 * Created by docent on 18.11.15.
 */

def torrentManager = new TorrentManager()
//def torrent = torrentManager.addTorrent(new File("/Users/docent/Downloads/[rutracker.org].t5116163.torrent"))
//def torrent = torrentManager.addTorrent(new File("/Users/docent/Downloads/ubuntu-15.10-desktop-amd64.iso.torrent"))
def torrent = torrentManager.addTorrent(new File("/Users/docent/Downloads/[rutor.org]3B3P3Ct.2015.D.WEB-DL.720p.mkv.torrent"))
println torrent


torrent.peerResponse.seeds.each {SeedConnection.connect(it, {}, new Object() as PeerConnectionStatusListener)}

