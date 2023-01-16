package Interface.Display;

import Interface.Display.ClockTools.AlarmVBox;
import Interface.Screens.MainScreen;
import Skills.Calendar.HandleReminders;
import Skills.Schedule.Course;
import Skills.Schedule.Skill_Schedule;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.geometry.*;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.paint.Color.LIGHTGRAY;


public class CalendarDisplay extends HBox {
    private final Duration period = Duration.ofMinutes(15);
    private final LocalTime beginningOfTheDay = LocalTime.of(00, 00);
    private final LocalTime endOfTheDay = LocalTime.of(23, 59);
    private final int NBR_OF_DAYS = 100;

    private LocalDate today;
    public LocalDate firstDate;
    public LocalDate lastDate;

    private final List<Slot> slots = new ArrayList<>();
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");
    private MainScreen mainScreen;

    private AlarmVBox alarmVBox;
    private GridPane calendar;
    private ScrollPane scrollPane;
    private ArrayList<Node> fix_days = new ArrayList<>();
    private ArrayList<Node> fix_hours = new ArrayList<>();

    private Skill_Schedule skill_schedule;
    private HandleReminders reminders;

    public CalendarDisplay(MainScreen mainScreen) throws ParseException, IOException {
        this.mainScreen = mainScreen;
        this.skill_schedule = new Skill_Schedule();
        this.reminders = new HandleReminders(this);
        today = LocalDate.now();

        createContent();
        addSchedule(firstDate,lastDate);

        centerTo(today,LocalTime.parse("12:00"));
    }
    public void centerTo(LocalDate date,LocalTime time) {
        if(date.isBefore(firstDate.plusDays(1))){
            int diff = firstDate.getDayOfYear()-date.getDayOfYear()-NBR_OF_DAYS;
            addPreviousCalendar(firstDate.minusDays(diff));
        }else if(date.isAfter(lastDate.minusDays(1))){
            int diff = date.getDayOfYear()-lastDate.getDayOfYear()+NBR_OF_DAYS;
            addAfterCalendar(lastDate.plusDays(diff));
        }

        int[] inTable = convertToTable(date,time,time);
        Node node = getNodeByRowColumnIndex(inTable[1],inTable[0]);
        centerNodeInScrollPane(scrollPane,node.getParent());
    }

    private void createContent() throws IOException, ParseException {
        calendar = new GridPane();
        calendar.setGridLinesVisible(true);
        calendar.setStyle("-fx-background-color:#3d3d3d;");

        firstDate = today.minusDays(NBR_OF_DAYS/2);
        lastDate = firstDate.plusDays(NBR_OF_DAYS-1);

        addToCalendar(firstDate,lastDate);
        scrollPane = new ScrollPane(calendar);
        scrollPane.hvalueProperty().addListener(
                (ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if(newValue.doubleValue() == scrollPane.getHmax()){
                        //IF the scroll reached max to the right, add new calendar values to the right
                        addAfterCalendar(lastDate.plusDays(NBR_OF_DAYS));
                    }else if(newValue.doubleValue() == 0){
                        //IF the scroll reached max to the left, add new calendar values to the left
                        addPreviousCalendar(firstDate.minusDays(NBR_OF_DAYS));
                    }
                });

        //updating V header - dates
        InvalidationListener vUpdater = o -> {
            final double ty = (calendar.getHeight() - scrollPane.getViewportBounds().getHeight()) * scrollPane.getVvalue();
            for (Node header : fix_days) {
                header.setTranslateY(ty);
            }
        };
        calendar.heightProperty().addListener(vUpdater);
        scrollPane.viewportBoundsProperty().addListener(vUpdater);
        scrollPane.vvalueProperty().addListener(vUpdater);

        //updating H header - hours
        InvalidationListener hUpdater = o -> {
            final double tx = (calendar.getWidth() - scrollPane.getViewportBounds().getWidth()) * scrollPane.getHvalue();
            for (Node header : fix_hours) {
                header.setTranslateX(tx);
            }
        };
        calendar.widthProperty().addListener(hUpdater);
        scrollPane.viewportBoundsProperty().addListener(hUpdater);
        scrollPane.hvalueProperty().addListener(hUpdater);

        getChildren().add(scrollPane);

        alarmVBox = new AlarmVBox(this.mainScreen,true);
        alarmVBox.setAlignment(Pos.TOP_CENTER);
        getChildren().add(alarmVBox);
    }

    private void addPreviousCalendar(LocalDate new1stDate){
        LocalDate old1stDate = firstDate;
        //add new dates to calendar
        addToCalendar(new1stDate,firstDate);
        //update first date
        firstDate = new1stDate;
        try {
            //add schedule and stored reminders
            addSchedule(firstDate,old1stDate);
            reminders.prepareReminders(firstDate,old1stDate);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private void addAfterCalendar(LocalDate newLastDate){
        LocalDate oldLastDate = lastDate;
        //add new dates to calendar
        addToCalendar(lastDate,newLastDate);
        //update last date
        lastDate = newLastDate;
        try {
            //add schedule and stored reminders
            addSchedule(oldLastDate,lastDate);
            reminders.prepareReminders(oldLastDate,lastDate);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private Node getNodeByRowColumnIndex (int row, int column) {
        for (Node node : calendar.getChildren()) {
            if((calendar.getRowIndex(node)!=null && calendar.getColumnIndex(node) !=null) && (calendar.getRowIndex(node) == row && calendar.getColumnIndex(node) == column)){
                return node;
            }
        }
        return null;
    }

    public void centerNodeInScrollPane(ScrollPane scrollPane, Node node) {
        double totalH = scrollPane.getContent().getBoundsInLocal().getHeight();
        double totalW = scrollPane.getContent().getBoundsInLocal().getWidth();
        double x = (node.getBoundsInParent().getMaxX() +
                node.getBoundsInParent().getMinX()) / 2.0;
        double y = (node.getBoundsInParent().getMaxY() +
                node.getBoundsInParent().getMinY()) / 2.0;
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        scrollPane.setVvalue(scrollPane.getVmax() * ((y - 0.5 * viewportHeight) / (totalH - viewportHeight)));
        scrollPane.setHvalue(scrollPane.getHmax() * ((x - 0.5 * viewportWidth) / (totalW- viewportWidth)));
    }

    private void addToCalendar(LocalDate firstDate,LocalDate lastDate){
        //ADD EACH CELLS
        for (LocalDate day = firstDate; !day.isAfter(lastDate); day = day.plusDays(1)) {
            int slotIndex = 1;

            for (LocalDateTime startTime = day.atTime(beginningOfTheDay);
                 !startTime.isAfter(day.atTime(endOfTheDay));
                 startTime = startTime.plus(period)) {

                Slot slot = new Slot(startTime, period);
                slots.add(slot);

                calendar.add(slot.getView(), slot.getBeginning().getDayOfYear(), slotIndex++);
            }
        }

        DateTimeFormatter dFormatter = DateTimeFormatter.ofPattern("E\nMMM d");

        //ADD DAY IN EACH CELL
        for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
            Label label = new Label(date.format(dFormatter));
            label.setPadding(new Insets(1));
            label.setTextAlignment(TextAlignment.CENTER);

            label.setTextFill(LIGHTGRAY);
            GridPane.setHalignment(label, HPos.CENTER);
            calendar.add(label, date.getDayOfYear(), 0);

            Node node = calendar.getChildren().get(calendar.getChildren().size()-1);
            node.setStyle("-fx-background-color:#3d3d3d; -fx-border-color: darkgrey; -fx-pref-height: 40; -fx-pref-width: 100; -fx-alignment: center");
            node.toFront();
            fix_days.add(node);
        }

        int slotIndex = 1;
        DateTimeFormatter tFormatter = DateTimeFormatter.ofPattern("H:mm");

        //ADD EACH HOURS
        LocalDate today = LocalDate.now();
        for (LocalDateTime startTime = today.atTime(beginningOfTheDay);
             !startTime.isAfter(today.atTime(endOfTheDay));
             startTime = startTime.plus(period)) {
            Label label = new Label(startTime.format(tFormatter));
            label.setPadding(new Insets(2));
            label.setTextFill(LIGHTGRAY);
            GridPane.setHalignment(label, HPos.RIGHT);
            calendar.add(label, 0, slotIndex);
            slotIndex++;

            Node node = calendar.getChildren().get(calendar.getChildren().size()-1);
            node.setStyle("-fx-background-color:#3d3d3d; -fx-border-color: darkgrey; -fx-pref-height: 10; -fx-pref-width: 50;");
            node.toFront();
            fix_hours.add(node);
        }
        for (Node node : fix_days) {    //keeping dates above hours
            node.toFront();
        }
    }


    private void addSchedule(LocalDate firstDate,LocalDate lastDate) throws ParseException {
        ArrayList<Course> courses = skill_schedule.getInInterval(firstDate,lastDate);
        Color color = null;
        String desc = "";
        LocalDate date = null;
        LocalTime time = null;
        LocalTime time1 = null;
        for (Course course:courses) {
            color = Color.BLUEVIOLET;
            desc = course.getSummary();
            if(course.getStart_Time()==null){
                time = LocalTime.of(0,0);
                color = Color.RED;
            }else{
                time =new SimpleDateFormat("HHmmss").parse(course.getStart_Time()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            }
            if(course.getEnd_Time()==null){
                time1 = LocalTime.of(2,0);
                color = Color.RED;
            }else{
                time1 =new SimpleDateFormat("HHmmss").parse(course.getEnd_Time()).toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            }
            date =new SimpleDateFormat("yyyyMMdd").parse(course.getDate()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            addReminder(desc,date,time,time1,color);
        }
    }

    public void addReminder(String desc,LocalDate date,LocalTime fromTime,LocalTime toTime,Color color){
        BackgroundFill backgroundFill =
                new BackgroundFill(
                        color,
                        new CornerRadii(10),
                        new Insets(1)
                );
        Background background = new Background(backgroundFill);

        int[] inTable = convertToTable(date,fromTime,toTime);
        if(inTable!=null){
            Pane pane1  = new Pane();
            pane1.setBackground(background);
            pane1.setCursor(Cursor.HAND);
            pane1.toBack();
            for (Node node : fix_hours) {
                node.toFront();
            }
            for (Node node : fix_days) {
                node.toFront();
            }
            pane1.setOnMouseClicked(event -> {
                getReminderInfo(desc,date.toString(),fromTime.toString(),toTime.toString());
            });
            calendar.add(pane1,inTable[0],inTable[1],1,inTable[2]);

            Text text = new Text(desc);
            text.setDisable(true);
            text.setTextOrigin(VPos.CENTER);
            text.setFill(LIGHTGRAY);
            text.setWrappingWidth(80);
            calendar.add(text, inTable[0], inTable[1], 1, inTable[2]);
        }
    }

    /**
     *
     * @param date
     * @param fromTime
     * @param toTime
     * @return [columnIndex, rowIndex, nbr of cell used in each row] in table
     */
    private int[] convertToTable(LocalDate date, LocalTime fromTime, LocalTime toTime){
        int col = 1; int row = 1; int rowSpan = 1;
        if(date.isAfter(firstDate.minusDays(1)) && date.isBefore(lastDate.plusDays(1))){
            col = date.getDayOfYear();

            long timePeriod = Duration.between(beginningOfTheDay, fromTime).toMinutes();
            row = (int) (timePeriod/period.toMinutes())+1;

            long duration = Math.abs(Duration.between(fromTime, toTime).toMinutes());
            if(duration<=period.toMinutes()){
                rowSpan = 1;
            }else{
                rowSpan = (int) (duration/period.toMinutes());
            }
        }else{
            mainScreen.chat.receiveMessage("Error - the date you entered is not contained in the date interval of the calendar.");
            return null;
        }

        return new int[]{col,row,rowSpan};
    }

    private void getReminderInfo(String desc,String date,String from,String to) {
        VBox notification = new VBox(40);
        notification.setAlignment(Pos.TOP_CENTER);
        notification.setPrefSize(300, 285);
        notification.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(7))));
        notification.setBackground(new Background(new BackgroundFill(Color.LIGHTSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setOpacity(0.91);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(notification, 320, 190));
        stage.show();

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2 - 280);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4 + 110);

        Label timerLabel = new Label(date + " from " + from + " to " + to);
        timerLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 13));
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setAlignment(Pos.TOP_LEFT);
        timerLabel.setTranslateX(15);

        Button exit = new Button("x");
        exit.setCursor(Cursor.HAND);
        exit.setBackground(Background.EMPTY);
        exit.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));
        exit.setTextFill(Color.DARKRED);
        exit.setBorder(null);
        exit.setAlignment(Pos.TOP_RIGHT);
        exit.setOnAction(e -> stage.close());

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        HBox topBox = new HBox(60);
        topBox.setAlignment(Pos.CENTER);
        topBox.setBackground(new Background(new BackgroundFill(MainScreen.themeColor, CornerRadii.EMPTY, Insets.EMPTY)));
        topBox.getChildren().addAll(timerLabel, region, exit);

        Label label = new Label(desc);
        label.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 26));
        label.setTextFill(Color.WHITESMOKE);
        label.setAlignment(Pos.CENTER);

        notification.getChildren().addAll(topBox, label);
    }

    public VBox getCalendarShortcut(ArrayList<String> info) {
        VBox calendarShortcut = new VBox(20);

        Label fromCalendar = new Label("From Calendar:");
        fromCalendar.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
        calendarShortcut.getChildren().add(fromCalendar);

        if (info.isEmpty()) {
            Label none = new Label("Nothing planned today!");
            none.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
            none.setWrapText(true);
            none.setTextFill(Color.DARKRED.darker());
            none.setTranslateY(20);
            none.setAlignment(Pos.CENTER);
            none.setTextAlignment(TextAlignment.CENTER);
            calendarShortcut.getChildren().add(none);
        }
        else {
            int numOfEvents = info.size();
            int count = 0;
            int remaining = info.size();
            for (int i = 0; i<numOfEvents; i++) {
                count=count+1;
                if (count<=2) {
                    Label time = new Label(info.get(i).substring(0,5) + "- " + info.get(i).substring(6,11));
                    time.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
                    time.setTextFill(Color.WHITESMOKE);
                    time.setWrapText(true);
                    time.setMaxWidth(46);
                    time.setMaxWidth(46);

                    String desc = info.get(i).substring(12);
                    if (desc.length()>16) {
                        desc = desc.substring(0, 15) + "...";
                    }
                    Label description = new Label(desc);
                    description.setAlignment(Pos.CENTER);
                    description.setTextAlignment(TextAlignment.LEFT);
                    description.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
                    description.setTextFill(Color.WHITESMOKE);
                    description.setWrapText(true);
                    description.setMaxWidth(115);
                    description.setMaxWidth(115);

                    HBox event = new HBox(20);
                    event.setAlignment(Pos.CENTER_LEFT);
                    event.setStyle("-fx-background-color:#3d3d3d; -fx-background-insets: -7; -fx-background-radius: 10;");
                    event.getChildren().addAll(time, description);

                    calendarShortcut.getChildren().add(event);
                    remaining--;
                }
            }
            if (remaining>0) {
                Label l = new Label("+ " + remaining + " more today");
                l.setWrapText(true);
                l.setAlignment(Pos.CENTER);
                l.setTextAlignment(TextAlignment.CENTER);
                l.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
                calendarShortcut.getChildren().add(l);
            }
        }
        return calendarShortcut;
    }


    public static class Slot {

        private final LocalDateTime beginning;
        private final Duration period;
        private final Region view;

        public Slot(LocalDateTime beginning, Duration period) {

            this.beginning = beginning;
            this.period = period;

            view = new Region();
            view.setMinSize(80, 16);

        }


        public LocalTime getTime() {
            return beginning.toLocalTime();
        }


        public LocalDateTime getBeginning() {
            return beginning;
        }

        public DayOfWeek getDayOfWeek() {
            return beginning.getDayOfWeek();
        }


        public Node getView() {
            return view;
        }

    }
}
