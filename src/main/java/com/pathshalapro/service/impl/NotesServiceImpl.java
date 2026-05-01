package com.pathshalapro.service.impl;

import com.pathshalapro.dto.notes.NotesRequest;
import com.pathshalapro.dto.notes.NotesResponse;
import com.pathshalapro.entity.*;
import com.pathshalapro.exception.ApiException;
import com.pathshalapro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class NotesServiceImpl {

    private final NotesRepository notesRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public NotesResponse createNotes(Long schoolId, NotesRequest request, User uploadedBy) {
        School school = schoolRepository.findByIdAndIsDeletedFalse(schoolId)
                .orElseThrow(() -> ApiException.notFound("School not found."));

        Subject subject = subjectRepository.findByIdAndSchoolIdAndIsDeletedFalse(request.getSubjectId(), schoolId)
                .orElseThrow(() -> ApiException.notFound("Subject not found."));

        Notes notes = Notes.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .contentUrl(request.getContentUrl())
                .contentType(request.getContentType())
                .grade(request.getGrade())
                .academicYear(request.getAcademicYear())
                .isVisible(request.isVisible())
                .school(school)
                .subject(subject)
                .uploadedBy(uploadedBy)
                .build();

        return mapToResponse(notesRepository.save(notes));
    }

    @Transactional(readOnly = true)
    public Page<NotesResponse> getNotesBySchool(Long schoolId, Pageable pageable) {
        return notesRepository.findBySchoolIdAndIsDeletedFalse(schoolId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotesResponse> getNotesBySubject(Long subjectId, Long schoolId, Pageable pageable) {
        return notesRepository.findBySubjectIdAndSchoolIdAndIsDeletedFalse(subjectId, schoolId, pageable).map(this::mapToResponse);
    }

    @Transactional
    public NotesResponse updateNotes(Long noteId, Long schoolId, NotesRequest request) {
        Notes notes = notesRepository.findById(noteId)
                .filter(n -> n.getSchool().getId().equals(schoolId) && !n.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Notes not found."));

        notes.setTitle(request.getTitle());
        notes.setDescription(request.getDescription());
        notes.setContentUrl(request.getContentUrl());
        notes.setContentType(request.getContentType());
        notes.setGrade(request.getGrade());
        notes.setVisible(request.isVisible());

        return mapToResponse(notesRepository.save(notes));
    }

    @Transactional
    public void deleteNotes(Long noteId, Long schoolId) {
        Notes notes = notesRepository.findById(noteId)
                .filter(n -> n.getSchool().getId().equals(schoolId) && !n.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Notes not found."));
        notes.setDeleted(true);
        notesRepository.save(notes);
    }

    private NotesResponse mapToResponse(Notes n) {
        return NotesResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .description(n.getDescription())
                .contentUrl(n.getContentUrl())
                .contentType(n.getContentType())
                .grade(n.getGrade())
                .academicYear(n.getAcademicYear())
                .isVisible(n.isVisible())
                .subjectId(n.getSubject().getId())
                .subjectName(n.getSubject().getName())
                .uploadedById(n.getUploadedBy().getId())
                .uploadedByName(n.getUploadedBy().getFirstName() + " " + n.getUploadedBy().getLastName())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
