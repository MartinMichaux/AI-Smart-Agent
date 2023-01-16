package SkillEditor;

import FileParser.FileParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class SkillEditorHandler {

    private List<List<String>> allSkillsKind;
    private FileParser fileParser;

    private File dataBase = new File("src\\DataBase\\textRecognitionSkills.txt");

    public SkillEditorHandler(){
        fileParser = new FileParser();
        allSkillsKind = fileParser.getAllSkillsKind();
    }

    public String allOperations(){
        String r = "";
        for (List<String> operation: allSkillsKind) {
            r+=("Main skill: " + operation.get(0) +", description the operation: " + operation.get(2) +  ", required nbr of variables: " + operation.get(3)+", corresponding task number: " + operation.get(1) + "\n");
        }
        return r;
    }

    public List<String> getMainSkills(){
        List<String> mainSkills = new ArrayList<>();
        for (List<String> row: allSkillsKind) {
            if(!mainSkills.contains(row.get(0))){
                mainSkills.add(row.get(0));
            }
        }
        return mainSkills;
    }
    public List<String> getTasks(String skill){
        List<String> allTasks = new ArrayList<>();
        for (List<String> row: allSkillsKind) {
            if(row.get(0).equals(skill)){
                allTasks.add(row.get(2));
            }
        }
        return allTasks;
    }


    public String handleAddSkill(String question, String answer){
        String response = "";
        if (question.isEmpty()||question.isBlank()) {
            response = ("Question : \"" + question + "\" is not under the correct form.");
        } else {
            int result = 0;
            result = addSkill(question,answer);
            if (result == 1) {
                response = "Question : \"" + question+ "\" was successfully added to the database.";
            } else if(result == -1) {
                response = "Question : \"" + question + "\" could not be added to the database";
            }else if(result==-2){
                response = "Question : \"" + question +  "\" does not contain the required number of variables";
            }
        }
        return response;
    }

    public int addSkill(String question, String answer){
        int success = -1;
        try{
            BufferedWriter newData = new BufferedWriter(new FileWriter(new File("src\\DataBase\\textRecognitionSkills.txt"), true));
            success = 1;
            newData.append("U " + question + System.lineSeparator());
            newData.append("B " + answer + System.lineSeparator());
            newData.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public String skillToDisplay(String question,String skill,String task){
        String displayNbr = "-1";
        for (List<String> row: allSkillsKind) {
            if(row.get(0).equals(skill)&&row.get(2).equals(task)){
                if(containsSameNbrOfVariables(question,Integer.valueOf(row.get(3)))){
                    displayNbr = row.get(1);
                }
            }
        }
        return displayNbr;
    }

    /**
     * @param s message from the skill database
     * @return true if s contains the same nbr of variables than the message stored in the current node
     */
    public boolean containsSameNbrOfVariables(String s, int nbrOfVar){
        int nbrOfRandomWords = 0;
        for (int i = 0; i < s.length(); i++) {
            if(s.charAt(i)=='<'){
                nbrOfRandomWords++;
            }
        }
        if(nbrOfRandomWords==nbrOfVar){
            return true;
        }
        return false;
    }

    public List<String> getSentences(String task) throws IOException {
        BufferedReader data = new BufferedReader(new FileReader(dataBase));
        List<String> allSentences = new ArrayList<>();
        List<String> tempSentences = new ArrayList<>();
        String skillNum = null;

        for (List<String> row: allSkillsKind) {
            if(row.get(2).equals(task)){
                skillNum = row.get(1); //getting skill number
            }
        }

        String s;
        while ((s = data.readLine()) != null) { //getting all sentences with skillNum
            if (s.startsWith("U")) {
                tempSentences.add(s.substring(2));
            }
            if(s.startsWith("B")){
                if (s.equals("B " + skillNum)) {
                    for (String str : tempSentences) {
                        allSentences.add(str); }
                } else {
                    tempSentences.clear();
                }
            }
        }
        return allSentences;
    }

    public void deleteSentenceFromFile(String lineContent) throws IOException
    {
        List<String> out = Files.lines(dataBase.toPath())
                .filter(line -> !line.contains("U " + lineContent))
                .collect(Collectors.toList());
        Files.write(dataBase.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void editSentence(String oldSentence, String newSentence) throws IOException {
        String data = "";
        try {
            Scanner myReader = new Scanner(dataBase);
            while (myReader.hasNextLine()) {
                String line = myReader.nextLine();
                if (line.startsWith("U") && line.equals("U " + oldSentence)) {
                    data += ("U " + newSentence) + "\n";
                } else {
                    data += line + "\n";
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println(data);

        FileWriter fileWriter = new FileWriter(dataBase);
        fileWriter.write(data);
        fileWriter.close();
    }
}
