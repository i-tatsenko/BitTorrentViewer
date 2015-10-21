package com.example

import org.apache.commons.codec.net.URLCodec

def map = new BencodeDecoder().decode(new File('D:\\Castle.S08E01.rus.LostFilm.TV.avi (2).torrent'))
map.info.remove 'pieces'
println map

def params = ""
use(Util) {
    params = params.appendProperty("info_hash", map.info_hash)
            .appendProperty("peer_id", generatePeerId())
            .appendProperty("key", generateKey())
            .appendProperty("port", "21412")
            .appendProperty("no_peer_id", '1')
            .appendProperty("uploaded", "0")
            .appendProperty("downloaded", "0")
            .appendProperty("left", map.info.length as String)
            .appendProperty("event", "started")
}
def response = (map.announce + "?$params" as String).toURL().bytes
println "response: " + new String(response, 'ASCII')
def decoded = new BencodeDecoder().decode(response)
decoded.remove('peers')
println decoded


def generateKey() {
    def random = new Random()
    def nums = 'ABCDEF1234567890'
    def result = ''
    1..9.each { result += nums[random.nextInt(nums.length())] }
    return result
}

def generatePeerId() {
    def result = new byte[13]
    new Random().nextBytes(result)
    '-BT7950' + new String(new URLCodec().encode(result))
}