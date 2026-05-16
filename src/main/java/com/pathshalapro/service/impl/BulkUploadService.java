package com.pathshalapro.service.impl;

import com.pathshalapro.dto.bulkupload.ExcelUploadResult;
import com.pathshalapro.entity.School;
import com.pathshalapro.repository.SchoolRepository;
import com.pathshalapro.service.bulkupload.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;

/**
 * Orchestrator service that delegates Excel parsing to the correct module parser.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadService {

    private final SchoolRepository schoolRepository;
    private final StudentExcelParser studentParser;
    private final TeacherExcelParser teacherParser;
    private final SubjectExcelParser subjectParser;
    private final ClassRoomExcelParser classRoomParser;
    private final TimetableExcelParser timetableParser;
    private final FeeStructureExcelParser feeStructureParser;

    /**
     * Generate a downloadable Excel template for the given module.
     */
    public byte[] generateTemplate(String module) {
        return switch (module.toLowerCase()) {
            case "students" -> studentParser.generateTemplate();
            case "teachers" -> teacherParser.generateTemplate();
            case "subjects" -> subjectParser.generateTemplate();
            case "classes" -> classRoomParser.generateTemplate();
            case "timetable" -> timetableParser.generateTemplate();
            case "fees" -> feeStructureParser.generateTemplate();
            default -> throw new IllegalArgumentException("Unknown module: " + module);
        };
    }

    /**
     * Parse and import an Excel file for the given module and school.
     */
    @Transactional
    public ExcelUploadResult processUpload(Long schoolId, String module, InputStream inputStream) {
        if (schoolId == null) {
            throw new IllegalArgumentException("schoolId must not be null");
        }
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with ID: " + schoolId));

        log.info("Processing bulk upload for module '{}' in school '{}'", module, school.getName());

        ExcelUploadResult result = switch (module.toLowerCase()) {
            case "students" -> studentParser.parse(inputStream, school);
            case "teachers" -> teacherParser.parse(inputStream, school);
            case "subjects" -> subjectParser.parse(inputStream, school);
            case "classes" -> classRoomParser.parse(inputStream, school);
            case "timetable" -> timetableParser.parse(inputStream, school);
            case "fees" -> feeStructureParser.parse(inputStream, school);
            default -> throw new IllegalArgumentException("Unknown module: " + module);
        };

        log.info("Bulk upload complete for '{}': {} total, {} success, {} failed",
                module, result.getTotalRows(), result.getSuccessCount(), result.getFailedCount());

        return result;
    }
}
