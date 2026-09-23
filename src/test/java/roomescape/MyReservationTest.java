package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
)
class MyReservationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long myReservationId;

    @BeforeEach
    void setUp() {
        Member me = memberRepository.save(
                new Member(
                        "테스터", "mine-test@email.com", "password", "USER"
                )
        );

        Member other = memberRepository.save(
                new Member(
                        "테스터", "other-test@email.com", "password", "USER"
                )
        );

        Time time = timeRepository.save(new Time("09:30"));
        Theme theme = themeRepository.save(
                new Theme("조회 테스트 테마", "내 예약 조회 테스트")
        );

        myReservationId = reservationRepository.save(
                new Reservation(me, "2026-10-01", time, theme)
        ).getId();

        reservationRepository.save(
                new Reservation(other, "2026-10-02", time, theme)
        );
    }

    @Test
    void 이름이_같아도_로그인한_회원의_예약만_조회한다() {
        String token = login("mine-test@email.com");

        var response = RestAssured.given()
                .port(port)
                .cookie("token", token)
                .when()
                .get("/reservations-mine")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract();

        List<Long> ids = response.jsonPath()
                .getList("reservationId", Long.class);

        assertThat(ids).containsExactly(myReservationId);
        assertThat(response.jsonPath().getString("[0].theme"))
                .isEqualTo("조회 테스트 테마");
        assertThat(response.jsonPath().getString("[0].date"))
                .isEqualTo("2026-10-01");
        assertThat(response.jsonPath().getString("[0].time"))
                .isEqualTo("09:30");
        assertThat(response.jsonPath().getString("[0].status"))
                .isEqualTo("예약");
    }

    @Test
    void 예약이_없는_회원은_빈_목록을_받는다() {
        memberRepository.save(
                new Member(
                        "예약 없음",
                        "empty-test@email.com",
                        "password",
                        "USER"
                )
        );

        String token = login("empty-test@email.com");

        var response = RestAssured.given()
                .port(port)
                .cookie("token", token)
                .when()
                .get("/reservations-mine")
                .then()
                .statusCode(200)
                .extract();

        assertThat(response.jsonPath().getList("$")).isEmpty();
    }

    @Test
    void 로그인하지_않으면_조회할_수_없다() {
        RestAssured.given()
                .port(port)
                .when()
                .get("/reservations-mine")
                .then()
                .statusCode(401);
    }

    @Test
    void 유효하지_않은_토큰으로_조회할_수_없다() {
        RestAssured.given()
                .port(port)
                .cookie("token", "invalid-token")
                .when()
                .get("/reservations-mine")
                .then()
                .statusCode(401);
    }

    private String login(String email) {
        return RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", "password"
                ))
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .cookie("token");
    }
}
