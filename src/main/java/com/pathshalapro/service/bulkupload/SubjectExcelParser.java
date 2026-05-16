package com.pathshalapro.service.bulkupload;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.Subject;
import com.pathshalapro.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectExcelParser {

    private final SubjectRepository subjectRepository;

    private static final String[] HEADERS = {
        "Name*", "Code*", "Grade*", "Description", "Credit Hours"
    };

    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Subjects");
            createHeaderRow(wb, sheet, HEADERS);
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Mathematics");
            sample.createCell(1).setCellValue("MATH101");
            sample.createCell(2).setCellValue("10");
            sample.createCell(3).setCellValue("Core mathematics for grade 10");
            sample.createCell(4).setCellValue("5");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate subject template", e);
        }
    }

    public ExcelUploadResult parse(InputStream inputStream, School school) {
        ExcelUploadResult result = ExcelUploadResult.builder().module("subjects").build();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getCellString(row, 0);
                    String code = getCellString(row, 1);
                    String grade = getCellString(row, 2);
                    String description = getCellString(row, 3);
                    String creditHours = getCellString(row, 4);

                    if (name == null || name.isBlank()) {
                        result.addError(i + 1, "name", "Subject name is required");
                        continue;
                    }
                    if (code == null || code.isBlank()) {
                        result.addError(i + 1, "code", "Subject code is required");
                        continue;
                    }
                    if (grade == null || grade.isBlank()) {
                        result.addError(i + 1, "grade", "Grade is required");
                        continue;
                    }
                    if (subjectRepository.existsByCodeAndSchoolIdAndIsDeletedFalse(code.trim(), school.getId())) {
                        result.addError(i + 1, "code", "Subject code '" + code + "' already exists");
                        continue;
                    }

                    Subject.SubjectBuilder builder = Subject.builder()
                            .name(name.trim())
                            .code(code.trim().toUpperCase())
                            .grade(grade.trim())
                            .description(description)
                            .school(school);

                    if (creditHours != null && !creditHours.isBlank()) {
                        try {
                            builder.creditHours(Integer.parseInt(creditHours.trim()));
                        } catch (NumberFormatException e) {
                            result.addError(i + 1, "creditHours", "Credit hours must be a number");
                            continue;
                        }
                    }

                    subjectRepository.save(builder.build());
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, "general", "Unexpected error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse subject Excel", e);
            result.addError(0, "file", "Failed to read Excel file: " + e.getMessage());
        }

        return result;
    }

    private void createHeaderRow(Workbook wb, Sheet sheet, String[] headers) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, 5000);
        }
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        DataFormatter formatter = new DataFormatter();
        String val = formatter.formatCellValue(cell);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : null;
    }
}
