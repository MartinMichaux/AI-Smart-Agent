package Interface.Screens;

import Agents.User;
import FileParser.FileParser;
import FaceDetection.FaceDetection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.control.TextField;
import DataBase.Data;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class StartScreen extends Application {

    public Stage stage;
    public MainScreen mainScreen;

    private VBox menuBox;
    public TextField user;
    private PasswordField psw;
    public Text errorInfo;
    public CheckBox recognizeUser;
    private List<Pair<String, Runnable>> menuData;

    private int counter = 0;

    private StackPane root = new StackPane();

    private FileParser fileParser;
    public FaceDetection faceDetection;
    public boolean signUp = false;

    /**
     * initializer method
     * @param primaryStage stage of the app
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        //class used for reading/writing in the text files
        fileParser = new FileParser();
        //class that uses the openCV face detection
        faceDetection = new FaceDetection(mainScreen,this);

        root.setBackground(Data.createBackGround());
        Scene scene = new Scene(root, 800, 800);
        addContent();

        primaryStage = new Stage();
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        this.stage = primaryStage;
        //start the webcam
        faceDetection.controller.init();
    }

    /**
     * handle login
     * @throws Exception
     */
    private void login() throws Exception {
        //if the face detection is required
        if(faceDetection.isVisible()){
            //if the camera is open
            if(faceDetection.controller.cameraActive){
                //if a face is detected now
                if(faceDetection.faceDetected()){
                    if(user.getText().isEmpty()||user.getText().isBlank() && psw.getText().isEmpty()||psw.getText().isBlank()){
                        errorInfo.setText("Sorry, username or password not possible");
                    }else{
                        counter++;
                        //checks if user exists
                        if(userAlreadyExists()){
                            //check if password is correct
                            if(fileParser.checkUserInfo("-Password",user.getText(),psw.getText())){
                                //start mainscreen and initialize
                                initialize(false);
                            }else{
                                if(counter ==1 ) {
                                    errorInfo.setText("Username or password is wrong, 2 attempts left");
                                }else if(counter ==2 ) {
                                    errorInfo.setText("Username or password is wrong, 1 attempts left");
                                }else if(counter >=3 ) {
                                    errorInfo.setText("Username or password is wrong, you have used your 3 attempts");
                                    System.out.println("Sorry, you have used your 3 attempts");
                                    System.exit(0);
                                }
                            }
                        }else if(counter ==1 ) {
                            errorInfo.setText("Try again, 2 attempts left");
                        }else if(counter ==2 ) {
                            errorInfo.setText("Try again, 1 attempts left");
                        }else if(counter >=3 ) {
                            errorInfo.setText("Sorry, you have used your 3 attempts");
                            System.out.println("Sorry, you have used your 3 attempts");
                            System.exit(0);
                        }
                    }
                }else{
                    errorInfo.setText("No face detected, please retry");
                }
            }else{
                errorInfo.setText("Camera is not activated, please turn it on");
            }
        }else{
            //same as above except face detection is not required
            if(user.getText().isEmpty()||user.getText().isBlank() && psw.getText().isEmpty()||psw.getText().isBlank()){
                errorInfo.setText("Sorry, username or password not possible");
            }else{
                counter++;
                if(userAlreadyExists()){
                    if(fileParser.checkUserInfo("-Password",user.getText(),psw.getText())){
                        initialize(false);
                    }else{
                        if(counter ==1 ) {
                            errorInfo.setText("Username or password is wrong, 2 attempts left");
                        }else if(counter ==2 ) {
                            errorInfo.setText("Username or password is wrong, 1 attempts left");
                        }else if(counter >=3 ) {
                            errorInfo.setText("Username or password is wrong, you have used your 3 attempts");
                            System.out.println("Sorry, you have used your 3 attempts");
                            System.exit(0);
                        }
                    }
                }else if(counter ==1 ) {
                    errorInfo.setText("Try again, 2 attempts left");
                }else if(counter ==2 ) {
                    errorInfo.setText("Try again, 1 attempts left");
                }else if(counter >=3 ) {
                    errorInfo.setText("Sorry, you have used your 3 attempts");
                    System.out.println("Sorry, you have used your 3 attempts");
                    System.exit(0);
                }
            }
        }
    }

    /**
     * handle new player signup
     * @throws Exception
     */
    private void signup() throws Exception {
        //work as login except create a new user
        if(faceDetection.isVisible()){
            if (faceDetection.controller.cameraActive) {
                if (faceDetection.faceDetected()) {
                    if (user.getText().isEmpty() || user.getText().isBlank() && psw.getText().isEmpty() || psw.getText().isBlank()) {
                        errorInfo.setText("Sorry, username or password not possible");
                    } else {
                        //if the username is not already used
                        if (!userAlreadyExists()) {
                            handleSignUp();
                        } else {
                            if (user.getText().isEmpty()) {
                                errorInfo.setText("Sorry, username not possible");
                            } else {
                                errorInfo.setText("Sorry, username already used");
                            }
                        }
                    }
                } else {
                    errorInfo.setText("No face detected, please retry");
                }
            }else{
                errorInfo.setText("Camera is not activated, please turn it on");
            }
        }else{
            if (user.getText().isEmpty() || user.getText().isBlank() && psw.getText().isEmpty() || psw.getText().isBlank()) {
                errorInfo.setText("Sorry, username or password not possible");
            } else {
                if (!userAlreadyExists()) {
                    handleSignUp();
                } else {
                    if (user.getText().isEmpty()) {
                        errorInfo.setText("Sorry, username not possible");
                    } else {
                        errorInfo.setText("Sorry, username already used");
                    }
                }
            }
        }

    }

    public void handleSignUp(){
        recognizeUser.setSelected(true);
        signUp = true;
        manageUserDetection();
    }

    /**
     * know whether the username is already used
     * @return true if the username is already used, false else
     */
    public boolean userAlreadyExists(){
        File userFile = new File("src/DataBase/Users/"+user.getText()+"/"+user.getText()+".txt");
        return userFile.exists();
    }

    /**
     * initialize user and assistant
     * @param signup is true if it's a new user, false else
     */
    public void initialize(boolean signup) throws Exception {
        if(signup){
            fileParser.createUser(user.getText(),psw.getText(), errorInfo);
        }
        Data.setUsername(user.getText());
        Data.setPassword(psw.getText());

        Data.setUser(new User(user.getText(), psw.getText()));
        if(fileParser.getUsersPicture("background")!=null){
            Data.setImage(fileParser.getUsersPicture("background"));
        }
        mainScreen = new MainScreen(faceDetection,stage);
    }

    /**
     * create content in the login screen
     */
    private void addContent() {
        addMenu();

        faceDetection.setVisible(true);
        faceDetection.setManaged(true);
        menuBox.getChildren().add(faceDetection);

        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);

        Button seeCamera = new Button("Do not require Face Recognition");
        seeCamera.setAlignment(Pos.CENTER);
        seeCamera.setOnAction(event -> {
            if(faceDetection.isVisible()){
                seeCamera.setText("Require Face Recognition");
                hBox.getChildren().remove(recognizeUser);
                faceDetection.setVisible(false);
                faceDetection.setManaged(false);
            }else{
                seeCamera.setText("Do not require Face Recognition");
                hBox.getChildren().add(recognizeUser);
                faceDetection.setVisible(true);
                faceDetection.setManaged(true);
            }
        });

        recognizeUser = new CheckBox("Recognize user");
        recognizeUser.setFont(Font.font("Tahoma", 15));
        //recognizeUser.setTextFill(MainScreen.themeColor.darker());
        recognizeUser.setAlignment(Pos.CENTER);
        recognizeUser.setOnMouseClicked(event -> {
            manageUserDetection();
        });

        hBox.getChildren().addAll(seeCamera,recognizeUser);
        menuBox.getChildren().add(hBox);

        root.getChildren().add(menuBox);
    }

    private void addMenu() {
        menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(10,0,10,0));
        menuBox.setBackground(new Background(new BackgroundFill(new Color(0.2,0.35379, 0.65, 0.5), CornerRadii.EMPTY, Insets.EMPTY)));
        menuBox.setBorder(new Border(new BorderStroke(Color.DARKGRAY, BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(10))));
        menuBox.setMaxSize(520, 600);

        MenuTitle title = new MenuTitle("Project 2-2 Assistant");
        title.setTranslateX(150);
        menuBox.getChildren().add(title);

        HBox userN = new HBox(10);
        userN.setAlignment(Pos.CENTER);
        userN.setPadding(new Insets(0,105,0,0));
        MenuTitle username = new MenuTitle("Username :");
        username.setTranslateY(15);
        user = new TextField();
        user.setFont(Font.font("Verdana", FontWeight.BOLD,15));
        user.setStyle("-fx-text-fill: dimgray;");
        user.setMaxWidth(200);
        userN.getChildren().addAll(username,user);
        menuBox.getChildren().add(userN);

        HBox passW = new HBox(10);
        passW.setAlignment(Pos.CENTER);
        passW.setPadding(new Insets(0,100,0,0));
        MenuTitle password = new MenuTitle("Password :");
        password.setTranslateY(15);
        psw = new PasswordField();
        psw.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        psw.setStyle("-fx-text-fill: dimgray;");
        psw.setMaxWidth(200);
        passW.getChildren().addAll(password,psw);
        menuBox.getChildren().add(passW);

        errorInfo = new Text("");
        errorInfo.setFont(Font.font("Verdana", FontWeight.BOLD, 10));
        errorInfo.setFill(Color.RED);
        menuBox.getChildren().add(errorInfo);

        menuData = Arrays.asList(
                new Pair<String, Runnable>("Log in", () -> {
                    try {
                        login();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }),
                new Pair<String, Runnable>("Sign up", () -> {
                    try {
                        signup();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }),
                new Pair<String, Runnable>("Exit to Desktop", Platform::exit)
        );
        menuData.forEach(data -> {
            Button button = new Button(data.getKey());
            button.setBackground(new Background(new BackgroundFill(Color.SLATEGREY.darker(), new CornerRadii(3,3,3,3,false), Insets.EMPTY)));
            button.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 16)));
            button.setPrefSize(200, 30);
            button.setTextFill(Color.LIGHTGRAY);
            button.setCursor(Cursor.HAND);
            button.setOnMouseClicked(e -> data.getValue().run());

            menuBox.getChildren().add(button);
        });
    }

    public void manageUserDetection(){
        if(recognizeUser.isSelected()){
            //faceDetection.draw.setVisible(true);
            int maxDelay = 30;
            //start a timer
            long start = System.currentTimeMillis();
            final long[] now = {System.currentTimeMillis()};
            final long[] elapsedTime = {Math.abs(now[0] - start)};
            Task task = new Task<Void>() {
                @Override public Void call() throws InterruptedException {
                    while(elapsedTime[0]/1000<maxDelay){
                        now[0] = System.currentTimeMillis();
                        elapsedTime[0] = Math.abs(now[0] - start);

                        if(!recognizeUser.isSelected()){
                            errorInfo.setText("");
                            break;
                        }

                        if(elapsedTime[0]/1000>5){
                            //start the analyze of the face
                            errorInfo.setText("Face being analyzed");
                            faceDetection.analyzeFace();
                            break;
                        }else {
                            errorInfo.setText("Face analysis in " + (maxDelay - 25 - elapsedTime[0] / 1000) + ", please get close to the camera so that your face, eyes and mouth are detected");
                        }
                        Thread.sleep(1000);
                    }
                    return null;
                }
            };
            new Thread(task).start();
        }
    }

    public void loginFace(String username) throws Exception {
        File[] users = new File("src/DataBase/Users").listFiles();

        for (File file:users) {
            if(file.getName().equals(username)){
                user.setText(username);
                psw.setText(FileParser.getUserInfo(username,"-Password"));
                break;
            }
        }
        initialize(false);
    }


    public static class MenuTitle extends Pane {
        private Text text;

        public MenuTitle(String name) {
            String spread = "";
            for (char c : name.toCharArray()) {
                spread += c + " ";
            }


            text = new Text(spread);
            text.setId("title");
            text.setFill(Color.BLACK);
            text.setEffect(new DropShadow(30, Color.BLACK));
            getChildren().addAll(text);
        }

        public double getTitleWidth() {
            return text.getLayoutBounds().getWidth();
        }

        public double getTitleHeight() {
            return text.getLayoutBounds().getHeight();
        }
    }
}