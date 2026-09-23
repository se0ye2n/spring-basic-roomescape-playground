package roomescape.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findByDeletedFalseOrderByIdAsc();

    Optional<Theme> findByIdAndDeletedFalse(Long id);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Theme t set t.deleted = true where t.id = :id")
    void softDeleteById(@Param("id") Long id);
}
