package com.pathshalapro.service.impl;

import com.pathshalapro.dto.school.SubjectRequest;
import com.pathshalapro.dto.school.SubjectResponse;
import com.pathshalapro.entity.School;
import com.pathshalapro.entity.Subject;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.SchoolRepository;
import com.pathshalapro.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class SubjectServiceImpl {

    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;

    public List<SubjectResponse> getSubjects(Long schoolId) {
        return subjectRepository.findBySchoolIdAndIsDeletedFalse(schoolId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubjectResponse createSubject(Long schoolId, SubjectRequest request) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found with id: " + schoolId));

        if (subjectRepository.existsByCodeAndSchoolIdAndIsDeletedFalse(request.getCode(), schoolId)) {
            throw ApiException.conflict("Subject code '" + request.getCode() + "' already exists for this school");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .grade(request.getGrade())
                .creditHours(request.getCreditHours())
                .school(school)
                .build();

        return mapToResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse updateSubject(Long schoolId, Long subjectId, SubjectRequest request) {
        Subject subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(subjectId, schoolId)
                .orElseThrow(() -> ApiException.notFound("Subject not found with id: " + subjectId));

        if (!subject.getCode().equals(request.getCode()) &&
            subjectRepository.existsByCodeAndSchoolIdAndIsDeletedFalse(request.getCode(), schoolId)) {
            throw ApiException.conflict("Subject code '" + request.getCode() + "' already exists for this school");
        }

        subject.setName(request.getName());
        subject.setCode(request.getCode());
        subject.setDescription(request.getDescription());
        subject.setGrade(request.getGrade());
        subject.setCreditHours(request.getCreditHours());

        return mapToResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void deleteSubject(Long schoolId, Long subjectId) {
        Subject subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(subjectId, schoolId)
                .orElseThrow(() -> ApiException.notFound("Subject not found with id: " + subjectId));

        subject.setDeleted(true);
        subjectRepository.save(subject);
    }

    private SubjectResponse mapToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .description(subject.getDescription())
                .grade(subject.getGrade())
                .creditHours(subject.getCreditHours())
                .schoolId(subject.getSchool().getId())
                .build();
    }
}
