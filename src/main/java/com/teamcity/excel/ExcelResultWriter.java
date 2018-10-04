package com.teamcity.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class ExcelResultWriter {
    protected final Workbook workbook = new XSSFWorkbook();
    protected final Font headerFont;
    protected final CellStyle headerCellStyle;


    public ExcelResultWriter() {
        headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK1.getIndex());

        // Create a CellStyle with the font
        headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
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
