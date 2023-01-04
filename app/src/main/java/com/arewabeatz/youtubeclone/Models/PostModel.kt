package com.arewabeatz.youtubeclone.Models

class PostModel {

    var publisher: String = ""
    var videoId:String = ""
    var videoTitle:String = ""
    var description:String = ""
    var videoUrl:String = ""
    var videoThumbnail:String = ""
    var videoDate:String = ""
    var playlist:String = ""
    var category:String = ""

    constructor()

    constructor(
        publisher: String,
        postId: String,
        postTitle: String,
        description: String,
        videoDate: String,
        videoUrl: String,
        postThumbnail: String,
        playlist: String,
        category: String
    ) {
        this.publisher = publisher
        this.videoId = postId
        this.videoTitle = postTitle
        this.description = description
        this.videoUrl = videoUrl
        this.videoThumbnail = postThumbnail
        this.videoDate = videoDate
        this.playlist = playlist
        this.category = category
    }


}