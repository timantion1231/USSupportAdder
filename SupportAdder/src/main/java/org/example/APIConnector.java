package org.example;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class APIConnector {
    private final String url;
    private final String key;

    private final String cat = "node";

    public APIConnector(String url, String key) {
        this.url = url;
        this.key = key;
    }

    protected boolean checkSupportExistence(Point point) {

        boolean supportExist = checkSupportExistence(point.getNum());
        point.setStatus(supportExist);
        return supportExist;
    }

    protected boolean checkSupportExistence(String num) {

        boolean supportExist;

        supportExist = getIdByNum(num) > -1;
        return supportExist;
    }

    protected int getIdByNum(String num) {

        JSONObject jsonObject;

        String action = "get_id";
        String dataType = "number";

        HttpClient client;
        HttpRequest request;
        HttpResponse<String> response;

        String queryParams = String.format(
                "key=%s&action=%s&data_type=%s&cat=%s&data_value=%s",
                URLEncoder.encode(this.key, StandardCharsets.UTF_8),
                URLEncoder.encode(action, StandardCharsets.UTF_8),
                URLEncoder.encode(dataType, StandardCharsets.UTF_8),
                URLEncoder.encode(this.cat, StandardCharsets.UTF_8),
                URLEncoder.encode(num, StandardCharsets.UTF_8)
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
        if (jsonObject.has("Id")) {
            return (int) jsonObject.get("Id");
        } else return -1;

    }

    public ArrayList<String> addPoints(int supType, ArrayList<FullPoint> fullPoints) {

        String action = "add";
        String coordinates;
        String queryParams;
        String fullURL;

        HttpClient client;
        HttpRequest request;
        StringBuilder returnString = new StringBuilder();
        int addedCount = 0;

        for (FullPoint fullPoint : fullPoints) {
            if (!checkSupportExistence(fullPoint)) {
                addedCount++;
                coordinates = fullPoint.getLat() + "," + fullPoint.getLon();
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
                        URLEncoder.encode(fullPoint.getNum(), StandardCharsets.UTF_8)
                );
                returnString.append("Объект ").append(fullPoint.getNum()).append(" добавлен\n");
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
                returnString.append("Объект ").append(fullPoint.getNum()).append(" уже существует\n");
            }
        }

        ArrayList<String> returnVals = new ArrayList<>();
        returnVals.add(returnString.toString());
        returnVals.add(Integer.toString(addedCount));
        return returnVals;
    }


    public ArrayList<String> renamePoints(ArrayList<RenamingPoint> renamingPoints) {

        String action = "edit";
        String queryParams;
        String fullURL;

        HttpClient client;
        HttpRequest request;
        StringBuilder returnString = new StringBuilder();
        int addedCount = 0;

        for (RenamingPoint renamingPoint : renamingPoints) {
            if (checkSupportExistence(renamingPoint) && !checkSupportExistence(renamingPoint.getNewNum())) {
                addedCount++;
                int id = getIdByNum(renamingPoint.getNum());
                queryParams = String.format(
                        "key=%s" +
                                "&action=%s" +
                                "&cat=%s" +
                                "&id=%s" +
                                "&number=%s",
                        URLEncoder.encode(this.key, StandardCharsets.UTF_8),
                        URLEncoder.encode(action, StandardCharsets.UTF_8),
                        URLEncoder.encode(this.cat, StandardCharsets.UTF_8),
                        URLEncoder.encode(String.valueOf(id), StandardCharsets.UTF_8),
                        URLEncoder.encode(renamingPoint.getNewNum(), StandardCharsets.UTF_8)
                );
                returnString.append("Объект ").append(renamingPoint.getNum()).append(" переименован в ")
                        .append(renamingPoint.getNewNum()).append("\n");
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
                returnString.append("Объект ").append(renamingPoint.getNum()).append(" не переименован в ")
                        .append(renamingPoint.getNewNum()).append("\n");
            }
        }
        ArrayList<String> returnVals = new ArrayList<>();
        returnVals.add(returnString.toString());
        returnVals.add(Integer.toString(addedCount));

        return returnVals;
    }

}
