package CFGrammar;

import java.util.ArrayList;

public class Branch {
    private ArrayList<Branch> words = null;
    private String word = null;
    private String word_category = null;
    private Branch left_branch = null;
    private Branch right_branch = null;

    /**
     * Every Branch of the solution tree 
     */
    public Branch()
    {
        words = new ArrayList<>();
    }

    public void addRule(Branch Br, Branch LHS, Branch RHS)
    {
        Br.setLeft_branch(LHS);
        Br.setRight_branch(RHS);
        words.add(Br);
    }

    public ArrayList<Branch> getValues()
    {
        return words;
    }

    @Override
    public String toString()
    {
        StringBuffer rule = new StringBuffer();
        for(int i = 0; i < words.size(); i++)
        {

            rule.append(words.get(i).getWord_category()+" : ");
        }
        return rule.toString();
    }


    public void print_endResult()
    {
        for(int i = 0; i < words.size(); i++)
        {
            Branch word = words.get(i);
            word.print_history();
            System.out.println();
        }
    }

    public void print_history()
    {
        System.out.print("("+getWord_category());
        if(left_branch != null)
        {
            left_branch.print_history();
            right_branch.print_history();
        }
        else
        {
            System.out.print(" "+ getWord());
        }
        System.out.print(")");
    }

    public void get_endResult(StringBuffer result)
    {
        for(int i = 0; i < words.size(); i++)
        {
            Branch word = words.get(i);
            word.get_History(result);
            result.append("\n");
        }
    }

    public void get_History(StringBuffer result)
    {
        result.append("("+getWord_category());
        if(left_branch != null)
        {
            left_branch.get_History(result);
            right_branch.get_History(result);
        }
        else
        {
            result.append(" "+ getWord());
        }
        result.append(")");
    }

    public void setWord(String pWord)
    {
        word = pWord;
    }

    public String getWord()
    {
        return word;
    }

    public void setWord_category(String pWord_category)
    {
        word_category = pWord_category;
    }

    public String getWord_category()
    {
        return word_category;
    }

    public Branch getLeft_branch()
    {
        return left_branch;
    }

    public Branch getRight_branch()
    {
        return right_branch;
    }

    public void setLeft_branch(Branch pLeft_branch)
    {
        left_branch = pLeft_branch;
    }

    public void setRight_branch(Branch pRight_branch)
    {
        right_branch = pRight_branch;
    }
}
