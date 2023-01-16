package Interface.Display.ClockTools;

import DataBase.Data;
import Interface.Display.ClockAppDisplay;
import Interface.Screens.MainScreen;
import Skills.Calendar.HandleReminders;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class AlarmVBox extends VBox {
    private Label datePickerTxt;
    public DatePicker d;

    //FROM TIME
    private Label timePickerTxt;
    private int hoursTimer = 0; int minutesTimer = 0; int secondsTimer = 0;
    private Label timerTime;
    private HBox plus; HBox minus;

    //TO TIME
    private Label timePickerTxt1;
    private int hoursTimer1 = 0; int minutesTimer1 = 0; int secondsTimer1 = 0;
    private Label timerTime1;
    private HBox plus1; HBox minus1;

    private Label descriptionTxt;
    private TextField description;

    public ColorPicker colorPicker;

    private Button enter;

    private MainScreen mainScreen;
    private boolean isReminder;

    private Timeline timeline;

    private HandleReminders reminders;

    public AlarmVBox(MainScreen mainScreen,boolean isReminder) throws IOException, ParseException {
        this.mainScreen = mainScreen;
        this.isReminder = isReminder;

        reminders = new HandleReminders(mainScreen.calendarDisplay);

        this.timeline = new Timeline();
        if(isReminder){
            setSpacing(6);
        }else{
            setSpacing(19);
        }
        setAlignment(Pos.CENTER);

        createContent();
        if(isReminder){
            setPadding(new Insets(30,0, 0, 0));

            VBox date = new VBox(12);
            date.setAlignment(Pos.CENTER);
            date.getChildren().addAll(datePickerTxt,d);

            timePickerTxt.setTranslateY(27);
            VBox from = new VBox();
            from.getChildren().addAll(plus,timerTime,minus);
            from.setTranslateY(-12);
            from.setScaleX(0.85); from.setScaleY(0.85);

            timePickerTxt1.setTranslateY(-25);
            VBox to = new VBox();
            to.getChildren().addAll(plus1,timerTime1,minus1);
            to.setTranslateY(-67);
            to.setScaleX(0.85); to.setScaleY(0.85);

            VBox box = new VBox(10);
            box.setAlignment(Pos.CENTER);
            box.getChildren().addAll(descriptionTxt,description,colorPicker,enter);
            box.setTranslateY(-75);

            getChildren().addAll(date,timePickerTxt,from,timePickerTxt1,to,box);
        }else{
            setPadding(new Insets(40,0,0,0));
            getChildren().addAll(timePickerTxt,plus,timerTime,minus,descriptionTxt,description,enter);
        }
    }

    private void createContent(){
        datePickerTxt = new Label("Date:");
        datePickerTxt.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        datePickerTxt.setTextFill(ClockAppDisplay.color.darker());
        datePickerTxt.setAlignment(Pos.CENTER);

        // create a date picker
        d = new DatePicker();
        d.setValue(LocalDate.now());
        d.setOnAction(event -> {
            mainScreen.calendarDisplay.centerTo(d.getValue(),LocalTime.parse("12:00"));
        });

        timePickerTxt = new Label();
        if(isReminder){
            timePickerTxt.setText("From:");
        }else{
            timePickerTxt.setText("Time:");
        }
        timePickerTxt.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        timePickerTxt.setTextFill(ClockAppDisplay.color.darker());
        timePickerTxt.setAlignment(Pos.CENTER);

        timerTime = new Label();
        timerTime.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 58));
        timerTime.setTextFill(ClockAppDisplay.color.darker().darker());
        timerTime.setAlignment(Pos.CENTER);

        plus = new HBox(60);
        setPlusButtons(plus,true);
        minus = new HBox(60);
        setMinusButtons(minus,true);
        disablePlusMinus(false, plus, minus);

        timePickerTxt1 = new Label("To:");
        timePickerTxt1.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        timePickerTxt1.setTextFill(ClockAppDisplay.color.darker());
        timePickerTxt1.setAlignment(Pos.CENTER);

        timerTime1 = new Label();
        timerTime1.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 58));
        timerTime1.setTextFill(ClockAppDisplay.color.darker().darker());
        timerTime1.setAlignment(Pos.CENTER);

        plus1 = new HBox(60);
        setPlusButtons(plus1,false);
        minus1 = new HBox(60);
        setMinusButtons(minus1,false);
        disablePlusMinus(false, plus1, minus1);

        setTimerTime();

        descriptionTxt = new Label("Description:");
        descriptionTxt.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        descriptionTxt.setTextFill(ClockAppDisplay.color.darker());
        descriptionTxt.setAlignment(Pos.CENTER);

        description = new TextField();
        description.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        if (!isReminder) {
            description.setMaxSize(700,60);
            description.setMinSize(700,60);
        }else {
            description.setMaxSize(250,40);
            description.setMinSize(250,40);
        }

        colorPicker = new ColorPicker();
        colorPicker.setValue(Color.ORANGE);
        colorPicker.setOnAction((EventHandler) t -> enter.setBackground(new Background(new BackgroundFill(colorPicker.getValue(), new CornerRadii(90,true), Insets.EMPTY))));

        enter = new Button("Enter");
        enter.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(90,true), Insets.EMPTY)));
        designAlarmButton(enter);
        if (!isReminder) {enter.setTranslateY(60);} else {enter.setTranslateY(20);}
        enter.setOnMouseClicked(e-> {
            try {
                if(isReminder){
                    createAlert("",timerTime.getText(),timerTime1.getText(),description.getText(),colorPicker.getValue());
                }else{
                    addAlarm(timerTime.getText(),description.getText());
                }

            } catch (IOException | ParseException ioException) {
                ioException.printStackTrace();
            }
            hoursTimer = 0;
            minutesTimer = 0;
            secondsTimer = 0;
            timerTime.setText(twoDigitString(hoursTimer)+":"+twoDigitString(minutesTimer)+":"+twoDigitString(secondsTimer));
            if(isReminder){
                hoursTimer1 = 0;
                minutesTimer1 = 0;
                secondsTimer1 = 0;
                timerTime1.setText(twoDigitString(hoursTimer1)+":"+twoDigitString(minutesTimer1)+":"+twoDigitString(secondsTimer1));
            }
            description.setText("");
        });
    }

    private void designAlarmButton(Button button) {
        button.setCursor(Cursor.HAND);
        button.setUnderline(true);
        button.setPrefSize(90, 62);
        button.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        button.setTextFill(Color.LIGHTGRAY);
        button.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(90,true), new BorderWidths(2))));
        button.setAlignment(Pos.CENTER);
    }

    public void createAlert(String da,String from,String to,String desc,Color color) throws IOException{
        try{
            String res = getAlreadyOnFile();
            addReminder(res,da,from,to,desc,color);
            if(da.equals("")){
                mainScreen.chat.receiveMessage("Reminder added for the " + d.getValue() + " from " + from + " to " + to + " with description " + desc);
            }else{
                mainScreen.chat.receiveMessage("Reminder added for the " + da + " from " + from + " to " + to + " with description " + desc);
            }
        }catch (ParseException e){
            mainScreen.chat.receiveMessage("Dates must be as yyyy-MM-dd and hours must be as HH:mm:ss");
        }
    }
    private String getAlreadyOnFile() throws IOException {
        String res = "";
        FileReader fr=new FileReader(Data.getRemindersFile());
        int i;
        while((i=fr.read())!=-1)
            res += ((char)i);
        fr.close();
        return res;
    }
    private void addReminder(String res,String da,String from,String to,String desc,Color color) throws ParseException {
        if(da.equals("")){
            //convert date to string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = d.getValue();
            if (date != null) {
                da = (formatter.format(date));
            } else {
                da = LocalDate.now().format(formatter);
            }
        }else{
            //use to get the parser exception
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Date date1 = (Date) formatter.parse(da);

            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            Date dateF = format.parse(from);
            Date dateT = format.parse(to);
        }

        FileWriter writer;
        {
            try {
                writer = new FileWriter(Data.getRemindersFile());
                PrintWriter out = new PrintWriter(writer);
                out.print(res);
                if(desc.isEmpty()||desc.isBlank()){
                    out.print(Data.getUsername() + ";" + da + ";"+ from + ";" +to + ";" + color +  ";" + "\"no description\"" + "\n");
                }else{
                    out.print(Data.getUsername() + ";" + da + ";"+ from + ";" +to + ";" + color+  ";" + desc + "\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(da,dateFormatter);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = LocalTime.parse(timerTime.getText(),timeFormatter);
        LocalTime localTime1 = LocalTime.parse(timerTime1.getText(),timeFormatter);
        mainScreen.calendarDisplay.addReminder(description.getText(),localDate,localTime,localTime1,colorPicker.getValue());
        String today = java.time.LocalDate.now().toString();
        if (da.equals(today)) { //updating calendar shortcut and reminders
            mainScreen.todaysRemindersShortcut.add(localTime +";"+ localTime1 +";"+ description.getText());
            reminders.displayReminderAtTime(timerTime.getText(), description.getText());
        }
        mainScreen.chat.receiveMessage("Reminder on the " + da + " from " + timerTime.getText() + " to " + timerTime1.getText() + " with description \"" + description.getText() + "\" has been added");
    }

    public void addAlarm(String time,String desc) throws ParseException {
        try{
            displayAlarmAtTime(time,desc);
            mainScreen.chat.receiveMessage("Today, there will be the alarm at " + time + " with description \"" + desc + "\"");
        }catch (ParseException e){
            mainScreen.chat.receiveMessage("Hour must be as HH:mm:ss");
        }
    }

    public void displayAlarmAtTime(String time,String desc) throws ParseException {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(getTimeDiffInSec(time)), event -> ClockAppDisplay.notifyUser("alarm",time,desc));
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

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

    private void disablePlusMinus(boolean b, HBox plus, HBox minus) {
        for(int i = 0; i<3; i++) {
            plus.getChildren().get(i).setDisable(b);
            minus.getChildren().get(i).setDisable(b);
        }
    }

    private void setPlusButtons(HBox plus,boolean isFrom) {
        if(isReminder){
            if(isFrom){
                Button plusH = new Button("+");
                designPlusMinusButton(plusH);
                plusH.setOnAction(e -> {
                    if (hoursTimer==23) {hoursTimer=0;} else {hoursTimer++;} setTimerTime();
                });

                Button plusM = new Button("+");
                designPlusMinusButton(plusM);
                plusM.setOnAction(e -> {
                    if (minutesTimer==59) {minutesTimer=0;} else {minutesTimer++;} setTimerTime();
                });

                Button plusS = new Button("+");
                designPlusMinusButton(plusS);
                plusS.setOnAction(e -> {
                    if (secondsTimer==59) {secondsTimer=0;} else {secondsTimer++;} setTimerTime();
                });

                plus.setAlignment(Pos.CENTER);
                plus.setTranslateY(28);
                plus.getChildren().addAll(plusH, plusM, plusS);
            }else{
                Button plusH1 = new Button("+");
                designPlusMinusButton(plusH1);
                plusH1.setOnAction(e -> {
                    if (hoursTimer1==23) {hoursTimer1=0;} else {hoursTimer1++;} setTimerTime();
                });

                Button plusM1 = new Button("+");
                designPlusMinusButton(plusM1);
                plusM1.setOnAction(e -> {
                    if (minutesTimer1==59) {minutesTimer1=0;} else {minutesTimer1++;} setTimerTime();
                });

                Button plusS1 = new Button("+");
                designPlusMinusButton(plusS1);
                plusS1.setOnAction(e -> {
                    if (secondsTimer1==59) {secondsTimer1=0;} else {secondsTimer1++;} setTimerTime();
                });

                plus.setAlignment(Pos.CENTER);
                plus.setTranslateY(28);
                plus.getChildren().addAll(plusH1, plusM1, plusS1);
            }
        }else{
            Button plusH = new Button("+");
            designPlusMinusButton(plusH);
            plusH.setOnAction(e -> {
                if (hoursTimer==23) {hoursTimer=0;} else {hoursTimer++;} setTimerTime();
            });

            Button plusM = new Button("+");
            designPlusMinusButton(plusM);
            plusM.setOnAction(e -> {
                if (minutesTimer==59) {minutesTimer=0;} else {minutesTimer++;} setTimerTime();
            });

            Button plusS = new Button("+");
            designPlusMinusButton(plusS);
            plusS.setOnAction(e -> {
                if (secondsTimer==59) {secondsTimer=0;} else {secondsTimer++;} setTimerTime();
            });

            plus.setAlignment(Pos.CENTER);
            plus.setTranslateY(28);
            plus.getChildren().addAll(plusH, plusM, plusS);
        }
    }

    private void setMinusButtons(HBox minus,boolean isFrom) {
        if(isReminder){
            if(isFrom){
                Button minusH = new Button("_");
                designPlusMinusButton(minusH);
                minusH.setOnAction(e -> {if(hoursTimer==0) {hoursTimer=23;} else {hoursTimer--;} setTimerTime();});

                Button minusM = new Button("_");
                designPlusMinusButton(minusM);
                minusM.setOnAction(e -> {if (minutesTimer==0) {minutesTimer=59;} else {minutesTimer--;} setTimerTime();});

                Button minusS = new Button("_");
                designPlusMinusButton(minusS);
                minusS.setOnAction(e -> {if (secondsTimer==0) {secondsTimer=59;} else {secondsTimer--;} setTimerTime();});

                minus.setAlignment(Pos.CENTER);
                minus.setTranslateY(-31);
                minus.getChildren().addAll(minusH, minusM, minusS);
            }else{
                Button minusH1 = new Button("_");
                designPlusMinusButton(minusH1);
                minusH1.setOnAction(e -> {if(hoursTimer1==0) {hoursTimer1=23;} else {hoursTimer1--;} setTimerTime();});

                Button minusM1 = new Button("_");
                designPlusMinusButton(minusM1);
                minusM1.setOnAction(e -> {if (minutesTimer1==0) {minutesTimer1=59;} else {minutesTimer1--;} setTimerTime();});

                Button minusS1 = new Button("_");
                designPlusMinusButton(minusS1);
                minusS1.setOnAction(e -> {if (secondsTimer1==0) {secondsTimer1=59;} else {secondsTimer1--;} setTimerTime();});

                minus.setAlignment(Pos.CENTER);
                minus.setTranslateY(-31);
                minus.getChildren().addAll(minusH1, minusM1, minusS1);
            }
        }else{
            Button minusH = new Button("_");
            designPlusMinusButton(minusH);
            minusH.setOnAction(e -> {if(hoursTimer==0) {hoursTimer=23;} else {hoursTimer--;} setTimerTime();});

            Button minusM = new Button("_");
            designPlusMinusButton(minusM);
            minusM.setOnAction(e -> {if (minutesTimer==0) {minutesTimer=59;} else {minutesTimer--;} setTimerTime();});

            Button minusS = new Button("_");
            designPlusMinusButton(minusS);
            minusS.setOnAction(e -> {if (secondsTimer==0) {secondsTimer=59;} else {secondsTimer--;} setTimerTime();});

            minus.setAlignment(Pos.CENTER);
            minus.setTranslateY(-31);
            minus.getChildren().addAll(minusH, minusM, minusS);
        }
    }

    private void designPlusMinusButton(Button button) {
        if (isReminder && button.getText().equals("+")) { button.setTranslateY(-7);}
        button.setCursor(Cursor.HAND);
        button.setBackground(Background.EMPTY);
        button.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 28));
        button.setTextFill(Color.DARKVIOLET.darker().darker());
        button.setBorder(null);
        button.setAlignment(Pos.CENTER);
    }

    private void setTimerTime() {
        timerTime.setText(twoDigitString(hoursTimer)+":"+twoDigitString(minutesTimer)+":"+twoDigitString(secondsTimer));
        if(isReminder){
            timerTime1.setText(twoDigitString(hoursTimer1)+":"+twoDigitString(minutesTimer1)+":"+twoDigitString(secondsTimer1));
        }
    }

    private String twoDigitString(long number) {
        if (number == 0) { return "00"; }
        if (number / 10 == 0) { return "0" + number; }
        return String.valueOf(number);
    }
}
