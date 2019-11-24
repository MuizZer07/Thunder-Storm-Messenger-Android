package com.muizzer07.thunderstormmessenger.models

class TextMessage(val id: String, val text: String, val fromId: String,  val toId: String, val timeStamp: Long){
    constructor() : this("","","","",0)
}