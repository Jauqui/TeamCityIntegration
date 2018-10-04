package com.teamcity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExcelWriter {
    Workbook workbook = new XSSFWorkbook();

    public ExcelWriter() {

    }

    public void addResultsToFile(TCResult result) {
        LocalDateTime startDateTime = result.getStartDateTime();
        // Create a Sheet
        Sheet sheet = workbook.createSheet(startDateTime.toString().replace(":", "-"));

        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK1.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

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
        cell = headerRow.createCell(4);
        cell.setCellValue("StackTrace");
        cell.setCellStyle(headerCellStyle);

        int classRow = 1, methodRow = 1, testRow = 1, testResultRow = 1;
        for (TCClass tcClass : result.getClasses()) {
            methodRow = classRow;
            for (TCMethod tcMethod : tcClass.getMethods()) {
                testRow = methodRow;
                for (TCTest tcTest : tcMethod.getTests()) {
                    Row row = sheet.createRow(testRow);
                    row.createCell(0).setCellValue(tcClass.getClassName());
                    row.createCell(1).setCellValue(tcMethod.getMethodName());
                    row.createCell(2).setCellValue(tcTest.getParameters());
                    row.createCell(3).setCellValue(tcTest.getStatus().name());
                    row.createCell(4).setCellValue(tcTest.getStackTrace());
                    testRow++;
                }
                if (testRow > methodRow + 1) {
                    CellRangeAddress cellRangeAddress = new CellRangeAddress(methodRow, testRow - 1, 1, 1);
                    sheet.addMergedRegion(cellRangeAddress);
                }
                methodRow = testRow;
            }
            if (testRow > classRow + 1) {
                CellRangeAddress cellRangeAddress = new CellRangeAddress(classRow, testRow - 1, 0, 0);
                sheet.addMergedRegion(cellRangeAddress);
            }
            classRow = testRow;
        }
    }

    public void writeToFile(String filename) {
        // Write the output to a file
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(filename + ".xlsx");
            workbook.write(fileOut);
            fileOut.close();

            // Closing the workbook
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
