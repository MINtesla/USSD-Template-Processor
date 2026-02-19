package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Iterator;

public class ExcelRowFilter {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\ashutosh.singh4\\OneDrive - Comviva Technologies Ltd\\Documents\\vdrc\\J4U_USSD.xlsx";

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Iterate from bottom to top (important when deleting rows)
            for (int i = sheet.getLastRowNum(); i >= 0; i--) {
                Row row = sheet.getRow(i);

                if (row != null) {
                    Cell cell = row.getCell(0); // first column

                    String cellValue = "";

                    if (cell != null) {
                        cell.setCellType(CellType.STRING);
                        cellValue = cell.getStringCellValue();
                    }

                    if (!cellValue.startsWith("NEW_MAIN_MENU_")) {
                        removeRow(sheet, i);
                    }
                }
            }

            // Write changes back to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }

            System.out.println("Rows filtered successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void removeRow(Sheet sheet, int rowIndex) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        }
        if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }
}