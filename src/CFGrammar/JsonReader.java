package CFGrammar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class JsonReader {
    public ArrayList<String> rules = new ArrayList<>();
    public ArrayList<ArrayList<String>> allRules = new ArrayList<>();

    /**
     * Gets all rules from JSON file and saves them as Strings
     * @return ArrayList filled with String Rules
     */
    public ArrayList<String> getAllRules() {
        rules = new ArrayList<>();
        allRules = new ArrayList<>();
        /*
        Open file
         */
        FileReader reader = null;
        try {
            reader = new FileReader("src\\CFGrammar\\grammar.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONParser parser = new JSONParser();
        JSONObject grammar = null;
        /*
        Parse the JSON file if found
         */
        try {
            grammar = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        /*
        Iterate over all key = Lefthand sides of rules.
        Get Righthand and concat to string rule.
         */

        Iterator<String> keys = grammar.keySet().iterator();

        while (keys.hasNext()) {
            String key = keys.next();
            String lefthand = key;
            if(grammar.get(key) instanceof JSONObject){
                processObject((JSONObject) grammar.get(key), lefthand);
                //System.out.println(key + " is JSONObject");

            }

            else if (grammar.get(key) instanceof JSONArray){
                //System.out.println(key + " is JSONArray");
                processArray((JSONArray) grammar.get(key), lefthand);

            }
        }
        //System.out.println("number of rules: " + rules.size());
        //System.out.println(rules);

        return rules;
    }
    public void processArray(JSONArray array, String lhs){
        for(int i = 0; i< array.size();i++){
            if(array.get(i) instanceof JSONArray){
                JSONArray values = (JSONArray) array.get(i);
                String rhs = "";
                for(int k = 0; k < values.size(); k++)
                {
                    rhs = rhs.concat((String) values.get(k));
                    if(k+1 < values.size()){
                        rhs = rhs.concat(" ");
                    }
                }
                rules.add(lhs+" : "+rhs);
            }
            else if(array.get(i) instanceof JSONObject){
                processObject((JSONObject) array.get(i),lhs);
            }
            else{
                rules.add(lhs+" : "+array.get(i).toString());
            }
        }
    }
    public void processObject(JSONObject object, String lhs){
        Iterator keys = object.keySet().iterator();
        while(keys.hasNext()){
            String key = (String) keys.next();
            if(object.get(key) instanceof JSONArray){
                processArray((JSONArray) object.get(key), key);
            }
            else if(object.get(key) instanceof JSONObject){
                processObject((JSONObject) object.get(key), key);
            }
            else{
                rules.add(key + ":" + object.get(key).toString());
            }
        }

    }

    /**
     * return arraylist as [RHS1,[LHS11,LHS12,...],RHS2,[LHS21,LHS22,...]]
     * @return
     */
    public void splitRules(){
        for (String rule:rules) {
            String[] split = rule.split(" : ");
            String lhs = split[0];
            String[] rhs = split[1].split(" ");
            addRul(lhs,rhs);
        }
    }
    
    public void addRul(String lhs,String[] rhs){
        boolean alreadyContains = false;
        for (ArrayList<String> rule:allRules) {
            if(rule.get(0).equals(lhs)){
                /*
                for (int i = 0; i < rhs.length; i++) {
                    rule.add(rhs[i]);
                }
                 */
                rule.add(Arrays.toString(rhs));
                alreadyContains = true;
                break;
            }
        }
        if(!alreadyContains){
            allRules.add(new ArrayList<>());
            allRules.get(allRules.size()-1).add(lhs);
            /*
            for (int i = 0; i < rhs.length; i++) {
                allRules.get(allRules.size()-1).add(rhs[i]);
            }
             */
            allRules.get(allRules.size()-1).add(Arrays.toString(rhs));
        }
    }


    /**
     * Should be able to add a rule to the json file in the right place
     * @param rule
     */
    public void addRules(String rule, boolean terminal) throws IOException {
         /*
        Open file
         */
        FileReader reader = null;
        try {
            reader = new FileReader("src\\CFGrammar\\grammar.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONParser parser = new JSONParser();
        JSONObject grammar = null;
        /*
        Parse the JSON file if found
         */
        try {
            grammar = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String[] rule_split = rule.split(":");
        String lhs = rule_split[0];
        String righthand = rule_split[1];
        String[] rhs;
        if(righthand.contains(","))
        {
            rhs = righthand.split(",");
        }
        else {
            rhs = new String[]{righthand};
        }
        String[] rElements = rhs[rhs.length-1].split("#");
        String probability = rElements[rElements.length-1];
        if(terminal){
            if(grammar.containsKey("terminals")){
                if(grammar.get("terminals") instanceof JSONObject){
                    JSONObject terminals = (JSONObject) grammar.get("terminals");
                    if(terminals.containsKey(lhs)){
                        if(terminals.get(lhs) instanceof JSONArray){
                            JSONArray values = (JSONArray) terminals.get(lhs);
                            for(int k = 0; k<rhs.length;k++)
                            {
                                values.add(rhs[k]);
                            }
                            values.add(probability);
                            terminals.put(lhs,values);
                        }
                    }
                    else {
                        JSONArray values = new JSONArray();
                        for(int k = 0; k<rhs.length;k++)
                        {
                            values.add(rhs[k]);
                        }
                        values.add(probability);
                        terminals.put(lhs,values);
                    }
                }
            }
        }
        else if (grammar.containsKey(lhs)){
            if(grammar.get(lhs) instanceof JSONArray){
                JSONArray values = (JSONArray) grammar.get(lhs);
                JSONArray value = new JSONArray();
                for(int k = 0; k<rhs.length;k++)
                {
                    value.add(rhs[k]);
                }
                value.add(probability);
                values.add(value);
                grammar.put(lhs,values);
            }
        }
        else{
            JSONArray values = new JSONArray();
            JSONArray value = new JSONArray();
            for(int k = 0; k<rhs.length;k++)
            {
                value.add(rhs[k]);
            }
            value.add(probability);
            values.add(value);
            grammar.put(lhs,values);
        }
        FileWriter writer = new FileWriter("src\\CFGrammar\\grammar.json");
        writer.write(grammar.toJSONString());
        writer.close();
    }

    /**
     * Should remove the specific rule from the json file
     * @param rule
     */
    public void removeRule(String rule, boolean terminal) throws IOException {
        FileReader reader = null;
        try {
            reader = new FileReader("src\\CFGrammar\\grammar.json");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONParser parser = new JSONParser();
        JSONObject grammar = null;
        try {
            grammar = (JSONObject) parser.parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] rule_split = rule.split(":");
        String lhs = rule_split[0];
        String righthand = rule_split[1];
        String[] rhs;
        if(righthand.contains(","))
        {
            rhs = righthand.split(",");
        }
        else {
            rhs = new String[]{righthand};
        }
        if(terminal){
            if(grammar.containsKey("terminals")){
                if(grammar.get("terminals") instanceof JSONObject) {
                    JSONObject terminals = (JSONObject) grammar.get("terminals");
                    if (terminals.containsKey(lhs)) {
                        boolean remove = false;
                        if (terminals.get(lhs) instanceof JSONArray) {
                            JSONArray values = (JSONArray) terminals.get(lhs);
                            for (int k = 0; k < values.size(); k++) {
                                for(int j = 0; j < rhs.length; j++){
                                    if(values.contains(rhs[j])) {
                                        remove = true;
                                    }
                                }
                                if(remove) {
                                    for(int c = 0; c < rhs.length; c++)
                                    {
                                        if(values.size()>1) {
                                            values.remove(rhs[c]);
                                            terminals.put(lhs, values);
                                        }
                                        else{
                                            terminals.remove(lhs);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (grammar.containsKey(lhs)) {
            boolean remove = false;
            if (grammar.get(lhs) instanceof JSONArray) {
                JSONArray values = (JSONArray) grammar.get(lhs);
                for (int k = 0; k < values.size(); k++) {
                    if(values.get(k) instanceof JSONArray){
                        JSONArray singleton = (JSONArray) values.get(k);
                        for(int l = 0; l < rhs.length; l++)
                        {
                            if(singleton.contains(rhs[l])){
                                remove = true;
                            }
                        }
                        if(remove){
                            for(int c = 0; c < rhs.length; c++)
                            {
                                if(values.size()>1) {
                                    values.remove(singleton);
                                    grammar.put(lhs, values);
                                }
                                else{
                                    grammar.remove(lhs);
                                }
                            }
                        }
                    }
                }
            }
        }
        FileWriter writer = new FileWriter("src\\CFGrammar\\grammar.json");
        writer.write(grammar.toJSONString());
        writer.close();
    }

    public void editRule(String rule, boolean terminal,String newRule) throws IOException {
        removeRule(rule,terminal);
        addRules(newRule,terminal);
    }

    /**
     * Change the probability score of a word or production rule.
     * @param rule
     * @param new_prob, the new probability of this rule
     * @param terminal, is the rule terminal or not
     */
    private void modifyProp(String rule , double new_prob, boolean terminal) throws IOException {
        String[] r = rule.split("#");
        r[r.length-1] = Double.toString(new_prob);
        StringBuffer rulemaker = new StringBuffer();
        for(int i = 0; i < r.length; i++){
            rulemaker.append(r[i]);
        }
        String new_rule = rulemaker.toString();
        editRule(rule, terminal, new_rule);
    }

    public ArrayList<String> getRules() {
        return rules;
    }
}
