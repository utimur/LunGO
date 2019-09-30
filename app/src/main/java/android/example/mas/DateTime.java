package android.example.mas;

import android.text.format.Time;
import android.util.Log;

import java.util.Date;

public class DateTime {

    static String getDateTime()
    {
        Time now = new Time();
        now.setToNow();
        String timeMinute="";
        if (now.minute < 10)
        {
            timeMinute = "0"+now.minute;

        }
        else timeMinute="" + now.minute;
        String timeHour ="";
        if (now.hour < 10)
        {
            timeHour = "0" + now.hour;
        }
        else timeHour ="" + now.hour;
        String day = "" + now.monthDay;
        if (now.monthDay < 10)
        {
            day = "0" + now.monthDay;
        }
        else day ="" + now.monthDay;
        String month = "" + now.month;
        month = String.valueOf((Integer.parseInt(month ) +1));
        if (Integer.parseInt(month) < 10)
        {
            month = "0" + month;
        }
        String second = "" + now.second;
        if (now.second < 10)
        {
            second = "0" + now.second;
        }
        else second ="" + now.second;
        String date = day  + "." + month + " " + timeHour + ":" + timeMinute + ":" + second;
        return date;
    }

    static String getDate()
    {
        Time now = new Time();
        now.setToNow();
        String day = "" + now.monthDay;
        if (now.monthDay < 10)
        {
            day = "0" + now.monthDay;
        }
        else day ="" + now.monthDay;
        String month = "" + now.month;
        month = String.valueOf((Integer.parseInt(month ) +1));
        if (Integer.parseInt(month) < 10)
        {
            month = "0" + month;
        }
        String date = day + "." + month;
        return date;
    }

     static String getTime()
     {
         Time now = new Time();
         now.setToNow();
         String timeMinute="";
         if (now.minute < 10)
         {
             timeMinute = "0"+now.minute;

         }
         else timeMinute="" + now.minute;
         String timeHour ="";
         if (now.hour < 10)
         {
             timeHour = "0" + now.hour;
         }
         else timeHour ="" + now.hour;
         String second = "" + now.second;
         if (now.second < 10)
         {
             second = "0" + now.second;
         }
         else second ="" + now.second;
         String time = timeHour + "." + timeMinute + "." + second;
         return time;
     }
    static String getTime_Hour_And_Minute(int hour, int minute)
    {

        String timeMinute="";
        if (minute < 10)
        {
            timeMinute = "0"+minute;

        }
        else timeMinute="" + minute;
        String timeHour ="";
        if (hour < 10)
        {
            timeHour = "0" + hour;
        }
        else timeHour ="" + hour;

        return timeHour + ":" + timeMinute;
    }

    public static boolean dateCompare(String s1, String s2)
    {
        // МЕсяц
            String m1 = s1.substring(3, 5);
            String m2 = s2.substring(3, 5);

            // День
            String d1 = s1.substring(0, 2);
            String d2 = s2.substring(0, 2);

            Log.d("TimLog", d1 +"d1");
            Log.d("TimLog", m1 +"m1");

        if (!m1.equals( m2)) {
            return true;
        } else {
            if (!d1.equals( d2)) {
                return true;
            }
        }

        return false;
    }
}
