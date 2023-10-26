import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimeHandler {

    private int searchSplit = 5; //Mins

    private DateTime dt;
    private String currentYear;
    private String currentMonth;
    private String currentDayOfMonth;


    public TimeHandler(){

        DateTime dtus = new DateTime();
        DateTimeZone dtZone = DateTimeZone.forID("America/Chicago");
        this.dt = dtus.withZone(dtZone);

        LocalDate localDate = LocalDate.now();
        DateTimeFormatter myFormatCurrentYear = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter myFormatCurrentDayOfMonth = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter myFormatCurrentMonth = DateTimeFormatter.ofPattern("yyyy-dd-MM");

        this.currentYear = localDate.format(myFormatCurrentYear);
        this.currentMonth = localDate.format(myFormatCurrentMonth);
        this.currentDayOfMonth = localDate.format(myFormatCurrentDayOfMonth);
    }

    public String getCurrentYear() {
        return currentYear;
    }

    public String getCurrentMonth() {
        return currentMonth;
    }

    public String getCurrentDayOfMonth() {
        return currentDayOfMonth;
    }

    public String getTodayTrello() {
        return trelloDateAdjuster(this.currentYear + "-" + this.currentMonth + "-" + this.currentDayOfMonth);
    }

    private String trelloDateAdjuster(String date){
        int dateHold = Integer.parseInt(date.substring(8, 10));

        if(dateHold+1>31) {
            return date.substring(0, 8) + (Integer.valueOf(date.substring(8, 10)));
        }else{
            dateHold++;
            return date.substring(0, 8) + dateHold;
        }
    }

    private String addZeroIfLessThanTen(int unit){

        if(unit<10)
            return "0" + unit;
        else
            return "" + unit;

    }

}