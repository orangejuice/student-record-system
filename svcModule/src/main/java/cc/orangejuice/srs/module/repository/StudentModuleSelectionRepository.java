package cc.orangejuice.srs.module.repository;


import cc.orangejuice.srs.module.domain.ModuleGrade;
import cc.orangejuice.srs.module.domain.StudentModuleSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the StudentModuleSelection entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StudentModuleSelectionRepository extends JpaRepository<StudentModuleSelection, Long> {
    @Modifying
    @Query(value = "update StudentModuleSelection studentModuleSelection SET studentModuleSelection.marks=:marks WHERE studentModuleSelection.id=:id")
    void updateMarksById(@Param("id") Long id, @Param("marks") Double marks);

    @Modifying
    @Query(value = "update StudentModuleSelection studentModuleSelection SET studentModuleSelection.studentModuleGradeType=:studentModuleGradeType, studentModuleSelection.qcs=:qcs, studentModuleSelection.creditHour=:creditHour WHERE studentModuleSelection.id=:id")
    void updateById(@Param("id") Long id, @Param("studentModuleGradeType") ModuleGrade studentModuleGradeType, @Param("qcs") Double qcs, @Param("creditHour") Double creditHour);

    Optional<StudentModuleSelection> findAllByAcademicYearAndYearNoAndSemesterNo(@Param("academic_year") Integer academic_year, @Param("yearNo") Integer yearNo, @Param("semesterNo") Integer semesterNo);

    Optional<StudentModuleSelection> findAllByStudentIdAndAcademicYearAndYearNoAndSemesterNo(@Param("studentId") Long studentId, @Param("academicYear") Integer academicYear, @Param("yearNo") Integer yearNo, @Param("semesterNo") Integer semesterNo);

    Optional<List<StudentModuleSelection>> findAllByStudentIdAndAcademicYearAndYearNo(@Param("studentId") Long studentId, @Param("academicYear") Integer academicYear, @Param("yearNo") Integer yearNo);

}
