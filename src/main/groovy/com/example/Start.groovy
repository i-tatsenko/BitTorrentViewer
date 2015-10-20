package com.example

def map = new BencodeDecoder().decode(new File('F:\\Downloads\\ubuntu-15.04-desktop-amd64.iso.torrent'))
map.info.remove 'pieces'
println map
