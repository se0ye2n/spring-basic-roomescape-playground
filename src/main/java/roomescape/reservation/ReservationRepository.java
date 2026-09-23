package roomescape.reservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"time"})
    List<Reservation> findByDateAndTheme_Id(String date, Long themeId);

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findByMember_IdOrderByIdAsc(Long memberId);
}
