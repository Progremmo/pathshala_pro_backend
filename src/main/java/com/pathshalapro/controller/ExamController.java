package com.pathshalapro.controller;

import com.pathshalapro.dto.ApiResponse;
import com.pathshalapro.dto.exam.ExamRequest;
import com.pathshalapro.dto.exam.ExamResponse;
import com.pathshalapro.dto.exam.MarksEntryRequest;
import com.pathshalapro.entity.Marks;
import com.pathshalapro.security.SecurityUtils;
import com.pathshalapro.service.impl.ExamServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Exam creation, marks entry, and result publishing controller.
 */
@RestController
@RequestMapping("/schools/{schoolId}/exams")
@RequiredArgsConstructor
@Tag(name = "Exam Management", description = "Create exams, enter marks, and publish results")
public class ExamController {

    private final ExamServiceImpl examService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Create exam")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @PathVariable Long schoolId,
            @Valid @RequestBody ExamRequest request) {
        ExamResponse exam = examService.createExam(schoolId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(exam, "Exam created."));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get all exams for a school")
    public ResponseEntity<ApiResponse<Page<ExamResponse>>> getExams(
            @PathVariable Long schoolId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "examDate"));
        return ResponseEntity.ok(ApiResponse.success(examService.getExamsBySchool(schoolId, pageable)));
    }

    @PostMapping("/{examId}/marks")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Enter marks for a student")
    public ResponseEntity<ApiResponse<Marks>> enterMarks(
            @PathVariable Long schoolId,
            @PathVariable Long examId,
            @Valid @RequestBody MarksEntryRequest request) {
        request.setExamId(examId);
        Marks marks = examService.enterMarks(schoolId, request, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success(marks, "Marks entered successfully."));
    }

    @PatchMapping("/{examId}/publish")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN')")
    @Operation(summary = "Publish exam results")
    public ResponseEntity<ApiResponse<ExamResponse>> publishResults(
            @PathVariable Long schoolId,
            @PathVariable Long examId) {
        ExamResponse exam = examService.publishResults(schoolId, examId);
        return ResponseEntity.ok(ApiResponse.success(exam, "Results published successfully."));
    }

    @GetMapping("/student/{studentId}/results")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    @Operation(summary = "Get student exam results")
    public ResponseEntity<ApiResponse<List<Marks>>> getStudentResults(
            @PathVariable Long schoolId,
            @PathVariable Long studentId,
            @RequestParam Long classRoomId,
            @RequestParam String academicYear) {
        return ResponseEntity.ok(ApiResponse.success(examService.getStudentResults(studentId, classRoomId, academicYear)));
    }

    @GetMapping("/{examId}/statistics")
    @PreAuthorize("hasAnyRole('PROJECT_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    @Operation(summary = "Get exam statistics (pass rate, average, etc.)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @PathVariable Long schoolId,
            @PathVariable Long examId) {
        return ResponseEntity.ok(ApiResponse.success(examService.getExamStatistics(examId)));
    }
}
