package Skills.Schedule;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Skill_Schedule {

    private String um_url = "https://timetable.maastrichtuniversity.nl/ical?6034e60d&group=false&eu=STYyMjE4NDQ=&h=Msp_x9ez0v2UDuVhKbtJ82wTla65FcnvbVxh-lPS3DM=";
    private ArrayList<Course> courses;
    private UM_Schedule schedule;
    private String today_date;
    private String today_time;
    private int weekday;
    private int month;
    private int year;

    /**
     * This class is used to classify the events/lectures from the UM_Schedule file, it reads/ranks/sort
     * the Courses from the UM_Schedule ArrayList
     */
    public Skill_Schedule()
    {
        schedule = new UM_Schedule(um_url);
        courses = schedule.getCourses();

        LocalDateTime todayDateTime = LocalDateTime.now();
        weekday = todayDateTime.getDayOfWeek().getValue();
        month = todayDateTime.getMonthValue();
        year = todayDateTime.getYear();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String todayFormattedDateTime = todayDateTime.format(formatter);
        String[] split_today_DateTime = todayFormattedDateTime.split("T");
        today_date = split_today_DateTime[0].replaceAll("-", "");
        if(split_today_DateTime.length > 1)
        {
            today_time = split_today_DateTime[1].replaceAll(":", "");
            today_time = today_time.substring(0,today_time.length()-8);
        }
    }

    /**
     * Finds the next course in the UM_Schedule array, first by checking the date and then by checking the time. If
     * there a multiple courses in the same day finds the nearest to the actual time.
     * @return a String containing the next course
     */
    public String getNextCourse()
    {
        Course next_Course = null;
        int i = 0;

        while (i < courses.size() && next_Course == null)
        {
            if(courses.get(i).getDate().compareTo(today_date) == 0)
            {
                if(courses.get(i).getStart_Time().compareTo(today_time) >= 0)
                {
                    next_Course = courses.get(i);
                }
            }
            if(courses.get(i).getDate().compareTo(today_date) > 0)
            {
                next_Course = courses.get(i);
            }
            i++;
        }

        if(next_Course != null)
        {
            return next_Course.getCourse();
        }
        else
        {
            return "There is no lecture.";
        }
    }

    /**
     * Finds the course(s) on a specific date
     * @param date in format DDMMYYYY
     * @return a String containing every course on the date asked
     */
    public String getCourseOnDate(String date)
    {
        ArrayList<Course> courses_thatDay = new ArrayList<>();
        String onDate_courses = "";

        for(int i = 0; i < courses.size(); i++)
        {
            if(courses.get(i).getDate().equals(date))
            {
                courses_thatDay.add(courses.get(i));
            }
        }

        if(courses_thatDay.isEmpty())
        {
            return "There are no Lecture on that day. Date: "+date;
        }
        else
        {
            for(int j = 0; j < courses_thatDay.size(); j++)
            {
                onDate_courses = onDate_courses + courses_thatDay.get(j).getCourse() + System.lineSeparator();
            }
            return onDate_courses;
        }
    }

    public String getToday()
    {
        return getCourseOnDate(today_date);
    }

    /**
     * Finds every course in the current week
     * @return an Array containing 7 elements (one for each day), these elements contain the courses from one day
     */
    public ArrayList<String> getThisWeek()
    {
        ArrayList<String> this_week = new ArrayList<>();

        int up = weekday;
        int down = weekday-1;

        while(down >= 0)
        {
            this_week.add(getCourseOnDate(dateMinusDate(down)));
            down--;
        }
        int i = 1;
        while(up < 7)
        {
            this_week.add(getCourseOnDate(datePlusDays(i)));
            up++;
            i++;
        }
        return this_week;
    }

    /**
     * Finds every course in the current month
     * @return an Array containing 28-31 elements (one for each day), these elements contain the courses from one day
     */
    public ArrayList<String> getThisMonth()
    {
        ArrayList<String> this_month = new ArrayList<>();
        String this_YearMonth = today_date.substring(0,6);

        for(int i = 0; i < courses.size(); i++)
        {
            if(courses.get(i).getDate().startsWith(this_YearMonth))
            {
                this_month.add(courses.get(i).getCourse());
            }
        }

        return this_month;
    }

    /**
     * Gets a list of courses in an interval of time between start_date and end_date
     * @param start_date
     * @param end_date
     * @return List containing Course class
     */
    public ArrayList<Course> getInInterval(LocalDate start_date, LocalDate end_date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        ArrayList<Course> courses_inInterval = new ArrayList<>();

        for(int i = 0; i < courses.size(); i++)
        {
            String course_date = courses.get(i).getDate();
            LocalDate course_Date = LocalDate.parse(course_date, formatter);
            if(course_Date.isAfter(start_date.minusDays(1)) && course_Date.isBefore(end_date.plusDays(1)))
            {
                courses_inInterval.add(courses.get(i));
            }
        }
        return courses_inInterval;
    }

    /**
     * Gets the actual date and removes a number of days (ex: Wednesday - 2 = Monday)
     * @param minus_days the number of days we want to go back
     * @return the date in format DDMMYYYY
     */
    public String dateMinusDate(int minus_days)
    {
        LocalDateTime todayDateTime = LocalDateTime.now();
        LocalDateTime minus_dateTime = todayDateTime.minusDays(minus_days);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String minus_FormattedDateTime = minus_dateTime.format(formatter);
        String[] split_minus_DateTime = minus_FormattedDateTime.split("T");
        return split_minus_DateTime[0].replaceAll("-", "");
    }

    /**
     * Gets the actual date and adds a number of days (ex: Wednesday + 2 = Friday)
     * @param plus_days the number of days we want to add
     * @return the date in format DDMMYYYY
     */
    public String datePlusDays(int plus_days)
    {
        LocalDateTime todayDateTime = LocalDateTime.now();
        LocalDateTime plus_dateTime = todayDateTime.plusDays(plus_days);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        String minus_FormattedDateTime = plus_dateTime.format(formatter);
        String[] split_plus_DateTime = minus_FormattedDateTime.split("T");
        return split_plus_DateTime[0].replaceAll("-", "");
    }

    /**
     * Transforms LocalDate variable to a standard String containing only the date, without time T
     * @param date_format
     * @return String format of the given date
     */
    public String fromDateToString(LocalDate date_format)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String formatted_String = date_format.format(formatter);
        return formatted_String;
    }
}
