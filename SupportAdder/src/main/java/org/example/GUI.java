package org.example;

import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class GUI extends Application {

    private TextArea textOutput;
    private Button buttonSubmit;
    private ComboBox<String> dropdownMenu;
    private ComboBox<String> operationMenu;
    private SupportAdder supportAdder;
    private Button chkButton;


    @Override
    public void start(Stage primaryStage) {
        supportAdder = new SupportAdder();
        primaryStage.setTitle("Приложение для добавления опор в UserSide");
        primaryStage.setWidth(800);
        primaryStage.setHeight(400);

        // Установка иконки
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png")));
        primaryStage.getIcons().add(icon);

        //значок загрузки
        Image loadingIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/loadingIcon.png")));


        // Создание основного контейнера
        BorderPane root = new BorderPane();

        // Создание левой панели
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        root.setLeft(leftPanel);

        // Кнопка "Помощь"
        Button helpButton = new Button("Помощь");
        helpButton.setOnAction(e -> onHelp());
        leftPanel.getChildren().add(helpButton);

        // Выпадающее меню
        Label menuLabel = new Label("Выберите тип объектов:");
        leftPanel.getChildren().add(menuLabel);

        dropdownMenu = new ComboBox<>();
        dropdownMenu.getItems().addAll("Опора", "Опора ТМПК", "Опора Россети", "Опора Мособлэнерго", "Колодец", "Трубостойка ТМПК");
        dropdownMenu.setValue("Опора");
        leftPanel.getChildren().add(dropdownMenu);

        //Кнопка "Проверить существование опор
        chkButton = new Button("Проверить сущ-ние объектов");
        chkButton.setOnAction(e -> chkSupports());
        chkButton.setDisable(true);
        leftPanel.getChildren().add(chkButton);

        // Кнопка "Добавить опоры в UserSide"
        buttonSubmit = new Button("Выполнить");
        buttonSubmit.setOnAction(e -> onSubmit());
        buttonSubmit.setDisable(true);
        leftPanel.getChildren().add(buttonSubmit);

        /*
        // Кнопка "Переименовать опоры в UserSide"
        renameButton = new Button("Переименовать объекты в UserSide");
        renameButton.setOnAction(e -> onRename());
        renameButton.setDisable(true);
        leftPanel.getChildren().add(renameButton);
         */
        //Выбор операции
        Label operLabel = new Label("Выберите операцию:");
        leftPanel.getChildren().add(operLabel);

        operationMenu = new ComboBox<>();
        operationMenu.getItems().addAll("Добавить объекты", "Переименовать объекты");
        operationMenu.setValue("Добавить объекты");
        leftPanel.getChildren().add(operationMenu);

        // Кнопка "Открыть файл"
        Button browseButton = new Button("Открыть файл");
        browseButton.setOnAction(e -> onBrowse());
        leftPanel.getChildren().add(browseButton);


        // Кнопка "Очистить текстовое поле"
        Button clearButton = new Button("Очистить текстовое поле");
        clearButton.setOnAction(e -> onClear());
        leftPanel.getChildren().add(clearButton);

        // Создание правой панели
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        root.setCenter(rightPanel);

        // Текстовое поле для вывода сообщений
        textOutput = new TextArea();
        textOutput.setEditable(false);
        textOutput.setText("Координаты должны быть по шаблону 56 37");
        rightPanel.getChildren().add(textOutput);

        // Создание сцены и установка ее на сцену
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void onSubmit() {
        try {
            String text = submit();
            textOutput.setText(text);
        } catch (Exception e) {
            textOutput.setText("Exception: " + e.getMessage());
        }
    }

    private void onBrowse() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Excel files", "*.xls"),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                String path = selectedFile.getParent() + File.separator;
                String filename = selectedFile.getName();
                supportAdder.loadXls(path, filename);
                textOutput.setText("Выбранный файл: " + filename + "\n");
                buttonSubmit.setDisable(false);
                chkButton.setDisable(false);
            }
        } catch (Exception e) {
            textOutput.setText("Exception: " + e.getMessage());
        }
    }

    private void onClear() {
        textOutput.clear();
    }

    private void chkSupports() {
        String selectedItem = operationMenu.getValue();
        switch (selectedItem) {
            case "Добавить объекты" -> supportAdder.loadAddingPoints();
            case "Переименовать объекты" -> supportAdder.loadRenamingPoints();
        }
        StringBuilder messages = new StringBuilder();
        try {
            HashMap<String, Boolean> hm;
            hm = supportAdder.chkSupportsExistence();
            Map.Entry<String, Boolean> elem;
            Iterator<Map.Entry<String, Boolean>> iterator = hm.entrySet().iterator();
            while (iterator.hasNext()) {
                elem = iterator.next();
                if (elem.getValue()) {
                    messages.append("Объект ").append(elem.getKey()).append(" cуществует.\n");
                } else {
                    messages.append("Объекта ").append(elem.getKey()).append(" нет.\n");
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        textOutput.setText(messages.toString());
    }

    private void onHelp() {
        URL helpFileUrl = getClass().getResource("/SupportAdderHelp.pdf");
        if (helpFileUrl != null) {
            File helpFile = new File(helpFileUrl.getPath());
            try {
                java.awt.Desktop.getDesktop().open(helpFile);
            } catch (Exception e) {
                textOutput.setText("Exception: " + e.getMessage());
            }
        } else {
            System.out.println("Файл не найден: SupportAdderHelp.pdf");
        }
    }

    private String addPoints() {

        String selectedItem = dropdownMenu.getValue();
        int pointType = switch (selectedItem) {
            case "Опора ТМПК" -> 7;
            case "Опора Россети" -> 11;
            case "Опора Мособлэнерго" -> 10;
            case "Колодец" -> 3;
            case "Трубостойка ТМПК" -> 8;
            default -> 2;
        };

        return supportAdder.addPoints(pointType);
    }

    private String renamePoints() {
        return supportAdder.renamePoints();
    }

    private String submit() {
        String response = "";
        String selectedItem = operationMenu.getValue();
        switch (selectedItem) {
            case "Добавить объекты" -> response = addPoints();
            case "Переименовать объекты" -> response = renamePoints();
        }
        return response;
    }

}