package roomescape;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.member.Member;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JpaTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void 사단계() {
        Time time = entityManager.persist(new Time("10:00"));
        entityManager.flush();
        entityManager.clear();

        Time persistTime = timeRepository.findById(time.getId())
                .orElseThrow();

        assertThat(persistTime.getValue()).isEqualTo(time.getValue());
    }

    @Test
    void 예약의_회원_시간_테마를_매핑한다() {
        Member member = entityManager.persist(
                new Member("테스터", "jpa-test@email.com", "password", "USER")
        );
        Time time = entityManager.persist(new Time("11:00"));
        Theme theme = entityManager.persist(
                new Theme("JPA 테마", "연관관계 테스트")
        );

        Reservation reservation = reservationRepository.save(
                new Reservation(member, "2026-10-01", time, theme)
        );

        entityManager.flush();
        entityManager.clear();

        Reservation found = reservationRepository.findById(reservation.getId())
                .orElseThrow();

        assertThat(found.getMember().getEmail())
                .isEqualTo("jpa-test@email.com");
        assertThat(found.getTime().getValue()).isEqualTo("11:00");
        assertThat(found.getTheme().getName()).isEqualTo("JPA 테마");

        assertThat(
                reservationRepository.findByDateAndTheme_Id(
                        "2026-10-01", theme.getId()
                )
        ).extracting(Reservation::getId).contains(reservation.getId());
    }

    @Test
    void 삭제한_시간은_목록에서_숨기고_기존_예약에는_유지한다() {
        Member member = entityManager.persist(
                new Member("테스터", "soft-delete@email.com", "password", "USER")
        );
        Time time = entityManager.persist(new Time("23:00"));
        Theme theme = entityManager.persist(
                new Theme("삭제 테스트", "기존 예약 유지")
        );
        Reservation reservation = entityManager.persist(
                new Reservation(member, "2026-10-02", time, theme)
        );

        entityManager.flush();
        entityManager.clear();

        timeRepository.softDeleteById(time.getId());

        assertThat(timeRepository.findByDeletedFalseOrderByIdAsc())
                .extracting(Time::getId)
                .doesNotContain(time.getId());

        Reservation found = reservationRepository.findById(reservation.getId())
                .orElseThrow();

        assertThat(found.getTime().getValue()).isEqualTo("23:00");
    }
}
