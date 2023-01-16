package Skills.Calendar;

import DataBase.Data;
import Interface.Display.CalendarDisplay;
import Interface.Display.ClockAppDisplay;
import Interface.Screens.MainScreen;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class HandleReminders {

    public ArrayList<String> todaysRemindersShortcut = new ArrayList<>();
    private ArrayList<String[]> alarmsTime;
    private Timeline timeline;
    private CalendarDisplay calendarDisplay;

    public HandleReminders(CalendarDisplay calendarDisplay) throws IOException, ParseException {
        this.calendarDisplay = calendarDisplay;
        //list of all alarms of today
        alarmsTime = new ArrayList<>();
        //contains all today alarms that are notified today
        timeline = new Timeline();
    }

    /**
     * method used to prepare the reminders in a certain interval of time
     * @param firstDate of the interval
     * @param lastDate of the interval
     * @throws IOException
     * @throws ParseException
     */
    public void prepareReminders(LocalDate firstDate, LocalDate lastDate) throws IOException, ParseException {
        String allReminders = getAlreadyOnFile();
        int nbrOfInfo = 5;
        int counter = 0;
        String username = "";
        String day = "";
        String time = "";
        String time1 = "";
        String color = "";
        String desc = "";
        int linesNbrChar = 0;
        for (int i = 0; i < allReminders.length(); i++) {
            if(allReminders.charAt(i)==';'&&counter<nbrOfInfo){
                if(counter==0){
                    int counter1 = linesNbrChar;
                    while(counter1<i){
                        username+=allReminders.charAt(counter1++);
                    }
                    //System.out.println("username = " + username);
                }else if(counter==1){
                    int counter1 = linesNbrChar+username.length()+1;
                    while(counter1<i){
                        day+=allReminders.charAt(counter1++);
                    }
                    //System.out.println("day = " + day);
                }else if(counter==2){
                    int counter1 = linesNbrChar+username.length() + day.length() +2;
                    while(counter1<i){
                        time+=allReminders.charAt(counter1++);
                    }
                    //System.out.println("time = " + time);
                }else if(counter==3){
                    int counter1 = linesNbrChar+username.length() + day.length()+time.length() +3;
                    while(counter1<i){
                        time1+=allReminders.charAt(counter1++);
                    }
                    //System.out.println("time1 = " + time);
                }else if(counter==4){
                    int counter1 = linesNbrChar+username.length() + day.length()+time.length()+time1.length() +4;
                    while(counter1<i){
                        color+=allReminders.charAt(counter1++);
                    }
                    //System.out.println("color  = " + color);
                }
                counter++;
            }
            if(allReminders.charAt(i)=='\n'&&counter==nbrOfInfo){
                int counter1 = linesNbrChar+username.length()+day.length()+time.length()+time1.length()+color.length()+nbrOfInfo;
                while(allReminders.charAt(counter1)!='\n'){
                    desc+=allReminders.charAt(counter1);
                    counter1++;
                }

                if(username.equals(Data.getUsername())){
                    //notify user if a reminder is for today
                    String today = java.time.LocalDate.now().toString();
                    if(day.equals(today)){
                        todaysRemindersShortcut.add(time.substring(0,5) +";"+ time1.substring(0,5) +";"+ desc);
                        alarmsTime.add(new String[]{time,desc});
                        displayReminderAtTime(time,desc);
                    }

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate localDate = LocalDate.parse(day,dateFormatter);
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    LocalTime localTime = LocalTime.parse(time,timeFormatter);
                    LocalTime localTime1 = LocalTime.parse(time1,timeFormatter);
                    //add any reminder in the calendar
                    if(localDate.isAfter(firstDate.minusDays(1)) && localDate.isBefore(lastDate.plusDays(1))) {
                        calendarDisplay.addReminder(desc, localDate, localTime, localTime1, Color.valueOf(color));
                    }
                }

                linesNbrChar=i+1;
                counter = 0;
                username = "";
                day = "";
                time = "";
                time1 = "";
                color = "";
                desc = "";
            }
        }
    }

    /**
     * get the content of the reminder.txt file
     * @return
     * @throws IOException
     */
    private String getAlreadyOnFile() throws IOException {
        String res = "";
        FileReader fr=new FileReader(Data.getRemindersFile());
        int i;
        while((i=fr.read())!=-1)
            res += ((char)i);
        fr.close();
        return res;
    }

    /**
     * display all reminders of today
     * @param time
     * @param desc
     * @throws ParseException
     */
    public void displayReminderAtTime(String time, String desc) throws ParseException {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(getTimeDiffInSec(time)), event -> ClockAppDisplay.notifyUser("reminder",time,desc));
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /**
     * converts time(String) to second
     * @param time
     * @return
     * @throws ParseException
     */
    public int getTimeDiffInSec(String time) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date date1 = format.parse(time);
        Date date2 = format.parse(java.time.LocalTime.now().toString());
        int difference = (int) (date2.getTime() - date1.getTime());
        if(date2.before(date1)){
            return -difference/1000;
        }
        return difference/1000;
    }
}
