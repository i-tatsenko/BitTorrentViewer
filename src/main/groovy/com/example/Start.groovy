package com.example

import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.apache.commons.codec.net.URLCodec

def map = new BencodeDecoder().decode(new File('F:\\Downloads\\[rutracker.org].t5095200.torrent'))
map.info.remove 'pieces'
println map


def url = new HttpUrl.Builder()
.host(map.announce as String)

        .addEncodedQueryParameter("info_hash", new String(new URLCodec().encode(map.info_hash as byte[])))
        .addQueryParameter("peer_id", generatePeerId())
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
println new OkHttpClient().newCall(request).execute().body().toString()


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