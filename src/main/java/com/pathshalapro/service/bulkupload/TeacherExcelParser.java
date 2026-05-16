package com.pathshalapro.service.bulkupload;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.Role;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.User;
import com.pathshalapro.entity.enums.RoleName;
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
public class TeacherExcelParser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] HEADERS = {
        "First Name*", "Last Name*", "Email*", "Phone", "Gender",
        "Qualification", "Employee ID", "Joining Date (YYYY-MM-DD)"
    };

    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Teachers");
            createHeaderRow(wb, sheet, HEADERS);
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("Priya");
            sample.createCell(1).setCellValue("Sharma");
            sample.createCell(2).setCellValue("priya.sharma@school.com");
            sample.createCell(3).setCellValue("+91-9876543211");
            sample.createCell(4).setCellValue("Female");
            sample.createCell(5).setCellValue("M.Sc. Mathematics");
            sample.createCell(6).setCellValue("EMP001");
            sample.createCell(7).setCellValue("2024-04-01");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate teacher template", e);
        }
    }

    public ExcelUploadResult parse(InputStream inputStream, School school) {
        ExcelUploadResult result = ExcelUploadResult.builder().module("teachers").build();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);

            Role teacherRole = roleRepository.findByName(RoleName.TEACHER)
                    .orElseThrow(() -> new RuntimeException("TEACHER role not found"));

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String firstName = getCellString(row, 0);
                    String lastName = getCellString(row, 1);
                    String email = getCellString(row, 2);
                    String phone = getCellString(row, 3);
                    String gender = getCellString(row, 4);
                    String qualification = getCellString(row, 5);
                    String employeeId = getCellString(row, 6);
                    String joiningDate = getCellString(row, 7);

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
                    if (userRepository.existsByEmail(email.trim().toLowerCase())) {
                        result.addError(i + 1, "email", "Email '" + email + "' already exists");
                        continue;
                    }

                    User.UserBuilder builder = User.builder()
                            .firstName(firstName.trim())
                            .lastName(lastName.trim())
                            .email(email.trim().toLowerCase())
                            .password(passwordEncoder.encode("Welcome@123"))
                            .phone(phone)
                            .gender(gender)
                            .qualification(qualification)
                            .employeeId(employeeId)
                            .isActive(true)
                            .isEmailVerified(false)
                            .school(school)
                            .roles(List.of(teacherRole));

                    if (joiningDate != null && !joiningDate.isBlank()) {
                        try {
                            builder.joiningDate(java.time.LocalDate.parse(joiningDate.trim()));
                        } catch (Exception e) {
                            result.addError(i + 1, "joiningDate", "Invalid date format. Use YYYY-MM-DD");
                            continue;
                        }
                    }

                    userRepository.save(builder.build());
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.addError(i + 1, "general", "Unexpected error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse teacher Excel", e);
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
