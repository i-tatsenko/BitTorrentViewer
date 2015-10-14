package com.example

def map = new BencodeDecoder().decode(new File('D:\\The.Originals.S03E01.rus.LostFilm.TV.avi.torrent'))
map.info.remove 'pieces'
println map
