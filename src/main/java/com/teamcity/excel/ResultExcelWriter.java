package com.teamcity.excel;

import com.teamcity.*;
import com.teamcity.enums.TCStatus;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.time.LocalDateTime;
import java.util.HashSet;

public class ResultExcelWriter extends ExcelBaseWriter {
    public void writeResultFile(TCResults result, String filename) {
        HashSet<LocalDateTime> startDateTimes = result.getTestStartDateTimes();

        for (LocalDateTime startDateTime : startDateTimes) {
            // Create a Sheet
            Sheet sheet = workbook.createSheet(startDateTime.toString().replace(":", "-"));
            Sheet sheetFail = workbook.createSheet("Fail");
            Sheet sheetSkip = workbook.createSheet("Skip");
            Sheet sheetPass = workbook.createSheet("Pass");

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
                int classFailures = tcClass.getTestRunsSize(startDateTime, TCStatus.FAIL);
                Row failRow = sheetFail.createRow(testResultRow++);
                failRow.createCell(0).setCellValue(tcClass.getClassName());
                failRow.createCell(1).setCellValue(classFailures);
                int classSkips = tcClass.getTestRunsSize(startDateTime, TCStatus.SKIP);
                Row skipRow = sheetSkip.createRow(testResultRow);
                skipRow.createCell(0).setCellValue(tcClass.getClassName());
                skipRow.createCell(1).setCellValue(classSkips);
                int classPass = tcClass.getTestRunsSize(startDateTime, TCStatus.PASS);
                Row passRow = sheetPass.createRow(testResultRow);
                passRow.createCell(0).setCellValue(tcClass.getClassName());
                passRow.createCell(1).setCellValue(classPass);
                methodRow = classRow;
                for (TCMethod tcMethod : tcClass.getMethods()) {
                    testRow = methodRow;
                    for (TCTest tcTest : tcMethod.getTests()) {
                        Row row = sheet.createRow(testRow);
                        row.createCell(0).setCellValue(tcClass.getClassName());
                        row.createCell(1).setCellValue(tcMethod.getMethodName());
                        row.createCell(2).setCellValue(tcTest.getParameters());
                        TCTestRun tcTestRun = tcTest.getTestRun(startDateTime);
                        row.createCell(3).setCellValue(tcTestRun.getStatus().name());
                        if (tcTestRun.getStackTrace().length()>32000)
                            row.createCell(4).setCellValue(tcTestRun.getStackTrace().substring(0, 32700) + "... too long");
                        else
                            row.createCell(4).setCellValue(tcTestRun.getStackTrace());
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

        writeToFile(filename);
    }
}
