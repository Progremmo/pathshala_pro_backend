package com.pathshalapro.service.bulkupload;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.Role;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.SchoolSubscription;
import com.pathshalapro.entity.User;
import com.pathshalapro.entity.enums.RoleName;
import com.pathshalapro.entity.enums.SubscriptionStatus;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses Excel files for bulk school + admin creation.
 * Each row creates a School and its SCHOOL_ADMIN user.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchoolExcelParser {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SchoolSubscriptionRepository subscriptionRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] HEADERS = {
        "School Name*", "School Code*", "Address", "City*", "State", "Pincode",
        "School Phone", "School Email*",  "Website",
        "Admin First Name*", "Admin Last Name*", "Admin Email*", "Admin Phone"
    };

    public byte[] generateTemplate() {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Schools");
            createHeaderRow(wb, sheet, HEADERS);

            // Sample row 1
            Row s1 = sheet.createRow(1);
            s1.createCell(0).setCellValue("Delhi Public School");
            s1.createCell(1).setCellValue("DPS001");
            s1.createCell(2).setCellValue("Sector 24, Mathura Road");
            s1.createCell(3).setCellValue("New Delhi");
            s1.createCell(4).setCellValue("Delhi");
            s1.createCell(5).setCellValue("110044");
            s1.createCell(6).setCellValue("+91-11-26831234");
            s1.createCell(7).setCellValue("info@dps.edu.in");
            s1.createCell(8).setCellValue("https://dps.edu.in");
            s1.createCell(9).setCellValue("Rajesh");
            s1.createCell(10).setCellValue("Sharma");
            s1.createCell(11).setCellValue("rajesh.sharma@dps.edu.in");
            s1.createCell(12).setCellValue("+91-9876543210");

            // Sample row 2
            Row s2 = sheet.createRow(2);
            s2.createCell(0).setCellValue("Modern Academy");
            s2.createCell(1).setCellValue("MOD002");
            s2.createCell(2).setCellValue("Anna Nagar, Chennai");
            s2.createCell(3).setCellValue("Chennai");
            s2.createCell(4).setCellValue("Tamil Nadu");
            s2.createCell(5).setCellValue("600040");
            s2.createCell(6).setCellValue("+91-44-28201234");
            s2.createCell(7).setCellValue("info@modernacademy.in");
            s2.createCell(8).setCellValue("");
            s2.createCell(9).setCellValue("Priya");
            s2.createCell(10).setCellValue("Nair");
            s2.createCell(11).setCellValue("priya.nair@modernacademy.in");
            s2.createCell(12).setCellValue("+91-9988776655");

            // Instructions sheet
            Sheet instrSheet = wb.createSheet("Instructions");
            int r = 0;
            instrSheet.createRow(r++).createCell(0).setCellValue("MANDATORY FIELDS (marked with *):");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - School Name");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - School Code (unique identifier, e.g. 'DPS001')");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - City");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - School Email (must be unique)");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Admin First Name");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Admin Last Name");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Admin Email (must be unique, used for login)");
            instrSheet.createRow(r++).createCell(0).setCellValue("");
            instrSheet.createRow(r++).createCell(0).setCellValue("OPTIONAL FIELDS:");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Address, State, Pincode, School Phone, Website, Admin Phone");
            instrSheet.createRow(r++).createCell(0).setCellValue("");
            instrSheet.createRow(r++).createCell(0).setCellValue("NOTES:");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Each row creates a School AND its School Administrator user");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Default password for admin: Welcome@123");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Each school gets a 30-day TRIAL subscription automatically");
            instrSheet.createRow(r++).createCell(0).setCellValue("  - Duplicate school codes or emails will be rejected");
            instrSheet.createRow(r).createCell(0).setCellValue("  - Delete the sample rows before uploading your data");
            instrSheet.setColumnWidth(0, 20000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate school template", e);
        }
    }

    public ExcelUploadResult parse(InputStream inputStream) {
        ExcelUploadResult result = ExcelUploadResult.builder().module("schools").build();

        try (Workbook wb = WorkbookFactory.create(inputStream)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            result.setTotalRows(totalRows);

            Role adminRole = roleRepository.findByName(RoleName.SCHOOL_ADMIN)
                    .orElseThrow(() -> new RuntimeException("SCHOOL_ADMIN role not found"));

            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String schoolName = getCellString(row, 0);
                    String schoolCode = getCellString(row, 1);
                    String address = getCellString(row, 2);
                    String city = getCellString(row, 3);
                    String state = getCellString(row, 4);
                    String pincode = getCellString(row, 5);
                    String schoolPhone = getCellString(row, 6);
                    String schoolEmail = getCellString(row, 7);
                    String website = getCellString(row, 8);
                    String adminFirstName = getCellString(row, 9);
                    String adminLastName = getCellString(row, 10);
                    String adminEmail = getCellString(row, 11);
                    String adminPhone = getCellString(row, 12);

                    // === School validations ===
                    if (schoolName == null || schoolName.isBlank()) {
                        result.addError(i + 1, "schoolName", "School name is required");
                        continue;
                    }
                    if (schoolCode == null || schoolCode.isBlank()) {
                        result.addError(i + 1, "schoolCode", "School code is required");
                        continue;
                    }
                    if (city == null || city.isBlank()) {
                        result.addError(i + 1, "city", "City is required");
                        continue;
                    }
                    if (schoolEmail == null || schoolEmail.isBlank()) {
                        result.addError(i + 1, "schoolEmail", "School email is required");
                        continue;
                    }
                    if (schoolRepository.existsByCode(schoolCode.trim())) {
                        result.addError(i + 1, "schoolCode", "School code '" + schoolCode + "' already exists");
                        continue;
                    }
                    if (schoolRepository.existsByEmail(schoolEmail.trim().toLowerCase())) {
                        result.addError(i + 1, "schoolEmail", "School email '" + schoolEmail + "' already exists");
                        continue;
                    }

                    // === Admin validations ===
                    if (adminFirstName == null || adminFirstName.isBlank()) {
                        result.addError(i + 1, "adminFirstName", "Admin first name is required");
                        continue;
                    }
                    if (adminLastName == null || adminLastName.isBlank()) {
                        result.addError(i + 1, "adminLastName", "Admin last name is required");
                        continue;
                    }
                    if (adminEmail == null || adminEmail.isBlank()) {
                        result.addError(i + 1, "adminEmail", "Admin email is required");
                        continue;
                    }
                    if (userRepository.existsByEmail(adminEmail.trim().toLowerCase())) {
                        result.addError(i + 1, "adminEmail", "Admin email '" + adminEmail + "' already exists");
                        continue;
                    }

                    // === Create School ===
                    School school = School.builder()
                            .name(schoolName.trim())
                            .code(schoolCode.trim())
                            .address(address)
                            .city(city.trim())
                            .state(state)
                            .pincode(pincode)
                            .phone(schoolPhone)
                            .email(schoolEmail.trim().toLowerCase())
                            .website(website)
                            .isActive(true)
                            .subscriptionStatus(SubscriptionStatus.TRIAL)
                            .build();

                    School savedSchool = schoolRepository.save(school);

                    // Auto-assign TRIAL subscription
                    planRepository.findByIsActiveTrueAndIsDeletedFalse().stream().findFirst()
                            .ifPresent(plan -> {
                                SchoolSubscription trial = SchoolSubscription.builder()
                                        .school(savedSchool)
                                        .plan(plan)
                                        .status(SubscriptionStatus.TRIAL)
                                        .startDate(LocalDate.now())
                                        .endDate(LocalDate.now().plusDays(30))
                                        .trialEndDate(LocalDate.now().plusDays(30))
                                        .billingCycle("MONTHLY")
                                        .autoRenew(false)
                                        .build();
                                subscriptionRepository.save(trial);
                            });

                    // === Create SCHOOL_ADMIN User ===
                    User admin = User.builder()
                            .firstName(adminFirstName.trim())
                            .lastName(adminLastName.trim())
                            .email(adminEmail.trim().toLowerCase())
                            .password(passwordEncoder.encode("Welcome@123"))
                            .phone(adminPhone)
                            .school(savedSchool)
                            .isActive(true)
                            .isEmailVerified(false)
                            .roles(new ArrayList<>(List.of(adminRole)))
                            .build();

                    userRepository.save(admin);
                    result.incrementSuccess();

                    log.info("Created school '{}' [{}] with admin '{}'",
                            schoolName, schoolCode, adminEmail);

                } catch (Exception e) {
                    result.addError(i + 1, "general", "Unexpected error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse school Excel", e);
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
