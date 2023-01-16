package FaceDetection;
import Interface.Screens.MainScreen;
import Interface.Screens.StartScreen;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.opencv.core.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceDetection extends VBox {
    public MainScreen mainScreen;
    public StartScreen startScreen;
    public FD_Controller controller;
    private final int DELAY = 30;

    public FaceDetection() throws IOException {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // load the FXML resource
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FD_FX.fxml"));
        getChildren().add(loader.load());
        // set a whitesmoke background
        setStyle("-fx-background-color: whitesmoke;");
        // init the controller
        controller = loader.getController();
    }

    public FaceDetection(MainScreen mainScreen,StartScreen startScreen){
        this.mainScreen = mainScreen;
        this.startScreen = startScreen;
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //add content of the pane
        addContent();
    }
    public void addContent()
    {
        try
        {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FD_FX.fxml"));
            getChildren().add(loader.load());
            // set a whitesmoke background
            setStyle("-fx-background-color: whitesmoke;");

            // init the controller
            controller = loader.getController();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * handles the fact that a face has came back to the webcam in a certain amount of time
     * @return
     */
    public boolean faceDetected(){
        // current frame
        Mat frame = new Mat();
        // read the current frame
        controller.capture.read(frame);
        //in order to update the data of the face detection
        controller.detectAndDisplay(frame);
        //Check if there is a face
        if(controller.currentFacesArray.length>0){
            //Check if the face is moving
            if(controller.previousFacesArray.length>0){
                return true;
            }
        }
        return false;
    }

    public void manageFaceLeaving(){
        //start a timer
        final boolean[] faceDetect = {false};
        long start = System.currentTimeMillis();
        Task task = new Task<Void>() {
            @Override public Void call() throws InterruptedException {
                while(!faceDetect[0]){
                    long now = System.currentTimeMillis();
                    long elapsedTime = Math.abs(now - start);
                    if(elapsedTime/1000>DELAY){
                        //LOG OUT if face not detected after delay
                        Platform.runLater(new Runnable(){
                            @Override
                            public void run() {
                                mainScreen.logOut(true);
                            }
                        });
                        faceDetect[0] = true;
                    }
                    if(faceDetected()){
                        //face detected
                        //System.out.println("face detected after " + elapsedTime/1000+  " seconds.");
                        if(elapsedTime/1000>DELAY/10){
                            //if face is detected after a certain time
                            //System.out.println("hey you are back");
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    mainScreen.chat.receiveMessage("Hey you are back !(after " + elapsedTime/1000 + "sec)");
                                    mainScreen.manageFaceDetection();
                                }
                            });
                        }else {
                            //if face is detected smaller than a certain delay
                            Platform.runLater(new Runnable(){
                                @Override
                                public void run() {
                                    mainScreen.manageFaceDetection();
                                }
                            });
                        }
                        faceDetect[0] = true;
                    }
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    public void analyzeFace(){
        Task task = new Task<Void>() {
            @Override public Void call() throws Exception {
                while(true){
                    if(!startScreen.recognizeUser.isSelected()){
                        startScreen.errorInfo.setText("");
                        break;
                    }

                    List<Rect> faces = new ArrayList<>();
                    List<Rect> leftEyes = new ArrayList<>();
                    List<Rect> rightEyes = new ArrayList<>();
                    List<Rect> mouth = new ArrayList<>();

                    faces.addAll(Arrays.asList(controller.currentFacesArray));
                    leftEyes.addAll(Arrays.asList(controller.currentLEyesArray));
                    rightEyes.addAll(Arrays.asList(controller.currentREyesArray));
                    mouth.addAll(Arrays.asList(controller.currentMouthArray));

                    FaceClassifier.addFace(faces);
                    FaceClassifier.addEyes(leftEyes);
                    FaceClassifier.addEyes(rightEyes);
                    FaceClassifier.addMouth(mouth);

                    FaceClassifier.getPerson();

                    if(FaceClassifier.data.size()>300){
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                if(startScreen.signUp) {
                                    startScreen.errorInfo.setText("Face analysis done");
                                    try {
                                        startScreen.initialize(true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    FaceClassifier.getClosestPerson(true);
                                    startScreen.signUp = false;
                                }else{
                                    String userDetected = FaceClassifier.getClosestPerson(false);
                                    if (userDetected.equals("not found")) {
                                        startScreen.errorInfo.setText("Face analysis done, no one found");
                                    } else {
                                        startScreen.errorInfo.setText("Face analysis done");
                                        try {
                                            startScreen.loginFace(userDetected);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    startScreen.recognizeUser.setSelected(false);
                                }
                            }
                        });
                        break;
                    }
                }
                return null;
            }
        };
        new Thread(task).start();
    }
}