package com.teamcity.excel;

import com.teamcity.*;
import com.teamcity.enums.TCMetric;
import com.teamcity.enums.TCStatus;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ComparativeResultExcelWriter extends ExcelBaseWriter {
    private HashMap<String, Integer> headerRows = new HashMap<>();

    private List<TCMetric> metrics = Arrays.asList(TCMetric.Total_Runs, TCMetric.Pass_Percentage);

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
    }

    public void addResult(TCResults result) {
        //Update header row
        Sheet sheet = workbook.getSheetAt(0);
        Row headerRow = sheet.getRow(0);
        for (LocalDateTime startDateTime : result.getTestStartDateTimes()) {
            int cellPos = headerRow.getLastCellNum();
            Cell cell = headerRow.createCell(cellPos);
            cell.setCellValue(startDateTime.toString());
            cell.setCellStyle(headerCellStyle);

            headerRows.put(startDateTime.toString(), cellPos);
        }
    }

    public void writeToFile(TCResults results, String fileName) {
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        for (TCMetric tcMetric : metrics) {
            int cellPos = headerRow.getLastCellNum();
            Cell cell = headerRow.createCell(cellPos);
            cell.setCellValue(tcMetric.getHeader());
            cell.setCellStyle(headerCellStyle);

            headerRows.put(tcMetric.getHeader(), cellPos);
        }

        HashSet<LocalDateTime> startDateTimes = results.getTestStartDateTimes();

        for (TCClass tcClass : results.getClasses()) {
            String className = tcClass.getClassName();
            for (TCMethod tcMethod : tcClass.getMethods()) {
                String methodName = tcMethod.getMethodName();
                for (TCTest tcTest : tcMethod.getTests()) {
                    String testParameters = tcTest.getParameters();
                    Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                    row.createCell(0).setCellValue(className);
                    row.createCell(1).setCellValue(methodName);
                    row.createCell(2).setCellValue(testParameters);
                    for (LocalDateTime startDateTime : tcTest.getRunTimes()) {
                        int col = headerRows.get(startDateTime.toString());
                        TCTestRun tcTestRun = tcTest.getTestRun(startDateTime);
                        row.createCell(col).setCellValue(tcTestRun.getStatus().name());
                    }

                    //Metrics
                    for (TCMetric tcMetric : metrics) {
                        int col = headerRows.get(tcMetric.getHeader());
                        row.createCell(col).setCellValue(MetricsCalculator.processMetric(startDateTimes, tcTest, tcMetric));
                    }
                }
            }
        }

        //Write overall status
        Row rowPass = sheet.createRow(sheet.getLastRowNum() + 1);
        Row rowSkip = sheet.createRow(sheet.getLastRowNum() + 1);
        Row rowFail = sheet.createRow(sheet.getLastRowNum() + 1);

        CellRangeAddress cellRangeAddress = new CellRangeAddress(rowPass.getRowNum(), rowPass.getRowNum(), 0, 2);
        sheet.addMergedRegion(cellRangeAddress);
        cellRangeAddress = new CellRangeAddress(rowSkip.getRowNum(), rowSkip.getRowNum(), 0, 2);
        sheet.addMergedRegion(cellRangeAddress);
        cellRangeAddress = new CellRangeAddress(rowFail.getRowNum(), rowFail.getRowNum(), 0, 2);
        sheet.addMergedRegion(cellRangeAddress);
        for (LocalDateTime startDateTime : startDateTimes) {
            int runPass = results.getTestRunsSize(startDateTime, TCStatus.PASS);
            int runSkip = results.getTestRunsSize(startDateTime, TCStatus.SKIP);
            int runFail = results.getTestRunsSize(startDateTime, TCStatus.FAIL);

            rowPass.createCell(0).setCellValue(TCStatus.PASS.name());
            rowPass.createCell(headerRows.get(startDateTime.toString())).setCellValue(runPass);

            rowSkip.createCell(0).setCellValue(TCStatus.SKIP.name());
            rowSkip.createCell(headerRows.get(startDateTime.toString())).setCellValue(runSkip);

            rowFail.createCell(0).setCellValue(TCStatus.FAIL.name());
            rowFail.createCell(headerRows.get(startDateTime.toString())).setCellValue(runFail);
        }

        writeToFile(fileName);
    }
}
