package Interface.Screens;

import DataBase.Data;
import FileParser.FileParser;
import Interface.Display.MediaPlayerDisplay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;

public class Menu {
    private VBox shortcutsMenu;
    private VBox mainMenu;
    private VBox settingsMenu;
    private VBox themeColorsMenu;
    private HBox mainMenu_shortcuts_Option;
    private HBox shortcutsHBox1;
    private HBox shortcutsHBox2;
    private Boolean update = false;

    private MainScreen mainScreen;

    public Menu(MainScreen mainScreen) throws Exception {
        this.mainScreen = mainScreen;
        setMainMenu();
        setSettingsMenu();
        setThemeColorsMenu();
        setShortcutsMenu();
    }

    public void displayMainMenu() {
        setMenuBackground(mainMenu);
        mainScreen.root.setLeft(mainMenu);
    }

    public void displaySettingsMenu() {
        setMenuBackground(settingsMenu);
        mainScreen.root.setLeft(settingsMenu);
    }

    public void displayThemeColorsMenu() {
        setMenuBackground(themeColorsMenu);
        mainScreen.root.setLeft(themeColorsMenu);
    }

    public void displayShortcutsMenu() throws Exception {
        updateWeatherAndCalendarShortcut();
        setMenuBackground(shortcutsMenu);
        mainScreen.root.setLeft(shortcutsMenu);
    }

    public void displayBackgroundEditing(){
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(mainScreen.stage);
        try {
            if(selectedFile.toURI().toString().endsWith(".png")||selectedFile.toURI().toString().endsWith(".jpg")){
                String file = selectedFile.toPath().toString();
                Data.setImage(file);
                mainScreen.root.setBackground(Data.createBackGround());
            }else {
                mainScreen.chat.receiveMessage("The file "+selectedFile.toURI().toString()+" is not an image");
            }
        } catch(NullPointerException e){
            mainScreen.chat.receiveMessage("No file chosen");
        }
    }

    private HBox getMainMenu_shortcuts_Option(Boolean isShortcutsMenuDisplayed) {
        mainMenu_shortcuts_Option = new HBox();
        mainMenu_shortcuts_Option.setPadding(new Insets(90,0,60,0));
        mainMenu_shortcuts_Option.setAlignment(Pos.TOP_CENTER);
        mainMenu_shortcuts_Option.setFillHeight(true);

        Button menuBtn = new Button("Menu");
        menuBtn.setCursor(Cursor.HAND);
        menuBtn.setFont(Font.font("Cambria", FontWeight.EXTRA_BOLD, 18));
        menuBtn.setTextFill(Color.WHITE);
        menuBtn.setPrefWidth(120);
        menuBtn.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(10,0,0,10,false), new BorderWidths(0.5))));

        Button shortcutsBtn = new Button("Shortcuts");
        shortcutsBtn.setCursor(Cursor.HAND);
        shortcutsBtn.setFont(Font.font("Cambria", FontWeight.EXTRA_BOLD, 18));
        shortcutsBtn.setTextFill(Color.WHITE);
        shortcutsBtn.setPrefWidth(120);
        shortcutsBtn.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, new CornerRadii(0,10,10,0,false), new BorderWidths(0.5))));

        if (isShortcutsMenuDisplayed.equals(true)) {
            menuBtn.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(10, 0, 0, 10, false), Insets.EMPTY)));
            shortcutsBtn.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
        }
        else {
            menuBtn.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(10, 0, 0, 10, false), Insets.EMPTY)));
            shortcutsBtn.setBackground(new Background(new BackgroundFill(Color.BLACK, new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
        }

        menuBtn.setOnMouseClicked(e-> displayMainMenu());
        shortcutsBtn.setOnMouseClicked(e-> {
            try {
                displayShortcutsMenu();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        mainMenu_shortcuts_Option.getChildren().setAll(menuBtn, shortcutsBtn);
        return mainMenu_shortcuts_Option;
    }

    private void setMainMenu() {
        mainMenu = new VBox(40);
        editMenu(mainMenu);

        Label userNameLabel = new Label(Data.getUsername());
        userNameLabel.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 45)));
        userNameLabel.setStyle("-fx-text-fill: white");

        Button settings = new Button("Settings");
        Button help = new Button("Help");
        Button logOut = new Button("Log out");

        designOptionButton(settings);
        settings.setOnMouseClicked(event -> displaySettingsMenu());

        designOptionButton(help);
        help.setOnMouseClicked(event -> mainScreen.chat.receiveMessage("Tell me what to do for you. For example you can check the weather by typing \"How is the weather?\" or your UM schedule by typing \"Next Lecture\", \"This week Lecture\"."));

        designOptionButton(logOut);
        logOut.setOnMouseClicked(event -> {
            mainScreen.logOut(false);
        });
        mainMenu.getChildren().addAll(getMainMenu_shortcuts_Option(false), userNameLabel, settings, help, logOut);
    }

    private void setSettingsMenu() {
        settingsMenu = new VBox(40);
        editMenu(settingsMenu);

        Label settingsLabel = new Label("Settings");
        settingsLabel.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 45)));
        settingsLabel.setStyle("-fx-text-fill: white");

        Button editProf = new Button("Edit profile");
        Button themeColor = new Button("Theme Color");
        Button changeBackground = new Button("Background");
        Button back = new Button("Back");

        designOptionButton(editProf);
        editProf.setOnMouseClicked(event -> mainScreen.chat.receiveMessage("You can change your password/location by typing \"Change my password/location/... to <YourNewPassword/YourNewLocation/...>\"."));

        designOptionButton(themeColor);
        themeColor.setOnMouseClicked(e-> displayThemeColorsMenu());

        designOptionButton(changeBackground);
        changeBackground.setOnMouseClicked(event -> displayBackgroundEditing());

        designOptionButton(back);
        back.setOnMouseClicked(event -> displayMainMenu());

        settingsMenu.getChildren().addAll(getMainMenu_shortcuts_Option(false), settingsLabel, editProf, themeColor,changeBackground, back);
    }

    public void setThemeColorsMenu() {
        themeColorsMenu = new VBox(40);
        editMenu(themeColorsMenu);

        Label themeColorLabel = new Label("Theme Color");
        themeColorLabel.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 45)));
        themeColorLabel.setStyle("-fx-text-fill: white");

        HBox colors = new HBox(40);
        colors.setAlignment(Pos.CENTER);

        Color color = new Color(0.2, 0.35379, 0.65, 1);
        String colorString = "rgb(" + color.getRed() * 255 + "," + color.getGreen() * 255 + "," + color.getBlue() * 255 + ");";
        Button blue = new Button();
        blue.setStyle("-fx-border-radius: 5em; -fx-border-color:black; -fx-background-radius: 5em; -fx-min-width: 40px; -fx-min-height: 40px; -fx-max-width: 40px; -fx-max-height: 40px; -fx-background-color: "+ colorString +";");
        blue.setFocusTraversable(false);
        blue.setCursor(Cursor.HAND);

        Button black = new Button();
        black.setStyle("-fx-border-radius: 5em; -fx-border-color:white; -fx-background-radius: 5em; -fx-min-width: 40px; -fx-min-height: 40px; -fx-max-width: 40px; -fx-max-height: 40px; -fx-background-color: black;");
        black.setFocusTraversable(false);
        black.setCursor(Cursor.HAND);

        Button white = new Button();
        white.setStyle("-fx-border-radius: 5em; -fx-border-color:black; -fx-background-radius: 5em; -fx-min-width: 40px; -fx-min-height: 40px; -fx-max-width: 40px; -fx-max-height: 40px; -fx-background-color: lightgray;");
        white.setFocusTraversable(false);
        white.setCursor(Cursor.HAND);

        Button back = new Button("Back");
        designOptionButton(back);
        back.setOnMouseClicked(e -> displaySettingsMenu());

        blue.setOnMouseClicked(e -> {
            mainScreen.themeColor = new Color(0.2, 0.35379, 0.65, 1);
            try {
                mainScreen.chat.changeColor(mainScreen.themeColor);
                setMenuBackground(themeColorsMenu);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        black.setOnMouseClicked(e -> {
            mainScreen.themeColor = Color.BLACK;
            try {
                mainScreen.chat.changeColor(mainScreen.themeColor);
                setMenuBackground(themeColorsMenu);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });
        white.setOnMouseClicked(e -> {
            mainScreen.themeColor = Color.LIGHTGRAY;
            try {
                mainScreen.chat.changeColor(mainScreen.themeColor);
                setMenuBackground(themeColorsMenu);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        colors.getChildren().addAll(blue, black, white);
        themeColorsMenu.getChildren().addAll(getMainMenu_shortcuts_Option(false), themeColorLabel, colors, back);
    }

    private void setShortcutsMenu() throws Exception {
        shortcutsMenu = new VBox(40);
        editMenu(shortcutsMenu);

        VBox clockShortcut = mainScreen.clockAppDisplay.clockVBox.getClockShortcut();
        designShortcut(clockShortcut, Color.SLATEGREY.darker(), Pos.CENTER, 200);
        clockShortcut.setOnMouseClicked(e-> {
            try {
                mainScreen.setClockAppDisplay("Clock");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (ParseException parseException) {
                parseException.printStackTrace();
            }
        });

        VBox mapShortcut = getIconShortcut("src/res/shortcutIcons/mapIcon.png", 80);
        mapShortcut.setOnMouseClicked(e-> {
            String c = FileParser.getUserInfo(Data.getUsername(),"-City");
            if(c.isEmpty()){
                System.out.println("It seems like you haven't completed your location yet.");
                c = "Maastricht";
            }
            try {
                mainScreen.setMapDisplay("map", c, null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox googleShortcut = getIconShortcut("src/res/shortcutIcons/googleIcon.png", 80);
        googleShortcut.setOnMouseClicked(e-> {
            try {
                mainScreen.setMapDisplay("google",null,null);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox mediaPlayerShortcut = getIconShortcut("src/res/shortcutIcons/mediaPlayerIcon.png", 80);
        mediaPlayerShortcut.setOnMouseClicked(e-> {
            if(Data.getMp()!=null){
                Data.getMp().play();
                MediaPlayerDisplay mediaControl = new MediaPlayerDisplay(Data.getMp());
                mainScreen.displayUrlMediaPlayer(mediaControl);
            }else{
                mainScreen.chat.receiveMessage("Choose a file.");
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(mainScreen.stage);
                try {
                    Media media = new Media (selectedFile.toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    Data.setMp(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    MediaPlayerDisplay mediaControl = new MediaPlayerDisplay(mediaPlayer);
                    mainScreen.displayUrlMediaPlayer(mediaControl);
                } catch(NullPointerException ex){
                    mainScreen.chat.receiveMessage("No file chosen");
                } catch(MediaException ex){
                    mainScreen.chat.receiveMessage("Filetype not supported");
                }
            }
        });

        HBox smallShortcuts1 = new HBox(30);
        smallShortcuts1.setAlignment(Pos.TOP_LEFT);
        smallShortcuts1.getChildren().addAll(mediaPlayerShortcut, mapShortcut);

        HBox smallShortcuts2 = new HBox(30);
        smallShortcuts2.setAlignment(Pos.TOP_LEFT);
        smallShortcuts2.getChildren().addAll(googleShortcut);

        VBox smallShortcuts = new VBox(30);
        smallShortcuts.getChildren().addAll(smallShortcuts1, smallShortcuts2);
        smallShortcuts1.setAlignment(Pos.TOP_CENTER);

        shortcutsHBox1 = new HBox(50);
        shortcutsHBox1.setAlignment(Pos.CENTER);
        shortcutsHBox1.getChildren().add(clockShortcut);

        shortcutsHBox2 = new HBox(50);
        shortcutsHBox2.setAlignment(Pos.CENTER);
        shortcutsHBox2.getChildren().add(smallShortcuts);
        updateWeatherAndCalendarShortcut();    //adding weather shortcut before the clock shortcut and calendar last

        shortcutsMenu.getChildren().addAll(getMainMenu_shortcuts_Option(true), shortcutsHBox1, shortcutsHBox2);
    }

    private VBox getIconShortcut(String icon, int dim) throws FileNotFoundException {
        VBox shortcut = new VBox();

        Rectangle r = new Rectangle(0, 0, dim, dim);
        r.setArcWidth(20.0);
        r.setArcHeight(20.0);
        r.setCursor(Cursor.HAND);
        r.setEffect(new DropShadow(15, Color.BLACK));
        r.setFill(Color.LIGHTGRAY);
        if (!icon.equals("")) {
            ImagePattern mapIcon = new ImagePattern(new Image(new FileInputStream(icon), dim, dim, false, true));
            r.setFill(mapIcon);
        }

        shortcut.getChildren().add(r);
        shortcut.setAlignment(Pos.CENTER_LEFT);
        shortcut.setPrefSize(dim,dim);
        shortcut.setMaxSize(dim,dim);
        shortcut.setMinSize(dim, dim);
        return shortcut;
    }

    private void designShortcut(VBox vBox, Color backgroundColor, Pos position, int dim) {
        vBox.setPrefSize(dim,dim);
        vBox.setMaxSize(dim,dim);
        vBox.setMinSize(dim, dim);
        vBox.setCursor(Cursor.HAND);
        vBox.setBackground(new Background(new BackgroundFill(backgroundColor, new CornerRadii(20), Insets.EMPTY)));
        vBox.setAlignment(position);
        vBox.setPadding(new Insets(10));
        vBox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(20), new BorderWidths(5))));
        vBox.setEffect(new DropShadow(10, Color.BLACK));
    }

    private void updateWeatherAndCalendarShortcut() throws Exception {
        String city = FileParser.getUserInfo(Data.getUsername(),"-City");
        String country = FileParser.getUserInfo(Data.getUsername(),"-Country");
        if (city.isEmpty() || country.isEmpty()) {
            System.out.println("It seems like you haven't completed your location yet.");
            city = "Maastricht";
            country = "NL";
        }
        mainScreen.weatherDisplay.setLocation(city, country, false);
        VBox weatherShortcut = mainScreen.weatherDisplay.getWeatherShortcut();
        designShortcut(weatherShortcut, Color.LIGHTBLUE, Pos.CENTER, 200);
        String finalCity = city;
        String finalCountry = country;
        weatherShortcut.setOnMouseClicked(e -> {
            try {
                mainScreen.setWeatherDisplay(finalCity, finalCountry, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox calendarShortcut = mainScreen.calendarDisplay.getCalendarShortcut(mainScreen.todaysRemindersShortcut);
        designShortcut(calendarShortcut, Color.DARKORANGE.brighter(), Pos.TOP_CENTER, 200);
        calendarShortcut.setOnMouseClicked(e-> mainScreen.displaySkill(mainScreen.calendarDisplay,"calendar"));

        if (update) {
            shortcutsHBox1.getChildren().remove(0);
            shortcutsHBox2.getChildren().remove(0);
        }
        shortcutsHBox1.getChildren().add(0, weatherShortcut);
        shortcutsHBox2.getChildren().add(0, calendarShortcut);
        update = true;
    }

    private void editMenu(VBox menu) {
        menu.setAlignment(Pos.TOP_CENTER);
        menu.setBorder(mainScreen.border);
        menu.prefHeightProperty().bind(mainScreen.root.heightProperty().subtract(mainScreen.borderWidth*2));
        menu.prefWidthProperty().bind(mainScreen.root.widthProperty().subtract(mainScreen.chat.prefWidthProperty()).subtract(mainScreen.borderWidth*2));
        menu.setScaleX(0.8);
        menu.setScaleY(0.8);
    }

    private void setMenuBackground(VBox vBox) {
        if (mainScreen.themeColor.equals(Color.BLACK)) {
            vBox.setBackground(new Background(new BackgroundFill(new Color(mainScreen.themeColor.getRed(), mainScreen.themeColor.getGreen(),mainScreen.themeColor.getBlue(), 0.4), CornerRadii.EMPTY, Insets.EMPTY))); }
        else if (mainScreen.themeColor.equals(Color.LIGHTGRAY)) {
            vBox.setBackground(new Background(new BackgroundFill(new Color(mainScreen.themeColor.getRed(), mainScreen.themeColor.getGreen(), mainScreen.themeColor.getBlue(), 0.29), CornerRadii.EMPTY, Insets.EMPTY))); }
        else { vBox.setBackground(new Background(new BackgroundFill(new Color(0.2, 0.35379, 0.65, 0.5), CornerRadii.EMPTY, Insets.EMPTY))); }
    }

    private void designOptionButton(Button button) {
        button.setBackground(new Background(new BackgroundFill(Color.SLATEGREY.darker(), new CornerRadii(8,8,8,8,false), Insets.EMPTY)));
        button.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 20)));
        button.setPrefWidth(250);
        button.setTextFill(Color.LIGHTGRAY);
        button.setCursor(Cursor.HAND);
    }
}