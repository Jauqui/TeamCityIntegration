package com.teamcity.excel;

import com.teamcity.TCClass;
import com.teamcity.TCMethod;
import com.teamcity.TCResult;
import com.teamcity.TCTest;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDateTime;
import java.util.HashMap;

public class ComparativeResultExcelWriter extends ExcelResultWriter {
    private HashMap<LocalDateTime, Integer> headerRows = new HashMap<>();


    public ComparativeResultExcelWriter() {
        super();

        Sheet sheet = workbook.createSheet("ComparativeResults");

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Create cells
        Cell cell = headerRow.createCell(0);
        cell.setCellValue("Class");
        cell.setCellStyle(headerCellStyle);
        cell = headerRow.createCell(1);
        cell.setCellValue("Method");
        cell.setCellStyle(headerCellStyle);
        cell = headerRow.createCell(2);
        cell.setCellValue("Paremeters");
        cell.setCellStyle(headerCellStyle);
        cell = headerRow.createCell(3);
        cell.setCellValue("Status");
        cell.setCellStyle(headerCellStyle);
    }

    public void addResult(TCResult result) {
        for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
            //Update header row
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            int cellPos = headerRow.getLastCellNum();
            Cell cell = headerRow.createCell(cellPos);
            cell.setCellValue(startDateTime.toString());
            cell.setCellStyle(headerCellStyle);

            headerRows.put(startDateTime, cellPos);
        }
    }

    public void writeToFile(TCResult result, String fileName) {
        Sheet sheet = workbook.getSheetAt(0);

        for (TCClass tcClass : result.getClasses()) {
            for (TCMethod tcMethod : tcClass.getMethods()) {
                HashMap<String, Integer> testParametersRow = new HashMap<>();
                for (TCTest tcTest : tcMethod.getTests()) {
                    if (testParametersRow.containsKey(tcTest.getParameters())) {
                        int rowTest = testParametersRow.get(tcTest.getParameters());
                        Row row = sheet.getRow(rowTest);
                        int col = headerRows.get(tcTest.getStartDateTime());
                        row.createCell(col).setCellValue(tcTest.getStatus().name());
                    } else {
                        int rowCount = sheet.getLastRowNum()+1;
                        testParametersRow.put(tcTest.getParameters(), rowCount);
                        Row row = sheet.createRow(rowCount);
                        row.createCell(0).setCellValue(tcClass.getClassName());
                        row.createCell(1).setCellValue(tcMethod.getMethodName());
                        row.createCell(2).setCellValue(tcTest.getParameters());

                        int col = headerRows.get(tcTest.getStartDateTime());
                        row.createCell(col).setCellValue(tcTest.getStatus().name());
                    }
                }
            }
        }

        writeToFile(fileName);
    }
}
