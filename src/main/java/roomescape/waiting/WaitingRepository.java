package roomescape.waiting;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByMember_IdAndDateAndTheme_IdAndTime_Id(
            Long memberId,
            String date,
            Long themeId,
            Long timeId
    );

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Waiting> findByMember_IdOrderByIdAsc(Long memberId);

    @Query("""
            select count(w)
            from Waiting w
            where w.date = :date
              and w.theme.id = :themeId
              and w.time.id = :timeId
              and w.id < :waitingId
            """)
    long countAhead(
            @Param("date") String date,
            @Param("themeId") Long themeId,
            @Param("timeId") Long timeId,
            @Param("waitingId") Long waitingId
    );
}
