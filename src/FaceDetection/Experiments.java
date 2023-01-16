package FaceDetection;

import DataBase.Data;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import javafx.stage.Stage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Experiments extends Application {
    FaceDetection faceDetection = new FaceDetection();

    private Imgcodecs Highgui;

    public Experiments() throws IOException {
    }

    public Mat imageToMat(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];

        PixelReader reader = image.getPixelReader();
        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

        Mat mat = new Mat(height, width, CvType.CV_8UC4);
        mat.put(0, 0, buffer);
        return mat;
    }

    public Image detectFaceFromFrame(Mat frame){

        // detection
        faceDetection.controller.detectAndDisplay(frame);

        // convert the Mat object (OpenCV) to Image (JavaFX)
        return mat2Image(frame);
    }

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     *
     * @param frame
     *            the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    private Image mat2Image(Mat frame)
    {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Highgui.imencode(".png", frame, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public void initialize(){
        faceDetection.controller.faceCascade = new CascadeClassifier();
        faceDetection.controller.absoluteFaceSize = 0;

        faceDetection.controller.rEyeCascade = new CascadeClassifier();
        faceDetection.controller.absoluteREyesSizeWidth = 0;
        faceDetection.controller.absoluteREyesSizeHeight = 0;
        faceDetection.controller.lEyeCascade = new CascadeClassifier();
        faceDetection.controller.absoluteLEyesSizeWidth = 0;
        faceDetection.controller.absoluteLEyesSizeHeight = 0;

        faceDetection.controller.mouthCascade = new CascadeClassifier();
        faceDetection.controller.absoluteMouthSizeWidth = 0;
        faceDetection.controller.absoluteMouthSizeHeight = 0;

        faceDetection.controller.haarClassifier.setSelected(true);
        faceDetection.controller.haarSelected();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> results = new ArrayList();


        File[] users = new File("src/DataBase/FaceImages/").listFiles();

        Image imageDetected = null;

        for (int i = 0; i < users.length; i++) {
            initialize();
            Image image = new Image(new File(users[i].getAbsolutePath()).toURI().toString(), Double.MAX_VALUE, Double.MAX_VALUE, false, true);
            Mat frame = imageToMat(image);

            imageDetected = detectFaceFromFrame(frame);

            String output = getInfo();

            results.add(output + " " + image.getUrl());
            System.out.println(output + " " + image.getUrl());

            FaceClassifier.wipe();
        }


        //Creating the image view
        ImageView imageView = new ImageView();
        //Setting image to the image view
        imageView.setImage(imageDetected);
        //Setting the image view parameters
        imageView.setX(10);
        imageView.setY(10);
        imageView.setFitWidth(0);
        imageView.setPreserveRatio(true);
        //Setting the Scene object
        Group root = new Group(imageView);
        Scene scene = new Scene(root, imageDetected.getWidth(), imageDetected.getHeight());
        primaryStage.setTitle("Displaying Image");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public String getInfo() throws IOException {
        FaceClassifier.MAX_EYES = 2;
        FaceClassifier.MAX_FACES = 1;
        FaceClassifier.MAX_MOUTHS = 1;
        FaceClassifier.MAX_WRITECOUNT = 1;

        FaceClassifier.face = new ArrayList();
        FaceClassifier.eyes = new ArrayList();
        FaceClassifier.mouth = new ArrayList();


        List<Rect> faces = new ArrayList<>();
        List<Rect> leftEyes = new ArrayList<>();
        List<Rect> rightEyes = new ArrayList<>();
        List<Rect> mouth = new ArrayList<>();

        faces.addAll(Arrays.asList(faceDetection.controller.currentFacesArray));
        leftEyes.addAll(Arrays.asList(faceDetection.controller.currentLEyesArray));
        rightEyes.addAll(Arrays.asList(faceDetection.controller.currentREyesArray));
        mouth.addAll(Arrays.asList(faceDetection.controller.currentMouthArray));

        if (faces.size() > 0 && leftEyes.size()+rightEyes.size() > 1 && mouth.size() > 0) {

            FaceClassifier.addFace(faces);
            FaceClassifier.addEyes(leftEyes);
            FaceClassifier.addEyes(rightEyes);
            FaceClassifier.addMouth(mouth);

            FaceClassifier.getPerson();
//            System.out.println(FaceClassifier.comb);
            return FaceClassifier.getClosestPerson(false);
        }
        else{
            return "no person found";
        }
    }
}
