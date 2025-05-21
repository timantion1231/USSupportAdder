package org.example;

import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Colour;
import jxl.write.*;

import java.io.File;
import java.util.ArrayList;

public class FileWorker {
    private final String docPath;
    private final String fileName;
    private Sheet sheet;

    public FileWorker(String docFile, String fileName) {
        this.docPath = docFile;
        this.fileName = fileName;
    }


    protected void loadXls() {
        try {
            Workbook wb = Workbook.getWorkbook(new File(this.docPath + File.separator + this.fileName));
            this.sheet = wb.getSheet(0);
        } catch (Exception ex) {
            System.err.println("Loading error : " + ex.getMessage());
        }
    }

    public Sheet getSheet() {
        return this.sheet;
    }

    protected String saveModifiedFile(ArrayList<Point> points) {
        try {
            WritableWorkbook newWorkbook = Workbook.createWorkbook(new File(this.docPath + "/" + "modified_" + this.fileName));
            WritableSheet newSheet = newWorkbook.createSheet("Modified", 0);

            WritableFont okFont = new WritableFont(WritableFont.ARIAL, 10);
            okFont.setColour(Colour.GREEN);

            WritableFont notOkFont = new WritableFont(WritableFont.ARIAL, 10);
            notOkFont.setColour(Colour.DARK_RED2);

            Label numLabel;
            Label latLabel;
            Label lonLabel;
            Label messageLabel;

            Label oldNumLabel;
            Label newNumLabel;

            WritableCellFormat cellFormat;

            for (int i = 0; i < this.sheet.getRows(); i++) {

                if (points.get(i).getStatus()) {
                    cellFormat = new WritableCellFormat(notOkFont);
                } else {
                    cellFormat = new WritableCellFormat(okFont);
                }
                if (points.get(i).getClass() == FullPoint.class) {
                    FullPoint fullPoint = (FullPoint) points.get(i);
                    numLabel = new Label(0, i, fullPoint.getNum(), cellFormat);
                    lonLabel = new Label(1, i, fullPoint.getLon(), cellFormat);
                    latLabel = new Label(2, i, fullPoint.getLat(), cellFormat);
                    messageLabel = new Label(3, i, "Объект уже существует", cellFormat);

                    newSheet.addCell(latLabel);
                    newSheet.addCell(lonLabel);
                    newSheet.addCell(numLabel);
                    if (points.get(i).getStatus()) {
                        newSheet.addCell(messageLabel);
                    }
                } else if (points.get(i).getClass() == RenamingPoint.class) {
                    RenamingPoint renamingPoint = (RenamingPoint) points.get(i);
                    oldNumLabel = new Label(0, i, renamingPoint.getOldNum(), cellFormat);
                    newNumLabel = new Label(1, i, renamingPoint.getNewNum(), cellFormat);
                    messageLabel = new Label(2, i, "Объект не переименован", cellFormat);

                    newSheet.addCell(oldNumLabel);
                    newSheet.addCell(newNumLabel);
                    if (points.get(i).getStatus()) {
                        newSheet.addCell(messageLabel);
                    }
                }

            }

            newWorkbook.write();
            newWorkbook.close();

            //https://www.machinet.net/tutorial-ru/java-excel-data-processing-with-jexcelapi


        } catch (Exception ex) {
            System.out.println("Error at marking nodes " + ex.getMessage());
        }
        return this.docPath + "modified_" + this.fileName;

    }


}
