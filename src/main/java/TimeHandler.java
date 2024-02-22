import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class TimeHandler {

    private final Integer currentYear;
    private final Integer currentMonth;
    private final Integer currentDayOfMonth;
    private final LocalDate today;
    private final LocalDate nextTuesday;
    private final LocalDate nextFriday;

    public TimeHandler() {

        this.today = LocalDate.now();
        DateTimeFormatter myFormatCurrentYear = DateTimeFormatter.ofPattern("yyyy");
        DateTimeFormatter myFormatCurrentDayOfMonth = DateTimeFormatter.ofPattern("dd");
        DateTimeFormatter myFormatCurrentMonth = DateTimeFormatter.ofPattern("MM");

        this.currentYear = Integer.valueOf(this.today.format(myFormatCurrentYear));
        this.currentMonth = Integer.valueOf(this.today.format(myFormatCurrentMonth));
        this.currentDayOfMonth = Integer.valueOf(this.today.format(myFormatCurrentDayOfMonth));

        this.nextTuesday = setNextDay(DayOfWeek.TUESDAY);
        this.nextFriday = setNextDay(DayOfWeek.FRIDAY);
    }

    public Integer getCurrentYear() {
        return currentYear;
    }

    public Integer getCurrentMonth() {
        return currentMonth;
    }

    public Integer getCurrentDayOfMonth() {
        return currentDayOfMonth;
    }

    public LocalDate getNextTuesday() {
        return nextTuesday;
    }

    public LocalDate getNextFriday() {
        return nextFriday;
    }

    public String getTodayTrello() {
        return trelloDateAdjuster(this.currentYear + "-" + addZeroIfLessThanTen(this.currentMonth) + "-" + addZeroIfLessThanTen(this.currentDayOfMonth));
    }

    private String trelloDateAdjuster(String date){
        return date+"T12:00:00.00-06:00";
    }

    private String addZeroIfLessThanTen(int unit) {

        if (unit < 10)
            return "0" + unit;
        else
            return "" + unit;
    }

    private LocalDate setNextDay(DayOfWeek day) {

        LocalDate nextDay;
        int leadDays = 4;

        nextDay = this.today.with(TemporalAdjusters.next(day));

        DateTimeFormatter nextDayDay = DateTimeFormatter.ofPattern("dd");

        DateTimeFormatter nextDayMonth = DateTimeFormatter.ofPattern("MM");

        if (Integer.valueOf(nextDay.format(nextDayMonth)).equals(this.currentMonth)) {

            switch (day) {
                case TUESDAY: {
                    leadDays = 5;
                }
                case FRIDAY: {
                    leadDays = 3;
                }
            }
            if (Integer.valueOf(nextDay.format(nextDayDay)) < this.currentDayOfMonth + leadDays) {
                nextDay = LocalDate.of(this.currentYear, this.currentMonth, this.currentDayOfMonth + leadDays);
                nextDay = nextDay.with(TemporalAdjusters.next(day));
            }
        }

        DateTimeFormatter formatShortDate = DateTimeFormatter.ofPattern("MM/dd");

        return nextDay;
    }
}
