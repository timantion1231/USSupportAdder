package org.example;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.*;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Boolean;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

public final class SupportAdder {
    private String url;
    private String key;

    private String docPath;
    private String fileName;
    private Sheet sheet;
    private ArrayList<Point> points;

    private final String cat = "node";

    public SupportAdder() {

        try (FileInputStream input = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(input);
            this.key = properties.getProperty("api.key");
            this.url = properties.getProperty("api.url");
        } catch (IOException e) {
            System.err.println("Ошибка при загрузки файлов:\n" + e.getMessage());
        }
    }

    public void loadXls(String docPath, String fileName) {
        try {
            this.docPath = docPath;
            this.fileName = fileName;

            Workbook wb = Workbook.getWorkbook(new File(this.docPath + File.separator + this.fileName));

            this.sheet = wb.getSheet(0);
            readPointsFromFile();
        } catch (Exception ex) {
            System.err.println("Loading error : " + ex.getMessage());
        }
    }

    private void readPointsFromFile() {
        this.points = new ArrayList<>();

        try {
            for (int i = 0; i < this.sheet.getRows(); i++) {
                this.points.add(new Point(
                        this.sheet.getCell(0, i).getContents(),
                        this.sheet.getCell(1, i).getContents(),
                        this.sheet.getCell(2, i).getContents()));
            }

        } catch (Exception ex) {
            System.out.println("Readings error " + ex.getMessage());
        }
    }

    private boolean checkSupportExistence(Point point) {

        JSONObject jsonObject;

        String action = "get_id";
        String dataType = "number";
        String dataValue = point.getNum();

        HttpClient client;
        HttpRequest request;
        HttpResponse<String> response;

        boolean supportExist;


        String queryParams = String.format(
                "key=%s&action=%s&data_type=%s&cat=%s&data_value=%s",
                URLEncoder.encode(this.key, StandardCharsets.UTF_8),
                URLEncoder.encode(action, StandardCharsets.UTF_8),
                URLEncoder.encode(dataType, StandardCharsets.UTF_8),
                URLEncoder.encode(this.cat, StandardCharsets.UTF_8),
                URLEncoder.encode(dataValue, StandardCharsets.UTF_8)
        );

        String fullURL = this.url + "?" + queryParams;
        client = HttpClient.newHttpClient();

        request = HttpRequest.newBuilder()
                .uri(URI.create(fullURL))
                .GET()
                .build();

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        jsonObject = new JSONObject(response.body());
        supportExist = jsonObject.has("Id");
        point.setStatus(supportExist);
        return supportExist;

    }

    public HashMap<String, Boolean> chkSupportsExistence() {
        HashMap<String, Boolean> returnHM = new HashMap<>();
        for (Point point : this.points) returnHM.put(point.getNum(), this.checkSupportExistence(point));
        return returnHM;
    }

    public String addSupports(int supType) {

        String action = "add";
        String coordinates;
        String queryParams;
        String fullURL;
        String returnPath = "";

        HttpClient client;
        HttpRequest request;
        StringBuilder returnString = new StringBuilder();// добавить возврат данных о добавлении опор
        int addedCount = 0;

        for (Point point : this.points) {
            if (!checkSupportExistence(point)) {
                addedCount++;
                coordinates = point.getLat() + "," + point.getLon();
                queryParams = String.format(
                        "key=%s" +
                                "&action=%s" +
                                "&cat=%s" +
                                "&type=%s" +
                                "&coordinates=%s" +
                                "&number=%s",
                        URLEncoder.encode(this.key, StandardCharsets.UTF_8),
                        URLEncoder.encode(action, StandardCharsets.UTF_8),
                        URLEncoder.encode(this.cat, StandardCharsets.UTF_8),
                        URLEncoder.encode(String.valueOf(supType), StandardCharsets.UTF_8),
                        URLEncoder.encode(coordinates, StandardCharsets.UTF_8),
                        URLEncoder.encode(point.getNum(), StandardCharsets.UTF_8)
                );
                returnString.append("Опора ").append(point.getNum()).append(" добавлена\n");
                fullURL = this.url + "?" + queryParams;
                client = HttpClient.newHttpClient();
                request = HttpRequest.newBuilder()
                        .uri(URI.create(fullURL))
                        .GET()
                        .build();
                try {
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                returnString.append("Опоара ").append(point.getNum()).append(" уже существует\n");
            }
        }
        returnPath = this.saveModifiedFile();
        if (addedCount > 0) {
            returnString.append("\nОпоры добавлены успешно\nКнига сохранена в ").append(returnPath);
        } else {
            returnString.append("\nДанные опоры уже существуют. Опоры не добавлены\nКнига сохранена в ").append(returnPath);
        }
        return returnString.toString();
    }

    public String saveModifiedFile() {
        try {
            WritableWorkbook newWorkbook = Workbook.createWorkbook(new File(this.docPath + "/" + "modified_" + this.fileName));
            WritableSheet newSheet = newWorkbook.createSheet("Modified", 0);

            WritableFont okFont = new WritableFont(WritableFont.ARIAL, 10);
            okFont.setColour(Colour.GREEN);

            WritableFont existFont = new WritableFont(WritableFont.ARIAL, 10);
            existFont.setColour(Colour.DARK_RED2);

            Label numLabel;
            Label latLabel;
            Label lonLabel;
            Label messageLabel;

            WritableCellFormat cellFormat;

            for (int i = 0; i < this.sheet.getRows(); i++) {

                if (this.points.get(i).getStatus()) {
                    cellFormat = new WritableCellFormat(existFont);
                } else {
                    cellFormat = new WritableCellFormat(okFont);
                }

                numLabel = new Label(0, i, this.points.get(i).getNum(), cellFormat);
                lonLabel = new Label(1, i, this.points.get(i).getLon(), cellFormat);
                latLabel = new Label(2, i, this.points.get(i).getLat(), cellFormat);
                messageLabel = new Label(3, i, "Опора уже существует", cellFormat);

                newSheet.addCell(latLabel);
                newSheet.addCell(lonLabel);
                newSheet.addCell(numLabel);

                if (this.points.get(i).getStatus()) {
                    newSheet.addCell(messageLabel);
                }

            }

            newWorkbook.write();
            newWorkbook.close();

            //https://www.machinet.net/tutorial-ru/java-excel-data-processing-with-jexcelapi


        } catch (Exception ex) {
            System.out.println("Error at marking nodes " + ex.getMessage());
        }
        return this.docPath + "modified_" + this.fileName;

    } // помечает добавленные опоры
}