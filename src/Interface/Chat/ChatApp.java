package Interface.Chat;

import Agents.Assistant;
import CFGrammar.Main_CFG;
import FileParser.FileParser;
import Interface.Screens.MainScreen;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ChatApp extends VBox {
    public ObservableList messages = FXCollections.observableArrayList();
    private HBox user;
    private Label userNameLabel;
    private Paint assistantMessageTextColor;
    private ScrollPane scroller;
    private HBox typeField;
    private TextField userInput;
    private Button sendMessageButton;
    private Image image;
    private ImageView userIcon;
    public Color themeColor = MainScreen.themeColor;
    public List<String>userMessages;
    public List<String>assistantMessages;
    public Assistant assistant_answer;

    private MainScreen mainScreen;
    private FileParser fileParser;

    public void changeColor(Color themeColor) throws FileNotFoundException {
        this.themeColor = themeColor;
        super.setBackground(new Background(new BackgroundFill(themeColor, CornerRadii.EMPTY, Insets.EMPTY)));
        List<MessageBubble> messageBubbles = (List<MessageBubble>) messages;
        for (MessageBubble mb : messageBubbles) {
            if (mb.getDirection() == 0) {
                mb.messageLabel.setBackground(new Background(new BackgroundFill(themeColor, new CornerRadii(0, 7, 7, 7, false), Insets.EMPTY)));
                if (themeColor.equals(Color.LIGHTGRAY)) { assistantMessageTextColor = Color.BLACK; }
                else { assistantMessageTextColor = Color.WHITE; }
                mb.messageLabel.setTextFill(assistantMessageTextColor);
            }
        }
        String userProfilePicture = fileParser.getUsersPicture("profilePicture");
        if (themeColor.equals(Color.BLACK)) {
            if(userProfilePicture!=null){
                changeUserIcon(new FileInputStream(userProfilePicture));
            }else{
                changeUserIcon(new FileInputStream("src/res/userIconBlack.png"));
            }
            userNameLabel.setStyle("-fx-text-fill: white");
            userInput.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(3,3,3,3,false), Insets.EMPTY)));
            userInput.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray");
        }
        else if (themeColor.equals(new Color(0.2, 0.35379, 0.65, 1))) {
            if(userProfilePicture!=null){
                changeUserIcon(new FileInputStream(userProfilePicture));
            }else{
                changeUserIcon(new FileInputStream("src/res/userIconBlue.png"));
            }
            userNameLabel.setStyle("-fx-text-fill: white");
            userInput.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(3,3,3,3,false), Insets.EMPTY)));
            userInput.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray");
        }
        else if (themeColor.equals(Color.LIGHTGRAY)) {
            if(userProfilePicture!=null){
                changeUserIcon(new FileInputStream(userProfilePicture));
            }else{
                changeUserIcon(new FileInputStream("src/res/userIconWhite.png"));
            }
            userNameLabel.setStyle("-fx-text-fill: black");
            userInput.setBackground(new Background(new BackgroundFill(Color.LIGHTSLATEGRAY, new CornerRadii(3,3,3,3,false), Insets.EMPTY)));
            userInput.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: white");
        }
    }

    private class MessageBubble extends HBox {
        private Background userBubbleBackground;
        private Background assistantBubbleBackground;
        private int direction;
        private Label messageLabel;

        public MessageBubble(String message, int direction) {
            this.direction = direction;
            if (direction == 0) {
                assistantMessages.add(message);
            } else {
                userMessages.add(message);
            }
            userBubbleBackground = new Background(new BackgroundFill(Color.GRAY.darker(), new CornerRadii(7, 0, 7, 7, false), Insets.EMPTY));
            assistantBubbleBackground = new Background(new BackgroundFill(themeColor, new CornerRadii(0, 7, 7, 7, false), Insets.EMPTY));
            createLabel(message, direction);
        }

        private void createLabel(String message, int direction) {
            messageLabel = new Label(message);
            messageLabel.setPadding(new Insets(6));
            messageLabel.setWrapText(true);
            messageLabel.setFont((Font.font("Cambria", 17)));
            messageLabel.maxWidthProperty().bind(widthProperty().multiply(0.75));
            messageLabel.setTranslateY(5);

            if (direction == 0) {
                messageLabel.setTextFill(assistantMessageTextColor);
                messageLabel.setBackground(assistantBubbleBackground);
                messageLabel.setAlignment(Pos.CENTER_LEFT);
                messageLabel.setTranslateX(10);
                setAlignment(Pos.TOP_LEFT);
            } else {
                messageLabel.setTextFill(Color.WHITE);
                messageLabel.setBackground(userBubbleBackground);
                messageLabel.setAlignment(Pos.CENTER_RIGHT);
                messageLabel.setTranslateX(-10);
                setAlignment(Pos.TOP_RIGHT);
            }
            getChildren().setAll(messageLabel);
        }

        public int getDirection() { return direction; }
    }

    public ChatApp(String userName, MainScreen mainScreen) throws Exception {
        super(7);
        super.setBackground(new Background(new BackgroundFill(themeColor, CornerRadii.EMPTY, Insets.EMPTY)));

        userMessages = new ArrayList<>();
        assistantMessages = new ArrayList<>();
        this.mainScreen = mainScreen;
        fileParser = new FileParser();

        assistant_answer = new Assistant(this.mainScreen, userName, assistantMessages);

        userNameLabel = new Label(userName);
        userNameLabel.setAlignment(Pos.CENTER);
        userNameLabel.setTranslateX(10);
        userNameLabel.setFont((Font.font("Cambria", FontWeight.EXTRA_BOLD, 20)));
        userNameLabel.setStyle("-fx-text-fill: white");

        FileInputStream fis = new FileInputStream("src/res/userIconBlue.png");
        String userProfilePicture = fileParser.getUsersPicture("profilePicture");
        if(userProfilePicture!=null){
            fis = new FileInputStream(userProfilePicture);
        }
        else if (mainScreen.themeColor.equals(Color.LIGHTGRAY)) {
            fis = new FileInputStream("src/res/userIconWhite.png");
        }
        else if (mainScreen.themeColor.equals(Color.BLACK)) {
            fis = new FileInputStream("src/res/userIconBlack.png");
        }
        image = new Image(fis,25,25,true,true);
        userIcon = new ImageView(image);

        user = new HBox(20);
        user.getChildren().addAll(userIcon, userNameLabel);

        if (themeColor.equals(Color.LIGHTGRAY)) { assistantMessageTextColor = Color.BLACK; }
        else { assistantMessageTextColor = Color.WHITE; }

        createComponents();
        getChildren().setAll(user, scroller, typeField);
        setPadding(new Insets(40));
        setMaxHeight(Double.MAX_VALUE);
        setMinHeight(Double.MIN_VALUE);
        changeColor(mainScreen.themeColor);
    }

    private void changeUserIcon(FileInputStream fis) {
        image = new Image(fis,25,25,true,true);
        userIcon = new ImageView(image);
        getChildren().remove(user);
        user.getChildren().clear();
        user.getChildren().addAll(userIcon, userNameLabel);
        getChildren().add(0, user);
    }

    private void createComponents() {
        createMessageView();
        createInputView();
    }

    private void createMessageView() {
        VBox messagesBox = new VBox(6);
        messagesBox.setPadding(new Insets(20,0,20,0));
        Bindings.bindContentBidirectional(messages, messagesBox.getChildren());

        scroller = new ScrollPane(messagesBox);
        scroller.setPrefHeight(750);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background: transparent; -fx-background-color: lightslategray;");
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.vvalueProperty().bind(messagesBox.heightProperty());   //updating scroller
    }

    private void createInputView() {
        typeField = new HBox(7);

        userInput = new TextField();
        userInput.setPromptText("Type message");
        userInput.setPrefWidth(380);
        userInput.setTranslateY(12);
        userInput.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(3,3,3,3,false), Insets.EMPTY)));
        userInput.setFont((Font.font("Cambria", 14)));
        userInput.setStyle("-fx-text-fill: black; -fx-prompt-text-fill: gray");
        userInput.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER){
                try {
                    sendMessage(userInput.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                userInput.setText("");
            }
        });

        sendMessageButton = new Button(">>");
        sendMessageButton.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 22));
        sendMessageButton.setTextFill(Color.LIGHTSEAGREEN.brighter());
        sendMessageButton.setBackground(null);
        sendMessageButton.setBorder(null);
        sendMessageButton.setCursor(Cursor.HAND);
        sendMessageButton.setTranslateY(5);

        sendMessageButton.disableProperty().bind(userInput.lengthProperty().isEqualTo(0));
        sendMessageButton.setOnAction(event-> {
            try {
                sendMessage(userInput.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
            userInput.setText("");
        });
        typeField.getChildren().setAll(userInput, sendMessageButton);
    }

    private boolean CFG_on = true;
    public void sendMessage(String message) throws Exception {
        messages.add(new MessageBubble(message, 1));
        assistant_answer.setAssistantMessage(assistantMessages);
        //receiveMessage(assistant_answer.getResponseWithRandom(message));

        if(!CFG_on)
        {
            if(message.contains("CFG on"))
            {
                CFG_on = true;
                String answer = "Context-free grammar parser turned on"+"\r\n"+
                        "To add a production rule to the grammar write: ADD/LHS:RHS1,RHS2"+"\r\n"+
                        "To add a terminal to the grammar write: ADD/LHS:Word"+"\r\n"+
                        "To remove same with REMOVE/...";
                receiveMessage(answer);
            }
            else
            {
                System.out.println("Deng mamm!");
                receiveMessage(assistant_answer.textRecognition.getResponse(message));
            }
        }
        else
        {
            if(message.contains("CFG off"))
            {
                CFG_on = false;
                receiveMessage("Context-free grammar parser turned on");
            }
            else if(message.contains("ADD/") || message.contains("REMOVE/"))
            {
                Main_CFG cfg = new Main_CFG(message);
                String answer = cfg.addOrRemoveRule(message);
                receiveMessage(answer);
            }
            else
            {
                Main_CFG cfg = new Main_CFG(message, assistant_answer);

                int skill_nbr = cfg.getSkillNbr();
                if(skill_nbr == 0)
                {
                    //Pas de Skill
                    receiveMessage(assistant_answer.textRecognition.getResponse(message));
                }
                else
                {
                    String skillnbr = String.valueOf(skill_nbr);
                    ArrayList<String> words_for_variables = cfg.getVariable_words();
                    receiveMessage(cfg.getSkill(skillnbr,words_for_variables));
                }
            }
        }
    }

    public void receiveMessage(String message) {    //adds assistant's response
        MessageBubble messageBubble = new MessageBubble(message, 0);
        messages.add(messageBubble);
    }

    public List getAssistantMessage()
    {
        return assistantMessages;
    }
}