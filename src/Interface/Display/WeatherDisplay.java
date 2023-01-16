package Interface.Display;

import Interface.Screens.MainScreen;
import Skills.Weather.WeatherFetch;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class WeatherDisplay extends VBox {
    private String cityName;
    private String countryName;
    private Map<String, Object> weatherData;
    private double imgDim;
    private HBox top;
    private Label city;
    private Button change;
    private Button exit;
    private VBox current;
    private ImagePattern pattern;
    private HBox hLBox;
    private HBox hourlyDailyChoice;
    private VBox dailyVBox;
    private HBox hourlyHBox;
    private ScrollPane scrollPaneDaily;
    private ScrollPane scrollPaneHourly;
    private Boolean selectHourly;
    private String addressTitle;
    private String currentTemp;
    private String currentSummary;

    private MainScreen mainScreen;

    public WeatherDisplay(MainScreen mainScreen) {
        cityName = "Maastricht";
        countryName = "NL";
        this.mainScreen = mainScreen;
    }

    public void setLocation(String city, String country, Boolean selectHourly) throws Exception {
        this.cityName = city;
        this.countryName = country;
        this.selectHourly = selectHourly;

        getData();
        setTop();
        setCurrent();
        setDailyHourlyChoice();
        setDaily();
        setHourly();

        getChildren().clear();
        if (selectHourly) { getChildren().addAll(top, current, hourlyDailyChoice, scrollPaneHourly); }
        else { getChildren().addAll(top, current, hourlyDailyChoice, scrollPaneDaily); }
        Color bgColor = Color.WHITE;
        setBackground(new Background(new BackgroundFill(new Color(bgColor.getRed(),bgColor.getGreen(), bgColor.getBlue(), 0.38), CornerRadii.EMPTY, Insets.EMPTY)));
        setMaxHeight(Double.MAX_VALUE);
        setMinHeight(Double.MIN_VALUE);
    }

    private void setDailyHourlyChoice() {
        hourlyDailyChoice = new HBox();
        hourlyDailyChoice.setAlignment(Pos.CENTER);
        Button hourly = new Button("Hourly");
        Button daily = new Button("Daily");

        hourly.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 15)));
        hourly.setPrefWidth(70);
        hourly.setTextFill(Color.WHITESMOKE);
        hourly.setCursor(Cursor.HAND);

        daily.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 15)));
        daily.setPrefWidth(70);
        daily.setTextFill(Color.WHITESMOKE);
        daily.setCursor(Cursor.HAND);

        if (selectHourly) { hourly.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(10,0,0,10,false), Insets.EMPTY)));
            daily.setBackground(new Background(new BackgroundFill(Color.GRAY.darker().darker(), new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
        }
        else { hourly.setBackground(new Background(new BackgroundFill(Color.GRAY.darker().darker(), new CornerRadii(10,0,0,10,false), Insets.EMPTY)));
            daily.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
        }

        hourly.setOnMouseClicked(e -> {
            if(getChildren().contains(scrollPaneDaily)) {
                getChildren().remove(scrollPaneDaily);
                getChildren().add(scrollPaneHourly);
                hourly.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(10,0,0,10,false), Insets.EMPTY)));
                daily.setBackground(new Background(new BackgroundFill(Color.GRAY.darker().darker(), new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
            }
            else {
                getChildren().remove(scrollPaneHourly);
                getChildren().add(scrollPaneDaily);
                hourly.setBackground(new Background(new BackgroundFill(Color.GRAY.darker().darker(), new CornerRadii(10,0,0,10,false), Insets.EMPTY)));
                daily.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
            }
        });

        daily.setOnMouseClicked(e -> {
            if(getChildren().contains(scrollPaneDaily)) {
                getChildren().remove(scrollPaneDaily);
                getChildren().add(scrollPaneHourly);
                hourly.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(10,0,0,10,false), Insets.EMPTY)));
                daily.setBackground(new Background(new BackgroundFill(Color.GRAY.darker().darker(), new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
            }
            else {
                getChildren().remove(scrollPaneHourly);
                getChildren().add(scrollPaneDaily);
                hourly.setBackground(new Background(new BackgroundFill(Color.GRAY.darker().darker(), new CornerRadii(10,0,0,10,false), Insets.EMPTY)));
                daily.setBackground(new Background(new BackgroundFill(Color.DARKBLUE.darker(), new CornerRadii(0,10,10,0,false), Insets.EMPTY)));
            }
        });

        hourlyDailyChoice.getChildren().addAll(hourly, daily);
    }

    public String currentDataString() {
        return ("Currently: " + currentTemp + " °C" + " (" + currentSummary + "). ");
    }

    private void getData() throws Exception {
        String rawWeatherData = WeatherFetch.getWeather(cityName, countryName);
        //System.out.println(rawWeatherData);   //For testing
        List<String[]> separateLines = new ArrayList<>();
        rawWeatherData.lines().forEach(s -> separateLines.add(s.split(",")));

        addressTitle = separateLines.get(1)[5];  //getting full address for the title
        int charCount = addressTitle.length() - addressTitle.replace("\"", "").length();    //count for " character
        int ii = 6;
        while(charCount == 1) {
            addressTitle += ", " + separateLines.get(1)[ii];    ii++;
            charCount = addressTitle.length() - addressTitle.replace("\"", "").length();
        }
        addressTitle = addressTitle.replace("\"", "");

        ArrayList<String> dayH = new ArrayList<>();
        ArrayList<String> dayL = new ArrayList<>();
        ArrayList<String> daySummary = new ArrayList<>();

        for(int i = 1; i <= 8; i++) {
            int lineLength = separateLines.get(i).length;

            //getting daily min and max temperature
            int cnt = 0;
            for(int j = 0; j < lineLength; j++) {
                if(separateLines.get(i)[j].equals(countryName+"\"")) {
                    if(cnt == 1) {
                        dayH.add(separateLines.get(i)[j+3]);
                        dayL.add(separateLines.get(i)[j+2]);
                    }
                    cnt++;
                }
            }

            //getting daily summaries
            String summary = separateLines.get(i)[lineLength-1];
            int count = summary.length() - summary.replace("\"", "").length();    //count for " character
            if(count == 1) {
                summary = separateLines.get(i)[lineLength-2].replace("\"", "") + "," + separateLines.get(i)[lineLength-1].replace("\"", "");
            }
            else {
                summary = summary.replace("\"", "");
            }
            daySummary.add(summary);
        }

        weatherData = new HashMap<>();
        Map<String, String> currentData = new HashMap<>();
        ArrayList<Object> dailyData = new ArrayList<>();

        DayOfWeek currentDayOfWeek = DayOfWeek.of(LocalDate.now().getDayOfWeek().getValue());
        String[] days = {currentDayOfWeek.plus(1).name(), currentDayOfWeek.plus(2).name(), currentDayOfWeek.plus(3).name(), currentDayOfWeek.plus(4).name(), currentDayOfWeek.plus(5).name(), currentDayOfWeek.plus(6).name(), currentDayOfWeek.plus(7).name()};

        for(int i = 0; i<7; i++) {
            Map<String, String> daily = new HashMap<>();
            daily.put("day", days[i]);
            daily.put("high", dayH.get(i+1));
            daily.put("low", dayL.get(i+1));
            daily.put("summary", daySummary.get(i+1));

            dailyData.add(daily);
        }
        weatherData.put("daily", dailyData);

        currentData.put("high", dayH.get(0));
        currentData.put("low", dayL.get(0));
        getHourlyData(currentData);
        weatherData.put("current", currentData);
    }

    private void getHourlyData(Map<String, String> currentData) throws Exception {
        String rawHourlyWeatherData = WeatherFetch.getHourlyWeather(cityName, countryName);
        //System.out.println(rawHourlyWeatherData);   //For testing
        List<String[]> separateLines = new ArrayList<>();
        rawHourlyWeatherData.lines().forEach(s -> separateLines.add(s.split(",")));

        //getting current and hourly data
        Map<String, ArrayList<String>> hourlyData = new HashMap<>();
        ArrayList<String> hours = new ArrayList<>();
        ArrayList<String> hourlyTemp = new ArrayList<>();
        ArrayList<String> iconDescriptions = new ArrayList<>();

        int cnt = 0;
        for(int i=1; i<26; i++) {
            for(int j = 0; j < separateLines.get(i).length; j++) {
                if (separateLines.get(i)[j].equals(countryName + "\"")) {
                    if (cnt == 1) {
                        hourlyTemp.add(separateLines.get(i)[j+2]);
                        cnt = 0;
                    } else {
                        String h = separateLines.get(i)[j+1].replace("\"", "");
                        String[] hr = h.split(" ", 0);
                        hours.add(hr[1].substring(0, hr[1].length()-3));
                        cnt++;
                    }
                }
            }
            String iconD;
            String summary = separateLines.get(i)[separateLines.get(i).length-1];
            int count = summary.length() - summary.replace("\"", "").length();    //count for " character
            if(count == 1) {
                summary = separateLines.get(i)[separateLines.get(i).length-2].replace("\"", "") + "," + separateLines.get(i)[separateLines.get(i).length-1].replace("\"", "");
                iconD = separateLines.get(i)[separateLines.get(i).length-3].replace("\"", "");
            }
            else {
                summary = summary.replace("\"", "");
                iconD = separateLines.get(i)[separateLines.get(i).length-2].replace("\"", "");
            }

            if(iconD.contains("night")) { iconDescriptions.add(iconD); }
            else if (iconD.equals("cloudy") && summary.equals("Partially cloudy")) { iconDescriptions.add("Overcast"); }
            else { iconDescriptions.add(summary); }

            if(i==1) { currentSummary = summary; }
        }

        hourlyData.put("temp", hourlyTemp);
        hourlyData.put("hours", hours);
        hourlyData.put("icons", iconDescriptions);
        weatherData.put("hourly", hourlyData);

        currentTemp = hourlyTemp.get(0);
        currentData.put("icon", iconDescriptions.get(0));
        currentData.put("currentSummary", currentSummary);
        currentData.put("temp", currentTemp + "  °C");
    }

    private void setTop() {
        top = new HBox(60);
        top.setAlignment(Pos.CENTER);
        top.setPrefHeight(60);
        top.setMinHeight(60);
        top.setBackground(new Background(new BackgroundFill(MainScreen.themeColor, CornerRadii.EMPTY, Insets.EMPTY)));

        city = new Label(addressTitle);
        city.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 29));
        if (mainScreen.themeColor.equals(Color.LIGHTGRAY)) { city.setTextFill(Color.BLACK); }
        else { city.setTextFill(Color.WHITE); }
        city.setAlignment(Pos.CENTER);

        change = new Button("Change");
        change.setCursor(Cursor.HAND);
        change.setBackground(Background.EMPTY);
        change.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 13));
        if (mainScreen.themeColor.equals(Color.LIGHTGRAY)) { change.setTextFill(Color.BLUEVIOLET); }
        else { change.setTextFill(Color.NAVAJOWHITE); }
        change.setBorder(new Border(new BorderStroke(Color.GRAY.darker().darker(), BorderStrokeStyle.SOLID, new CornerRadii(3,3,3,3,false), new BorderWidths(3))));
        change.setAlignment(Pos.CENTER);
        change.setOnAction(e -> setChangeLocation());

        exit = new Button("x");
        exit.setCursor(Cursor.HAND);
        exit.setBackground(Background.EMPTY);
        exit.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
        exit.setTextFill(Color.DARKRED);
        exit.setBorder(null);
        exit.setAlignment(Pos.CENTER_RIGHT);
        exit.setTranslateY(-7);
        exit.setOnAction(e -> {
            try {
                mainScreen.setMenu("MainMenu");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Region region2 = new Region();
        HBox.setHgrow(region2, Priority.ALWAYS);

        top.getChildren().addAll(region1, city, change, region2, exit);
    }

    private void setCurrent() throws FileNotFoundException {
        imgDim = 100;
        current = new VBox(23);
        current.setBackground(Background.EMPTY);
        current.setAlignment(Pos.CENTER);
        current.setPadding(new Insets(15, 0, 30, 0));

        Map<String, String> currentWeather = (Map<String, String>) weatherData.get("current");

        Rectangle currentConditionImage = new Rectangle(0, 0, imgDim, imgDim);
        currentConditionImage.setArcWidth(50.0);
        currentConditionImage.setArcHeight(50.0);
        pattern = new ImagePattern(getImage(currentWeather.get("icon")));
        currentConditionImage.setFill(pattern);
        currentConditionImage.setEffect(new DropShadow(20, Color.BLACK));

        Label currentConditionLabel = new Label(currentWeather.get("currentSummary"));
        currentConditionLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        currentConditionLabel.setTextFill(Color.DARKRED.darker().darker());

        Label currently = new Label("Currently: ");
        currently.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));
        currently.setTextFill(Color.DARKRED.darker().darker());
        Label currentTemp = new Label(currentWeather.get("temp"));
        currentTemp.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 25));
        currentTemp.setTextFill(Color.BLACK);

        HBox currentTempBox = new HBox(20);
        currentTempBox.setAlignment(Pos.CENTER);
        currentTempBox.getChildren().addAll(currently, currentTemp);

        Label h = new Label("H:");
        h.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
        h.setTextFill(Color.DARKBLUE.darker().darker());
        Label hTemp = new Label(currentWeather.get("high")+"°");
        hTemp.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 19));
        hTemp.setTextFill(Color.BLACK);
        Label l = new Label("L:");
        l.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
        l.setTextFill(Color.DARKBLUE.darker().darker());
        Label lTemp = new Label(currentWeather.get("low")+"°");
        lTemp.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 19));
        lTemp.setTextFill(Color.BLACK);

        hLBox = new HBox(12);
        hLBox.setAlignment(Pos.CENTER);
        hLBox.getChildren().addAll(h, hTemp, l, lTemp);

        current.getChildren().addAll(currentConditionImage, currentConditionLabel, currentTempBox, hLBox);
    }

    private void setDaily() {
        dailyVBox = new VBox(23);
        dailyVBox.setBackground(Background.EMPTY);
        dailyVBox.setAlignment(Pos.BOTTOM_CENTER);
        dailyVBox.setPadding(new Insets(0, 68, 0, 110));

        ArrayList<Map<String, String>> dailyForecast = (ArrayList<Map<String, String>>) weatherData.get("daily");
        for(int i = -1; i < dailyForecast.size(); i++) {
            HBox daily = new HBox();
            Label day = new Label();
            Label high = new Label();
            Label low = new Label();
            Label summary = new Label();

            if(i>-1) {
                day = new Label(dailyForecast.get(i).get("day"));
                day.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
                day.setTextFill(Color.GRAY.darker().darker().darker());
                high = new Label(dailyForecast.get(i).get("high") + " °");
                high.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
                high.setTextFill(Color.DARKBLUE.darker().darker());
                low = new Label(dailyForecast.get(i).get("low") + " °");
                low.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
                low.setTextFill(Color.DARKBLUE.darker().darker());
                summary = new Label(dailyForecast.get(i).get("summary"));
                summary.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
                summary.setTextFill(Color.BLACK);
                summary.setWrapText(true);
            }
            else {
                day.setText("");
                day.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
                day.setTextFill(Color.BLACK);
                high.setText("High");
                high.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
                high.setTextFill(Color.GRAY.darker().darker());
                low.setText("Low");
                low.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 18));
                low.setTextFill(Color.GRAY.darker().darker());
            }
            day.prefWidthProperty().bind(dailyVBox.widthProperty().divide(3));
            high.prefWidthProperty().bind(dailyVBox.widthProperty().divide(9/2));
            low.prefWidthProperty().bind(dailyVBox.widthProperty().divide(9/2));
            summary.prefWidthProperty().bind(dailyVBox.widthProperty().divide(9/2));

            daily.getChildren().addAll(day, high, low, summary);
            daily.setAlignment(Pos.CENTER);
            dailyVBox.getChildren().add(daily);
        }
        scrollPaneDaily = new ScrollPane(dailyVBox);
        scrollPaneDaily.setPadding(new Insets(40, 0, 40, 0));
        scrollPaneDaily.setFitToWidth(true);
        scrollPaneDaily.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPaneDaily.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPaneDaily.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    private void setHourly() throws FileNotFoundException {
        imgDim = 60;
        hourlyHBox = new HBox(84);
        hourlyHBox.setBackground(Background.EMPTY);
        hourlyHBox.setAlignment(Pos.BOTTOM_CENTER);
        hourlyHBox.setPadding(new Insets(80, 40, 30, 40));

        Map<String, ArrayList<String>> hourlyForecast = (Map<String, ArrayList<String>>) weatherData.get("hourly");

        for(int i=1; i<25; i++) {
            VBox hourlyBox = new VBox(35);
            hourlyBox.setBackground(Background.EMPTY);
            hourlyBox.setAlignment(Pos.CENTER);
            hourlyBox.setPadding(new Insets(0, 0, 35, 0));

            Label hour = new Label(hourlyForecast.get("hours").get(i));
            hour.setFont(Font.font("Arial", FontWeight.BOLD, 19));
            hour.setTextFill(Color.DARKRED.darker().darker());
            hour.setUnderline(true);

            Rectangle currentConditionImage = new Rectangle(0, 0, 60, 60);
            currentConditionImage.setArcWidth(40.0);
            currentConditionImage.setArcHeight(40.0);
            ImagePattern pattern = new ImagePattern(getImage(hourlyForecast.get("icons").get(i)));
            currentConditionImage.setFill(pattern);
            currentConditionImage.setEffect(new DropShadow(20, Color.BLACK));

            Label hourlyTemp = new Label(hourlyForecast.get("temp").get(i) + " °");
            hourlyTemp.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 20));
            hourlyTemp.setTextFill(Color.BLACK);

            hourlyBox.getChildren().addAll(hour, currentConditionImage, hourlyTemp);
            hourlyHBox.getChildren().addAll(hourlyBox);
        }
        scrollPaneHourly = new ScrollPane(hourlyHBox);
        scrollPaneHourly.setPadding(new Insets(15, 15, 0, 15));
        scrollPaneHourly.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPaneHourly.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPaneHourly.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void setChangeLocation() {
        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(7))));
        vBox.setBackground(new Background(new BackgroundFill(Color.LIGHTSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setOpacity(0.85);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(vBox, 440, 400));
        stage.show();

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2 - 280);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4 + 110);

        Label changeLocation = new Label("Change Location");
        changeLocation.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));
        changeLocation.setTextFill(Color.WHITE);
        changeLocation.setAlignment(Pos.TOP_LEFT);
        changeLocation.setTranslateX(15);

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
        if (MainScreen.themeColor.equals(Color.LIGHTGRAY)) {
            topBox.setBackground(new Background(new BackgroundFill(Color.GRAY.darker(), CornerRadii.EMPTY, Insets.EMPTY)));
        }
        else {
            topBox.setBackground(new Background(new BackgroundFill(MainScreen.themeColor, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        topBox.getChildren().addAll(changeLocation, region, exit);

        Label cityLabel = new Label("City: ");
        cityLabel.setPrefWidth(100);
        cityLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        cityLabel.setTextFill(Color.LIGHTSLATEGRAY.darker().darker());

        TextField city = new TextField();
        city.setPromptText("Maastricht");
        city.setFont(Font.font("Arial", FontWeight.BOLD,15));
        city.setStyle("-fx-text-fill: dimgray; -fx-prompt-text-fill: lightgray");
        city.setMaxWidth(200);

        HBox cityBox = new HBox(20);
        cityBox.setAlignment(Pos.CENTER);
        cityBox.getChildren().addAll(cityLabel, city);

        Label countryLabel = new Label("Country: ");
        countryLabel.setPrefWidth(100);
        countryLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 16));
        countryLabel.setTextFill(Color.LIGHTSLATEGRAY.darker().darker());

        TextField country = new TextField();
        country.setPromptText("NL");
        country.setFont(Font.font("Arial", FontWeight.BOLD,15));
        country.setStyle("-fx-text-fill: dimgray; -fx-prompt-text-fill: lightgray");
        country.setMaxWidth(200);

        HBox countryBox = new HBox(20);
        countryBox.setAlignment(Pos.CENTER);
        countryBox.getChildren().addAll(countryLabel, country);

        VBox textFieldBox = new VBox(25);
        textFieldBox.setBackground(Background.EMPTY);
        textFieldBox.setPadding(new Insets(70, 0, 0, 0));
        textFieldBox.setAlignment(Pos.CENTER);
        textFieldBox.getChildren().addAll(cityBox, countryBox);

        Label warning = new Label();
        warning.setFont(Font.font("Verdana", FontWeight.BOLD, 12));
        warning.setTextFill(Color.DARKRED.darker());
        warning.setTranslateY(-15);
        warning.setTranslateY(20);

        Button change = new Button("Change");
        change.setCursor(Cursor.HAND);
        change.setBackground(Background.EMPTY);
        change.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
        change.setTextFill(Color.DARKGREEN.darker());
        change.setBorder(new Border(new BorderStroke(Color.LIGHTSLATEGRAY.brighter(), BorderStrokeStyle.SOLID, new CornerRadii(3,3,3,3,false), new BorderWidths(3))));
        change.setAlignment(Pos.CENTER);
        change.setTranslateY(30);
        change.setOnAction(e -> {
            try {
                mainScreen.setWeatherDisplay(city.getText(), country.getText(), false);
                stage.close();
            } catch (Exception ex) {
                warning.setText("Please enter a valid location.");
            }
        });

        vBox.getChildren().addAll(topBox, textFieldBox, warning, change);
    }

    public VBox getWeatherShortcut() {
        imgDim = 67;
        VBox weatherShortcut = new VBox(17);

        Label address = new Label(addressTitle);
        address.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 15));
        address.setTextFill(Color.BLACK);
        address.setWrapText(true);
        address.setAlignment(Pos.CENTER_LEFT);

        Rectangle currentCondImage = new Rectangle(0, 0, imgDim, imgDim);
        currentCondImage.setArcWidth(50.0);
        currentCondImage.setArcHeight(50.0);
        currentCondImage.setFill(pattern);
        currentCondImage.setEffect(new DropShadow(20, Color.BLACK));

        VBox vBox = new VBox(10);
        vBox.setAlignment(Pos.CENTER_LEFT);

        HBox temp = new HBox();
        temp.setAlignment(Pos.CENTER_LEFT);
        Label currentTempLabel = new Label(currentTemp);
        currentTempLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 25));
        currentTempLabel.setTextFill(Color.BLACK);
        Label celsius = new Label("  °C");
        celsius.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 15));
        celsius.setTextFill(Color.BLACK);
        temp.getChildren().addAll(currentTempLabel, celsius);

        Label currentCondLabel = new Label(currentSummary);
        currentCondLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 14));
        currentCondLabel.setTextFill(Color.BLACK);
        currentCondLabel.setWrapText(true);
        vBox.getChildren().addAll(temp, currentCondLabel);

        HBox hBox = new HBox(15);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(currentCondImage, vBox);
        hLBox.setScaleX(0.85);
        hLBox.setScaleY(0.85);
        hLBox.setAlignment(Pos.CENTER_LEFT);
        weatherShortcut.getChildren().addAll(address, hBox, hLBox);

        return weatherShortcut;
    }

    private Image getImage(String status) throws FileNotFoundException {
        Image img;
        if(status.contains(",")) {
            String[] s = status.split(",");
            status = s[0];
        }
        switch (status) {
            case "Clear":
                img = new Image(new FileInputStream("src/res/weatherIcons/day_clear.png"),imgDim,imgDim,false,true);
                break;
            case "clear-night":
                img = new Image(new FileInputStream("src/res/weatherIcons/night_half_moon_clear.png"),imgDim,imgDim,false,true);
                break;
            case "Partially cloudy":
                img = new Image(new FileInputStream("src/res/weatherIcons/day_partial_cloud.png"),imgDim,imgDim,false,true);
                break;
            case "partly-cloudy-night":
                img = new Image(new FileInputStream("src/res/weatherIcons/night_full_moon_partial_cloud.png"),imgDim,imgDim,false,true);
                break;
            case "Overcast":
                img =  new Image(new FileInputStream("src/res/weatherIcons/overcast.png"),imgDim,imgDim,false,true);
                break;
            case "Thunderstorm":
                img = new Image(new FileInputStream("ssrc/res/weatherIcons/rain_thunder.png"),imgDim,imgDim,false,true);
                break;
            case "Thunderstorm Without Precipitation":
                img = new Image(new FileInputStream("src/res/weatherIcons/thunder.png"),imgDim,imgDim,false,true);
                break;
            case "Rain": case "Rain Showers": case "Heavy Rain": case "Light Rain": case "Drizzle": case "Heavy Drizzle": case "Light Drizzle":
            case "Heavy Drizzle/Rain": case "Freezing Drizzle/Freezing Rain": case "Heavy Freezing Drizzle/Freezing Rain":
            case "Light Freezing Drizzle/Freezing Rain": case "Heavy Freezing Rain": case "Light Freezing Rain":
                img = new Image(new FileInputStream("src/res/weatherIcons/rain.png"),imgDim,imgDim,false,true);
                break;
            case "Snow And Rain Showers": case "Heavy Rain And Snow": case "Light Rain And Snow":
                img = new Image(new FileInputStream("src/res/weatherIcons/sleet.png"),imgDim,imgDim,false,true);
                break;
            case "Snow": case "Snow Showers": case "Heavy Snow": case "Light Snow": case "Blowing Or Drifting Snow":
                img = new Image(new FileInputStream("src/res/weatherIcons/snow.png"),imgDim,imgDim,false,true);
                break;
            case "Mist":
                img = new Image(new FileInputStream("src/res/weatherIcons/mist.png"),imgDim,imgDim,false,true);
                break;
            case "Squalls":
                img = new Image(new FileInputStream("src/res/weatherIcons/wind.png"),imgDim,imgDim,false,true);
                break;
            case "Fog": case "Freezing Fog":
                img = new Image(new FileInputStream("src/res/weatherIcons/fog.png"),imgDim,imgDim,false,true);
                break;
            case "Funnel Cloud/Tornado":
                img = new Image(new FileInputStream("src/res/weatherIcons/tornado.png"),imgDim,imgDim,false,true);
                break;
            default:
                return new Image(new FileInputStream("src/res/weatherIcons/unknown.png"),imgDim,imgDim,false,true);
        }
        return img;
    }
}