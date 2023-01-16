package Interface.Display.SkillEditorDisplayTools;

import CFGrammar.JsonReader;
import FileParser.FileParser;
import Interface.Screens.MainScreen;
import SkillEditor.SkillEditorHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.TextField;
import java.io.*;
import java.util.List;

public class AddSkillEditorVBox extends VBox {
    private MainScreen mainScreen;
    private JsonReader jsonReader;
    private FileParser fileParser;
    private SkillEditorHandler skillEditor;

    private File dataBase = new File("src\\DataBase\\textRecognitionSkills.txt");

    private List<List<String>> allSkills;

    private VBox allQuestions;
    private ScrollPane qScroll;

    private HBox howManyQ;
    private Spinner<Integer> spinner;
    private int oldValue;
    private Label qLabel;
    private Label aLabel;
    private TextField answer;
    private Label skillDisplayLabel;
    private Button enter;
    private ObservableList<String> options1;
    private ComboBox skillDisplay;
    private ObservableList<String> options2;
    private ComboBox tasksDisplay;
    private HBox propositions;

    public AddSkillEditorVBox(MainScreen mainScreen){
        this.mainScreen = mainScreen;
        jsonReader = new JsonReader();
        fileParser = new FileParser();
        skillEditor = new SkillEditorHandler();
        allSkills = fileParser.getAllSkillsKind();

        setSpacing(16);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40,0,0,0));
        createContent();
        getChildren().addAll(howManyQ,qScroll,aLabel,answer,skillDisplayLabel,propositions,enter);
    }

    public void createContent(){

        spinner = new Spinner<Integer>();
        spinner.setMaxWidth(60);
        int initialValue = 0;
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, initialValue);
        spinner.setValueFactory(valueFactory);
        oldValue = initialValue;
        spinner.setOnMouseClicked(event -> {
            int newVal = spinner.getValue();
            if(newVal>oldValue){
                addQuestion();
            }else if(newVal<oldValue){
                allQuestions.getChildren().remove(allQuestions.getChildren().size()-1);
            }
            if(oldValue==4){
                qScroll.setMaxHeight(qScroll.getHeight());
            }
            oldValue = newVal;
        });

        qLabel = new Label("Question:");
        qLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        qLabel.setTextFill(MainScreen.themeColor.darker());
        qLabel.setAlignment(Pos.CENTER);

        howManyQ = new HBox();
        howManyQ.setSpacing(20);
        howManyQ.setAlignment(Pos.CENTER);
        howManyQ.getChildren().addAll(qLabel,spinner);

        allQuestions = new VBox();
        allQuestions.setSpacing(10);
        allQuestions.setAlignment(Pos.CENTER);

        qScroll = new ScrollPane(allQuestions);
        qScroll.setMaxWidth(710);
        qScroll.setBackground(getBackground());
        qScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        aLabel = new Label("Answer:");
        aLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        aLabel.setTextFill(MainScreen.themeColor.darker());
        aLabel.setAlignment(Pos.CENTER);
        aLabel.setPadding(new Insets(35,0,0,0));

        answer = new TextField();
        answer.setPromptText("Either write an answer for a talk/discussion or select a skill to display");
        answer.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        answer.setPrefSize(10,36);
        answer.setMaxWidth(710);

        skillDisplayLabel = new Label("Which skill displays:");
        skillDisplayLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        skillDisplayLabel.setTextFill(MainScreen.themeColor.darker());
        skillDisplayLabel.setAlignment(Pos.CENTER);
        skillDisplayLabel.setPadding(new Insets(35,0,10,0));

        options1 =
                FXCollections.observableArrayList(
                        skillEditor.getMainSkills()
                );
        skillDisplay = new ComboBox(options1);
        skillDisplay.setValue(options1.get(0));
        skillDisplay.setOnAction(event -> {
            options2.setAll(FXCollections.observableArrayList(
                    skillEditor.getTasks((String) skillDisplay.getValue())
            ));
            tasksDisplay.setValue(options2.get(0));
        });

        options2 =
                FXCollections.observableArrayList(
                        skillEditor.getTasks((String) skillDisplay.getValue())
                );
        tasksDisplay = new ComboBox(options2);
        tasksDisplay.setValue(options2.get(0));

        propositions = new HBox();
        propositions.setSpacing(20);
        propositions.setAlignment(Pos.CENTER);
        propositions.getChildren().addAll(skillDisplay,tasksDisplay);

        enter = new Button("Enter");
        enter.setScaleX(2);enter.setScaleY(2);
        enter.setTranslateY(50);
        enter.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(90,true), Insets.EMPTY)));
        enter.setOnAction(e-> {
            for (Node node:allQuestions.getChildren()) {
                TextField question = (TextField) node;
                if (question.getText().isEmpty()||question.getText().isBlank()) {
                    mainScreen.chat.receiveMessage("Question : \"" + question.getText() + "\" is not under the correct form.");
                } else {
                    int result = 0;
                    try {
                        result = handleAddSkill(question.getText());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    String response = "";
                    if (result == 1) {
                        response = "Question : \"" + question.getText() + "\" was successfully added to the database.";
                        question.setText("");
                    } else if(result == -1) {
                        response = "Question : \"" + question.getText() + "\" could not be added to the database";
                    }else if(result==-2){
                        response = "Question : \"" + question.getText() + "\" does not contain the required number of variables";
                    }
                    mainScreen.chat.receiveMessage(response);
                }
            }
            answer.setText("");
            skillDisplay.setValue(options1.get(0));
            tasksDisplay.setValue(options2.get(0));
        });
    }

    private void addQuestion(){
        TextField question = new TextField();
        question.setPromptText("If you wish to include a variable, please replace it by <VARIABLE>");
        question.setPrefSize(qScroll.getWidth()-20,25);
        question.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        allQuestions.getChildren().add(question);
    }

    public int handleAddSkill(String question) throws IOException {
        int success = -1;
        try{
            BufferedWriter newData = new BufferedWriter(new FileWriter(dataBase, true));
            if(answer.getText().isEmpty()||answer.getText().isBlank()){
                String skill = skillToDisplay(question);
                if(!skill.equals("-1")){
                    success = 1;
                    newData.append("U " + question + System.lineSeparator());
                    newData.append("B " + skill + System.lineSeparator());
                }else{
                    success = -2;
                }
            }else{
                success = 1;
                newData.append("U " + question + System.lineSeparator());
                newData.append("B " + answer.getText() + System.lineSeparator());
            }
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

    public String skillToDisplay(String question){
        String displayNbr = "-1";
        for (List<String>row:allSkills) {
            if(row.get(0).equals(skillDisplay.getValue())&&row.get(2).equals(tasksDisplay.getValue())){
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
}

