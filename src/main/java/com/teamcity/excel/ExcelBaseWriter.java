package com.teamcity.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class ExcelBaseWriter {
    protected final Workbook workbook = new SXSSFWorkbook();
    protected final Font headerFont;
    protected final CellStyle headerCellStyle;


    public ExcelBaseWriter() {
        headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);

        // Create a CellStyle with the font
        headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    protected void writeToFile(String filename) {
        for (int s=0; s<workbook.getNumberOfSheets(); s++) {
            Sheet sheet = workbook.getSheetAt(s);
            sheet.createFreezePane(0, 1);
        }

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
