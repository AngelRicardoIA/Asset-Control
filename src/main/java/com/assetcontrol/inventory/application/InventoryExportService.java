package com.assetcontrol.inventory.application;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.util.List;

@Service
public class InventoryExportService {

    private final JdbcTemplate jdbcTemplate;

    public InventoryExportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public byte[] export() {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            CellStyle headerStyle = headerStyle(workbook);
            List<String> tables = jdbcTemplate.queryForList("SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' AND name <> 'flyway_schema_history' ORDER BY name", String.class);
            for (String table : tables) {
                jdbcTemplate.query("SELECT * FROM \"" + table.replace("\"", "\"\"") + "\"", resultSet -> {
                    writeSheet(workbook, table, resultSet, headerStyle);
                    return null;
                });
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No fue posible generar el archivo de inventario.", exception);
        }
    }

    private CellStyle headerStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private void writeSheet(XSSFWorkbook workbook, String table, java.sql.ResultSet resultSet, CellStyle headerStyle) throws java.sql.SQLException {
        var sheet = workbook.createSheet(table);
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columns = metadata.getColumnCount();
        Row header = sheet.createRow(0);
        for (int column = 1; column <= columns; column++) {
            Cell cell = header.createCell(column - 1);
            cell.setCellValue(metadata.getColumnLabel(column));
            cell.setCellStyle(headerStyle);
        }
        int rowIndex = 1;
        while (resultSet.next()) {
            Row row = sheet.createRow(rowIndex++);
            for (int column = 1; column <= columns; column++) {
                Object value = resultSet.getObject(column);
                row.createCell(column - 1).setCellValue(value == null ? "" : String.valueOf(value));
            }
        }
        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, Math.max(0, rowIndex - 1), 0, columns - 1));
        for (int column = 0; column < columns; column++) sheet.autoSizeColumn(column);
    }
}
