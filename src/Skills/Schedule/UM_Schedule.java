package Skills.Schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class UM_Schedule{

    private Scanner file_reader;
    private String schedule_text;
    private ArrayList<Course> courses = new ArrayList<>();

    /**
     * Gets the .ics file from Maastricht University Timetable
     * @param UM_file (.ics at url: https://timetable.maastrichtuniversity.nl/ical?6034e60d&group=false&eu=STYyMjE4NDQ=&h=Msp_x9ez0v2UDuVhKbtJ82wTla65FcnvbVxh-lPS3DM=)
     */
    public UM_Schedule(String UM_file)
    {
        try{URL UM_Url = new URL(UM_file);
            BufferedReader file_reader = new BufferedReader(new InputStreamReader(UM_Url.openStream()));

            while (file_reader.readLine() != null)
            {
                schedule_text = file_reader.readLine();
                if(schedule_text.equals("BEGIN:VEVENT"))
                {
                    file_reader.readLine();
                    String start_DateTime = file_reader.readLine();
                    String end_DateTime = file_reader.readLine();
                    String summary = file_reader.readLine();
                    String location;

                    schedule_text = file_reader.readLine();
                    if(schedule_text.startsWith("LOCATION"))
                    {
                        location = schedule_text;
                    }
                    else
                    {
                        summary = summary + schedule_text;
                        location = file_reader.readLine();
                    }

                    Course course = new Course(start_DateTime,end_DateTime,summary,location);
                    courses.add(course);
                }
            }
            file_reader.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //List with every course from the file
    public ArrayList<Course> getCourses()
    {
        return courses;
    }
}
