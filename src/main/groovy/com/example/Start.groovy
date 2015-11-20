package com.example

import cf.docent.bittorrent.Util
import cf.docent.bittorrent.protocol.bencode.SimpleBencodeDecoder
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import groovyx.gpars.GParsPool
import org.apache.commons.codec.net.URLCodec

//def map = new SimpleBencodeDecoder().decode(new File('D:\\Castle.S08E01.rus.LostFilm.TV.avi (2).torrent'))
//def map = new SimpleBencodeDecoder().decode(new File('D:\\[rutracker.org].t5094912.torrent'))
def map = new SimpleBencodeDecoder().decode(new File('D:\\The.Originals.S03E01.rus.LostFilm.TV.avi (1).torrent'))
map.info.remove 'pieces'
println map

def peerID = generatePeerId()
def url = Util.announceToBuilder(map.announce)
        .addEncodedQueryParameter("info_hash", new String(new URLCodec().encode(map.info_hash as byte[])))
        .addQueryParameter("peer_id", peerID)
        .addQueryParameter("key", generateKey())
        .addQueryParameter("port", "21412")
        .addQueryParameter("no_peer_id", '1')
        .addQueryParameter("uploaded", "0")
        .addQueryParameter("downloaded", "0")
        .addQueryParameter("left", map.info.length as String)
        .addQueryParameter("event", "started")
        .build()

def request = new Request.Builder()
        .url(url)
        .get()
        .build()
println "Asking tracker about peers. URI: ${request.toString()}"
def response = new OkHttpClient().newCall(request).execute()
println "Response from tracker, code: ${response.code()}"
def decodedResponse = new SimpleBencodeDecoder().decode(response.body().bytes())
List peers = decodedResponse.peers.toNetDestinations()
println "Server said us about ${peers.size()} peers"
GParsPool.withPool 20, {
    peers.eachParallel {
        def ipAndPort = it.split(':')
        def socketAddress = new InetSocketAddress(ipAndPort[0], ipAndPort[1] as int)

        def socket = new Socket()
        try {
            socket.connect(socketAddress, 10_000)
            println "Connected to $it!!"
            def out = new DataOutputStream(socket.outputStream)
            out.write(19)
            out.write("BitTorrent protocol".bytes)
            out.write(new byte[8])
            out.write(map.info_hash as byte[])
            out.write(peerID.bytes)
            out.writeInt(1)
            out.write(2)
out.flush()
            def inStream = socket.inputStream
            int read = 0
            byte[] buffer = new byte[100]
            while ((read = inStream.read(buffer)) != -1) {
                println "$it sent us: " + buffer
            }
            println "Closing socket with ${it}"
            socket.close()
        } catch (Exception e) {
//            println "Can't connect to $it due to ${e.message}"
        }
    }
}


def generateKey() {
    return Util.randomString(9)
}

def generatePeerId() {
    def peerId = Util.randomString(20)
    println "Peer id $peerId length ${peerId.length()}"
    return peerId.toString()
}