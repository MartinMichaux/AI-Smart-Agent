package TextRecognition;

import Agents.Assistant;
import DataBase.Data;
import Interface.Display.MediaPlayerDisplay;
import SkillEditor.SkillEditorHandler;
import Skills.Schedule.Skill_Schedule;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TextRecognition {
    private Assistant assistant;
    private SkillEditorHandler skillEditor;

    private int max_Distance = 1;
    private String originalCleanM;
    private String response;

    private boolean firstPhase;
    private boolean secondPhase;
    private boolean thirdPhase;
    private boolean fourthPhase;
    private int BFSdepth;
    private Node nodeInvestigated;

    public String skillEditorState = "";

    private final File dataBase = new File("src/DataBase/textRecognitionSkills.txt");

    public TextRecognition(Assistant assistant){
        this.assistant = assistant;
        skillEditor = assistant.skillEditor;
    }

    public TextRecognition() { }

    public String getResponse(String uMessage) throws Exception
    {
        originalCleanM = assistant.removePunctuation(uMessage);
        max_Distance = Math.max(2, (int)(originalCleanM.length()*0.15));

        secondPhase = false;
        thirdPhase = false;
        fourthPhase = false;
        firstPhase = true;
        System.out.println("FIRST PHASE");
        //first test without variables
        if(getInfo_withLevenshtein(new Node(originalCleanM))) {
            System.out.println("SOLUTION FOUND");
            System.out.println("SEARCH DONE in first phase");
        }else{
            firstPhase = false;
            //START BFS
            BFSdepth = 0;
            Node root = new Node(originalCleanM);
            //First create tree
            if(createTree(root)){
                System.out.println("TREE CREATION DONE");
            }else{
                System.out.println("smth went wrong creating the tree");
            }
            System.out.println("START SEARCHING IN TREE");
            //Then search in the tree
            secondPhase = true;
            if(search(root)){
                System.out.println("SEARCH DONE in second phase");
            }else{
                secondPhase = false;
                thirdPhase = true;
                if(search(root)){
                    System.out.println("SEARCH DONE in third phase");
                }else{
                    thirdPhase = false;
                    fourthPhase = true;
                    if(search(root)){
                        System.out.println("SEARCH DONE in fourth phase");
                    }
                }
            }
        }
        System.out.println("//////");
        return response;
    }

    /**
     * Create tree in recursive DFS
     * @param currentNode
     */
    public boolean createTree(Node currentNode){
        Node startNode = currentNode;
        String message = startNode.getSentence();
        String [] arr = startNode.getSentence().split(" ");
        if(arr.length==1){
            BFSdepth++;
        }
        int newIndexToRemove = BFSdepth;
        while(newIndexToRemove<arr.length){
            String[] newMessage = removeWord(message,newIndexToRemove);
            Node newNode = new Node(newMessage[0]);
            newNode.getWordsRemoved().addAll(startNode.getWordsRemoved());newNode.getWordsRemoved().add(newMessage[1]);
            currentNode.getChildren().add(newNode);
            createTree(newNode);
            currentNode = newNode;
            newIndexToRemove++;
            BFSdepth = 0;
        }
        return true;
    }

    /**
     * search in the tree in iterative BFS
     * @param root
     * @throws Exception
     */
    public boolean search(Node root) throws Exception {
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(root);
        Node currentNode;
        while(!queue.isEmpty()){
            currentNode = queue.remove();
            //VISIT current node
            nodeInvestigated = currentNode;
            if(getInfo_withLevenshtein(currentNode)){
                //found a solution
                return true;
            }else{
                queue.addAll(currentNode.getChildren());
            }
        }
        return false;
    }

    public String[] removeWord(String message,int wordToRemoveIndex){
        //System.out.println("REMOVE WORD");
        String [] arr = message.split(" ");
        String wordToRemove = arr[wordToRemoveIndex];
        String newMessage = "";
        if(arr.length==1){
            System.out.println("word removed message is empty");
        }
        if(wordToRemoveIndex!=arr.length-1){
            wordToRemove = addCharToString(wordToRemove,' ', wordToRemove.length());
        }
        newMessage = message.replaceAll(wordToRemove, "");
        //if word to remove is at the end of the message, also remove the last char
        if(wordToRemoveIndex==arr.length-1){
            if(newMessage.charAt(newMessage.length()-1)==' '){
                newMessage = newMessage.substring(0, newMessage.length() - 1);
            }
        }
        //System.out.println("random word : " + arr[wordToRemoveIndex] + " , message without the random word : " + newMessage);
        return new String[]{newMessage,wordToRemove};
    }
    public String addCharToString(String str, char c, int pos) {
        StringBuilder stringBuilder = new StringBuilder(str);
        stringBuilder.insert(pos, c);
        return stringBuilder.toString();
    }


    /**
     * Tries to find the nearest possible answer in a given range of errors max_Distance
     * @param node the user message node without punctuation
     */
    public boolean getInfo_withLevenshtein(Node node) throws Exception{
        ArrayList<Answers> res = new ArrayList<>();
        ArrayList<Answers> best_score_res = new ArrayList<>();
        int score = 0;
        int best_score = Integer.MAX_VALUE;
        try{
            BufferedReader data = new BufferedReader(new FileReader(dataBase));

            String s;
            while ((s = data.readLine()) != null) {
                if (s.startsWith("U")) {
                    //CHECKS SKILLS WITH NO VARIABLES (WITHOUT DELETING WORDS)
                    if (firstPhase) {
                        if(!s.contains("<VARIABLES>")){
                            score = LevenshteinDistance(node.getSentence().toLowerCase(), s.substring(2).toLowerCase(), max_Distance);
                            if (score != -1) {
                                if (score < best_score) {
                                    best_score = score;
                                }

                                String r;
                                while ((r = data.readLine()) != null) {
                                    if(r.startsWith("B")){
                                        res.add(new Answers(score, r.substring(2)));
                                        break;
                                    }
                                }
                            }
                        }
                    }else if(secondPhase){
                        //CHECKS SKILLS WITH ONLY THE SAME NBR OF VARIABLES AS WORDS DELETED IN THE MESSAGE (WITH DELETING WORDS)
                        if (s.contains("<VARIABLE>") && containsSameNbrOfVariables(s,node)) {
                            String sWithSameNbrOfVar = assistant.removeVariables(s);
                            score = LevenshteinDistance(node.getSentence().toLowerCase(), sWithSameNbrOfVar.substring(2).toLowerCase(), max_Distance);
                            if (score != -1) {
                                if (score < best_score) {
                                    best_score = score;
                                }

                                String r;
                                while ((r = data.readLine()) != null) {
                                    if(r.startsWith("B")){
                                        res.add(new Answers(score, r.substring(2)));
                                        break;
                                    }
                                }
                            }
                        }
                    }else if(thirdPhase){
                        String sWithVar = assistant.removeVariables(s);
                        //CHECKS SKILL WITH AND WITHOUT VARIABLES (WITH DELETE WORDS WHICH ALLOWS FOR "TOO MUCH" WORDS)
                        score = LevenshteinDistance(node.getSentence().toLowerCase(), sWithVar.substring(2).toLowerCase(), max_Distance);
                        if (score != -1) {
                            if (score < best_score) {
                                best_score = score;
                            }

                            String r;
                            while ((r = data.readLine()) != null) {
                                if(r.startsWith("B")){
                                    res.add(new Answers(score, r.substring(2)));
                                    break;
                                }
                            }
                        }

                    }else if(fourthPhase){
                        if(s.contains("<VARIABLE>")){
                            //CHECKS SKILL ONLY VARIABLES (WITH DELETE WORDS WHICH ALLOWS FOR "TOO MUCH" WORDS)
                            String sWithVar = assistant.removeVariables(s);
                            score = LevenshteinDistance(node.getSentence().toLowerCase(), sWithVar.substring(2).toLowerCase(), max_Distance);
                            if (score != -1) {
                                if (score < best_score) {
                                    best_score = score;
                                }

                                String r;
                                while ((r = data.readLine()) != null) {
                                    if(r.startsWith("B")){
                                        res.add(new Answers(score, r.substring(2)));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            data.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(skillEditorState.equals("skill adder"))
        {
            if(node.getSentence().equals("cancel") || node.getSentence().equals("cancel skill editor"))
            {
                skillEditorState = "";
                response = "Canceled the skill editor, you can now type in your request.";
            }
            else if(node.getSentence().equals("See all possible operations")){
                response = skillEditor.allOperations();
            }
            else
            {
                skillEditorState = "";
                response = assistant.handleNewSkill(node.getSentence());
            }
        }
        else if(skillEditorState.equals("skill editor")){
            if(node.getSentence().equals("cancel") || node.getSentence().equals("cancel skill editor"))
            {
                skillEditorState = "";
                response = "Canceled the rule editor, you can now type in your request.";
            }
            else
            {
                skillEditorState = "";
                assistant.editSkill(node.getSentence());
                response = "Sentence has been edited to the database with success.";
            }
        }
        else if(skillEditorState.equals("rule adder")){
            if(node.getSentence().equals("cancel") || node.getSentence().equals("cancel skill editor"))
            {
                skillEditorState = "";
                response = "Canceled the rule editor, you can now type in your request.";
            }
            else
            {
                skillEditorState = "";
                assistant.addNewRule(node.getSentence());
                response = "Rules have been added to the database with success.";
            }
        }
        else if(skillEditorState.equals("rule editor")){
            if(node.getSentence().equals("cancel") || node.getSentence().equals("cancel skill editor"))
            {
                skillEditorState = "";
                response = "Canceled the rule editor, you can now type in your request.";
            }
            else
            {
                skillEditorState = "";
                assistant.editRule(node.getSentence());
                response = "Rule have been edited in the database with success.";
            }
        }
        else if(res.isEmpty()||best_score>1)
        {
            response = "I could not understand your demand...";
            return false;
        }
        else
        {
            for(int i = 0; i < res.size(); i++)
            {
                if(res.get(i).getScore() == best_score)
                {
                    System.out.println("The score is : " + best_score + " | And the answer is: "+res.get(i).getAnswer());
                    best_score_res.add(res.get(i));
                }
            }

            if(assistant.isNumber(best_score_res.get(0).getAnswer()))
            {
                String skill_answer = getSkill(res.get(0).getAnswer(),node.getSentence());
                if(skill_answer == null)
                {
                    response =  "This is what I found for your request.";
                }
                else
                {
                    response =  skill_answer;
                }
            }
            else
            {
                int max = best_score_res.size();
                int n = new Random().nextInt(max);
                response =  best_score_res.get(n).getAnswer();
            }
        }
        return true;
    }


    /**
     * @param s message from the skill database
     * @param node current node being investifated
     * @return true if s contains the same nbr of variables than the message stored in the current node
     */
    public boolean containsSameNbrOfVariables(String s, Node node){
        int nbrOfRandomWords = 0;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i)=='<'){
                nbrOfRandomWords++;
            }
        }
        if(nbrOfRandomWords==node.getWordsRemoved().size()){
            return true;
        }
        return false;
    }


    /**
     * Every skill has it's specific number, finds the corresponding skill and does the appropriate
     * action to use the skill.
     * @param pNumb the number from the database corresponding to an action
     * @param message the user message without punctuation
     */
    public String getSkill(String pNumb, String message) throws Exception
    {
        //The specific Skills will be called here
        int skill_num = Integer.parseInt(pNumb);
        String final_answer = null;
        if(skill_num == 1)
        {
            String city = assistant.fileParser.getUserInfo(Data.getUsername(),"-City");
            String country = assistant.fileParser.getUserInfo(Data.getUsername(),"-Country");
            if(city.isEmpty()||country.isEmpty()){
                System.out.println("It seems like you haven't completed your location yet.");
                city = "Maastricht";
                country = "NL";
            }
            if (originalCleanM.contains("hourly")) { assistant.mainScreen.setWeatherDisplay(city,country,true); }
            else  { assistant.mainScreen.setWeatherDisplay(city,country,false); }
            final_answer = "This is what I found for the weather in "+ city + ", " + country + ". " + assistant.mainScreen.weatherDisplay.currentDataString() + "If you want to change the location, type 'Change weather location to City,Country.' (e.g. Amsterdam,NL).";
        }
        else if(skill_num == 2){
            try {
                String city;
                String country = "";
                String temp = nodeInvestigated.getWordsRemoved().get(0);
                if (temp.contains(",")) {
                    String[] split2 = temp.split(",");
                    city = split2[0].replace(" ", "");
                    country = split2[1];
                }
                else {
                    city = temp.replace(" ", "");
                }
               assistant.mainScreen.setWeatherDisplay(city,country,false);
                final_answer = "This is what I found for the weather in "+ city + ". " + assistant.mainScreen.weatherDisplay.currentDataString() + "If you want to change the location, type 'Change weather location to City,Country.' (e.g. Amsterdam,NL).";
            }
            catch (Exception ex) {
                final_answer = "Something went wrong! Please try again.";
            }
        }
        else if(skill_num == 10)
        {
            final_answer = "Your next lecture is:" + System.lineSeparator();
            final_answer = final_answer + new Skill_Schedule().getNextCourse();
        }
        else if(skill_num == 11)
        {
            final_answer = "Your lectures this week are:" + System.lineSeparator();
            final_answer = final_answer + new Skill_Schedule().getThisWeek();
        }
        else if(skill_num == 12)
        {
            ArrayList<String> this_month = new Skill_Schedule().getThisMonth();
            final_answer = "Your lectures this month are:" + System.lineSeparator();

            for(int i = 0; i < this_month.size(); i++)
            {
                final_answer = final_answer + System.lineSeparator() + System.lineSeparator() + this_month.get(i);
            }
        }
        else if(skill_num == 14) {
            assistant.mainScreen.setClockAppDisplay("Alarm");
        }
        else if(skill_num == 15) {
            assistant.mainScreen.setClockAppDisplay("Clock");
            final_answer = "Here's the clock! To add a new clock use the options on the left screen or type 'Add a new clock for Continent/City'. If you want the available areas you can add, type 'What areas can I add to the clock'.";
        }
        else if(skill_num == 16) {
            String messageT = "";
            for (String string : assistant.mainScreen.clockAppDisplay.clockVBox.listOfZoneIDs) {
                messageT += string + ", ";
            }
            final_answer = "The available timezones you can add to the clock are:  " + messageT;
        }
        else if(skill_num == 17) {
            final_answer = "Here's the timer! To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 18) {
            if (message.toLowerCase().contains("start") && !assistant.mainScreen.clockAppDisplay.timerVBox.timerTime.getText().equals("00 : 00 : 00") && assistant.mainScreen.clockAppDisplay.timerVBox.startPauseResume.getText().equals("Start")) {
                assistant.mainScreen.clockAppDisplay.timerVBox.startTimer();
                final_answer = "The timer started. Type 'Pause/Cancel timer' or use the options on the left screen.";
            }
            else {
                final_answer = "To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            }
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 19) {
            if (message.toLowerCase().contains("pause") &&  assistant.mainScreen.clockAppDisplay.timerVBox.startPauseResume.getText().equals("Pause")) {
                assistant.mainScreen.clockAppDisplay.timerVBox.pauseTimer();
                final_answer = "The timer is paused. Type 'Resume/Cancel timer' or use the options on the left screen.";
            }
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 20) {
            if (message.toLowerCase().contains("resume") &&  assistant.mainScreen.clockAppDisplay.timerVBox.startPauseResume.getText().equals("Resume")) {
                assistant.mainScreen.clockAppDisplay.timerVBox.resumeTimer();
                final_answer = "The timer is resumed. Type 'Pause/Cancel timer' or use the options on the left screen.";
            }
            else {
                final_answer = "To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            }
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 21) {
            if (message.toLowerCase().contains("cancel") &&  !assistant.mainScreen.clockAppDisplay.timerVBox.cancel.isDisabled()) {
                assistant.mainScreen.clockAppDisplay.timerVBox.cancelTimer();
                final_answer = "The timer is canceled. To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            }
            else {
                final_answer = "To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            }
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 22) {
            assistant.mainScreen.setClockAppDisplay("Stopwatch");
            final_answer = "Here's the stopwatch! Type 'Start the stopwatch' or use the buttons on the left screen.";
        }
        else if(skill_num == 23) {
            if (assistant.mainScreen.clockAppDisplay.stopwatchVBox.startPause.getText().equals("Start")) {
                assistant.mainScreen.clockAppDisplay.stopwatchVBox.startStopwatch();
                final_answer = "The stopwatch has been started! Type 'lap/pause stopwatch' or use the buttons on the left screen.";
            }
            assistant.mainScreen.setClockAppDisplay("Stopwatch");
        }
        else if(skill_num == 24) {
            if (assistant.mainScreen.clockAppDisplay.stopwatchVBox.startPause.getText().equals("Pause")) {
                assistant.mainScreen.clockAppDisplay.stopwatchVBox.pauseStopwatch();
                final_answer = "The stopwatch is paused! Type 'reset/start stopwatch' or use the buttons on the left screen.";
            }
            assistant.mainScreen.setClockAppDisplay("Stopwatch");
        }
        else if(skill_num == 25) {
            if (assistant.mainScreen.clockAppDisplay.stopwatchVBox.lapReset.getText().equals("Lap") && !assistant.mainScreen.clockAppDisplay.stopwatchVBox.lapReset.isDisabled()) {
                assistant.mainScreen.clockAppDisplay.stopwatchVBox.lapStopwatch();
                final_answer = assistant.mainScreen.clockAppDisplay.stopwatchVBox.lap.getText();
            }
            assistant.mainScreen.setClockAppDisplay("Stopwatch");
        }
        else if(skill_num == 26) {
            if (assistant.mainScreen.clockAppDisplay.stopwatchVBox.lapReset.getText().equals("Reset")) {
                assistant.mainScreen.clockAppDisplay.stopwatchVBox.resetStopwatch();
                final_answer = "The stopwatch was reset. Type 'Start the stopwatch' or use the buttons on the left screen.";
            }
            assistant.mainScreen.setClockAppDisplay("Stopwatch");
        }
        else if(skill_num == 27) {
            assistant.mainScreen.setClockAppDisplay("Alarm");
            assistant.mainScreen.clockAppDisplay.alarmVBox.addAlarm(nodeInvestigated.getWordsRemoved().get(0),nodeInvestigated.getWordsRemoved().get(1));
        }
        else if(skill_num == 28) {
            String err = "Something went wrong! To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            assistant.mainScreen.setClockAppDisplay("Timer");
            String time = nodeInvestigated.getWordsRemoved().get(0).replace(" ", "");
            if (time.length() == 8) {
                String[] arr = new String[time.length()];
                for(int i = 0; i < time.length(); i++)
                {
                    arr[i] = String.valueOf(time.charAt(i));
                }
                if ((arr[2].equals(":") || arr[2].equals(".")) && (arr[5].equals(":")|| arr[5].equals("."))) {
                    try {
                        assistant.mainScreen.clockAppDisplay.timerVBox.hoursTimer = Integer.parseInt(arr[0] + arr[1]);
                        assistant.mainScreen.clockAppDisplay.timerVBox.minutesTimer = Math.min(Integer.parseInt(arr[3] + arr[4]), 59); //max value for seconds and minutes is 59
                        assistant.mainScreen.clockAppDisplay.timerVBox.secondsTimer = Math.min(Integer.parseInt(arr[6] + arr[7]), 59);

                        assistant.mainScreen.clockAppDisplay.timerVBox.setTimerTime();
                        assistant.mainScreen.clockAppDisplay.timerVBox.startTimer();
                        final_answer = "A timer has been set for " + assistant.mainScreen.clockAppDisplay.timerVBox.timerTime.getText() + ". Type 'Pause/Cancel timer' or use the options on the left screen.";
                    }
                    catch (NumberFormatException e) {
                        final_answer = err;
                    }
                }
                else { final_answer = err; }
            }
            else { final_answer = err; }
        }
        else if(skill_num == 29) {
            if(assistant.mainScreen.clockAppDisplay.clockVBox.tempTimeZoneIDs.contains(nodeInvestigated.getWordsRemoved().get(0).replace(" ", ""))) {
                final_answer = assistant.mainScreen.clockAppDisplay.clockVBox.getTimeFromZoneID(nodeInvestigated.getWordsRemoved().get(0).replace(" ", "")) + " If you want to add a new clock type 'Add a new clock for Continent/City'.";
            }
            else {
                final_answer = "The area you requested the time for couldn't be found. If you want the available areas, type 'What are the time-zone IDs'.";
            }
        }
        else if(skill_num == 30) {
            if(assistant.mainScreen.clockAppDisplay.clockVBox.tempTimeZoneIDs.contains(nodeInvestigated.getWordsRemoved().get(0).replace(" ", ""))) {
                assistant.mainScreen.clockAppDisplay.clockVBox.addClock(nodeInvestigated.getWordsRemoved().get(0).replace(" ", ""));
                final_answer = "The clock was successfully added!";
            }
            else {
                final_answer = "The area you requested couldn't be found. If you want the available areas, type 'What areas can I add to the clock' or use the options on the left screen.";
            }
            assistant.mainScreen.setClockAppDisplay("Clock");
        }
        else if(skill_num == 31)
        {
            skillEditorState = "skill adder";
            assistant.mainScreen.setSkillEditorAppDisplay("Add skill");
        }else if(skill_num == 32){
            skillEditorState = "rule adder";
            assistant.mainScreen.setSkillEditorAppDisplay("Add rule");
        }else if(skill_num == 33){
            skillEditorState = "skill editor";
            assistant.mainScreen.setSkillEditorAppDisplay("Edit skill");
        }else if(skill_num == 34){
            skillEditorState = "rule editor";
            assistant.mainScreen.setSkillEditorAppDisplay("Edit rule");
        }
        else if(skill_num == 40){
            String searchURL = "https://www.google.com/search" + "?q=" + assistant.messageToUrl(nodeInvestigated.getWordsRemoved().get(0));
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome.exe " + searchURL});
        }
        else if(skill_num == 50){
            if(Data.getMp()!=null){
                Data.getMp().play();
                MediaPlayerDisplay mediaControl = new MediaPlayerDisplay(Data.getMp());
                assistant.mainScreen.displayUrlMediaPlayer(mediaControl);
            }else{
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(assistant.mainScreen.stage);
                try {
                    Media media = new Media (selectedFile.toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    Data.setMp(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    MediaPlayerDisplay mediaControl = new MediaPlayerDisplay(mediaPlayer);
                    assistant.mainScreen.displayUrlMediaPlayer(mediaControl);
                } catch(NullPointerException e){
                    assistant.mainScreen.chat.receiveMessage("No file chosen");
                } catch(MediaException e){
                    assistant.mainScreen.chat.receiveMessage("Filetype not supported");
                }
            }
        }
        else if(skill_num==51){
            if(Data.getMp()!=null){
                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(assistant.mainScreen.stage);
                try {
                    Data.getMp().pause();
                    Media media = new Media (selectedFile.toURI().toString());
                    MediaPlayer mediaPlayer = new MediaPlayer(media);
                    Data.setMp(mediaPlayer);
                    mediaPlayer.setAutoPlay(true);
                    MediaPlayerDisplay mediaControl = new MediaPlayerDisplay(mediaPlayer);
                    assistant.mainScreen.displayUrlMediaPlayer(mediaControl);
                } catch(NullPointerException e){
                    Data.getMp().play();
                    assistant.mainScreen.chat.receiveMessage("No file chosen");
                } catch(MediaException e){
                    Data.getMp().play();
                    assistant.mainScreen.chat.receiveMessage("Filetype not supported");
                }
            }else{
                assistant.mainScreen.chat.receiveMessage("No music is being played");
            }
        }
        else if(skill_num==52){
            Data.getMp().pause();
        }
        else if(skill_num==53){
            Data.getMp().stop();
        }
        else if(skill_num == 59){
            WebView webview = new WebView();
            webview.getEngine().load(
                    nodeInvestigated.getWordsRemoved().get(0)
            );
            Pane pane = new Pane();
            pane.getChildren().add(webview);
            assistant.mainScreen.displaySkill(pane,"ytb watcher");
        }
        else if(skill_num == 60){
            assistant.mainScreen.displaySkill(assistant.mainScreen.calendarDisplay,"calendar");
        }
        else if(skill_num == 61){
            assistant.mainScreen.displaySkill(assistant.mainScreen.calendarDisplay,"calendar");
            assistant.mainScreen.clockAppDisplay.alarmVBox.createAlert(nodeInvestigated.getWordsRemoved().get(0),nodeInvestigated.getWordsRemoved().get(1),nodeInvestigated.getWordsRemoved().get(2),nodeInvestigated.getWordsRemoved().get(3), Color.ORANGE);
        }
        else if(skill_num == 70){
            assistant.mainScreen.chat.receiveMessage("Route from " + nodeInvestigated.getWordsRemoved().get(0) + " to "+nodeInvestigated.getWordsRemoved().get(1) + " being computed");
            assistant.mainScreen.setMapDisplay("route",nodeInvestigated.getWordsRemoved().get(0),nodeInvestigated.getWordsRemoved().get(1));
        }
        else if(skill_num == 71){
            assistant.mainScreen.setMapDisplay("google",null,null);
        }
        else if(skill_num == 72){
            assistant.mainScreen.setMapDisplay("map",nodeInvestigated.getWordsRemoved().get(0),null);
        }
        else if(skill_num == 73){
            assistant.mainScreen.chat.receiveMessage("Places " + nodeInvestigated.getWordsRemoved().get(0) + "in "+nodeInvestigated.getWordsRemoved().get(1) + " being computed");
            assistant.mainScreen.setMapDisplay("places",nodeInvestigated.getWordsRemoved().get(0),nodeInvestigated.getWordsRemoved().get(1));
        }
        else if(skill_num == 74){
            assistant.mainScreen.setMapDisplay("moon", null, null);
        }
        else if(skill_num == 80){
            if(!nodeInvestigated.getWordsRemoved().get(0).contains(" ")){
                if(!assistant.fileParser.changeUserInfo("-Password", nodeInvestigated.getWordsRemoved().get(0),assistant.mainScreen)){
                    assistant.mainScreen.chat.receiveMessage("Couldn't change the password for some reason.");
                }
            }else{
                assistant.mainScreen.chat.receiveMessage("Please remove the space in the password.");
            }
        }
        else if(skill_num==81){
            assistant.mainScreen.chat.receiveMessage("You can change your password/location/age/profession by typing \"Change my password/location/age/profession to <...>\".");
        }
        else if(skill_num==82){
            if(!assistant.fileParser.changeUserInfo("-City", nodeInvestigated.getWordsRemoved().get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the location for some reason.");
            }
        }
        else if(skill_num==83){
            if(!assistant.fileParser.changeUserInfo("-Country", nodeInvestigated.getWordsRemoved().get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the location for some reason.");
            }
        }
        else if(skill_num==84){
            if(!assistant.fileParser.changeUserInfo("-Age", nodeInvestigated.getWordsRemoved().get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the age for some reason.");
            }
        }
        else if(skill_num==85){
            if(!assistant.fileParser.changeUserInfo("-Profession", nodeInvestigated.getWordsRemoved().get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the profession for some reason.");
            }
        }
        else if(skill_num==86){
            assistant.mainScreen.setMenu("Background");
        }
        else if(skill_num==87){
            assistant.mainScreen.setMenu("ThemeColors");
        }
        else if(skill_num==89){
            String info = Files.readString(Path.of("src/DataBase/Users/" + Data.getUsername() + "/" + Data.getUsername() + ".txt"));
            assistant.mainScreen.chat.receiveMessage(info);
        }
        else if(skill_num == 90)
        {
            Platform.exit();
            System.exit(0);
        }
        else if(skill_num == 91){
            assistant.mainScreen.chat.receiveMessage(skillEditor.allOperations());
        }
        else if(skill_num == 92){
            assistant.mainScreen.displayCamera();
        }
        return final_answer;
    }

    /**
     * Compares the difference between two Strings using the Levenshtein algorithm.
     * @param uMessage the user message without punctuation
     * @param dataBase_message the message in the database
     * @param threshold the maximum accepted distance between the Strings
     * @return the score between -1 and threshold
     */
    public int LevenshteinDistance(String uMessage, String dataBase_message, int threshold)
    {
        int uM = uMessage.length();
        int dB = dataBase_message.length();
        int[] cost_previous = new int[uM + 1];
        int[] cost_distance = new int[uM + 1];
        int[] cost_memory;
        int limit = Math.min(uM,threshold);
        int score = -1;

        if(uM == 0 || dB == 0)
        {
            return score;
        }

        if(uM > dB)
        {
            String temp = uMessage;
            uMessage = dataBase_message;
            dataBase_message = temp;
            int temp2 = uM;
            uM = dB;
            dB = temp2;
        }

        for(int i = 0; i <= limit; i++)
        {
            cost_previous[i] = i;
        }
        Arrays.fill(cost_previous, limit, cost_previous.length, Integer.MAX_VALUE);
        Arrays.fill(cost_distance, Integer.MAX_VALUE);

        for(int i = 1; i <= dB; i++)
        {
            char database_char = dataBase_message.charAt(i-1);
            cost_distance[0] = i;

            int min = Math.max(1, i-threshold);
            int max = i > Integer.MAX_VALUE - threshold ? uM : Math.min(uM,threshold+i);

            if(min > max)
            {
                return -1;
            }

            if(min > 1)
            {
                cost_distance[min-1] = Integer.MAX_VALUE;
            }

            for(int j = min; j <= max; j++)
            {
                if(uMessage.charAt(j-1) == database_char)
                {
                    cost_distance[j] = cost_previous[j-1];
                }
                else
                {
                    cost_distance[j] = 1 + Math.min(Math.min(cost_distance[j-1], cost_previous[j]), cost_previous[j-1]);
                }
            }

            cost_memory = cost_previous;
            cost_previous = cost_distance;
            cost_distance = cost_memory;
        }

        if(cost_previous[uM] <= threshold)
        {
            score = cost_previous[uM];
        }
        return score;
    }

    /**
     * class used to store answers and their score
     */
    public class Answers{
        private String answer;
        private int score = -1;

        /**
         * Used by the Levenshtein distance to rank the possible answers with their
         * corresponding score in an ArrayList
         * @param pScore the LevenshteinDistance
         * @param pAnswer the answer from the database
         */
        public Answers(int pScore, String pAnswer)
        {
            this.answer = pAnswer;
            this.score = pScore;
        }

        public int getScore()
        {
            return score;
        }

        public String getAnswer()
        {
            return answer;
        }
    }
}
