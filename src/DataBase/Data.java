package DataBase;

import Agents.Assistant;
import Agents.User;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Data {
    private static String username = "defaultUsername";
    private static String password = "defaultPasseword";

    private static User user;
    private static Assistant assistant;

    private static File remindersFile = new File("src/DataBase/reminders.txt");

    private static MediaPlayer mp;

    private static String image = "src/DataBase/defaultBackground.jpg";


    public static Background createBackGround(){
        Image image = new Image(new File(getImage()).toURI().toString(),Double.MAX_VALUE,Double.MAX_VALUE,false,true);

        BackgroundSize backgroundSize = new BackgroundSize(Double.MAX_VALUE, Double.MAX_VALUE, true, true, true, true);

        BackgroundImage backgroundImage = new BackgroundImage(image, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, backgroundSize);

        Background background = new Background(backgroundImage);
        return background;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Data.username = username;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        Data.password = password;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        Data.user = user;
    }

    public static Assistant getAssistant() {
        return assistant;
    }

    public static void setAssistant(Assistant assistant) {
        Data.assistant = assistant;
    }

    public static File getRemindersFile() {
        return remindersFile;
    }

    public static void setRemindersFile(File remindersFile) {
        Data.remindersFile = remindersFile;
    }

    public static MediaPlayer getMp() {
        return mp;
    }

    public static void setMp(MediaPlayer mp) {
        Data.mp = mp;
    }

    public static String getImage() {
        return image;
    }

    public static void setImage(String image) {
        Data.image = image;
    }
}
