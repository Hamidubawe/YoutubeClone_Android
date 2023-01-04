package com.arewabeatz.youtubeclone.Models

class UserModel {

    var name: String = ""
    var userId:String = ""
    var profilePhoto:String = ""
    var coverPhoto:String = ""
    var email:String = ""
    var token:String = ""
    var about: String = ""
    var verified: Boolean = false
    var interest: String = ""
    var joined: String = ""

    constructor()

    constructor(
        name: String,
        userId: String,
        profilePhoto: String,
        coverPhoto: String,
        email: String,
        token: String,
        about: String,
        verified: Boolean,
        interest: String,
        joined: String
    ) {
        this.name = name
        this.userId = userId
        this.profilePhoto = profilePhoto
        this.coverPhoto = coverPhoto
        this.email = email
        this.token = token
        this.about = about
        this.verified = verified
        this.interest = interest
        this.joined = joined
    }


}