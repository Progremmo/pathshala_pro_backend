package com.pathshalapro.repository;

import com.pathshalapro.entity.Notes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotesRepository extends JpaRepository<Notes, Long> {

    Page<Notes> findBySchoolIdAndIsDeletedFalse(Long schoolId, Pageable pageable);

    Page<Notes> findBySubjectIdAndSchoolIdAndIsDeletedFalse(Long subjectId, Long schoolId, Pageable pageable);

    Page<Notes> findBySchoolIdAndGradeAndIsVisibleTrueAndIsDeletedFalse(Long schoolId, String grade, Pageable pageable);

    Page<Notes> findByUploadedByIdAndIsDeletedFalse(Long teacherId, Pageable pageable);
}
