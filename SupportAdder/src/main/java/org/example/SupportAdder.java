package org.example;

import jxl.Sheet;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Boolean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public final class SupportAdder {


    //private ArrayList<FullPoint> fullPoints;
    private final String cat = "node";
    private APIConnector api;
    private FileWorker fileWorker;
    //private ArrayList<RenamingPoint> renamingPoints;
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

    public void loadAddingPoints() {
        readPointsFromFile();
    }

    public void loadXls(String docPath, String fileName){// добавить или переименовать
        this.fileWorker = new FileWorker(docPath, fileName);
        fileWorker.loadXls();
    }

    public void loadRenamingPoints(){
        readRenamingPointsFromFile();
    }

    private void readPointsFromFile() {
        this.points = new ArrayList<>();
        Sheet sheet = fileWorker.getSheet();

        try {
            for (int i = 0; i < sheet.getRows(); i++)
                this.points.add(new FullPoint(
                        sheet.getCell(0, i).getContents(),
                        sheet.getCell(1, i).getContents(),
                        sheet.getCell(2, i).getContents()));

        } catch (Exception ex) {
            System.out.println("Readings error " + ex.getMessage());
        }
    }

    private void readRenamingPointsFromFile() {
        Sheet sheet = fileWorker.getSheet();
        points = new ArrayList<>();
        try {
            for (int i = 0; i < sheet.getRows(); i++) {
                //first column = old number; second column = new number
                this.points.add(new RenamingPoint(
                        sheet.getCell(0, i).getContents(),
                        sheet.getCell(1, i).getContents()
                ));
            }
        } catch (Exception ex) {
            System.out.println("Reading error " + ex.getMessage());
        }
    }


    public HashMap<String, Boolean> chkSupportsExistence() {
        HashMap<String, Boolean> returnHM = new HashMap<>();
        for (Point point : this.points) returnHM.put(point.getNum(), api.checkSupportExistence(point));
        return returnHM;
    }

    @SuppressWarnings("unchecked")
    public String addPoints(int supType) {
        loadAddingPoints();
        ArrayList<String> addedPoints = api.addPoints(supType, (ArrayList<FullPoint>) (ArrayList<?>) this.points);
        StringBuilder returnString = new StringBuilder();
        int addedCount = Integer.parseInt(addedPoints.get(1));
        String returnPath = this.saveModifiedFile();
        if (addedCount > 0) {
            returnString.append("\nОбъекты добавлены успешно\nКнига сохранена в ").append(returnPath);
        } else {
            returnString.append("\nДанные объекты уже существуют. Объекты не добавлены\nКнига сохранена в ").append(returnPath);
        }
        return returnString.toString();
    }

    @SuppressWarnings("unchecked")
    public String renamePoints() {
        loadRenamingPoints();
        ArrayList<String> renamedPoints = api.renamePoints((ArrayList<RenamingPoint>) (ArrayList<?>) this.points);
        StringBuilder returnString = new StringBuilder();
        int renamedCount = Integer.parseInt(renamedPoints.get(1));
        String returnPath = this.saveModifiedFile();
        if (renamedCount > 0) {
            returnString.append("\nОбъекты переименованы успешно\nКнига сохранена в ").append(returnPath);
        } else {
            returnString.append("\nДанные объекты не переименованы.\nКнига сохранена в ").append(returnPath);
        }
        return returnString.toString();
    }

    private String saveModifiedFile() {
        return fileWorker.saveModifiedFile(points);
    }

}