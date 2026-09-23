package roomescape.time;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TimeRepository extends JpaRepository<Time, Long> {

    List<Time> findByDeletedFalseOrderByIdAsc();

    Optional<Time> findByIdAndDeletedFalse(Long id);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Time t set t.deleted = true where t.id = :id")
    void softDeleteById(@Param("id") Long id);
}