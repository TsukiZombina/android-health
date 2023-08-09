package com.example.healthapp

class ModelSpecialty {
    var id:String = ""
    var specialty:String = ""
    var timestamp:Long = 0
    var uid:String = ""

    constructor()
    constructor(id:String, specialty: String, timestamp: Long, uid:String) {
        this.id = id
        this.specialty = specialty
        this.timestamp = timestamp
        this.uid = uid
    }
}