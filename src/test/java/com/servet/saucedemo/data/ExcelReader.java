package com.servet.saucedemo.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Bir Excel sayfasını satır listesi olarak okur. İlk satır başlıktır: her satır "sütun adı → değer".
 *
 * DataFormatter, hücreyi Excel'de GÖRÜNDÜĞÜ gibi metne çevirir (sayı, tarih, boş hücre).
 * cell.getStringCellValue() sayı içeren hücrede exception fırlatır; boş hücre ise null gelir.
 */
public final class ExcelReader {

    private ExcelReader() {
    }

    public static List<Map<String, String>> readSheet(String classpathResource, String sheetName) {
        try (InputStream in = Objects.requireNonNull(ExcelReader.class.getResourceAsStream(classpathResource),
                "Excel dosyası bulunamadı: " + classpathResource);
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = Objects.requireNonNull(workbook.getSheet(sheetName), "Sayfa bulunamadı: " + sheetName);
            DataFormatter formatter = new DataFormatter();

            List<String> headers = new ArrayList<>();
            for (Cell cell : sheet.getRow(0)) {
                headers.add(formatter.formatCellValue(cell).trim());
            }

            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Map<String, String> values = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    // Boş hücre → "" (null değil). Değer kırpılmaz: baştaki/sondaki boşluk da test verisidir.
                    values.put(headers.get(c), formatter.formatCellValue(row.getCell(c, MissingCellPolicy.CREATE_NULL_AS_BLANK)));
                }
                if (values.values().stream().anyMatch(v -> !v.isBlank())) {
                    rows.add(values);
                }
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
