package org.example;

import jxl.Cell;
import jxl.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Boolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public final class SupportAdder {

    private APIConnector api;
    private FileWorker fileWorker;
    private ArrayList<Point> points;

    public SupportAdder() {

        try (FileInputStream input = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            this.api = new APIConnector(properties.getProperty("api.url"), properties.getProperty("api.key"));
        } catch (IOException e) {
            System.err.println("Ошибка при загрузки файлов:\n" + e.getMessage());
        }
    }

    public boolean loadAddingPoints() {
        return readPointsFromFile();
    }

    public void loadXls(String docPath, String fileName) {
        this.fileWorker = new FileWorker(docPath, fileName);
        fileWorker.loadXls();
    }

    public boolean loadRenamingPoints() {
        return readRenamingPointsFromFile();
    }

    private boolean readPointsFromFile() {
        this.points = new ArrayList<>();
        Sheet sheet = fileWorker.getSheet();

        try {
            for (int i = 0; i < sheet.getRows(); i++) {
                Cell numCell = sheet.getCell(0, i);
                Cell firstCoord = sheet.getCell(1, i);
                Cell secondCoord = sheet.getCell(2, i);
                if (numCell != null && firstCoord != null && secondCoord != null &&
                        chkNumberPattern(numCell.getContents()) && !chkNumberPattern(
                        firstCoord.getContents()) && !chkNumberPattern(secondCoord.getContents()))
                    this.points.add(new FullPoint(
                            sheet.getCell(0, i).getContents(),
                            sheet.getCell(1, i).getContents(),
                            sheet.getCell(2, i).getContents()));
                else return false;
            }

        } catch (Exception ex) {
            System.out.println("Readings error " + ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean readRenamingPointsFromFile() {
        Sheet sheet = fileWorker.getSheet();
        points = new ArrayList<>();
        try {
            for (int i = 0; i < sheet.getRows(); i++) {
                //first column = old number; second column = new number
                Cell oldNumCell = sheet.getCell(0, i);
                Cell newNumCell = sheet.getCell(1, i);
                if (oldNumCell != null && newNumCell != null && chkNumberPattern(
                        oldNumCell.getContents()) && chkNumberPattern(
                        newNumCell.getContents())
                )
                    this.points.add(new RenamingPoint(
                            sheet.getCell(0, i).getContents(),
                            sheet.getCell(1, i).getContents()
                    ));
                else return false;
            }
        } catch (Exception ex) {
            System.out.println("Reading error " + ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean chkNumberPattern(String string) {
        return string.matches("^№\\d+\\([А-ЯЁа-яё]{2,4}-\\d+\\)$");
    }


    public HashMap<String, Boolean> chkSupportsExistence() {
        HashMap<String, Boolean> returnHM = new HashMap<>();
        for (Point point : this.points) returnHM.put(point.getNum(), api.checkSupportExistence(point));
        return returnHM;
    }

    @SuppressWarnings("unchecked")
    public String addPoints(int supType) {
        StringBuilder returnString = new StringBuilder();
        if (loadAddingPoints()) {
            ArrayList<String> addedPoints = api.addPoints(supType, (ArrayList<FullPoint>) (ArrayList<?>) this.points);
            int addedCount = Integer.parseInt(addedPoints.get(1));
            String returnPath = this.saveModifiedFile();
            if (addedCount > 0) {
                returnString.append("\nОбъекты добавлены успешно\nКнига сохранена в ").append(returnPath);
            } else {
                returnString.append(
                        "\nДанные объекты уже существуют. Объекты не добавлены\nКнига сохранена в ").append(returnPath);
            }
        } else returnString.append("\nОшибка чтения файла. Проверьте файл");
        return returnString.toString();
    }

    @SuppressWarnings("unchecked")
    public String renamePoints() {
        StringBuilder returnString = new StringBuilder();
        if (loadRenamingPoints()) {
            ArrayList<String> renamedPoints = api.renamePoints((ArrayList<RenamingPoint>) (ArrayList<?>) this.points);
            int renamedCount = Integer.parseInt(renamedPoints.get(1));
            String returnPath = this.saveModifiedFile();
            if (renamedCount > 0) {
                returnString.append("\nОбъекты переименованы успешно\nКнига сохранена в ").append(returnPath);
            } else {
                returnString.append("\nДанные объекты не переименованы.\nКнига сохранена в ").append(returnPath);
            }
        } else returnString.append("\nОшибка чтения файла. Проверьте файл");
        return returnString.toString();
    }

    private String saveModifiedFile() {
        return fileWorker.saveModifiedFile(points);
    }

}