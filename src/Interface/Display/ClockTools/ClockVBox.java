package Interface.Display.ClockTools;

import Interface.Display.ClockAppDisplay;
import Interface.Screens.MainScreen;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

public class ClockVBox extends VBox {
    private VBox currentClock;
    private VBox addedClocks;
    private ScrollPane addedClocksScrollPane;
    public String[] listOfZoneIDs;
    public List<String> tempTimeZoneIDs;

    public ClockVBox() {
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(90,0,45,0));

        createCurrentClock();
        addedClocks = new VBox(20);
        addedClocks.setAlignment(Pos.CENTER);
        addedClocks.setBackground(new Background(new BackgroundFill(new Color(0.1,0.1, 0.12, 0.3), CornerRadii.EMPTY, Insets.EMPTY)));
        addedClocksScrollPane = new ScrollPane(addedClocks);
        addedClocksScrollPane.setFitToWidth(true);
        addedClocksScrollPane.setMaxHeight(310);
        addedClocksScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        addedClocksScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        addedClocksScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        addedClocksScrollPane.vvalueProperty().bind(addedClocks.heightProperty());  //updating scrollPane

        getChildren().addAll(currentClock, addedClocksScrollPane);

        //adding all options of zoneIDs to the array
        tempTimeZoneIDs = new ArrayList<>(ZoneId.getAvailableZoneIds());
        listOfZoneIDs = new String[tempTimeZoneIDs.size()];
        tempTimeZoneIDs.toArray(listOfZoneIDs);
    }

    private void createCurrentClock() {
        currentClock = new VBox(20);
        currentClock.setAlignment(Pos.CENTER);
        currentClock.setPadding(new Insets(0,0,60,0));

        Label digitalClock = new Label();
        digitalClock.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 58));
        digitalClock.setTextFill(ClockAppDisplay.color.darker().darker());
        digitalClock.setAlignment(Pos.CENTER);
        bindClockLabelToTime(digitalClock);

        LocalDate currentDate = LocalDate.now();
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        int dayOfMonth = currentDate.getDayOfMonth();
        Month month = currentDate.getMonth();

        Label dateLabel = new Label(dayOfWeek.toString()+", "+dayOfMonth+" "+month);
        dateLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        dateLabel.setTextFill(ClockAppDisplay.color.darker());
        dateLabel.setAlignment(Pos.CENTER);

        Button addNew = new Button("+ Add");
        addNew.setCursor(Cursor.HAND);
        addNew.setBorder(new Border(new BorderStroke(Color.GREEN.darker(), BorderStrokeStyle.SOLID, new CornerRadii(3,3,3,3,false), new BorderWidths(3))));
        addNew.setBackground(Background.EMPTY);
        addNew.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 18));
        addNew.setTextFill(Color.DARKGREEN.darker());
        addNew.setAlignment(Pos.CENTER);
        addNew.setTranslateY(20);
        addNew.setOnAction(e -> setAddNewClockOptions());

        currentClock.getChildren().addAll(digitalClock, dateLabel, addNew);
    }

    private void setAddNewClockOptions() {
        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(7))));
        vBox.setBackground(new Background(new BackgroundFill(Color.LIGHTSLATEGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setOpacity(0.91);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(vBox, 400, 230));
        stage.show();

        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2 - 280);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4 + 110);

        Label chooseACity = new Label("Choose a city");
        chooseACity.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 17));
        chooseACity.setTextFill(Color.WHITE);
        chooseACity.setAlignment(Pos.TOP_LEFT);
        chooseACity.setTranslateX(15);

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
        if (MainScreen.themeColor.equals(Color.BLACK)) {
            topBox.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        else if (MainScreen.themeColor.equals(Color.LIGHTGRAY)) {
            topBox.setBackground(new Background(new BackgroundFill(Color.GRAY.darker(), CornerRadii.EMPTY, Insets.EMPTY)));
        }
        else {
            topBox.setBackground(new Background(new BackgroundFill(ClockAppDisplay.color, CornerRadii.EMPTY, Insets.EMPTY)));
        }
        topBox.getChildren().addAll(chooseACity, region, exit);

        ComboBox<String> cmb = new ComboBox<>();
        cmb.setTooltip(new Tooltip());
        cmb.getItems().addAll(listOfZoneIDs);
        cmb.setStyle("-fx-font: 14px \"Tahoma\";");
        new ComboBoxAutoComplete<>(cmb);

        Button add = new Button("Add");
        add.setCursor(Cursor.HAND);
        add.setBackground(Background.EMPTY);
        add.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 14));
        add.setTextFill(Color.DARKGREEN.darker());
        add.setBorder(new Border(new BorderStroke(Color.GRAY.darker(), BorderStrokeStyle.SOLID, new CornerRadii(3,3,3,3,false), new BorderWidths(3))));
        add.setAlignment(Pos.CENTER);
        add.setTranslateY(30);
        add.setOnAction(e -> {if(cmb.getValue() != null) {addClock(cmb.getValue()); stage.close();}});

        vBox.getChildren().addAll(topBox, cmb, add);
    }

    public String getTimeFromZoneID(String zoneString) {
        ZoneId zoneId = ZoneId.of(zoneString);
        LocalTime localTime = LocalTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return("The time in " + zoneString + " is " + localTime.format(formatter) + ".");
    }

    private ArrayList<String> addedZoneStrings = new ArrayList<>();
    public void addClock(String zoneString) {
        Boolean add = true;
        if (!addedZoneStrings.isEmpty()) {
            for (int i = 0; i<addedZoneStrings.size(); i++) {
                if (addedZoneStrings.get(i).equals(zoneString)) {
                    add = false;    //already exists
                }
            }
        }
        if (add) {
            addedZoneStrings.add(zoneString);

            HBox hBox = new HBox(20);
            hBox.setAlignment(Pos.CENTER);

            Label zoneIDLabel = new Label(zoneString);
            zoneIDLabel.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 18));
            zoneIDLabel.setTextFill(ClockAppDisplay.color.darker());
            zoneIDLabel.setTextAlignment(TextAlignment.CENTER);
            zoneIDLabel.setWrapText(true);
            zoneIDLabel.setAlignment(Pos.CENTER);

            Label newClock = new Label();
            newClock.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 29));
            newClock.setTextFill(ClockAppDisplay.color.darker().darker());
            newClock.setAlignment(Pos.CENTER);

            ZoneId zoneId = ZoneId.of(zoneString);
            bindClockLabelToTime(newClock, zoneId);

            //getting timezone offset
            LocalDateTime localDateTime = LocalDateTime.now();
            ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
            ZoneOffset zoneOffset = zonedDateTime.getOffset();

            Label zoneOffsetLabel = new Label("(UTC" + zoneOffset.getId() + ")");
            zoneOffsetLabel.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 17));
            zoneOffsetLabel.setTextFill(ClockAppDisplay.color.darker());
            zoneOffsetLabel.setAlignment(Pos.CENTER);

            zoneIDLabel.prefWidthProperty().bind(addedClocks.widthProperty().divide(4.8));  //fixing position
            newClock.prefWidthProperty().bind(addedClocks.widthProperty().divide(6));
            zoneOffsetLabel.prefWidthProperty().bind(addedClocks.widthProperty().divide(6));

            Label deleteClock = new Label("x");
            deleteClock.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 18));
            deleteClock.setTextFill(Color.DARKRED);
            deleteClock.setCursor(Cursor.HAND);
            deleteClock.setBackground(Background.EMPTY);
            deleteClock.setBorder(Border.EMPTY);
            deleteClock.setOnMouseClicked(e -> {
                addedClocks.getChildren().remove(hBox);
                addedZoneStrings.remove(zoneString);
            });

            hBox.getChildren().addAll(zoneIDLabel, newClock, zoneOffsetLabel, deleteClock);
            addedClocks.getChildren().add(hBox);
        }
    }

    private void bindClockLabelToTime(Label digitalClock) {
        //digital clock updates per second
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0),
                actionEvent -> {
                    Calendar time = Calendar.getInstance();
                    String hourString = pad(2, ' ', time.get(Calendar.HOUR) == 0 ? "12" : time.get(Calendar.HOUR) + "");
                    String minuteString = pad(2, '0', time.get(Calendar.MINUTE) + "");
                    String secondString = pad(2, '0', time.get(Calendar.SECOND) + "");
                    String ampmString = time.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
                    digitalClock.setText(hourString + ":" + minuteString + ":" + secondString + " " + ampmString);
                }), new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void bindClockLabelToTime(Label digitalClock, ZoneId zoneId) {
        //digital clock updates per minute
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0),
                actionEvent -> {
                    LocalTime localTime = LocalTime.now(zoneId);
                    Calendar time = Calendar.getInstance();
                    time.set(0, 0, 0, localTime.getHour(), localTime.getMinute(), localTime.getSecond());

                    //Calendar time = Calendar.getInstance();
                    String hourString = pad(2, ' ', time.get(Calendar.HOUR) == 0 ? "12" : time.get(Calendar.HOUR) + "");
                    String minuteString = pad(2, '0', time.get(Calendar.MINUTE) + "");
                    String ampmString = time.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
                    digitalClock.setText(hourString + ":" + minuteString + " " + ampmString);
                }), new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    //returns padded string from specified width
    public static String pad(int fieldWidth, char padChar, String s) {
        return String.valueOf(padChar).repeat(Math.max(0, fieldWidth - s.length())) + s;
    }

    public VBox getClockShortcut() {
        VBox clockShortcut = new VBox(17);

        Label digitalClock = new Label();
        digitalClock.setFont(Font.font("Tahoma", FontWeight.EXTRA_BOLD, 20));
        digitalClock.setTextFill(Color.WHITE);
        digitalClock.setAlignment(Pos.CENTER);
        bindClockLabelToTime(digitalClock);

        LocalDate currentDate = LocalDate.now();
        DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
        int dayOfMonth = currentDate.getDayOfMonth();
        Month month = currentDate.getMonth();

        Label dateLabel = new Label(dayOfWeek.toString()+", "+dayOfMonth+" "+month);
        dateLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
        dateLabel.setTextFill(Color.LIGHTGRAY);
        dateLabel.setTextAlignment(TextAlignment.CENTER);
        dateLabel.setAlignment(Pos.CENTER);
        dateLabel.setWrapText(true);

        clockShortcut.getChildren().addAll(digitalClock, dateLabel);

        return clockShortcut;
    }
}

class ComboBoxAutoComplete<T> {

    private ComboBox<T> cmb;
    String filter = "";
    private ObservableList<T> originalItems;

    public ComboBoxAutoComplete(ComboBox<T> cmb) {
        this.cmb = cmb;
        originalItems = FXCollections.observableArrayList(cmb.getItems());
        cmb.setTooltip(new Tooltip());
        cmb.setOnKeyPressed(this::handleOnKeyPressed);
        cmb.setOnHidden(this::handleOnHiding);
    }

    public void handleOnKeyPressed(KeyEvent e) {
        ObservableList<T> filteredList = FXCollections.observableArrayList();
        KeyCode code = e.getCode();

        if (code.isLetterKey()) {
            filter += e.getText();
        }
        if (code == KeyCode.BACK_SPACE && filter.length() > 0) {
            filter = filter.substring(0, filter.length() - 1);
            cmb.getItems().setAll(originalItems);
        }
        if (code == KeyCode.ESCAPE) {
            filter = "";
        }
        if (filter.length() == 0) {
            filteredList = originalItems;
            cmb.getTooltip().hide();
        } else {
            Stream<T> itens = cmb.getItems().stream();
            String txtUsr = filter.toLowerCase();
            itens.filter(el -> el.toString().toLowerCase().contains(txtUsr)).forEach(filteredList::add);
            cmb.getTooltip().setText(txtUsr);
            Window stage = cmb.getScene().getWindow();
            double posX = stage.getX() + cmb.getBoundsInParent().getMinX();
            double posY = stage.getY() + cmb.getBoundsInParent().getMinY();
            cmb.getTooltip().show(stage, posX, posY);
            cmb.show();
        }
        cmb.getItems().setAll(filteredList);
    }

    public void handleOnHiding(Event e) {
        filter = "";
        cmb.getTooltip().hide();
        T s = cmb.getSelectionModel().getSelectedItem();
        cmb.getItems().setAll(originalItems);
        cmb.getSelectionModel().select(s);
    }
}
