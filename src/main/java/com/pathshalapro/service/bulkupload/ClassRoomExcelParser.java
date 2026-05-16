package com.pathshalapro.service.bulkupload;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.ClassRoom;
import com.pathshalapro.entity.School;
import com.pathshalapro.repository.ClassRoomRepository;
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
public class ClassRoomExcelParser {

    private final ClassRoomRepository classRoomRepository;

    private static final String[] HEADERS = {
        "Name*", "Section*", "Grade*", "Academic Year*", "Capacity", "Room Number"
    };

    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Classes");
            createHeaderRow(wb, sheet, HEADERS);
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Class 10");
            sample.createCell(1).setCellValue("A");
            sample.createCell(2).setCellValue("10");
            sample.createCell(3).setCellValue("2026-27");
            sample.createCell(4).setCellValue("40");
            sample.createCell(5).setCellValue("R101");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate classroom template", e);
        }
    }

    public ExcelUploadResult parse(InputStream inputStream, School school) {
        ExcelUploadResult result = ExcelUploadResult.builder().module("classes").build();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getCellString(row, 0);
                    String section = getCellString(row, 1);
                    String grade = getCellString(row, 2);
                    String academicYear = getCellString(row, 3);
                    String capacity = getCellString(row, 4);
                    String roomNumber = getCellString(row, 5);

                    if (name == null || name.isBlank()) {
                        result.addError(i + 1, "name", "Class name is required");
                        continue;
                    }
                    if (section == null || section.isBlank()) {
                        result.addError(i + 1, "section", "Section is required");
                        continue;
                    }
                    if (grade == null || grade.isBlank()) {
                        result.addError(i + 1, "grade", "Grade is required");
                        continue;
                    }
                    if (academicYear == null || academicYear.isBlank()) {
                        result.addError(i + 1, "academicYear", "Academic year is required");
                        continue;
                    }
                    if (classRoomRepository.existsByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse(
                            name.trim(), section.trim(), school.getId(), academicYear.trim())) {
                        result.addError(i + 1, "name", "Class '" + name + "-" + section + "' already exists for " + academicYear);
                        continue;
                    }

                    ClassRoom.ClassRoomBuilder builder = ClassRoom.builder()
                            .name(name.trim())
                            .section(section.trim())
                            .grade(grade.trim())
                            .academicYear(academicYear.trim())
                            .roomNumber(roomNumber)
                            .school(school);

                    if (capacity != null && !capacity.isBlank()) {
                        try {
                            builder.capacity(Integer.parseInt(capacity.trim()));
                        } catch (NumberFormatException e) {
                            result.addError(i + 1, "capacity", "Capacity must be a number");
                            continue;
                        }
                    }

                    classRoomRepository.save(builder.build());
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, "general", "Unexpected error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse classroom Excel", e);
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
        cell.setCellType(CellType.STRING);
        String val = cell.getStringCellValue();
        return (val != null && !val.trim().isEmpty()) ? val.trim() : null;
    }
}
