package FaceDetection;

import DataBase.Data;
import FileParser.FileParser;
import org.opencv.core.Rect;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class FaceClassifier {
    public static Random rn = new Random();

    public static int count = 0;

    static public int MAX_EYES = 40;
    static public int MAX_FACES = 20;
    static public int MAX_MOUTHS = 20;
    static public int MAX_WRITECOUNT = 1000;
    static final int MAX_DIFF = 5;
    static final int MAX_CLUSS = 5;

    static boolean eyesDone = false;
    static boolean mouthDone = false;

    public static boolean canClassify = false;

    public static List<Rect> eyes = new ArrayList();
    public static List<Rect> face = new ArrayList();
    public static List<Rect> mouth = new ArrayList();

    public static int[] leftEyePos = new int[2];
    public static int[] rightEyePos = new int[2];
    public static int[] mouthPos = new int[2];
    public static int[] facePos = new int[2];

    public static double eyesD;
    public static double midMouthD;
    public static double lEyeMouthD;
    public static double rEyeMouthD;
    public static double lEyeMidD;
    public static double rEyeMidD;

    public static List<String> data = new ArrayList();

    public static String comb;

    public static int writeCount = 0;

    public static FileParser fileReader = new FileParser();


    public static void wipe(){
        List<Rect> eyes = new ArrayList();
        List<Rect> face = new ArrayList();
        List<Rect> mouth = new ArrayList();
    }

    public static String getPerson() throws IOException {
        eyesD = eucDis(leftEyePos, rightEyePos);
        midMouthD = eucDis(facePos, mouthPos);
        lEyeMouthD = eucDis(leftEyePos, mouthPos);
        rEyeMouthD = eucDis(rightEyePos, mouthPos);
        lEyeMidD = eucDis(leftEyePos, facePos);
        rEyeMidD = eucDis(rightEyePos, facePos);

        comb = eyesD + "," + midMouthD + "," + lEyeMouthD + "," + rEyeMouthD + "," + lEyeMidD + "," + rEyeMidD;
        writeCount++;

        if(writeCount == MAX_WRITECOUNT) {
            //System.out.println(comb);
            data.add(comb);
            writeCount = 0;
        }

        return comb;
    }

    public static String getClosestPerson(boolean signup){
        //System.out.println("relative to distance middle to Mouth");

        double eyd = 0;
        double mmo = 0;
        double lemo = 0;
        double remo = 0;
        double lemi = 0;
        double remi = 0;

        for(int i = 0; i< data.size(); i++){
            String[] hold = data.get(i).split(",");
            eyd += Double.parseDouble(hold[0]);
            mmo += Double.parseDouble(hold[1]);
            lemo += Double.parseDouble(hold[2]);
            remo += Double.parseDouble(hold[3]);
            lemi += Double.parseDouble(hold[4]);
            remi += Double.parseDouble(hold[5]);
            //System.out.println(i);
        }

        eyd /= data.size();
        mmo /= data.size();
        lemo /= data.size();
        remo /= data.size();
        lemi /= data.size();
        remi /= data.size();

        //System.out.println("here");

        double reyesD = eyd/mmo;
        double rmidMouthD = mmo/mmo;
        double rlEyeMouthD = lemo/mmo;
        double rrEyeMouthD = remo/mmo;
        double rlEyeMidD = lemi/mmo;
        double rrEyeMidD = remi/mmo;

        String comb = reyesD + "," + rmidMouthD + "," + rlEyeMouthD + "," + rrEyeMouthD + "," + rlEyeMidD + "," + rrEyeMidD;

//        System.out.println(comb);

        if(signup){
            fileReader.changeUserInfo("-Face", comb,null);
        }else {
            File[] users = new File("src/DataBase/Users").listFiles();
            double difference = 10000;
            String finalUser = "";
            for (File user : users) {
                double holddiff = 0;
                //System.out.println(user.toString());
                String[] distances = FileParser.getUserInfo(user.getName(), "-Face").split(",");
                String[] faceStat = comb.split(",");
                double[] dist = new double[distances.length];
                for (int i = 0; i < distances.length; i++) {
                    double a = Double.parseDouble(distances[i]);
                    double b = Double.parseDouble(faceStat[i]);
                    dist[i] = Double.parseDouble(distances[i]);
                    if (false) {

                        double newdiff = Math.min(Math.abs(Math.pow(a, 1) - Math.pow(b, 1)), difference);

                        if (newdiff < difference) {
                            difference = Math.min(Math.abs(Math.pow(a, 1) - Math.pow(b, 1)), difference);
                            finalUser = user.getName();
                        }

                    }
                    else{
                        holddiff += Math.abs(a - b)/(distances.length-1);
                    }
                }
                if (holddiff < difference) {
                    difference = holddiff;
                    finalUser = user.getName();
                }
//                System.out.println(Arrays.toString(dist));
//                System.out.println(difference);

            }
            if (difference < 0.05) {

//                System.out.println(difference);

                data = new ArrayList();

                eyesDone = false;
                mouthDone = false;
                canClassify = false;

                leftEyePos = new int[2];
                rightEyePos = new int[2];
                mouthPos = new int[2];
                facePos = new int[2];

                eyesD = 0;
                midMouthD = 0;
                lEyeMouthD = 0;
                rEyeMouthD = 0;
                lEyeMidD = 0;
                rEyeMidD = 0;

                return finalUser;
            }
        }

        data = new ArrayList();

        eyesDone = false;
        mouthDone = false;
        canClassify = false;

        leftEyePos = new int[2];
        rightEyePos = new int[2];
        mouthPos = new int[2];
        facePos = new int[2];

        eyesD = 0;
        midMouthD = 0;
        lEyeMouthD = 0;
        rEyeMouthD = 0;
        lEyeMidD = 0;
        rEyeMidD = 0;

        return "not found";
    }


    public static void writeToFile(){
        System.out.println("writing in data");

        PrintWriter writer = null;

        try {
            writer = new PrintWriter("Desktop/data.txt", "UTF-8");
            for(int i = 0; i < data.size(); i++) {
                writer.println(data.get(i));
            }
        } catch (IOException ex) {
            // Report
            System.out.println("bruuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuh");
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }

    }

    public static double eucDis(int[] a, int[] b){
        double distance = Math.sqrt(((double)(Math.pow(a[0] - b[0],2)) + (double)(Math.pow(a[1] - b[1],2))));
        return distance;
    }

    public static void addEyes(List<Rect> newEyes){

        if(face.size() != MAX_FACES)
            return;

        newEyes = Eyefilter(newEyes);

        int amountToGo = eyes.size() + newEyes.size() - MAX_EYES;


        for(int i = 0; i < Math.min(amountToGo,eyes.size()); i++){
            eyes.remove(0);
        }
        for(int i = 0; i <= newEyes.size()-1; i++){
//            System.out.println(newEyes.get(i).x + " " + newEyes.get(i).y);
            eyes.add(newEyes.get(i));
        }

        if (eyes.size() == MAX_EYES){

            int[][] hold = kCluster(eyes, 2);
            if (hold[0][0] < hold[1][0]){ //Right left
                rightEyePos = hold[0];
                leftEyePos = hold[1];
            }
            else{ // Left Right
                leftEyePos = hold[0];
                rightEyePos = hold[1];
            }
            eyesDone = true;

            if (mouthDone){
                canClassify=true;
            }
//            System.out.println("Left eye at " + Arrays.toString(leftEyePos));
//            System.out.println("Right eye at " + Arrays.toString(rightEyePos));
        }
    }

    public static List<Rect> Eyefilter(List<Rect> newParts){
        for(int i = newParts.size() -1 ; i >= 0; i--){
            if(newParts.get(i) == null){
                newParts.remove(i);
            }
            else if(newParts.get(i).y > facePos[1] || newParts.get(i).y < face.get(0).y){
                newParts.remove(i);
            }
        }
        return newParts;
    }

    public static List<Rect> MouthFilter(List<Rect> newParts){
        //System.out.println(newParts.size());
        for(int i = newParts.size() -1 ; i >= 0; i--){
            if(newParts.get(i) == null){
                newParts.remove(i);
            }
            else if(newParts.get(i).y < facePos[1]){
                newParts.remove(i);
            }
        }
        return newParts;
    }

    public static void addFace(List<Rect> newFace){
        int amountToGo = face.size() + newFace.size() - MAX_FACES;


        for(int i = 0; i < Math.min(amountToGo, face.size()); i++){
            face.remove(0);
        }
        for(int i = 0; i <= newFace.size()-1; i++){
            face.add(newFace.get(i));
        }

        if (face.size() == MAX_FACES){
            facePos = calcMiddle(face);
//            System.out.println("Face at " + Arrays.toString(facePos));
        }

    }

    public static int[] calcMiddle(List<Rect> boxes){
        int posX = 0;
        int posY = 0;
        for (int i = 0; i < boxes.size(); i++){
            Rect hold = boxes.get(i);
            posX += hold.x + hold.height/2;
            posY += hold.y + hold.height/2;
        }
        int[] returnArr = {posX/boxes.size(), posY/boxes.size()};
        return returnArr;
    }

    public static void addMouth(List<Rect> newMouth){
        if(face.size() != MAX_FACES)
            return;

        newMouth = MouthFilter(newMouth);
        int amountToGo = mouth.size() + newMouth.size() - MAX_MOUTHS;


        for(int i = 0; i < Math.min(amountToGo, mouth.size()); i++){
            mouth.remove(0);
        }
        for(int i = 0; i <= newMouth.size()-1; i++){
            mouth.add(newMouth.get(i));
        }

        if (mouth.size() == MAX_MOUTHS){
            mouthPos = calcMiddle(mouth);
            mouthDone = true;

            if (eyesDone){
                canClassify=true;
            }
//            System.out.println("Mouth at " + Arrays.toString(mouthPos));
        }
    }

    public static int[][] kCluster (List<Rect> boxes, int k){
        int[][] means = new int[k][2];
        List<Integer> numbers = new ArrayList();
        count = 1;
        for (int i = 0; i < k; i++){
            int random = rn.nextInt(boxes.size());
            while (numbers.contains(random)){
                random = rn.nextInt(boxes.size());
            }
            numbers.add(random);
            Rect boxHold = boxes.get(i);
            means[i][0] = boxHold.x + boxHold.width/2;
            means[i][1] = boxHold.y+ boxHold.height/2;
        }
        return kClusterRecursion(boxes, means, null);
    }

    public static int[][] kClusterRecursion (List<Rect> boxes, int[][] means, int[][] prevMeans){
        int k = means.length;
        int[][] holdMeans = new int[k][2];
        for(int i = 0; i < k; i++){ //You have to copy entry for entry because java is stupid :)))
            holdMeans[i][0] = means[i][0];
            holdMeans[i][1] = means[i][1];
        }
        List<ArrayList<Rect>> kLists = new ArrayList(); // List with lists of points that coincide with each mean
        for (int i = 0; i < k; i++){
            kLists.add(new ArrayList());
        }

        for(int i = 0; i < boxes.size(); i++){
            int memK = -1;
            double shortest = Double.POSITIVE_INFINITY;
            Rect holdBox = boxes.get(i);
            for(int j = 0; j < k; j++){
                double distance = Math.sqrt(((double)(Math.pow((holdBox.x + holdBox.height/2) - means[j][0],2)) + (double)(Math.pow((holdBox.y + holdBox.width/2) - means[j][1],2))));
                if (distance < shortest){
                    memK = j;
                    shortest = distance;
                }
            }
            kLists.get(memK).add(holdBox);
        }


        for(int i = 0; i < k; i++){
            int xPos = 0;
            int yPos = 0;
            List<Rect> holdList = kLists.get(i);
            for (int j = 0; j < holdList.size(); j++){
                xPos += holdList.get(j).x + holdList.get(j).height/2;
                yPos += holdList.get(j).y + holdList.get(j).width/2;
            }
            if(holdList.size() != 0) {
                xPos /= holdList.size();
                yPos /= holdList.size();

                means[i][0] = xPos;
                means[i][1] = yPos;
            }


        }


        if(prevMeans == null){
            count++;
            return kClusterRecursion(boxes, means, holdMeans);
        }
        else{
            int changeX = 0;
            int changeY = 0;
            for (int i = 0; i < k; i++){
                changeX += Math.abs(means[i][0] - prevMeans[i][0]);
                changeY += Math.abs(means[i][1] - prevMeans[i][1]);
            }
            changeX /= k;
            changeY /= k;

            if (changeX+changeY > MAX_DIFF && count < MAX_CLUSS){
                count++;
                return kClusterRecursion(boxes, means, holdMeans);
            }
            else{
                return means;
            }
        }
    }
}
