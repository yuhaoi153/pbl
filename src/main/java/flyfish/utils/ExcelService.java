package flyfish.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class ExcelService {

    public byte[] createExcelTemplate() {
        byte[] byteArray = null;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Template");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Column1", "Column2", "Column3", "Column4"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }
            workbook.write(out);
            byteArray = out.toByteArray();

            // Save to file for testing
            saveToFileForTesting(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    private void saveToFileForTesting(byte[] data) {
        try (FileOutputStream fos = new FileOutputStream("test.xlsx")) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}