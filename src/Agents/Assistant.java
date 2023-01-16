package Agents;

import CFGrammar.JsonReader;
import FileParser.FileParser;
import Interface.Screens.MainScreen;
import SkillEditor.SkillEditorHandler;
import TextRecognition.TextRecognition;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Assistant {
    private File dataBase;
    public MainScreen mainScreen;
    public FileParser fileParser;
    public SkillEditorHandler skillEditor;
    private String user_name;
    public List<String> assistantMessage;
    private ArrayList SkillKeys;
    private Properties keySet;
    private Stack<String> randomWords;
    private String response;

    public TextRecognition textRecognition;
    private JsonReader jsonParser;

    public void loadKeys() throws IOException {
        Properties keys = new Properties();
        String fileName = "keys.properties";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (inputStream != null) {
            keys.load(inputStream);
            keySet = keys;
            Set<Object> allKeys = keys.keySet();
            for(Object k:allKeys)
            {
                String key = (String) k;
                SkillKeys.add(k);
            }

        } else {
            throw new FileNotFoundException("property file '" + fileName + "' not found in the classpath");
        }

    }

    public Assistant(MainScreen pMainScreen, String pUser_name, List pAssistantMessage) throws IOException {
        mainScreen = pMainScreen;
        jsonParser = new JsonReader();
        fileParser = new FileParser();
        skillEditor = new SkillEditorHandler();
        user_name = pUser_name;
        assistantMessage = pAssistantMessage;
        response = "";
        randomWords = new Stack<>();
        if (System.getProperty("os.name").contains("Mac OS"))
        {
            dataBase = new File("src/DataBase/textRecognitionSkills.txt");
        }
        else
        {
            dataBase = new File("src\\DataBase\\textRecognitionSkills.txt");
        }

        textRecognition = new TextRecognition(this);
    }

    public String removeVariables(String s){
        String newS = "";
        //REMOVE VARIABLES <CITY>,<DAY>,... from the sentence starting with U
        String[] message = s.split(" ");
        for (int i = 0; i < message.length; i++) {
            if(!message[i].equals("<VARIABLE>")){
                if(i==message.length-1){
                    newS+=message[i];
                }else{
                    newS+=message[i] + " ";
                }
            }
        }
        return newS;
    }

    public String handleNewSkill(String clean_uMessage) throws IOException {
        int result = addNewSkill(clean_uMessage);
        if(result == 1)
        {
            response =  "The new skill was successfully added to the database.";
        }
        else
        {
            response =  "Sorry something went wrong, the new skill could not be added to the database";
        }
        return response;
    }


    public String messageToUrl(String message){
        String url = "";
        for (int i = 0; i < message.length(); i++) {
            if(message.charAt(i) == ' '){
                url+='+';
            }else{
                url+=message.charAt(i);
            }
        }
        return url;
    }

    public boolean isNumber(String res)
    {
        try{
            int d = Integer.parseInt(res);
        } catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    /**
     * Adds a new skill in the database, the user has to follow a specific structure to
     * add question(s) and answer(s) to the database.
     * @param uMessage the message from the user containing the new skill
     */
    public int addNewSkill(String uMessage) throws IOException {
        int success = -1;
        String[] split_uMessage = uMessage.split(";");

        //Ajouter des if pour les commas
        String[] uQuestions = split_uMessage[0].split(",");
        String bAnswer = split_uMessage[1];
        if(split_uMessage.length > 2 || split_uMessage.length < 2)
        {
            success = 0;
        }
        else
        {
            for (int i = 0; i < uQuestions.length; i++) {
                skillEditor.handleAddSkill(uQuestions[i],bAnswer);
            }
            success = 1;
        }
        return success;
    }

    public void editSkill(String message) throws IOException {
        String[] split = message.split("/");
        if(split[0].equals("REMOVE")){
            skillEditor.deleteSentenceFromFile(split[1]);
        }else{
            skillEditor.editSentence(split[0],split[1]);
        }
        mainScreen.skillEditorDisplay.editSkillEditorVBox.handleComboBoxes(true);
    }

    public void editRule(String message) throws IOException {
        String[] split = message.split("/");
        if(split[0].equals("REMOVE")){
            mainScreen.skillEditorDisplay.editRuleEditorVBox.jsonReader.removeRule(split[1],mainScreen.skillEditorDisplay.editRuleEditorVBox.isTerminal(split[1]));
        }else{
            mainScreen.skillEditorDisplay.editRuleEditorVBox.editRule(split[0],split[1],mainScreen.skillEditorDisplay.editRuleEditorVBox.isTerminal(split[0]));
        }
        mainScreen.skillEditorDisplay.editRuleEditorVBox.handleComboBoxes(true);
    }

    /**
     * Adds a new rule in the database, the user has to follow a specific structure to
     * add question(s) and answer(s) to the database.
     * @param uMessage the message from the user containing the new skill
     */
    public void addNewRule(String uMessage) throws IOException {
        boolean isTerminal = false;
        if(uMessage.charAt(uMessage.length()-1)=='+'){
            isTerminal = true;
        }
        //creating a constructor of StringBuffer class
        StringBuffer sb= new StringBuffer(uMessage);
        //invoking the method
        sb.deleteCharAt(sb.length()-1);
        jsonParser.addRules(sb.toString(),isTerminal);
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

    public void setAssistantMessage(List pAssistantMessage)
    {
        assistantMessage = pAssistantMessage;
    }

}
