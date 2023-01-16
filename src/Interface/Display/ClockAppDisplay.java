package Interface.Display;

import Interface.Display.ClockTools.AlarmVBox;
import Interface.Display.ClockTools.ClockVBox;
import Interface.Display.ClockTools.StopwatchVBox;
import Interface.Display.ClockTools.TimerVBox;
import Interface.Screens.MainScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class ClockAppDisplay extends VBox {
    private HBox tabs;
    public Button alarm;
    public Button clock;
    public Button timer;
    public Button stopwatch;
    public Button prevTab;
    public AlarmVBox alarmVBox;
    public ClockVBox clockVBox;
    public TimerVBox timerVBox;
    public StopwatchVBox stopwatchVBox;
    public static Color color = new Color(0.2,0.35379, 0.65, 1);

    private MainScreen mainScreen;

    public ClockAppDisplay(MainScreen mainScreen) {
        this.mainScreen = mainScreen;
        timerVBox = new TimerVBox();
        stopwatchVBox = new StopwatchVBox();
        clockVBox = new ClockVBox();

        Color bgColor = Color.LIGHTGRAY.brighter().brighter();
        setBackground(new Background(new BackgroundFill(new Color(bgColor.getRed(),bgColor.getGreen(), bgColor.getBlue(), 0.4), CornerRadii.EMPTY, Insets.EMPTY)));

        setTabs();
        getChildren().add(tabs);
    }

    public void setTabs() {
        tabs = new HBox(10);
        tabs.setAlignment(Pos.CENTER);
        tabs.setPrefHeight(80);
        tabs.setMinHeight(80);
        tabs.setBackground(new Background(new BackgroundFill(new Color(0.2, 0.35379, 0.65, 1), CornerRadii.EMPTY, Insets.EMPTY)));

        alarm = new Button("Alarm");
        designTab(alarm);

        clock = new Button("Clock");
        designTab(clock);

        timer = new Button("Timer");
        designTab(timer);

        stopwatch = new Button("Stopwatch");
        designTab(stopwatch);

        Button exit = new Button("x");
        exit.setCursor(Cursor.HAND);
        exit.setBackground(Background.EMPTY);
        exit.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        exit.setTextFill(Color.DARKRED);
        exit.setBorder(null);
        exit.setAlignment(Pos.CENTER);
        exit.setTranslateY(-17);
        exit.setTranslateX(-2);
        exit.setOnAction(e -> {
            try {
                mainScreen.setMenu("MainMenu");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        tabs.getChildren().addAll(alarm, clock, timer, stopwatch, region, exit);
    }

    private void designTab(Button tab) {
        tab.setCursor(Cursor.HAND);
        tab.setBackground(Background.EMPTY);
        tab.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
        tab.setTextFill(Color.LIGHTGRAY);
        tab.setPrefSize(160, 80);
        tab.setAlignment(Pos.CENTER);
        tab.setOnAction(e -> {deselectTab(prevTab);
            try {
                selectTab(tab);
            } catch (IOException | ParseException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    public void selectTab(Button selectedTab) throws IOException, ParseException {
        prevTab = selectedTab;
        selectedTab.setBackground(new Background(new BackgroundFill(MainScreen.themeColor.darker(), CornerRadii.EMPTY, Insets.EMPTY)));
        selectedTab.setTextFill(Color.LIGHTGRAY.brighter());
        selectedTab.setBorder(new Border(new BorderStroke(Color.LIGHTSLATEGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(1))));

        switch(selectedTab.getText()) {
            case "Alarm": setAlarmView(); break;
            case "Clock": setClockView(); break;
            case "Timer": setTimerView(); break;
            case "Stopwatch": setStopwatchView(); break;
        }
    }

    public void deselectTab(Button prevTab) {
        prevTab.setBackground(Background.EMPTY);
        prevTab.setTextFill(Color.LIGHTGRAY);
        prevTab.setBorder(null);

        switch(prevTab.getText()) {
            case "Alarm": getChildren().remove(alarmVBox); break;
            case "Clock": getChildren().remove(clockVBox); break;
            case "Timer": getChildren().remove(timerVBox); break;
            case "Stopwatch": getChildren().remove(stopwatchVBox); break;
        }
    }

    private void setAlarmView() throws IOException, ParseException {
        alarmVBox = new AlarmVBox(mainScreen,false);
        getChildren().add(alarmVBox);
    }

    private void setClockView() {
        getChildren().add(clockVBox);
    }

    private void setTimerView() {
        getChildren().add(timerVBox);
    }

    private void setStopwatchView() {
        getChildren().add(stopwatchVBox);
    }

    public static void notifyUser(String isFor, String time, String desc) { //isFor = "timer";"alarm";"reminder"
        String mediasrc = "src/res/analog-watch-alarm_daniel-simion.mp3";
        File file = new File(mediasrc);
        MediaPlayer mediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
        mediaPlayer.setAutoPlay(true);
        mediaPlayer.setVolume(0.5);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        MediaView mediaView = new MediaView(mediaPlayer);   //notification sound (*.mp3) , stops on exit

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

        Label timerLabel = new Label();
        Label label = new Label();
        switch (isFor) {
            case "timer":
                timerLabel = new Label("Timer");
                label = new Label("Time's up!");
                break;
            case "alarm":
                timerLabel = new Label("Alarm of " + time);
                label = new Label(desc);
                break;
            case "reminder":
                timerLabel = new Label("Reminder of " + time);
                label = new Label(desc);
                break;
        }
        timerLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 15));
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setAlignment(Pos.TOP_LEFT);
        timerLabel.setTranslateX(15);

        label.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 25));
        label.setTextFill(Color.WHITESMOKE);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);

        Button exit = new Button("x");
        exit.setCursor(Cursor.HAND);
        exit.setBackground(Background.EMPTY);
        exit.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));
        exit.setTextFill(Color.DARKRED);
        exit.setBorder(null);
        exit.setAlignment(Pos.TOP_RIGHT);
        exit.setOnAction(e -> {
            mediaPlayer.stop();
            stage.close();
        });

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        HBox topBox = new HBox(60);
        topBox.setAlignment(Pos.CENTER);
        if (MainScreen.themeColor.equals(Color.LIGHTGRAY)) {
            topBox.setBackground(new Background(new BackgroundFill(Color.GRAY.darker(), CornerRadii.EMPTY, Insets.EMPTY)));
        } else {
            topBox.setBackground(new Background(new BackgroundFill(MainScreen.themeColor, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        topBox.getChildren().addAll(timerLabel, region, exit);
        notification.getChildren().addAll(topBox, label, mediaView);
    }
}