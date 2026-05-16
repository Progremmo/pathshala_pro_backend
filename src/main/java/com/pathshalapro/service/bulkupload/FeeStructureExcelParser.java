package com.pathshalapro.service.bulkupload;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.FeeStructure;
import com.pathshalapro.entity.School;
import com.pathshalapro.repository.FeeStructureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeeStructureExcelParser {

    private final FeeStructureRepository feeStructureRepository;

    private static final String[] HEADERS = {
        "Name*", "Fee Type*", "Amount*", "Frequency*", "Grade",
        "Academic Year*", "Description", "Due Day (1-31)"
    };

    private static final Set<String> VALID_FEE_TYPES = Set.of(
        "TUITION", "TRANSPORT", "HOSTEL", "LIBRARY", "EXAM", "SPORTS", "ADMISSION", "OTHER"
    );

    private static final Set<String> VALID_FREQUENCIES = Set.of(
        "MONTHLY", "QUARTERLY", "HALF_YEARLY", "ANNUALLY", "ONE_TIME"
    );

    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Fees");
            createHeaderRow(wb, sheet, HEADERS);
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Tuition Fee - Class 10");
            sample.createCell(1).setCellValue("TUITION");
            sample.createCell(2).setCellValue("5000");
            sample.createCell(3).setCellValue("MONTHLY");
            sample.createCell(4).setCellValue("10");
            sample.createCell(5).setCellValue("2026-27");
            sample.createCell(6).setCellValue("Standard monthly tuition fee");
            sample.createCell(7).setCellValue("10");

            // Instructions sheet
            Sheet instrSheet = wb.createSheet("Instructions");
            int r = 0;
            instrSheet.createRow(r++).createCell(0).setCellValue("Fee Types: " + String.join(", ", VALID_FEE_TYPES));
            instrSheet.createRow(r++).createCell(0).setCellValue("Frequencies: " + String.join(", ", VALID_FREQUENCIES));
            instrSheet.createRow(r++).createCell(0).setCellValue("Amount should be a positive number.");
            instrSheet.createRow(r).createCell(0).setCellValue("Due Day should be between 1 and 31.");
            instrSheet.setColumnWidth(0, 15000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate fee template", e);
        }
    }

    public ExcelUploadResult parse(InputStream inputStream, School school) {
        ExcelUploadResult result = ExcelUploadResult.builder().module("fees").build();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getCellString(row, 0);
                    String feeType = getCellString(row, 1);
                    String amountStr = getCellString(row, 2);
                    String frequency = getCellString(row, 3);
                    String grade = getCellString(row, 4);
                    String academicYear = getCellString(row, 5);
                    String description = getCellString(row, 6);
                    String dueDayStr = getCellString(row, 7);

                    if (name == null || name.isBlank()) { result.addError(i+1, "name", "Fee name is required"); continue; }
                    if (feeType == null || feeType.isBlank()) { result.addError(i+1, "feeType", "Fee type is required"); continue; }
                    if (amountStr == null || amountStr.isBlank()) { result.addError(i+1, "amount", "Amount is required"); continue; }
                    if (frequency == null || frequency.isBlank()) { result.addError(i+1, "frequency", "Frequency is required"); continue; }
                    if (academicYear == null || academicYear.isBlank()) { result.addError(i+1, "academicYear", "Academic year is required"); continue; }

                    if (!VALID_FEE_TYPES.contains(feeType.trim().toUpperCase())) {
                        result.addError(i+1, "feeType", "Invalid fee type. Valid: " + String.join(", ", VALID_FEE_TYPES));
                        continue;
                    }
                    if (!VALID_FREQUENCIES.contains(frequency.trim().toUpperCase())) {
                        result.addError(i+1, "frequency", "Invalid frequency. Valid: " + String.join(", ", VALID_FREQUENCIES));
                        continue;
                    }

                    BigDecimal amount;
                    try {
                        amount = new BigDecimal(amountStr.trim());
                        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                            result.addError(i+1, "amount", "Amount must be positive");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        result.addError(i+1, "amount", "Invalid amount format");
                        continue;
                    }

                    FeeStructure.FeeStructureBuilder builder = FeeStructure.builder()
                            .name(name.trim())
                            .feeType(feeType.trim().toUpperCase())
                            .amount(amount)
                            .frequency(frequency.trim().toUpperCase())
                            .grade(grade)
                            .academicYear(academicYear.trim())
                            .description(description)
                            .school(school);

                    if (dueDayStr != null && !dueDayStr.isBlank()) {
                        try {
                            int dueDay = Integer.parseInt(dueDayStr.trim());
                            if (dueDay < 1 || dueDay > 31) {
                                result.addError(i+1, "dueDay", "Due day must be between 1 and 31");
                                continue;
                            }
                            builder.dueDay(dueDay);
                        } catch (NumberFormatException e) {
                            result.addError(i+1, "dueDay", "Due day must be a number");
                            continue;
                        }
                    }

                    feeStructureRepository.save(builder.build());
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, "general", "Unexpected error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse fee structure Excel", e);
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
