package com.muizzer07.thunderstormmessenger.helpers

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimeStampManagement{

    constructor()

    fun processTimeStamp(timeStamp: Long):String{
        val spent_time = System.currentTimeMillis() - timeStamp

        val time_in_seconds = TimeUnit.MILLISECONDS.toSeconds(spent_time)
        val time_in_minutes = TimeUnit.MILLISECONDS.toMinutes(spent_time)
        val time_in_hours = TimeUnit.MILLISECONDS.toHours(spent_time)
        val time_in_days = TimeUnit.MILLISECONDS.toDays(spent_time)

        var time_stamp_string: String = ""
        if(time_in_seconds < 60){
            if(time_in_seconds.toInt() == 1){
                time_stamp_string = "a second ago"
            }else{
                time_stamp_string = "" + time_in_seconds + " seconds ago"
            }
        }
        else if(time_in_minutes < 60){
            if(time_in_minutes.toInt() == 1){
                time_stamp_string = "a minute ago"
            }else{
                time_stamp_string = "" + time_in_minutes + " minutes ago"
            }

        }
        else if(time_in_hours < 24){
            if(time_in_hours.toInt() == 1){
                time_stamp_string = "an hour ago"
            }else{
                time_stamp_string = "" + time_in_hours + " hours ago"
            }

        }
        else if(time_in_days < 2){
            if(time_in_days.toInt() == 1){
                time_stamp_string = "a day ago"
            }else{
                time_stamp_string = "" + time_in_days + " days ago"
            }

        }else if(time_in_days < 7){
            val date = Date(timeStamp)
            val format_day = SimpleDateFormat("EEEE")
            val format_date = SimpleDateFormat("dd.MM.yyyy")
            val format_time = SimpleDateFormat("hh:mm a")

            val formatted_day = format_day.format(date)
            val formatted_date = format_date.format(date)
            val formatted_time = format_time.format(date)

            time_stamp_string = "on " + formatted_day + ", "+ formatted_date + " at " + formatted_time
        }else{
            val date = Date(timeStamp)
            val format_date = SimpleDateFormat("dd.MM.yyyy")
            val format_time = SimpleDateFormat("hh:mm a")

            val formatted_date = format_date.format(date)
            val formatted_time = format_time.format(date)

            time_stamp_string = "on " + formatted_date + " at " + formatted_time
        }

        return time_stamp_string
    }
}