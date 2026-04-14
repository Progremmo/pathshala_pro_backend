package com.pathshalapro.service.impl;

import com.pathshalapro.dto.notes.NotesRequest;
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
public class NotesServiceImpl {

    private final NotesRepository notesRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;

    @Transactional
    public Notes createNotes(Long schoolId, NotesRequest request, User uploadedBy) {
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

        return notesRepository.save(notes);
    }

    @Transactional(readOnly = true)
    public Page<Notes> getNotesBySchool(Long schoolId, Pageable pageable) {
        return notesRepository.findBySchoolIdAndIsDeletedFalse(schoolId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Notes> getNotesBySubject(Long subjectId, Long schoolId, Pageable pageable) {
        return notesRepository.findBySubjectIdAndSchoolIdAndIsDeletedFalse(subjectId, schoolId, pageable);
    }

    @Transactional
    public Notes updateNotes(Long noteId, Long schoolId, NotesRequest request) {
        Notes notes = notesRepository.findById(noteId)
                .filter(n -> n.getSchool().getId().equals(schoolId) && !n.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Notes not found."));

        notes.setTitle(request.getTitle());
        notes.setDescription(request.getDescription());
        notes.setContentUrl(request.getContentUrl());
        notes.setContentType(request.getContentType());
        notes.setGrade(request.getGrade());
        notes.setVisible(request.isVisible());

        return notesRepository.save(notes);
    }

    @Transactional
    public void deleteNotes(Long noteId, Long schoolId) {
        Notes notes = notesRepository.findById(noteId)
                .filter(n -> n.getSchool().getId().equals(schoolId) && !n.isDeleted())
                .orElseThrow(() -> ApiException.notFound("Notes not found."));
        notes.setDeleted(true);
        notesRepository.save(notes);
    }
}
