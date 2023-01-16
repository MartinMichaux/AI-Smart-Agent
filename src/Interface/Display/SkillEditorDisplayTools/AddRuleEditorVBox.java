package Interface.Display.SkillEditorDisplayTools;

import CFGrammar.JsonReader;
import Interface.Screens.MainScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AddRuleEditorVBox extends VBox {
    private MainScreen mainScreen;
    private JsonReader jsonReader;

    private VBox allQuestions;
    private ScrollPane qScroll;

    private HBox howManyQ;
    private Spinner<Integer> spinner;
    private int oldValue;
    private Label titleLabel;
    private TextField lhs;
    private CheckBox isTerminal;
    private Button enter;


    public AddRuleEditorVBox(MainScreen mainScreen){
        this.mainScreen = mainScreen;
        jsonReader=new JsonReader();
        setSpacing(16);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40,0,0,0));
        createContent();
        getChildren().addAll(titleLabel,lhs,howManyQ,qScroll,isTerminal,enter);
    }

    public void createContent(){
        allQuestions = new VBox();
        allQuestions.setSpacing(10);
        allQuestions.setAlignment(Pos.CENTER);

        qScroll = new ScrollPane(allQuestions);
        qScroll.setMaxWidth(710);
        qScroll.setBackground(getBackground());
        qScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        titleLabel = new Label("Title (LHS):");
        titleLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        titleLabel.setTextFill(MainScreen.themeColor.darker());
        titleLabel.setAlignment(Pos.CENTER);

        lhs = new TextField();
        lhs.setPromptText("Write the left hand side of the rule");
        lhs.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        lhs.setPrefSize(10,36);
        lhs.setMaxWidth(710);

        spinner = new Spinner<Integer>();
        spinner.setMaxWidth(60);
        int initialValue = 0;
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, initialValue);
        spinner.setValueFactory(valueFactory);
        oldValue = initialValue;
        spinner.setOnMouseClicked(event -> {
            int newVal = spinner.getValue();
            if(newVal>oldValue){
                addRule();
            }else if(newVal<oldValue){
                allQuestions.getChildren().remove(allQuestions.getChildren().size()-1);
            }
            if(oldValue==4){
                qScroll.setMaxHeight(qScroll.getHeight());
            }
            oldValue = newVal;
        });

        Label ruleLabel = new Label("Rule (RHS):");
        ruleLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        ruleLabel.setTextFill(MainScreen.themeColor.darker());
        ruleLabel.setAlignment(Pos.CENTER);

        howManyQ = new HBox();
        howManyQ.setSpacing(20);
        howManyQ.setAlignment(Pos.CENTER);
        howManyQ.getChildren().addAll(ruleLabel,spinner);

        isTerminal = new CheckBox("Is terminal");
        isTerminal.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        isTerminal.setTextFill(MainScreen.themeColor.darker());
        isTerminal.setAlignment(Pos.CENTER);

        enter = new Button("Add");
        enter.setScaleX(2);enter.setScaleY(2);
        enter.setTranslateY(50);
        enter.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(90,true), Insets.EMPTY)));
        enter.setOnAction(e-> {
            try {
                String rhs = "";
                for (int i=0;i<allQuestions.getChildren().size()-1;i++) {
                    TextField question = (TextField) allQuestions.getChildren().get(i);
                    rhs += question.getText()+",";
                }
                TextField question = (TextField) allQuestions.getChildren().get(allQuestions.getChildren().size()-1);
                rhs+=question.getText();
                mainScreen.chat.receiveMessage(lhs.getText()+":"+rhs + isTerminal.isSelected());
                jsonReader.addRules(lhs.getText()+":"+rhs,isTerminal.isSelected());
            } catch (Exception exception) {
                mainScreen.chat.receiveMessage("The rule could not be added for some reason, please recheck its format");
            }
            lhs.setText("");
            isTerminal.setSelected(false);
            mainScreen.chat.receiveMessage("The rule has been added in the database");
        });
    }

    private void addRule(){
        TextField rule = new TextField();
        rule.setPromptText("If you wish to include a variable, please replace it by <VARIABLE>");
        rule.setPrefSize(qScroll.getWidth()-20,25);
        rule.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
        allQuestions.getChildren().add(rule);
    }
}

