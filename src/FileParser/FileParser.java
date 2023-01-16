package FileParser;

import DataBase.Data;
import Interface.Screens.MainScreen;
import javafx.scene.text.Text;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class FileParser {

    public String getUsersPicture(String type){
        File userPicture = new File("src/DataBase/Users/"+ Data.getUsername()+"/"+type+".png");
        if(!userPicture.exists()||!userPicture.isFile()){
            userPicture = new File("src/DataBase/Users/"+Data.getUsername()+"/"+type+".jpg");
            if(!userPicture.exists()||!userPicture.isFile()){
                System.out.println("Something went wrong charging your " + type);
                if(!userPicture.exists()){
                    System.out.println("It seems like your " + type + " does not exists");
                }
                return null;
            }else{
                return "src/DataBase/Users/"+Data.getUsername()+"/"+type+".jpg";
            }
        }else{
            return "src/DataBase/Users/"+Data.getUsername()+"/"+type+".png";
        }
    }

    public void createUser(String user,String psw, Text left){
        //Creating a File object
        File file = new File("src/DataBase/Users/"+user);
        //Creating the directory
        boolean bool = file.mkdir();
        if(bool){
            System.out.println("Directory created successfully");
        }else {
            System.out.println("Sorry couldn’t create specified directory");
        }
        try {
            FileWriter writer;
            {
                try {
                    writer = new FileWriter("src/DataBase/Users/"+user+"/"+user+".txt");
                    PrintWriter out = new PrintWriter(writer);
                    out.println("-Password: "+"\n" + psw);
                    out.println("-Location: "+"\n" + "/");
                    out.println("-Age: "+"\n" + "/");
                    out.println("-Profession: "+"\n" + "/");
                    out.println("-Face: "+"\n" + "/");
                    writer.close();
                } catch (IOException e) {
                    left.setText("Sorry, something went wrong");
                    e.printStackTrace();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUserInfo(String username,String info){
        String result = "";
        File userFile = new File("src/DataBase/Users/"+username+"/"+username+".txt");

        try{
            BufferedReader reader = new BufferedReader(new FileReader(userFile));
            String line = "", oldtext = "";
            while((line = reader.readLine()) != null)
            {
                oldtext += line + "\r\n";
            }
            reader.close();

            String[] lines = oldtext.split(System.getProperty("line.separator"));
            for (int i = 0; i < lines.length; i++) {
                if(lines[i].startsWith(info))
                {
                    result = lines[i+1];
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean changeUserInfo(String info, String edit, MainScreen mainScreen){
        File userFile = new File("src/DataBase/Users/"+Data.getUsername()+"/"+Data.getUsername()+".txt");

        try{
            BufferedReader reader = new BufferedReader(new FileReader(userFile));
            String line = "", oldtext = "";
            while((line = reader.readLine()) != null)
            {
                oldtext += line + "\r\n";
            }
            reader.close();

            String[] lines = oldtext.split(System.getProperty("line.separator"));
            for (int i = 0; i < lines.length; i++) {
                if(lines[i].startsWith(info))
                {
                    lines[i+1] = edit;
                    break;
                }
            }
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < lines.length; i++) {
                sb.append(lines[i] + "\n");
            }
            String str = sb.toString();

            FileWriter writer = new FileWriter(userFile);
            writer.write(str);
            writer.close();
            if(mainScreen!=null) {
                mainScreen.chat.receiveMessage("Your new " + info + " is " + edit);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkUserInfo(String info, String user, String psw){
        File userFile = new File("src/DataBase/Users/"+user+"/"+user+".txt");

        try{
            BufferedReader data = new BufferedReader(new FileReader(userFile));

            String s;
            while ((s = data.readLine()) != null)
            {
                if(s.startsWith(info))
                {
                    if(psw.equals(data.readLine())){
                        return true;
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
        return false;
    }

    /**
     *
     * @return a matrix so that each row is [mainSkill, task nbr, task description,nbr of variables required]
     * EXAMPLE: [Weather,1,How is the weather here,0]
     */
    public List<List<String>> getAllSkillsKind(){
        // this gives you a 2-dimensional array of strings
        List<List<String>> lines = new ArrayList<>();
        Scanner inputStream;

        try{
            inputStream = new Scanner(new File("src/DataBase/tasks.csv"));

            while(inputStream.hasNext()){
                String line= inputStream.nextLine();
                String[] values = line.split(",");
                // this adds the currently parsed line to the 2-dimensional string array
                lines.add(Arrays.asList(values));
            }

            inputStream.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     *
     * @return a matrix so that each row is [answer, question1,question2,...,questionN]
     * EXAMPLE: [Hi,Hello,Hi]
     */
    public List<List<String>> getAllSkills(){
        // this gives you a 2-dimensional array of strings
        List<List<String>> skills = new ArrayList<>();

        return skills;
    }
}
