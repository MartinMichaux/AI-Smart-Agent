package TextRecognition;

import java.util.ArrayList;

public class Node {
    private String sentence;//with deleted word
    private ArrayList<Node> children;
    private ArrayList<String> wordsRemoved;

    public Node(String sentence){
        this.sentence = sentence;
        this.children = new ArrayList<>();
        this.wordsRemoved = new ArrayList<>();
    }

    public String getSentence() {
        return sentence;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public ArrayList<String> getWordsRemoved() {
        return wordsRemoved;
    }
}
