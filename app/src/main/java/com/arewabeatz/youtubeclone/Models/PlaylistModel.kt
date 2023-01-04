package com.arewabeatz.youtubeclone.Models

class PlaylistModel {

    var pName: String = ""
    var pDescription :String = ""
    var pThumbnail: String = ""
    var pUid: String = ""
    var playlistId: String = ""

    constructor()

    constructor(pName: String, pDescription: String, pThumbnail: String, pUid: String, playlistId: String) {
        this.pName = pName
        this.pUid = pUid
        this.pDescription = pDescription
        this.pThumbnail = pThumbnail
        this.playlistId = playlistId
    }


}