package CFGrammar;

import com.sun.tools.javadoc.Main;

import java.io.*;
import java.util.Arrays;

public class CFG_GA {

    public static void main(String[] args) throws IOException {
        System.out.println("Launch GA for CFG weights");
        CFG_GA cfg = new CFG_GA();
    }

    ////////////////////////////////////////////////////////////////////////////

    private int pop_size = 60;
    private int weight_size = 4;

    public CFG_GA() throws IOException, FileNotFoundException
    {
        String best_w = "";
        int generation = 1;
        int loops = 0;
        Weights[] pop = initializeWeights();

        while (!intersected(pop) && generation < 40) //Ici limite de génération
        {
            System.out.println("Generation: "+ generation + "\nWeights: ");
            int r;
            for(int i = 0; i < pop.length; i++)
            {
                System.out.print(i + " : ");
                double fit = getFitness(pop[i]);
                pop[i].setFitness(fit);
                r = (""+(i)).length();

                for(int j = 0; j < r + 2; j++)
                {
                    System.out.print("\b");
                }
            }

            HeapSort.sort(pop);

            System.out.println(Arrays.toString(pop[0].getWeights()));
            System.out.println(pop[0].getFitness());

            best_w = best_w + Arrays.toString(pop[0].getWeights()) + "\n";

            pop = crossOver(pop);

            generation++;
            loops++;
        }

        System.out.print("Generation: "+generation+"\nWeights: ");
        int r;
        for(int i = 0; i < pop.length; i++)
        {
            System.out.print(i+" : ");
            double fit = getFitness(pop[i]);
            pop[i].setFitness(fit);
            r = (""+(i)).length();
            for(int y = 0; y < r+2; y++)
            {
                System.out.println("");
                loops++;

            }
        }

        HeapSort.sort(pop);
        System.out.println("Final population: \n\n\n");
        toPrint(pop);
        System.out.println(Arrays.toString(pop[0].getWeights()) + "\nGenerations: "+ generation);
        best_w = best_w + Arrays.toString(pop[0].getWeights()) +"\n";
        saveAll(pop[0].getWeights(),"weights.txt", best_w, "best.txt");
        System.out.println("Total loops : "+ loops);
    }

    public Weights[] initializeWeights()
    {
        //Weights between 0 and 10
        Weights[] population = new Weights[pop_size];
        for(int i = 0; i < pop_size; i++)
        {
            double[] weights = new double[weight_size];
            for(int j = 0; j < weights.length; j++)
            {
                weights[j] = (Math.random()*10);
            }
            population[i] = new Weights(weights);
        }
        return population;
    }

    public Weights[] crossOver(Weights[] pop)
    {
        double[] parent1;
        double[] parent2;
        int[] parents = new int[2];
        int split;
        Weights[] new_pop = new Weights[pop.length];

        for(int i = 0; i < pop.length; i++)
        {
            double[] child = new double[weight_size];
            select(pop, parents);
            parent1 = pop[parents[0]].getWeights();
            parent2 = pop[parents[1]].getWeights();
            split = (int)(Math.random()*weight_size);

            for(int k = 0; k < weight_size; k++)
            {
                if(k > split && k < weight_size/2 + split)
                {
                    child[k] = parent1[k];
                }
                else if(k >= 0 && k < weight_size/2 - split)
                {
                    child[k] = parent1[k];
                }
                else
                {
                    child[k] = parent2[k];
                }
                mutate(child, k);
            }
            new_pop[i] = new Weights(child);
        }
        return new_pop;
    }

    public void select(Weights[] pop, int[] parents)
    {
        for(int j = 0; j < parents.length; j++)
        {
            int n = (int)(Math.random()*100);
            if(n < 50)
            {
                parents[j] = (int)(Math.random()*0.1*pop.length);
            }
            else if(n < 85)
            {
                parents[j] = (int)(Math.random()*0.5*pop.length);
            }
            else
            {
                parents[j] = (int)(Math.random()*pop.length);
            }
        }
    }

    public void mutate(double[] child, int index)
    {
        if(Math.random() < 0.2)
        {
            child[index] = child[index] + (Math.random()*3)-1;
        }
    }

    public double getFitness(Weights weight) throws IOException
    {
        int score = 0;
        int n = 70;

        for(int j = 1; j <= n; j++)
        {
            //Launch App here
            Main_CFG cfg = new Main_CFG(weight.getWeights());
            score = score + cfg.getTotal_score();
        }
        double average = score/(double)n;
        return average;
    }

    public boolean intersected(Weights[] pop)
    {
        for(int i = 1; i < pop.length; i++)
        {
            if(!pop[i-1].getWeights().equals(pop[i].getWeights()))
            {
                return false;
            }
        }
        return true;
    }

    public void toPrint(Weights[] pop)
    {
        for(int i = 0; i < pop.length; i++)
        {
            System.out.println(Arrays.toString(pop[i].getWeights()));
            System.out.println(pop[i].getFitness()+"\n\n");
        }
    }

    public void saveAll(double[] weights, String filename1, String best, String filename2)
    {
        try{
            PrintWriter txt1 = new PrintWriter(filename1, "UTF-8");
            PrintWriter txt2 = new PrintWriter(filename2, "UTF-8");

            for(int i = 0; i < weights.length; i++)
            {
                txt1.print(weights[i]+" , ");
            }
            txt2.print(best);

            txt1.close();
            txt2.close();
        } catch (FileNotFoundException e)
        {
            System.out.println("No file found.");
        } catch (IOException e)
        {
            System.out.println("Could not save the file.");
        }
    }

    public class Weights{
        private double[] weights;
        private double fitness;

        public Weights(double[] pWeights)
        {
            weights = pWeights;
        }

        public void setFitness(double pFitness)
        {
            fitness = pFitness;
        }

        public double[] getWeights()
        {
            return weights;
        }

        public double getFitness()
        {
            return fitness;
        }

        public Weights clone()
        {
            double[] new_weights = new double[weights.length];
            System.arraycopy(weights,0,new_weights,0,weights.length);
            return new Weights(new_weights);
        }
    }
}
