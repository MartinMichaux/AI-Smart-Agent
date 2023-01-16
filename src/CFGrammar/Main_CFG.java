package CFGrammar;

import Agents.Assistant;
import DataBase.Data;
import FileParser.FileParser;
import Interface.Display.MediaPlayerDisplay;
import SkillEditor.SkillEditorHandler;
import Skills.Schedule.Skill_Schedule;
import TextRecognition.TextRecognition;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.opencv.core.Mat;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main_CFG {
    /**
     * Use this main to test the CFG without running the whole project
     * [TO DELETE LATER]
     * @param args
     */
    public static void main(String[] args) throws IOException {
        String test = "What is the weather like?";

        Main_CFG cfg = new Main_CFG(test);

        //JsonReader jr = new JsonReader();
        //ArrayList<String> grammar = jr.getAllRules();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private ArrayList<Rule> rules = new ArrayList<>();
    private ArrayList<String> variable_words;
    private Branch_Rule BR = null;
    private Word_Rule WR = null;
    private Branch[][] Br = null;
    private String user_message;
    private String[] u_message;
    private int message_length;
    private Assistant assistant;
    private SkillEditorHandler skillEditor;

    private double verb_weight;
    private double noun_weight;
    private double var_weight;
    private double threshold;
    private int total_score = 0;

    public Main_CFG(String pUser_message) throws IOException
    {
        user_message = removePunctuation(pUser_message).toLowerCase();
        u_message = user_message.split("\\s");
        message_length = u_message.length;

        /*JsonReader reader = new JsonReader();
        ArrayList<String> checkgrammar = reader.getAllRules();
        splitGrammar(checkgrammar);
        initialize_Tree();
        implement_Tree();*/

        ArrayList<String> grammar = getAllRules();
        splitGrammar(grammar);
        initialize_Tree();
        implement_Tree();

        verb_weight = 8.416;
        noun_weight = 6.606;
        var_weight = 3.241;
        threshold = 4.238;

        StringBuffer result = new StringBuffer();
        getEndSplit(result);
        System.out.println(result.toString());

        //toPrint();
        getSkillNbr();
    }

    public Main_CFG(String pUser_message, Assistant assistant) throws IOException
    {
        user_message = removePunctuation(pUser_message).toLowerCase();
        u_message = user_message.split("\\s");
        message_length = u_message.length;

        this.assistant = assistant;
        skillEditor = assistant.skillEditor;

        /*JsonReader reader = new JsonReader();
        ArrayList<String> checkgrammar = reader.getAllRules();
        splitGrammar(checkgrammar);
        initialize_Tree();
        implement_Tree();*/

        ArrayList<String> grammar = getAllRules();
        splitGrammar(grammar);
        initialize_Tree();
        implement_Tree();

        verb_weight = 8.416;
        noun_weight = 6.606;
        var_weight = 3.241;
        threshold = 4.238;

        StringBuffer result = new StringBuffer();
        getEndSplit(result);
        System.out.println(result.toString());

        toPrint();
        getSkillNbr();
    }

    /**
     * Constructor for the Genetic algorithm experience
     * @param weights
     */
    public Main_CFG(double[] weights) throws IOException
    {
        verb_weight = weights[0];
        noun_weight = weights[1];
        var_weight = weights[2];
        threshold = weights[3];

        ArrayList<String> sentences = getAllSentences();

        for(int i = 1; i <= 100; i++)
        {
            Random rand = new Random();
            int index = rand.nextInt((sentences.size()) + 1);
            String[] temp = sentences.get(i).split(",");
            int skill_nbr = Integer.parseInt(temp[0]);
            String sentence = temp[1];

            user_message = removePunctuation(sentence).toLowerCase();
            u_message = user_message.split("\\s");
            message_length = u_message.length;

            ArrayList<String> grammar = getAllRules();
            splitGrammar(grammar);
            initialize_Tree();
            implement_Tree();

            StringBuffer result = new StringBuffer();
            getEndSplit(result);
            System.out.println(result.toString());

            toPrint();
            int skill_found = getSkillNbr();

            if(skill_found == skill_nbr)
            {
                setTotal_score(total_score++);
            }
        }
    }

    public String removePunctuation(String uMessage)
    {
        String clean_uMessage = "";
        String temp = uMessage.replaceAll("\\p{Punct}&&[^/]]","");
        clean_uMessage = temp.trim().replaceAll(" +", " ");
        if(clean_uMessage.endsWith("?")) {clean_uMessage = clean_uMessage.replaceAll("[?]", ""); }
        else if((clean_uMessage.endsWith("."))) { clean_uMessage = clean_uMessage.substring(0,clean_uMessage.length()-1);}
        return clean_uMessage;
    }

    public void splitGrammar(ArrayList<String> rules)
    {
        BR = new Branch_Rule();
        WR = new Word_Rule();

        for(int i = 0; i < rules.size(); i++)
        {
            String[] rule = rules.get(i).split("\\s");
            if(rule.length < 5)
            {
                continue;
            }
            else if(rule.length == 5)
            {
                //System.out.println("Word rule : "+rule[0]);
                WR.addRule(rule);
            }
            else
            {
                //System.out.println("Branch rule : "+rule[0]);
                BR.addRule(rule);
            }
        }
    }

    public int getCYK(String this_rule, int iter, int word_length)
    {
        return 1;
    }

    public int getSkillNbr()
    {
        double score = 0;
        double best_score = 0;
        int final_skill_nbr = 0;
        ArrayList<Integer> possible_skills = new ArrayList<>();
        ArrayList<String> words_toSearch = new ArrayList<>();
        variable_words = new ArrayList<>();
        TextRecognition TR = new TextRecognition();

        ArrayList<String> main_words = WR.getResult_array();
        for(int i = 0; i < main_words.size(); i++)
        {
            System.out.println(main_words.get(i));
        }
        System.out.println("");
        FileParser sk_file = new FileParser();
        List<List<String>> allSkills = sk_file.getAllSkillsKind();

        for(int i = 0; i < main_words.size(); i++)
        {
            String[] main_word = main_words.get(i).split(":");
            String category = main_word[0];
            String word = main_word[1];

            if(category.contains("_word") || category.equals("VB") || category.equals("VBZ") || category.equals("N"))
            {
                //System.out.println("Added word to the list: "+ word);
                words_toSearch.add(word);
            }

            if(category.equals("FW"))
            {
                System.out.println("Added FOREIGN word to the list: "+word);
                variable_words.add(word);
            }
        }

        for(int i = 0; i < allSkills.size(); i++)
        {
            score = 0;
            ArrayList<String> verbs = new ArrayList<>();
            ArrayList<String> nouns = new ArrayList<>();
            int var = Integer.parseInt(allSkills.get(i).get(3));
            int skill_nbr = Integer.parseInt(allSkills.get(i).get(1));

            for(int h = 0; h < 4; h++)
            {
                verbs.add(allSkills.get(i).get(h+4));
                nouns.add(allSkills.get(i).get(h+8));
            }

            for(int j = 0; j < words_toSearch.size(); j++)
            {
                for(int z = 0; z < verbs.size(); z++)
                {
                    String[] temp = verbs.get(z).split("#");
                    String verb = temp[0];
                    Double v_prob = Double.parseDouble(temp[1]);
                    if(words_toSearch.get(j).equals(verb))
                    {
                        score = score + verb_weight*v_prob;
                        //System.out.println("Score after verb = "+score);
                    }
                }
                for(int z = 0; z < nouns.size(); z++)
                {
                    String[] temp = nouns.get(z).split("#");
                    String noun = temp[0];
                    Double n_prob = Double.parseDouble(temp[1]);
                    if(words_toSearch.get(j).equals(noun))
                    {
                        score = score + noun_weight*n_prob;
                        //System.out.println("Score after noun = "+score);
                    }
                }
            }

            // nbr of variable for this skill
            if(var != 0)
            {
                if(var == variable_words.size())
                {
                    score = score + var_weight;
                    //System.out.println("Score after variable = "+score);
                }
                else
                {
                    score = score - var_weight;
                }
            }

            if(score > best_score)
            {
                possible_skills.clear();
                possible_skills.add(skill_nbr);
                best_score = score;
            }
            else if(score == best_score)
            {
                possible_skills.add(skill_nbr);
            }
        }

        for(int i = 0; i < variable_words.size(); i++)
        {
            System.out.println("Final variable :   "+ variable_words.get(i));
        }

        //System.out.println("Final skill list size : "+possible_skills.size());
        for(int z = 0; z < possible_skills.size(); z++)
        {
            System.out.println("Skill nbr :        "+possible_skills.get(z));
        }

        System.out.println("With best score:   "+ best_score);

        if(best_score >= threshold)
        {
            int n = new Random().nextInt(possible_skills.size());
            final_skill_nbr = possible_skills.get(n);
        }
        else
        {
            final_skill_nbr = 0;
        }
        return final_skill_nbr;
    }

    public String addOrRemoveRule(String message) throws IOException
    {
        Boolean terminal = true;
        String[] rule = message.split("/");
        if(rule[0].equals("ADD"))
        {
            String[] add = rule[1].split(":");
            String LHS = add[0];
            if(add[1].contains(","))
            {
                String[] RHS = add[1].split(",");
                terminal = false;
            }
            JsonReader jr = new JsonReader();
            jr.addRules(rule[1], terminal);
            return "The rule was added to the grammar.";
        }
        else if(rule[0].equals("REMOVE"))
        {
            String[] add = rule[1].split(":");
            String LHS = add[0];
            if(add[1].contains(","))
            {
                String[] RHS = add[1].split(",");
                terminal = false;
            }
            JsonReader jr = new JsonReader();
            jr.removeRule(rule[1], terminal);
            return "The rule was removed to the grammar.";
        }
        else
        {
            return "Error - could not add/remove the rule";
        }
    }

    /**
     * Partly Stolen from Github, we need the same from json (read,write)
     * @throws IOException
     */
    public static ArrayList<String> getAllRules() throws IOException
    {
        ArrayList<String> grammar = null;
        FileReader file = null;
        BufferedReader buffer = null;

        try{
            file = new FileReader(new File("src\\CFGrammar\\grammar.txt"));
            buffer = new BufferedReader(file);
            grammar = new ArrayList<>();
            String rule = null;
            while((rule = buffer.readLine()) != null)
            {
                grammar.add(rule);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(file != null)
            {
                try{
                    file.close();
                }
                catch(Exception e) {}
            }
            if(buffer != null)
            {
                try{
                    buffer.close();
                }
                catch(Exception e) {}
            }
        }
        return grammar;
    }

    public void getEndSplit(StringBuffer sentence)
    {
        Branch end_branch = Br[0][message_length-1];
        end_branch.get_endResult(sentence);
    }

    public ArrayList<String> getVariable_words()
    {
        return variable_words;
    }

    public String toStringTree(int l_side, int iter, int length)
    {
        return null;
    }

    public void addProductionRule(String rule)
    {
        //Replaces skill editor, add grammar rules to the json file
    }

    public void removeProductionRule(String rule)
    {
        //Removes an unwanted rule
    }

    public void addWords(String word_cat, String word)
    {
        //Adds a single word to the json file in the right category "N","V"...
    }

    public void initialize_Tree()
    {
        Br = new Branch[message_length][];

        for(int i = 0; i < message_length; i++)
        {
            Br[i] = new Branch[message_length];
            for(int j = i; j < message_length; j++)
            {
                Br[i][j] = new Branch();
            }
        }

        for(int i = 0; i < message_length; i++)
        {
            initialize_Branch(i);
        }
    }


    private int kk = 0;
    public void initialize_Branch(int nbr)
    {
        String Br_word = u_message[nbr];
        kk++;
        //System.out.println("Branch init. : "+kk);
        ArrayList<Branch> Br_ter = WR.interpret(Br_word);
        //System.out.println("Size with unknown word: "+Br_ter.size());
        for(int i = 0; i < Br_ter.size(); i++)
        {
            //Br[i][i].addRule(Br_ter.get(i), null, null);
            addRuleToBranch(Br[nbr][nbr], Br_ter.get(i), null, null);
        }
    }

    public void addRuleToBranch(Branch parent, Branch pBr, Branch LHS, Branch RHS)
    {
        parent.addRule(pBr, LHS, RHS);
    }

    public void implement_Tree()
    {
        for(int words = 1; words < message_length; words++)
        {
            for(int i = 0; i < message_length-words; i++)
            {
                implement_oneBranch(i,words+i);
            }
        }
    }

    public void implement_oneBranch(int i, int words_plus)
    {
        for(int j = i; j < words_plus; j++)
        {
            implement_Branch(i, j, words_plus);
        }
    }

    public void implement_Branch(int i, int j, int words_plus)
    {
        Branch this_Branch = Br[i][j];
        ArrayList<Branch> subTree = this_Branch.getValues();
        for(int k = 0; k < subTree.size(); k++)
        {
            Branch this_Branch2 = Br[j+1][words_plus];
            ArrayList<Branch> sub_subTree = this_Branch2.getValues();
            //kk++;
            //System.out.println("Into the second rule: "+subTree.size()+" : "+kk);
            for(int l = 0; l < sub_subTree.size(); l++)
            {
                //System.out.println("Find "+ subTree.get(k).getWord_category() + " + " + sub_subTree.get(l).getWord_category());
                Branch last_Branch = new Branch();
                last_Branch = BR.test(subTree.get(k), sub_subTree.get(l));

                if(last_Branch != null)
                {
                    //kk++;
                    //System.out.println("--- FINALLY HERE --- "+kk);
                    Br[i][words_plus].addRule(last_Branch, subTree.get(k), sub_subTree.get(l));
                }
            }
        }
        //}
    }

    public Map<String,Object> getSentenceStructure(String sentence)
    {
        return null;
    }

    public void toPrint()
    {
        System.out.println("--- Tree ---");
        for(int i = 0; i < message_length; i++)
        {
            for(int j = 0; j < message_length; j++)
            {
                if(j<i)
                {
                    System.out.print("\t");
                }
                else
                {
                    System.out.print(Br[i][j].toString()+"\t");
                }
            }
            System.out.println();
        }

    }

    public String getSkill(String pNumb, ArrayList<String> variable_words) throws Exception
    {
        //The specific Skills will be called here
        int skill_num = Integer.parseInt(pNumb);
        int variable_words_size = variable_words.size();
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
            assistant.mainScreen.setWeatherDisplay(city,country,false);
            final_answer = "This is what I found for the weather in "+ city + ", " + country + ". " + assistant.mainScreen.weatherDisplay.currentDataString() + "If you want to change the location, type 'Change weather location to City,Country.' (e.g. Amsterdam,NL).";
        }
        else if(skill_num == 2){
            String city;
            String country = "";
            if(variable_words_size == 1)
            {
                city = variable_words.get(0);
                assistant.mainScreen.setWeatherDisplay(city, country, false);
                final_answer = "This is what I found for the weather in "+ city + ". " + assistant.mainScreen.weatherDisplay.currentDataString() + "If you want to change the location, type 'Change weather location to City,Country.' (e.g. Amsterdam,NL).";
            }
            else if(variable_words_size == 2)
            {
                city = variable_words.get(0);
                country = variable_words.get(1);
                assistant.mainScreen.setWeatherDisplay(city, country, false);
                final_answer = "This is what I found for the weather in "+ city + ". " + assistant.mainScreen.weatherDisplay.currentDataString() + "If you want to change the location, type 'Change weather location to City,Country.' (e.g. Amsterdam,NL).";
            }
            else
            {
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
            final_answer = " This is what I found for your request.";
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
            if (!assistant.mainScreen.clockAppDisplay.timerVBox.timerTime.getText().equals("00 : 00 : 00") && assistant.mainScreen.clockAppDisplay.timerVBox.startPauseResume.getText().equals("Start")) {
                assistant.mainScreen.clockAppDisplay.timerVBox.startTimer();
                final_answer = "The timer started. Type 'Pause/Cancel timer' or use the options on the left screen.";
            }
            else {
                final_answer = "To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            }
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 19) {
            if (assistant.mainScreen.clockAppDisplay.timerVBox.startPauseResume.getText().equals("Pause")) {
                assistant.mainScreen.clockAppDisplay.timerVBox.pauseTimer();
                final_answer = "The timer is paused. Type 'Resume/Cancel timer' or use the options on the left screen.";
            }
            else
            {
                final_answer = "Can not pause the timer if it is not running.";
            }
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 20) {
            if (assistant.mainScreen.clockAppDisplay.timerVBox.startPauseResume.getText().equals("Resume")) {
                assistant.mainScreen.clockAppDisplay.timerVBox.resumeTimer();
                final_answer = "The timer is resumed. Type 'Pause/Cancel timer' or use the options on the left screen.";
            }
            else {
                final_answer = "To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            }
            assistant.mainScreen.setClockAppDisplay("Timer");
        }
        else if(skill_num == 21) {
            if (!assistant.mainScreen.clockAppDisplay.timerVBox.cancel.isDisabled()) {
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
            assistant.mainScreen.clockAppDisplay.alarmVBox.addAlarm(variable_words.get(0),variable_words.get(1));
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 28) {
            String err = "Something went wrong! To set a new timer use the options on the left screen or type 'Set/Start a timer for hh:mm:ss'.";
            assistant.mainScreen.setClockAppDisplay("Timer");
            String time = variable_words.get(0).replace(" ", "");
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
            if(assistant.mainScreen.clockAppDisplay.clockVBox.tempTimeZoneIDs.contains(variable_words.get(0).replace(" ", ""))) {
                final_answer = assistant.mainScreen.clockAppDisplay.clockVBox.getTimeFromZoneID(variable_words.get(0).replace(" ", "")) + " If you want to add a new clock type 'Add a new clock for Continent/City'.";
            }
            else {
                final_answer = "The area you requested the time for couldn't be found. If you want the available areas, type 'What are the time-zone IDs'.";
            }
        }
        else if(skill_num == 30) {
            if(assistant.mainScreen.clockAppDisplay.clockVBox.tempTimeZoneIDs.contains(variable_words.get(0).replace(" ", ""))) {
                assistant.mainScreen.clockAppDisplay.clockVBox.addClock(variable_words.get(0).replace(" ", ""));
                final_answer = "The clock was successfully added!";
            }
            else {
                final_answer = "The area you requested couldn't be found. If you want the available areas, type 'What areas can I add to the clock' or use the options on the left screen.";
            }
            assistant.mainScreen.setClockAppDisplay("Clock");
        }
        else if(skill_num == 31)
        {
            assistant.mainScreen.setSkillEditorAppDisplay("Add skill");
            final_answer = " This is what I found for your request.";
        }else if(skill_num == 32){
            assistant.mainScreen.setSkillEditorAppDisplay("Add rule");
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 40){
            String searchURL = "https://www.google.com/search" + "?q=" + assistant.messageToUrl(variable_words.get(0));
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome.exe " + searchURL});
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 50){
            if(Data.getMp()!=null){
                Data.getMp().play();
                MediaPlayerDisplay mediaControl = new MediaPlayerDisplay(Data.getMp());
                assistant.mainScreen.displayUrlMediaPlayer(mediaControl);
                final_answer = " This is what I found for your request.";
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
                    //assistant.mainScreen.chat.receiveMessage("No file chosen");
                    final_answer = "No file chosen";
                } catch(MediaException e){
                    Data.getMp().play();
                    //assistant.mainScreen.chat.receiveMessage("Filetype not supported");
                    final_answer = "Filetype not supported";
                }
            }else{
                final_answer = "No music is being played";
            }
        }
        else if(skill_num==52){
            Data.getMp().pause();
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num==53){
            Data.getMp().stop();
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 59){
            WebView webview = new WebView();
            webview.getEngine().load(
                    variable_words.get(0)
            );
            Pane pane = new Pane();
            pane.getChildren().add(webview);
            assistant.mainScreen.displaySkill(pane,"ytb watcher");
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 60){
            assistant.mainScreen.displaySkill(assistant.mainScreen.calendarDisplay,"calendar");
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 61){
            assistant.mainScreen.displaySkill(assistant.mainScreen.calendarDisplay,"calendar");
            assistant.mainScreen.clockAppDisplay.alarmVBox.createAlert(variable_words.get(0),variable_words.get(1),variable_words.get(2),variable_words.get(3), Color.ORANGE);
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 70){
            assistant.mainScreen.chat.receiveMessage("Route from " + variable_words.get(0) + " to "+variable_words.get(1) + " being computed");
            assistant.mainScreen.setMapDisplay("route",variable_words.get(0),variable_words.get(1));
        }
        else if(skill_num == 71){
            assistant.mainScreen.setMapDisplay("google",null,null);
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 72){
            assistant.mainScreen.setMapDisplay("map",variable_words.get(0),null);
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num == 80){
            if(!variable_words.get(0).contains(" ")){
                if(!assistant.fileParser.changeUserInfo("-Password", variable_words.get(0),assistant.mainScreen)){
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
            if(!assistant.fileParser.changeUserInfo("-City", variable_words.get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the location for some reason.");
            }
        }
        else if(skill_num==83){
            if(!assistant.fileParser.changeUserInfo("-Country", variable_words.get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the location for some reason.");
            }
        }
        else if(skill_num==84){
            if(!assistant.fileParser.changeUserInfo("-Age", variable_words.get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the age for some reason.");
            }
        }
        else if(skill_num==85){
            if(!assistant.fileParser.changeUserInfo("-Profession", variable_words.get(0),assistant.mainScreen)){
                assistant.mainScreen.chat.receiveMessage("Couldn't change the profession for some reason.");
            }
        }
        else if(skill_num==86){
            assistant.mainScreen.setMenu("Background");
            final_answer = " This is what I found for your request.";
        }
        else if(skill_num==87){
            assistant.mainScreen.setMenu("ThemeColors");
            final_answer = " This is what I found for your request.";
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
            final_answer = " This is what I found for your request.";
        }
        return final_answer;
    }

    public void setTotal_score(int total_score) {
        this.total_score = total_score;
    }

    public void setVerb_weight(double verb_weight) {
        this.verb_weight = verb_weight;
    }

    public void setVar_weight(double var_weight) {
        this.var_weight = var_weight;
    }

    public void setNoun_weight(double noun_weight) {
        this.noun_weight = noun_weight;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    public int getTotal_score() {
        return total_score;
    }

    public double getNoun_weight() {
        return noun_weight;
    }

    public double getVar_weight() {
        return var_weight;
    }

    public double getVerb_weight() {
        return verb_weight;
    }

    public ArrayList<String> getAllSentences() throws IOException
    {
        ArrayList<String> sentences = null;
        FileReader file = null;
        BufferedReader buffer = null;

        try{
            file = new FileReader(new File("src\\CFGrammar\\TrainCFG.txt"));
            buffer = new BufferedReader(file);
            sentences = new ArrayList<>();
            String rule = null;
            while((rule = buffer.readLine()) != null)
            {
                sentences.add(rule);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(file != null)
            {
                try{
                    file.close();
                }
                catch(Exception e) {}
            }
            if(buffer != null)
            {
                try{
                    buffer.close();
                }
                catch(Exception e) {}
            }
        }
        return sentences;
    }
}
