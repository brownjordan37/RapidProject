package com.rapidproject_1;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.control.Dialog;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BasicView extends View {

    private ScrollPane sc = new ScrollPane();
    private boolean chap = true;
    private boolean sect, ques, viewq = false;

    // VIEW IDS
    private static final int CHAPTER_VIEW = 0;
    private static final int SECTION_VIEW = 1;
    private static final int QUESTION_VIEW = 2;
    private static final int VIEWQ_VIEW = 3;
    private static final int LOGIN_VIEW = 4;
    private static final int REGISTER_VIEW = 5;
    private static final int ACCOUNT_VIEW = 6;

    // LAST VIEW WE VISITED
    private int last = CHAPTER_VIEW;

    private String ch, se, qu = "";

    private Connection connection;
    private ResultSetMetaData resultsMeta;
    private ResultSet results;
    private PreparedStatement insert;
    private Statement query;
    private String hint = "";

    private boolean correct = false;

    public BasicView(String name) {
        super(name);

        loadDrivers();
        connectDB();

        setView(login(), CHAPTER_VIEW);
        sc.setFitToWidth(true);

        setCenter(sc);
    }

    private void loadDrivers() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("DRIVERS LOADED");
        } catch (ClassNotFoundException c) {
            System.out.println("NO DRIVERS");
        }
    }

    private void connectDB() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://ec2-52-90-9-26.compute-1.amazonaws.com/mc", "csci", "1q2w#E$R");
            System.out.println("DATABASE CONNECTED");
            query = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("FAILED TO CONNECT TO DATABASE");
        }
    }

    void setView(VBox v, int lastView) {
        sc.setContent(v);
        last = lastView;
    }

    VBox chapterView() {
        VBox cv = new VBox();
        cv.setMaxWidth(100000);

        try {
            results = query.executeQuery("SELECT * from chapter;");
            resultsMeta = results.getMetaData();
            while (results.next()) {

                String c = results.getString(1);
                Button chapterx = new Button("Chapter " + c);
                chapterx.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");
                chapterx.setMaxWidth(Double.MAX_VALUE);
                chapterx.setAlignment(Pos.CENTER_RIGHT);

                chapterx.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        setView(sectionView(c), CHAPTER_VIEW);
                    }
                });

                cv.getChildren().add(chapterx);
            }

        } catch (SQLException e) {
            System.out.println("ERROR LOADING CHAPTER VIEW");
        }

        return cv;
    }

    VBox sectionView(String chapter) {
        VBox sv = new VBox();
        ch = chapter;
        try {
            results = query.executeQuery("SELECT title from chapter_section where idchapter='" + chapter + "';");
            resultsMeta = results.getMetaData();

            while (results.next()) {

                String s = results.getString(1);
                Button sectionx = new Button(s);
                sectionx.setMaxWidth(Double.MAX_VALUE);
                sectionx.setAlignment(Pos.CENTER_RIGHT);
                sectionx.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

                sectionx.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        setView(questionView(s), SECTION_VIEW);
                    }
                });

                sv.getChildren().add(sectionx);
            }
        } catch (SQLException e) {

        }
        return sv;
    }

    VBox questionView(String section) {
        VBox qv = new VBox();
        se = section;
        try {
            results = query.executeQuery("SELECT qnumber from question where qid = any(select qid from section_question where title=\"" + section + "\");");
            resultsMeta = results.getMetaData();
            System.out.println(resultsMeta.getColumnCount());
            while (results.next()) {
                String q = results.getString(1);
                Button sectionx = new Button(q);
                sectionx.setMaxWidth(Double.MAX_VALUE);
                sectionx.setAlignment(Pos.CENTER_RIGHT);
                sectionx.setStyle("-fx-font-weight: bold; -fx-font-size: 20px");

                sectionx.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        ques = false;
                        viewq = true;
                        try {
                            results = query.executeQuery("SELECT qid from question where qnumber=\"" + q + "\" AND qid = any(select qid from section_question where title=\"" + section + "\");");
                            while (results.next()) {
                                setView(question(results.getString(1)), QUESTION_VIEW);
                            }
                        } catch (SQLException f) {
                        }
                    }
                });

                qv.getChildren().add(sectionx);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return qv;
    }

    VBox accountView() {
        VBox l = new VBox();
        Label ul = new Label("THIS IS THE ACCOUNT VIEW");

        l.getChildren().addAll(ul);
        return l;
    }

    VBox login() {
        VBox l = new VBox();

        TextField user = new TextField();
        user.setMaxHeight(500);
        Label ul = new Label("Username:");
        l.getChildren().addAll(ul, user);

        TextField password = new TextField();
        Label pl = new Label("Password:");
        l.getChildren().addAll(pl, password);

        Button log = new Button("Login");
        log.setMaxWidth(5000);
        log.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    results = query.executeQuery("select * from user where name=\"" + user.getText() + "\" AND pw=\"" + password.getText() + "\"");
                    resultsMeta = results.getMetaData();
                    if (results.next()) {
                        setView(chapterView(), LOGIN_VIEW);
                    }
                    else {
                        Label incorrect = new Label("incorrect user name or password");
                        l.getChildren().add(incorrect);
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(BasicView.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        l.getChildren().add(log);

        Button reg = new Button("Register");
        reg.setMaxWidth(5000);
        reg.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                setView(register(), ACCOUNT_VIEW);
            }
        });
        l.getChildren().add(reg);
        l.setPadding(new Insets(100, 50, 50, 50));
        l.setAlignment(Pos.CENTER);

        return l;
    }

    VBox register() {
        VBox l = new VBox();

        TextField usern = new TextField();
        usern.setMaxHeight(500);
        Label rul = new Label("Username:");
        l.getChildren().addAll(rul, usern);

        TextField rpassword = new TextField();
        Label rpl = new Label("Password:");
        l.getChildren().addAll(rpl, rpassword);

        TextField reppassword = new TextField();
        Label rrpl = new Label("Repeat Password:");
        l.getChildren().addAll(rrpl, reppassword);

        Button reg = new Button("Register");
        reg.setMaxWidth(5000);

        reg.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (rpassword.getText().equals(reppassword.getText())) {
                    try {
                        results = query.executeQuery("select * from user where name=\"" + usern.getText() + "\"");
                        resultsMeta = results.getMetaData();
                        if (!results.next()) {
                            query.executeUpdate("insert into user (name, pw) values ('" + usern.getText() + "', '" + rpassword.getText() + "');");
                            Label success = new Label("User " + usern.getText() + " registered successfully!");
                            l.getChildren().add(success);
                        } else {
                            Label taken = new Label("Username " + usern.getText() + " taken");
                            l.getChildren().add(taken);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(BasicView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    Label error = new Label("Passwords do not match");
                    l.getChildren().add(error);
                }
            }
        });

        l.getChildren().add(reg);
        l.setPadding(new Insets(100, 50, 50, 50));
        l.setAlignment(Pos.CENTER);
        return l;
    }

    VBox question(String qe) {
        hint = "";
        VBox q = new VBox();
        Label title = new Label("?");

        Label prompt = new Label("???");
        prompt.setWrapText(true);

        Label answers = new Label("?????");
        Label feedback = new Label("");

        //Vbox for answers
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);

        title.setStyle("-fx-font-size:25px;");
        BorderPane bp = new BorderPane();
        bp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        BorderPane bp1 = new BorderPane();
        bp.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // HANDLING CORRECT ANSWERS
        ToggleGroup group = new ToggleGroup();

        ArrayList<RadioButton> radioAnswers = new ArrayList<>();
        ArrayList<RadioButton> correctAnswers = new ArrayList<>();

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog dialog = new Dialog();
                Button okButton = new Button("OK");
                okButton.setOnAction(q -> {
                    dialog.hide();
                });
                dialog.getButtons().add(okButton);

                ArrayList<Toggle> selected = new ArrayList<Toggle>();
                for (Toggle t : group.getToggles()) {
                    if (t.isSelected()) {
                        selected.add(t);
                    }
                }
                if (correctAnswers.containsAll(selected)) {
                    dialog.setContent(new Label("You're Correct!"));
                    dialog.showAndWait();

                    int qid = Integer.parseInt(qe);
                    qid++;
                    setView(question("" + qid), QUESTION_VIEW);

                } else {
                    dialog.setContent(new Label("Sorry, You're Wrong"));
                    dialog.showAndWait();
                }
            }
        });

        Button hintBt = new Button("Hint");
        hintBt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                feedback.setText(hint);
            }
        });

        try {
            results = query.executeQuery("select qnumber, qprompt, qhint from question where qid=" + qe + ";");
            while (results.next()) {
                title.setText(results.getString(1));
                prompt.setText(results.getString(2) + "\n");
                hint = results.getString(3);
            }

            results = query.executeQuery("select aprompt, correct from answer where aid IN( select aid from question_answer where qid=" + qe + ");");

            while (results.next()) {
                RadioButton b = new RadioButton(results.getString(1));
                radioAnswers.add(b);
                if (results.getString(2).contains("true")) {
                    System.out.println(results.getString(2));
                    correctAnswers.add(b);
                    System.out.println(correctAnswers);
                }
                vbox.getChildren().add(b);
                vbox.setAlignment(Pos.TOP_LEFT);
            }

            for (RadioButton b : radioAnswers) {
                b.setToggleGroup(group);
            }

        } catch (SQLException e) {

        }

        HBox tbox = new HBox();
        tbox.getChildren().addAll(title);
        tbox.setAlignment(Pos.CENTER);
        tbox.setPadding(new Insets(10, 50, 50, 50));

        HBox cbox = new HBox();
        cbox.getChildren().addAll(prompt);
        cbox.setAlignment(Pos.TOP_RIGHT);
        cbox.setPadding(new Insets(10, 50, 50, 50));

        HBox bbox = new HBox();
        bbox.getChildren().addAll(submitButton);
        if (!hint.equals("")) {
            bbox.getChildren().addAll(hintBt);
        }
        bbox.setAlignment(Pos.CENTER);
        bbox.setPadding(new Insets(10, 50, 50, 50));

        HBox feedbackBox = new HBox();
        feedbackBox.getChildren().addAll(feedback);
        feedback.setWrapText(true);
        feedbackBox.setAlignment(Pos.CENTER);
        feedbackBox.setPadding(new Insets(10, 50, 50, 50));

        VBox botContainer = new VBox();
        botContainer.getChildren().addAll(bbox, feedbackBox);
        botContainer.setPadding(new Insets(10, 50, 50, 50));

        bp.setTop(tbox);
        bp.setCenter(cbox);
        bp1.setCenter(vbox);
        bp1.setBottom(botContainer);

        bp.setBottom(bp1);
        q.getChildren().add(bp);
        return q;
    }

    @Override
    protected void updateAppBar(AppBar appBar) {

        /*ProgressBar p2 = new ProgressBar();
         p2.setProgress(0.25F);
         p2.setStyle("-fx-color: Green;");
         p2.setPrefHeight(25);
        
         appBar.getActionItems().add(p2);*/
        appBar.setNavIcon(MaterialDesignIcon.ARROW_BACK.button(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {

                switch (last) {
                    case CHAPTER_VIEW:
                        setView(chapterView(), CHAPTER_VIEW);
                        break;
                    case SECTION_VIEW:
                        setView(sectionView(ch), CHAPTER_VIEW);
                        break;
                    case QUESTION_VIEW:
                        setView(questionView(se), SECTION_VIEW);
                        break;
                    case VIEWQ_VIEW:
                        break;
                    case ACCOUNT_VIEW:
                        break;
                }
            }
        }));
        //appBar.setTitleText("Java Questions");
        appBar.getActionItems().add(MaterialDesignIcon.PAGES.button(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                setView(chapterView(), CHAPTER_VIEW);
            }
        }));
        appBar.getActionItems().add(MaterialDesignIcon.ACCOUNT_BOX.button(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                setView(accountView(), last + 1);
            }
        }));
    }
}
