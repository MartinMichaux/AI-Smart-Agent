package CFGrammar;

import java.util.ArrayList;

public class Branch_Rule {
    private ArrayList<Rule> branch_rules = null;

    public Branch_Rule()
    {
        branch_rules = new ArrayList<>();
    }

    public void addRule(String[] rules)
    {
        Rule rule = new Rule();
        Rule tempRule = new Rule();
        tempRule = rule.create_BranchRule(rules);
        branch_rules.add(tempRule);
    }

    private int kk = 0;

    /**
     * Checks the neighbour of the current branch
     * @param B1
     * @param B2
     * @return A new Branch
     */
    public Branch test(Branch B1, Branch B2)
    {
        //kk++;
        //System.out.println("Test ! "+kk);
        for(int i = 0; i < branch_rules.size(); i++)
        {
            if(branch_rules.get(i).getRHS_Branch().get(0).equals(B1.getWord_category())
                    && branch_rules.get(i).getRHS_Branch().get(1).equals(B2.getWord_category()))
            {
                Branch Br = new Branch();
                Br.setWord_category(branch_rules.get(i).getLHS());
                return Br;
            }
        }
        return null;
    }

    public ArrayList<Rule> getBranch_rules()
    {
        return branch_rules;
    }
}
