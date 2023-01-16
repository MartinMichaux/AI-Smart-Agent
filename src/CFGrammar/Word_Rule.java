package CFGrammar;

import java.util.ArrayList;
import java.util.Arrays;

public class Word_Rule {
    private ArrayList<Rule> word_Rules = null;
    private ArrayList<String> result_array = null;
    private ArrayList<Double> prob_word = null;

    public Word_Rule()
    {
        word_Rules = new ArrayList<>();
        result_array = new ArrayList<>();
        prob_word = new ArrayList<>();
    }

    public void addRule(String[] rules)
    {
        Rule rule = new Rule();
        Rule tempRule = new Rule();
        tempRule = rule.create_WordRule(rules);
        word_Rules.add(tempRule);
    }

    public ArrayList<Branch> interpret(String word)
    {
        ArrayList<Branch> subTree = new ArrayList<>();
        boolean foreign_word = true;
        for(int i = 0; i < word_Rules.size(); i++)
        {
            int threshold = (int)(word.length()*0.2);
            if(word_Rules.get(i).getRHS_Word().equals(word))
            {
                String res = (word_Rules.get(i).getLHS() +":"+word_Rules.get(i).getRHS_Word());
                result_array.add(res);

                Branch word_Branch = new Branch();
                word_Branch.setWord_category(word_Rules.get(i).getLHS());
                word_Branch.setWord(word_Rules.get(i).getRHS_Word());
                subTree.add(word_Branch);
                foreign_word = false;
                //System.out.println("Added word : "+ word);
            }
        }
        if(foreign_word)
        {
            System.out.println("Didn't found in corpus: "+word);

            String res = ("FW:"+word);
            result_array.add(res);

            Branch foreign_word_Branch = new Branch();
            foreign_word_Branch.setWord_category("FW");
            foreign_word_Branch.setWord(word);
            subTree.add(foreign_word_Branch);
        }
        return subTree;
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

    public ArrayList<Rule> getWord_Rules()
    {
        return word_Rules;
    }

    public ArrayList<String> getResult_array()
    {
        return result_array;
    }
}
