package com.teamcity.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class ExcelBaseWriter {
    protected final Workbook workbook = new XSSFWorkbook();
    protected final Font headerFont;
    protected final CellStyle headerCellStyle;


    public ExcelBaseWriter() {
        headerFont = workbook.createFont();
        //headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 10);
        //headerFont.setColor(IndexedColors.BLACK1.getIndex());

        // Create a CellStyle with the font
        headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        XSSFColor myColor = new XSSFColor(java.awt.Color.BLUE);
        headerCellStyle.setFillBackgroundColor((short) 0x222222);
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    protected void writeToFile(String filename) {
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
