package com.pathshalapro.service.impl;

import com.pathshalapro.dto.exam.ExamRequest;
import com.pathshalapro.dto.exam.ExamResponse;
import com.pathshalapro.dto.exam.MarksEntryRequest;
import com.pathshalapro.dto.exam.MarksResponse;
import com.pathshalapro.entity.*;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Exam and Marks service - handles exam creation, marks entry, and result generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ExamServiceImpl {

    private final ExamRepository examRepository;
    private final MarksRepository marksRepository;
    private final ClassRoomRepository classRoomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public ExamResponse createExam(Long schoolId, ExamRequest request) {
        if (request.getPassingMarks() > request.getTotalMarks()) {
            throw ApiException.badRequest("Passing marks cannot exceed total marks.");
        }

        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        ClassRoom classRoom = classRoomRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getClassRoomId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Classroom not found."));

        Subject subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getSubjectId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Subject not found."));

        Exam exam = Exam.builder()
                .name(request.getName())
                .examType(request.getExamType())
                .examDate(request.getExamDate())
                .startTime(request.getStartTime())
                .durationMinutes(request.getDurationMinutes())
                .totalMarks(request.getTotalMarks())
                .passingMarks(request.getPassingMarks())
                .academicYear(request.getAcademicYear())
                .instructions(request.getInstructions())
                .isResultPublished(false)
                .school(school)
                .classRoom(classRoom)
                .subject(subject)
                .build();

        return mapToResponse(examRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public Page<ExamResponse> getExamsBySchool(Long schoolId, Pageable pageable) {
        return examRepository.findBySchoolIdAndIsDeletedFalse(schoolId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public ExamResponse updateExam(Long schoolId, Long examId, ExamRequest request) {
        Exam exam = examRepository.findById(examId)
                .filter(e -> e.getSchool().getId().equals(schoolId) && !e.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Exam not found."));

        if (exam.isResultPublished()) {
            throw ApiException.badRequest("Cannot update an exam whose results are already published.");
        }

        if (request.getPassingMarks() > request.getTotalMarks()) {
            throw ApiException.badRequest("Passing marks cannot exceed total marks.");
        }

        ClassRoom classRoom = classRoomRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getClassRoomId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Classroom not found."));

        Subject subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(
                request.getSubjectId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Subject not found."));

        exam.setName(request.getName());
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setTotalMarks(request.getTotalMarks());
        exam.setPassingMarks(request.getPassingMarks());
        exam.setAcademicYear(request.getAcademicYear());
        exam.setInstructions(request.getInstructions());
        exam.setClassRoom(classRoom);
        exam.setSubject(subject);

        return mapToResponse(examRepository.save(exam));
    }

    @Transactional
    public void deleteExam(Long schoolId, Long examId) {
        Exam exam = examRepository.findById(examId)
                .filter(e -> e.getSchool().getId().equals(schoolId) && !e.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Exam not found."));

        if (exam.isResultPublished()) {
            throw ApiException.badRequest("Cannot delete an exam whose results are already published.");
        }

        exam.setDeleted(true);
        examRepository.save(exam);
    }

    @Transactional
    public MarksResponse enterMarks(Long schoolId, MarksEntryRequest request, User enteredBy) {
        Exam exam = examRepository.findById(request.getExamId())
                .filter(e -> e.getSchool().getId().equals(schoolId) && !e.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Exam not found."));

        if (exam.isResultPublished()) {
            throw ApiException.badRequest("Results are already published. Cannot modify marks.");
        }

        User student = userRepository.findByIdAndIsDeletedFalse(request.getStudentId())
                .orElseThrow(() -> ApiException.notFound("Student not found."));

        // Validate marks
        if (!request.isAbsent() && request.getMarksObtained() != null
                && request.getMarksObtained() > exam.getTotalMarks()) {
            throw ApiException.badRequest(
                    String.format("Marks obtained (%.1f) cannot exceed total marks (%.1f).",
                            request.getMarksObtained(), exam.getTotalMarks()));
        }

        // Create or update marks record
        Marks marks = marksRepository.findByExamIdAndStudentIdAndIsDeletedFalse(exam.getId(), student.getId())
                .orElse(Marks.builder()
                        .exam(exam)
                        .student(student)
                        .school(exam.getSchool())
                        .enteredBy(enteredBy)
                        .build());

        marks.setMarksObtained(request.isAbsent() ? 0.0 : request.getMarksObtained());
        marks.setAbsent(request.isAbsent());
        marks.setGrade(request.isAbsent() ? "AB" : calculateGrade(request.getMarksObtained(), exam.getTotalMarks()));
        marks.setRemarks(request.getRemarks());
        marks.setEnteredBy(enteredBy);

        return mapToMarksResponse(marksRepository.save(marks));
    }

    @Transactional
    public ExamResponse publishResults(Long schoolId, Long examId) {
        Exam exam = examRepository.findById(examId)
                .filter(e -> e.getSchool().getId().equals(schoolId) && !e.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Exam not found."));

        long totalStudents = classRoomRepository.countStudentsByClassRoomId(exam.getClassRoom().getId());
        long marksEntered = marksRepository.findByExamIdAndIsDeletedFalse(examId).size();

        if (marksEntered < totalStudents) {
            log.warn("Publishing results with {}/{} marks entered for exam {}", marksEntered, totalStudents, examId);
        }

        exam.setResultPublished(true);
        return mapToResponse(examRepository.save(exam));
    }

    @Transactional(readOnly = true)
    public List<MarksResponse> getStudentResults(Long studentId, Long classRoomId, String academicYear) {
        return marksRepository.findStudentResultsByClassAndYear(studentId, classRoomId, academicYear)
                .stream().map(this::mapToMarksResponse).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getExamStatistics(Long examId) {
        List<Marks> allMarks = marksRepository.findByExamIdAndIsDeletedFalse(examId);
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> ApiException.notFound("Exam not found."));

        long total = allMarks.size();
        long passed = allMarks.stream()
                .filter(m -> !m.isAbsent() && m.getMarksObtained() >= exam.getPassingMarks())
                .count();
        long absent = allMarks.stream().filter(Marks::isAbsent).count();

        Double average = marksRepository.getAverageMarksByExam(examId);

        return Map.of(
                "totalStudents", total,
                "passed", passed,
                "failed", total - passed - absent,
                "absent", absent,
                "average", average != null ? average : 0.0,
                "passPercentage", total > 0 ? (double) passed / (total - absent) * 100 : 0.0
        );
    }

    private String calculateGrade(Double marks, Double totalMarks) {
        if (marks == null || totalMarks == null) return "NG";
        double percentage = (marks / totalMarks) * 100;
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B+";
        if (percentage >= 60) return "B";
        if (percentage >= 50) return "C";
        if (percentage >= 40) return "D";
        return "F";
    }

    private ExamResponse mapToResponse(Exam exam) {
        return ExamResponse.builder()
                .id(exam.getId())
                .name(exam.getName())
                .type(exam.getExamType())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .durationMinutes(exam.getDurationMinutes())
                .totalMarks(exam.getTotalMarks())
                .passingMarks(exam.getPassingMarks())
                .academicYear(exam.getAcademicYear())
                .isPublished(exam.isResultPublished())
                .classRoomId(exam.getClassRoom().getId())
                .classRoomName(exam.getClassRoom().getName())
                .subjectId(exam.getSubject().getId())
                .subjectName(exam.getSubject().getName())
                .build();
    }

    private MarksResponse mapToMarksResponse(Marks m) {
        return MarksResponse.builder()
                .id(m.getId())
                .studentId(m.getStudent().getId())
                .studentName(m.getStudent().getFirstName() + " " + m.getStudent().getLastName())
                .examId(m.getExam().getId())
                .examName(m.getExam().getName())
                .examTitle(m.getExam().getName())
                .subjectName(m.getExam().getSubject().getName())
                .examType(m.getExam().getExamType() != null ? m.getExam().getExamType().name() : null)
                .academicYear(m.getExam().getAcademicYear())
                .examDate(m.getExam().getExamDate())
                .marksObtained(m.getMarksObtained())
                .maxMarks(m.getExam().getTotalMarks())
                .grade(m.getGrade() != null ? m.getGrade() : calculateGrade(m.getMarksObtained(), m.getExam().getTotalMarks()))
                .remarks(m.getRemarks())
                .isAbsent(m.isAbsent())
                .build();
    }
}
