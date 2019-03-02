package cc.orangejuice.srs.programme.repository;

import cc.orangejuice.srs.programme.domain.ProgrammeProp;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Spring Data  repository for the ProgrammeProp entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProgrammePropRepository extends JpaRepository<ProgrammeProp, Long> {
    Optional<ProgrammeProp> findOneByTypeAndForEnrollYearAndForYearNoAndKey(
        @Param("type") String type,
        @Param("forEnrollYear") Integer forEnrollYear,
        @Param("forYearNo") Integer forYearNo,
        @Param("key") String key);

    Optional<ProgrammeProp> findOneByTypeAndForEnrollYearAndForSemesterNoAndKey(
        @Param("type") String type,
        @Param("forEnrollYear") Integer forEnrollYear,
        @Param("forSemesterNo") Integer forSemesterNo,
        @Param("key") String key);
}