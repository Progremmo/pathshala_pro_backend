package com.pathshalapro.service.bulkupload;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.ClassRoom;
import com.pathshalapro.entity.Role;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.User;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.repository.ClassRoomRepository;
import com.pathshalapro.repository.RoleRepository;
import com.pathshalapro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentExcelParser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClassRoomRepository classRoomRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] HEADERS = {
        "First Name*", "Last Name*", "Email*", "Phone", "Gender",
        "Date of Birth (YYYY-MM-DD)", "Admission No*", "Class Name*", "Section*", "Parent Email"
    };

    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Students");
            createHeaderRow(wb, sheet, HEADERS);
            // Sample row 1
            Row sample1 = sheet.createRow(1);
            sample1.createCell(0).setCellValue("Amit");
            sample1.createCell(1).setCellValue("Kumar");
            sample1.createCell(2).setCellValue("amit.kumar@school.com");
            sample1.createCell(3).setCellValue("+91-9876543210");
            sample1.createCell(4).setCellValue("Male");
            sample1.createCell(5).setCellValue("2010-05-15");
            sample1.createCell(6).setCellValue("STU2026001");
            sample1.createCell(7).setCellValue("Class 10");
            sample1.createCell(8).setCellValue("A");
            sample1.createCell(9).setCellValue("parent@school.com");
            // Sample row 2
            Row sample2 = sheet.createRow(2);
            sample2.createCell(0).setCellValue("Priya");
            sample2.createCell(1).setCellValue("Sharma");
            sample2.createCell(2).setCellValue("priya.sharma@school.com");
            sample2.createCell(3).setCellValue("+91-9988776655");
            sample2.createCell(4).setCellValue("Female");
            sample2.createCell(5).setCellValue("2011-03-22");
            sample2.createCell(6).setCellValue("STU2026002");
            sample2.createCell(7).setCellValue("Class 9");
            sample2.createCell(8).setCellValue("B");
            sample2.createCell(9).setCellValue("");

            // Instructions sheet
            Sheet instrSheet = wb.createSheet("Instructions");
            int r = 0;
            instrSheet.createRow(r++).createCell(0).setCellValue("MANDATORY FIELDS (marked with *):");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - First Name");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Last Name");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Email (must be unique)");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Admission No (must be unique within the school)");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Class Name (must match an existing classroom, e.g. 'Class 10')");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Section (must match an existing section, e.g. 'A')");
            instrSheet.createRow(r++).createCell(0).setCellValue("");
            instrSheet.createRow(r++).createCell(0).setCellValue("OPTIONAL FIELDS:");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Phone, Gender, Date of Birth (YYYY-MM-DD), Parent Email");
            instrSheet.createRow(r++).createCell(0).setCellValue("");
            instrSheet.createRow(r++).createCell(0).setCellValue("NOTES:");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Default password for all imported students: Welcome@123");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Class Name + Section must match an existing classroom for academic year 2026-27");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Duplicate emails or admission numbers will be rejected");
            instrSheet.createRow(r).createCell(0).setCellValue("  - Delete the sample rows before uploading your data");
            instrSheet.setColumnWidth(0, 20000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate student template", e);
        }
    }

    public ExcelUploadResult parse(InputStream inputStream, School school) {
        ExcelUploadResult result = ExcelUploadResult.builder().module("students").build();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum(); // excluding header
            result.setTotalRows(totalRows);

            Role studentRole = roleRepository.findByName(RoleName.STUDENT)
                    .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String firstName = getCellString(row, 0);
                    String lastName = getCellString(row, 1);
                    String email = getCellString(row, 2);
                    String phone = getCellString(row, 3);
                    String gender = getCellString(row, 4);
                    String dob = getCellString(row, 5);
                    String admissionNo = getCellString(row, 6);
                    String className = getCellString(row, 7);
                    String section = getCellString(row, 8);
                    String parentEmail = getCellString(row, 9);

                    // Validations
                    if (firstName == null || firstName.isBlank()) {
                        result.addError(i + 1, "firstName", "First name is required");
                        continue;
                    }
                    if (lastName == null || lastName.isBlank()) {
                        result.addError(i + 1, "lastName", "Last name is required");
                        continue;
                    }
                    if (email == null || email.isBlank()) {
                        result.addError(i + 1, "email", "Email is required");
                        continue;
                    }
                    if (userRepository.existsByEmail(email)) {
                        result.addError(i + 1, "email", "Email '" + email + "' already exists");
                        continue;
                    }
                    if (admissionNo == null || admissionNo.isBlank()) {
                        result.addError(i + 1, "admissionNo", "Admission number is required");
                        continue;
                    }
                    if (className == null || className.isBlank()) {
                        result.addError(i + 1, "className", "Class name is required");
                        continue;
                    }
                    if (section == null || section.isBlank()) {
                        result.addError(i + 1, "section", "Section is required");
                        continue;
                    }

                    // Check duplicate admission number within same school
                    if (userRepository.findByAdmissionNoAndSchoolIdAndIsDeletedFalse(admissionNo.trim(), school.getId()).isPresent()) {
                        result.addError(i + 1, "admissionNo", "Admission number '" + admissionNo + "' already exists");
                        continue;
                    }

                    // Lookup classroom (mandatory)
                    var classRoomOpt = classRoomRepository
                            .findByNameAndSectionAndSchoolIdAndAcademicYearAndIsDeletedFalse(
                                    className.trim(), section.trim(), school.getId(), "2026-27");
                    if (classRoomOpt.isEmpty()) {
                        result.addError(i + 1, "className", "Classroom '" + className + "-" + section + "' not found for academic year 2026-27");
                        continue;
                    }

                    User.UserBuilder builder = User.builder()
                            .firstName(firstName.trim())
                            .lastName(lastName.trim())
                            .email(email.trim().toLowerCase())
                            .password(passwordEncoder.encode("Welcome@123"))
                            .phone(phone)
                            .gender(gender)
                            .admissionNo(admissionNo.trim())
                            .classRoom(classRoomOpt.get())
                            .isActive(true)
                            .isEmailVerified(false)
                            .school(school)
                            .roles(List.of(studentRole));

                    if (dob != null && !dob.isBlank()) {
                        try {
                            builder.dateOfBirth(java.time.LocalDate.parse(dob.trim()));
                        } catch (Exception e) {
                            result.addError(i + 1, "dateOfBirth", "Invalid date format. Use YYYY-MM-DD");
                            continue;
                        }
                    }

                    // Link parent if provided
                    if (parentEmail != null && !parentEmail.isBlank()) {
                        userRepository.findByEmailAndIsDeletedFalse(parentEmail.trim().toLowerCase())
                                .ifPresent(builder::parent);
                    }

                    userRepository.save(builder.build());
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, "general", "Unexpected error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse student Excel", e);
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
