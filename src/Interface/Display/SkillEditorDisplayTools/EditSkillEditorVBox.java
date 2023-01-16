package Interface.Display.SkillEditorDisplayTools;

import Interface.Screens.MainScreen;
import SkillEditor.SkillEditorHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

public class EditSkillEditorVBox extends VBox {
    private MainScreen mainScreen;
    private SkillEditorHandler skillEditor;

    private Label skillDisplayLabel;
    private ObservableList<String> options1;
    private ComboBox skills;
    private ObservableList<String> options2;
    private ComboBox tasks;
    private ObservableList<String> options3;
    private ComboBox sentences;
    private HBox options;
    private Label editLabel;
    private TextField editTextField;
    private Button edit;
    private Button delete;
    private HBox buttons;

    public EditSkillEditorVBox(MainScreen mainScreen) throws IOException {
        this.mainScreen = mainScreen;
        skillEditor = new SkillEditorHandler();

        setSpacing(25);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40,0,0,0));
        createContent();
        getChildren().addAll(skillDisplayLabel, options, editLabel, editTextField, buttons);
    }

    public void createContent() throws IOException {
        skillDisplayLabel = new Label("Select Skill-Task-Sentence to edit/delete:");
        skillDisplayLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        skillDisplayLabel.setTextFill(MainScreen.themeColor.darker());
        skillDisplayLabel.setAlignment(Pos.CENTER);
        skillDisplayLabel.setPadding(new Insets(35, 0, 15, 0));

        handleComboBoxes(false);

        editLabel = new Label("Edit:");
        editLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 30));
        editLabel.setTextFill(MainScreen.themeColor.darker());
        editLabel.setAlignment(Pos.CENTER);

        editTextField = new TextField();
        editTextField.setText(sentences.getValue().toString());
        sentences.setOnAction(e -> editTextField.setText(sentences.getValue().toString()));
        editTextField.setMinSize(780, 36);
        editTextField.setMaxSize(780, 36);
        editTextField.setFont(Font.font("Verdana", FontWeight.BOLD, 15));

        edit = new Button("Edit");
        edit.setPrefSize(100, 60);
        edit.setMinSize(100, 60);
        edit.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        edit.setCursor(Cursor.HAND);
        edit.setTextFill(Color.LIGHTGRAY);
        edit.setBackground(new Background(new BackgroundFill(Color.GREEN.darker(), new CornerRadii(90, true), Insets.EMPTY)));
        edit.setOnAction(e -> {
            String sentenceToEdit = sentences.getValue().toString();
            String newSentence = editTextField.getText();
            String response = "Sentence : \"" + sentenceToEdit + "\" could not be edited.";

            if (!editTextField.getText().equals(sentenceToEdit)) {
                try {
                    skillEditor.editSentence(sentenceToEdit,newSentence);
                    response = "Sentence : \"" + sentenceToEdit + "\" was successfully edited to \"" + newSentence + "\".";
                    handleComboBoxes(true);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }else {
                response = "You have not changed the sentence.";
            }
            mainScreen.chat.receiveMessage(response);
        });

        Label or = new Label("or");
        or.setFont(Font.font("Tahoma", FontWeight.BOLD, 23));
        or.setTextFill(MainScreen.themeColor.darker());

        delete = new Button("Delete");
        delete.setPrefSize(100, 60);
        delete.setMinSize(100, 60);
        delete.setFont(Font.font("Tahoma", FontWeight.BOLD, 20));
        delete.setCursor(Cursor.HAND);
        delete.setTextFill(Color.LIGHTGRAY);
        delete.setBackground(new Background(new BackgroundFill(Color.RED.darker(), new CornerRadii(90, true), Insets.EMPTY)));
        delete.setOnAction(e -> {
            String sentenceToDelete = sentences.getValue().toString();
            String response = "Sentence : \"" + sentenceToDelete + "\" could not be deleted from the database.";

            if (editTextField.getText().equals(sentenceToDelete)) {
                if (sentences.getItems().size() == 1) {
                    response = "Sentence : \"" + sentenceToDelete + "\" could not be deleted since it is the only sentence defined in the database for this task.";
                }
                else {
                    try {
                        skillEditor.deleteSentenceFromFile(sentenceToDelete);
                        response = "Sentence : \"" + sentenceToDelete + "\" was successfully deleted from the database.";
                        handleComboBoxes(true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }else {
                response = "Are you sure you want to delete this sentence? It seems like you started editing it. Please try again.";
            }
            mainScreen.chat.receiveMessage(response);
        });

        buttons = new HBox(50);
        buttons.setTranslateY(40);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(edit, or, delete);
    }

    public void handleComboBoxes(Boolean isUpdate) throws IOException {
        options1 = FXCollections.observableArrayList(skillEditor.getMainSkills());
        skills = new ComboBox(options1);
        skills.setValue(options1.get(0));
        skills.setOnAction(event -> {
            options2.setAll(FXCollections.observableArrayList(skillEditor.getTasks((String) skills.getValue())));
            tasks.setValue(options2.get(0));
        });

        options2 = FXCollections.observableArrayList(skillEditor.getTasks((String) skills.getValue()));
        tasks = new ComboBox(options2);
        tasks.setValue(options2.get(0));
        tasks.setOnAction(event -> {
            try {
                options3.setAll(FXCollections.observableArrayList(skillEditor.getSentences((String) tasks.getValue())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            sentences.setValue(options3.get(0));
        });

        options3 = FXCollections.observableArrayList(skillEditor.getSentences((String) tasks.getValue()));
        sentences = new ComboBox(options3);
        sentences.setValue(options3.get(0));

        options = new HBox();
        options.setSpacing(20);
        options.setAlignment(Pos.CENTER);
        options.getChildren().addAll(skills, tasks, sentences);
        options.setPadding(new Insets(0, 0, 28, 0));

        if (isUpdate) {
            editTextField.setText(sentences.getValue().toString());
            sentences.setOnAction(e -> editTextField.setText(sentences.getValue().toString()));
            getChildren().remove(1);
            getChildren().add(1, options);
        }
    }

}