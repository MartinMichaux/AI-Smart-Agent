package Interface.Screens;

import DataBase.Data;
import FaceDetection.FaceDetection;
import Interface.Chat.ChatApp;
import Interface.Display.*;
import Skills.Calendar.HandleReminders;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class MainScreen {
    public FaceDetection faceDetection;

    public ChatApp chat;
    public ClockAppDisplay clockAppDisplay;
    public WeatherDisplay weatherDisplay;
    public SkillEditorDisplay skillEditorDisplay;
    public BorderPane root;
    public CalendarDisplay calendarDisplay;
    public Menu menu;
    public int borderWidth;
    public Border border;
    public static Color themeColor = new Color(0.2,0.35379, 0.65, 1);
    public Stage stage;

    public ArrayList<String> todaysRemindersShortcut = new ArrayList<>();

    private boolean firstFaceViewed;

    public MainScreen(FaceDetection faceDetection,Stage stage) throws Exception {
        this.faceDetection = faceDetection;
        //refresh the mainscreen object
        faceDetection.mainScreen = this;
        //border of main pane
        borderWidth = 10;
        border = new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(borderWidth)));

        //chatapp
        chat = new ChatApp(Data.getUsername(),this);
        //all the skills display object
        clockAppDisplay = new ClockAppDisplay(this);
        skillEditorDisplay = new SkillEditorDisplay(this);
        weatherDisplay = new WeatherDisplay(this);
        calendarDisplay = new CalendarDisplay(this);
        Data.setMp(null);   //resetting mediaPlayer for each login attempt

        HandleReminders reminders = new HandleReminders(calendarDisplay);
        //reads the reminder file and add the one of today to the list
        reminders.prepareReminders(calendarDisplay.firstDate,calendarDisplay.lastDate);
        todaysRemindersShortcut = reminders.todaysRemindersShortcut;

        //add content to the pane
        createContent();

        //start displaying the scene
        this.stage = stage;
        start(this.stage);

        //handle face detection
        firstFaceViewed = false;
        manageFaceDetection();
        faceDetection.controller.mainScreen = this;
    }

    /**
     *handle the face leaving the webcam
     */
    public void manageFaceDetection(){
        //Starts a multithreading task that stops and call another multithreading task when a face is not detected
        final boolean[] faceDetected = {true};
        Task task = new Task<Void>() {
            @Override public Void call(){
                //stops if a face is not detected anymore
                while (faceDetected[0]){
                    if(!firstFaceViewed&&faceDetection.faceDetected()){
                        //Assistant's first message when sees user for first time
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                chat.receiveMessage("Welcome " + Data.getUsername() + "! How may I help you?"+"\r\n"+
                                        "The CFG parser is on, if you want to turn it off write: CFG off");
                            }
                        });
                        firstFaceViewed = true;
                    }
                    if(!faceDetection.faceDetected()){
                        //Manage face not detected
                        faceDetection.manageFaceLeaving();
                        faceDetected[0] = false;
                    }
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * method to log out of the mainscreen and come back to startscreen
     * @param inactive if user has been logged out for inactive cause
     */
    public void logOut(boolean inactive){
        Data.setImage("src/DataBase/defaultBackground.jpg");
        stage.close();
        try {
            StartScreen startScreen = new StartScreen();
            startScreen.start(stage);
            if(inactive){
                startScreen.errorInfo.setText("You have been logged out because of inactivity");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * method for showing the stage
     * @param primaryStage
     */
    public void start(Stage primaryStage) {
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
    }

    /**
     * create content on the mainscreen
     * @throws Exception
     */
    public void createContent() throws Exception {
        root = new BorderPane();
        root.setBorder(border);
        root.setBackground(Data.createBackGround());

        chat.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2));
        chat.prefWidthProperty().bind(root.widthProperty().divide(2.8));
        root.setRight(chat);

        menu = new Menu(this);
        setMenu("MainMenu");
    }

    /**
     * handles the menu-shortcuts tans
     * @param menuString
     * @throws Exception
     */
    public void setMenu(String menuString) throws Exception {
        switch (menuString){
            case "MainMenu": menu.displayMainMenu(); break;
            case "Shortcuts": menu.displayShortcutsMenu(); break;
            case "ThemeColors": menu.displayThemeColorsMenu(); break;
            case "Background": menu.displayBackgroundEditing(); break;
            case "Settings": menu.displaySettingsMenu(); break;
        }
    }

    //////
    //NEXT ARE ALL THE METHODS TO DISPLAY THE DIFFERENT SKILLS ON THE MAINSCREEN
    /////
    public void displayCamera(){
        VBox notification = new VBox(0);
        notification.setAlignment(Pos.TOP_CENTER);
        notification.setPrefSize(300, 285);
        notification.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(7))));
        notification.setBackground(new Background(new BackgroundFill(Color.LIGHTSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setOpacity(0.91);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(notification, 320, 320));
        stage.show();

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2 - 280);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4 + 110);

        Button exit = new Button("x");
        exit.setCursor(Cursor.HAND);
        exit.setBackground(Background.EMPTY);
        exit.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));
        exit.setTextFill(Color.DARKRED);
        exit.setBorder(null);
        exit.setAlignment(Pos.TOP_RIGHT);
        exit.setOnAction(e -> {
            if(!faceDetection.controller.cameraActive){
                faceDetection.controller.startCamera();
            }
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
        topBox.getChildren().add(exit);
        notification.getChildren().addAll(topBox,faceDetection);

        // to be able to move the camera window
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        notification.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            }
        });
        notification.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            }
        });
    }

    public void setWeatherDisplay(String city, String country, Boolean selectHourly) throws Exception {
        weatherDisplay.setLocation(city, country, selectHourly);
        weatherDisplay.setSpacing(7);
        weatherDisplay.setBorder(border);
        weatherDisplay.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2));
        weatherDisplay.prefWidthProperty().bind(root.widthProperty().subtract(chat.prefWidthProperty()).subtract(borderWidth*2));
        weatherDisplay.setScaleX(0.8);
        weatherDisplay.setScaleY(0.8);

        root.setLeft(weatherDisplay);
    }

    public void setClockAppDisplay(String firstTab) throws IOException, ParseException {
        if (clockAppDisplay.prevTab != null) { clockAppDisplay.deselectTab(clockAppDisplay.prevTab); }
        switch(firstTab) {
            case "Alarm": clockAppDisplay.selectTab(clockAppDisplay.alarm); break;
            case "Clock": clockAppDisplay.selectTab(clockAppDisplay.clock); break;
            case "Timer": clockAppDisplay.selectTab(clockAppDisplay.timer); break;
            case "Stopwatch": clockAppDisplay.selectTab(clockAppDisplay.stopwatch); break;
        }
        clockAppDisplay.setBorder(border);
        clockAppDisplay.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2));
        clockAppDisplay.prefWidthProperty().bind(root.widthProperty().subtract(chat.prefWidthProperty()).subtract(borderWidth*2));
        clockAppDisplay.setScaleX(0.8);
        clockAppDisplay.setScaleY(0.8);

        root.setLeft(clockAppDisplay);
    }

    public void setSkillEditorAppDisplay(String firstTab) {
        if (skillEditorDisplay.prevTab != null) { skillEditorDisplay.deselectTab(skillEditorDisplay.prevTab); }
        if (firstTab.equals("Add rule")) { skillEditorDisplay.selectTab(skillEditorDisplay.addRule); }
        else if(firstTab.equals("Add skill")) {
            skillEditorDisplay.selectTab(skillEditorDisplay.addSkill);
        }
        else if(firstTab.equals("Edit skill")) {
            skillEditorDisplay.selectTab(skillEditorDisplay.editSkill);
        }
        else {
            skillEditorDisplay.selectTab(skillEditorDisplay.editRule);
        }

        skillEditorDisplay.setBorder(border);
        skillEditorDisplay.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2));
        skillEditorDisplay.prefWidthProperty().bind(root.widthProperty().subtract(chat.prefWidthProperty()).subtract(borderWidth*2));
        skillEditorDisplay.setScaleX(0.8);
        skillEditorDisplay.setScaleY(0.8);

        root.setLeft(skillEditorDisplay);
    }

    public void setMapDisplay(String type,String loc1,String loc2) throws Exception {
        MapDisplay mapDisplay = new MapDisplay(this, type,loc1,loc2);
        mapDisplay.setBackground(Data.createBackGround());
        mapDisplay.setBorder(border);
        mapDisplay.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2));
        mapDisplay.prefWidthProperty().bind(root.widthProperty().subtract(chat.prefWidthProperty()).subtract(borderWidth*2));
        mapDisplay.setScaleX(0.8);
        mapDisplay.setScaleY(0.8);
        mapDisplay.myWebView.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2).subtract(45));

        root.setLeft(mapDisplay);
    }

    public void displayUrlMediaPlayer(MediaPlayerDisplay mediaPlayerDisplay){
        VBox mp = (VBox) addEscTo(mediaPlayerDisplay, true);
        mp.setBackground(Data.createBackGround());
        mp.setBorder(border);
        mp.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2));
        mp.prefWidthProperty().bind(root.widthProperty().subtract(chat.prefWidthProperty()).subtract(borderWidth*2));
        mp.setScaleX(0.8);
        mp.setScaleY(0.8);

        mediaPlayerDisplay.mediaView.setFitWidth(mp.getPrefWidth()-borderWidth*2);
        mediaPlayerDisplay.mediaView.setFitHeight(mp.getPrefHeight()-borderWidth*2);
        mediaPlayerDisplay.prefWidthProperty().bind(mp.widthProperty().subtract(borderWidth*2));
        mediaPlayerDisplay.prefHeightProperty().bind(mp.heightProperty().subtract(borderWidth*2));
        mediaPlayerDisplay.mediaView.setPreserveRatio(true);

        root.setLeft(mp);
    }

    public void displaySkill(Pane pane,String skill) {
        pane = (Pane) addEscTo(pane, false);
        pane.setBackground(Data.createBackGround());
        pane.setBorder(border);
        pane.prefHeightProperty().bind(root.heightProperty().subtract(borderWidth*2));
        pane.prefWidthProperty().bind(root.widthProperty().subtract(chat.prefWidthProperty()).subtract(borderWidth*2));
        pane.setScaleX(0.8);
        pane.setScaleY(0.8);

        root.setLeft(pane);
    }

    private Node addEscTo(Node node, Boolean isMediaPlayer) {
        VBox newPane = new VBox(0);

        HBox topBox = new HBox(0);
        topBox.setAlignment(Pos.TOP_CENTER);
        topBox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        Button exit = new Button("x");
        exit.setCursor(Cursor.HAND);
        exit.setBackground(Background.EMPTY);
        exit.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));
        exit.setTextFill(Color.WHITE);
        exit.setBorder(null);
        exit.setAlignment(Pos.TOP_RIGHT);
        exit.setOnAction(e -> {
            try {
                if (isMediaPlayer){Data.getMp().pause();}
                setMenu("MainMenu");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        topBox.getChildren().addAll(region, exit);
        newPane.getChildren().addAll(topBox, node);

        return newPane;
    }
}