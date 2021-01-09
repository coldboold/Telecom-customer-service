package common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static Date parse(String date, String format){

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date parsed = null;
        try {
            parsed = sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parsed;
    }

    public static String format(Date date,String format){

        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String formated = sdf.format(date);
        return formated;
    }
}
