package com.version2.tfgpedrollompart2

data class Message(
                   val sender:String,
                   val message:String,
                   val timestamp:Long=System.currentTimeMillis())
