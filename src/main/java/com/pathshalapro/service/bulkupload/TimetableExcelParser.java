package com.pathshalapro.service.bulkupload;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.Timetable;
import com.pathshalapro.entity.enums.DayOfWeek;
import com.pathshalapro.repository.ClassRoomRepository;
import com.pathshalapro.repository.SubjectRepository;
import com.pathshalapro.repository.TimetableRepository;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimetableExcelParser {

    private final TimetableRepository timetableRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    private static final String[] HEADERS = {
        "Class Name*", "Section*", "Subject Code*", "Teacher Email*",
        "Day of Week*", "Start Time* (HH:mm)", "End Time* (HH:mm)",
        "Period Number", "Academic Year*"
    };

    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Timetable");
            createHeaderRow(wb, sheet, HEADERS);
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Class 10");
            sample.createCell(1).setCellValue("A");
            sample.createCell(2).setCellValue("MATH101");
            sample.createCell(3).setCellValue("teacher@demo.com");
            sample.createCell(4).setCellValue("MONDAY");
            sample.createCell(5).setCellValue("08:00");
            sample.createCell(6).setCellValue("08:40");
            sample.createCell(7).setCellValue("1");
            sample.createCell(8).setCellValue("2026-27");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate timetable template", e);
        }
    }

    public ExcelUploadResult parse(InputStream inputStream, School school) {
        ExcelUploadResult result = ExcelUploadResult.builder().module("timetable").build();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String className = getCellString(row, 0);
                    String section = getCellString(row, 1);
                    String subjectCode = getCellString(row, 2);
                    String teacherEmail = getCellString(row, 3);
                    String dayStr = getCellString(row, 4);
                    String startTimeStr = getCellString(row, 5);
                    String endTimeStr = getCellString(row, 6);
                    String periodStr = getCellString(row, 7);
                    String academicYear = getCellString(row, 8);

                    // Required field checks
                    if (className == null || className.isBlank()) { result.addError(i+1, "className", "Class name is required"); continue; }
                    if (section == null || section.isBlank()) { result.addError(i+1, "section", "Section is required"); continue; }
                    if (subjectCode == null || subjectCode.isBlank()) { result.addError(i+1, "subjectCode", "Subject code is required"); continue; }
                    if (teacherEmail == null || teacherEmail.isBlank()) { result.addError(i+1, "teacherEmail", "Teacher email is required"); continue; }
                    if (dayStr == null || dayStr.isBlank()) { result.addError(i+1, "dayOfWeek", "Day of week is required"); continue; }
                    if (startTimeStr == null || startTimeStr.isBlank()) { result.addError(i+1, "startTime", "Start time is required"); continue; }
                    if (endTimeStr == null || endTimeStr.isBlank()) { result.addError(i+1, "endTime", "End time is required"); continue; }
                    if (academicYear == null || academicYear.isBlank()) { result.addError(i+1, "academicYear", "Academic year is required"); continue; }

                    // Lookup classroom
                    var classRoom = classRoomRepository
                            .findByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse(
                                    className.trim(), section.trim(), school.getId(), academicYear.trim())
                            .orElse(null);
                    if (classRoom == null) {
                        result.addError(i+1, "className", "Classroom '" + className + "-" + section + "' not found for " + academicYear);
                        continue;
                    }

                    // Lookup subject
                    var subject = subjectRepository.findByCodeAndSchoolIdAndIsDeletedFalse(subjectCode.trim().toUpperCase(), school.getId())
                            .orElse(null);
                    if (subject == null) {
                        result.addError(i+1, "subjectCode", "Subject with code '" + subjectCode + "' not found");
                        continue;
                    }

                    // Lookup teacher
                    var teacher = userRepository.findByEmailAndIsDeletedFalse(teacherEmail.trim().toLowerCase())
                            .orElse(null);
                    if (teacher == null) {
                        result.addError(i+1, "teacherEmail", "Teacher with email '" + teacherEmail + "' not found");
                        continue;
                    }

                    // Parse day
                    DayOfWeek day;
                    try {
                        day = DayOfWeek.valueOf(dayStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        result.addError(i+1, "dayOfWeek", "Invalid day. Use: MONDAY, TUESDAY, ... SATURDAY");
                        continue;
                    }

                    // Parse times
                    LocalTime startTime, endTime;
                    try {
                        startTime = LocalTime.parse(startTimeStr.trim());
                        endTime = LocalTime.parse(endTimeStr.trim());
                    } catch (Exception e) {
                        result.addError(i+1, "time", "Invalid time format. Use HH:mm (e.g. 08:00)");
                        continue;
                    }

                    // Check for conflicts
                    if (timetableRepository.existsClassRoomConflict(classRoom.getId(), day, startTime, endTime, academicYear.trim(), 0L)) {
                        result.addError(i+1, "slot", "Time slot conflict for classroom '" + className + "-" + section + "' on " + day);
                        continue;
                    }
                    if (timetableRepository.existsTeacherConflict(teacher.getId(), day, startTime, endTime, academicYear.trim(), 0L)) {
                        result.addError(i+1, "slot", "Teacher '" + teacherEmail + "' has a conflict on " + day + " at " + startTimeStr);
                        continue;
                    }

                    Timetable.TimetableBuilder builder = Timetable.builder()
                            .school(school)
                            .classRoom(classRoom)
                            .subject(subject)
                            .teacher(teacher)
                            .dayOfWeek(day)
                            .startTime(startTime)
                            .endTime(endTime)
                            .academicYear(academicYear.trim());

                    if (periodStr != null && !periodStr.isBlank()) {
                        try {
                            builder.periodNumber(Integer.parseInt(periodStr.trim()));
                        } catch (NumberFormatException ignored) {}
                    }

                    timetableRepository.save(builder.build());
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, "general", "Unexpected error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse timetable Excel", e);
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
