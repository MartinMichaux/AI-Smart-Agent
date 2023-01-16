package CFGrammar;

import java.util.*;

public class Rule
{
    private ArrayList<String> RHS_Branch = null;
    private String RHS_Word = null;
    private String LHS = null;
    private double word_prob = 0;

    /**
     * Can be both, Branch_Rule and Word_Rule.
     * Careful with the method called.
     */
    public Rule()
    {
        RHS_Branch = new ArrayList<>();
    }

    public Rule create_WordRule(String[] rules)
    {
        Rule rule = new Rule();
        rule.LHS = rules[0];
        rule.RHS_Word = rules[2];
        rule.word_prob = Double.parseDouble(rules[4]);
        return rule;
    }

    public Rule create_BranchRule(String[] rules)
    {
        Rule rule = new Rule();
        rule.LHS = rules[0];

        for(int i = 2; i < rules.length; i++)
        {
            rule.RHS_Branch.add(rules[i]);
        }
        return rule;
    }

    public Rule create_SubTree(String LHS, String RHS_1, String RHS_2)
    {
        RHS_Branch = new ArrayList<>();
        Rule rule = new Rule();
        rule.LHS = LHS;
        rule.RHS_Branch.add(RHS_1);
        rule.RHS_Branch.add(RHS_2);
        return rule;
    }

    public String print_WordRule()
    {
        StringBuffer rule = new StringBuffer();
        rule.append(LHS);
        rule.append(" : ");
        rule.append(RHS_Word);
        return rule.toString();
    }

    public String print_BranchRule()
    {
        StringBuffer rule = new StringBuffer();
        rule.append(LHS);
        rule.append(" : ");
        for(int i = 0; i < RHS_Branch.size(); i++)
        {
            rule.append(RHS_Branch.get(i)+" - ");
        }
        return rule.toString();
    }

    public String getLHS()
    {
        return LHS;
    }

    public ArrayList<String> getRHS_Branch()
    {
        return RHS_Branch;
    }

    public String getRHS_Word()
    {
        return RHS_Word;
    }

    public void setRHS_Branch(ArrayList<String> rHS_Branch)
    {
        RHS_Branch = rHS_Branch;
    }

    public void setRHS_Word(String rHS_Word)
    {
        RHS_Word = rHS_Word;
    }

    public boolean getMultiple()
    {
        return true;
    }

    public String getL_side()
    {
        return null;
    }

    public ArrayList<String> getR_side()
    {
        return null;
    }

    public ArrayList<String> getR_side_2()
    {
        return null;
    }

    public int getScore()
    {
        return 1;
    }

    public double getWord_prob()
    {
        return word_prob;
    }

    public void setWord_prob(double word_prob)
    {
        this.word_prob = word_prob;
    }
}
