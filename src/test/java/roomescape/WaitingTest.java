package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(
        classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD
)
class WaitingTest {

    @LocalServerPort
    private int port;

    @Test
    void 대기를_신청하고_내_목록에서_확인한_뒤_취소한다() {
        String token = login("brown@email.com");

        var created = createWaiting(token, slot(), 201);

        Long waitingId = created.jsonPath().getLong("id");
        assertThat(created.jsonPath().getLong("waitingNumber"))
                .isEqualTo(1L);

        var mine = findMine(token);

        assertThat(mine.jsonPath().getList("waitingId", Long.class))
                .containsExactly(waitingId);
        assertThat(mine.jsonPath().getString("[0].status"))
                .isEqualTo("1번째 예약대기");

        deleteWaiting(token, waitingId, 204);

        assertThat(findMine(token).jsonPath().getList("$")).isEmpty();
    }

    @Test
    void 앞사람이_취소하면_다음_대기자의_순번이_당겨진다() {
        String firstToken = login("brown@email.com");
        String secondToken = signupAndLogin("second@email.com");

        Long firstId = createWaiting(firstToken, slot(), 201)
                .jsonPath().getLong("id");

        var second = createWaiting(secondToken, slot(), 201);

        assertThat(second.jsonPath().getLong("waitingNumber"))
                .isEqualTo(2L);
        assertThat(findMine(secondToken).jsonPath().getString("[0].status"))
                .isEqualTo("2번째 예약대기");

        deleteWaiting(firstToken, firstId, 204);

        assertThat(findMine(secondToken).jsonPath().getString("[0].status"))
                .isEqualTo("1번째 예약대기");
    }

    @Test
    void 다른_슬롯의_대기는_순번에_포함하지_않는다() {
        String firstToken = login("brown@email.com");
        String secondToken = signupAndLogin("second@email.com");

        createWaiting(firstToken, slot(), 201);

        var otherSlot = Map.<String, Object>of(
                "date", "2024-03-01",
                "time", 2,
                "theme", 2
        );

        var response = createWaiting(secondToken, otherSlot, 201);

        assertThat(response.jsonPath().getLong("waitingNumber"))
                .isEqualTo(1L);
    }

    @Test
    void 동일_회원은_같은_시간에_중복_대기할_수_없다() {
        String token = login("brown@email.com");

        createWaiting(token, slot(), 201);
        createWaiting(token, slot(), 400);

        assertThat(findMine(token).jsonPath().getList("$")).hasSize(1);
    }

    @Test
    void 다른_회원의_대기를_취소할_수_없다() {
        String ownerToken = login("brown@email.com");
        String otherToken = signupAndLogin("other@email.com");

        Long waitingId = createWaiting(ownerToken, slot(), 201)
                .jsonPath().getLong("id");

        deleteWaiting(otherToken, waitingId, 403);

        assertThat(findMine(ownerToken).jsonPath().getList("$")).hasSize(1);
        assertThat(findMine(otherToken).jsonPath().getList("$")).isEmpty();
    }

    @Test
    void 본인이_예약한_시간에는_대기할_수_없다() {
        String token = login("admin@email.com");

        createWaiting(token, slot(), 400);
    }

    @Test
    void 예약되지_않은_시간에는_대기할_수_없다() {
        String token = login("brown@email.com");

        createWaiting(token, Map.of(
                "date", "2024-03-02",
                "time", 1,
                "theme", 1
        ), 400);
    }

    @Test
    void 중복_예약은_거절한다() {
        String token = login("brown@email.com");

        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(slot())
                .when()
                .post("/reservations")
                .then()
                .statusCode(400);
    }

    @Test
    void 예약과_대기를_함께_조회한다() {
        String token = login("brown@email.com");

        Long reservationId = RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(Map.of(
                        "date", "2024-03-02",
                        "time", 1,
                        "theme", 1
                ))
                .when()
                .post("/reservations")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath().getLong("id");

        Long waitingId = createWaiting(token, slot(), 201)
                .jsonPath().getLong("id");

        var response = findMine(token);

        assertThat(response.jsonPath().getList("$")).hasSize(2);

        List<Long> reservationIds = response.jsonPath()
                .getList("reservationId", Long.class);
        List<Long> waitingIds = response.jsonPath()
                .getList("waitingId", Long.class);

        assertThat(reservationIds).contains(reservationId);
        assertThat(waitingIds).contains(waitingId);
        assertThat(response.jsonPath().getList("status", String.class))
                .containsExactly("예약", "1번째 예약대기");
    }

    @Test
    void 로그인하지_않으면_신청과_취소를_할_수_없다() {
        String token = login("brown@email.com");
        Long waitingId = createWaiting(token, slot(), 201)
                .jsonPath().getLong("id");

        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(slot())
                .when()
                .post("/waitings")
                .then()
                .statusCode(401);

        RestAssured.given()
                .port(port)
                .when()
                .delete("/waitings/" + waitingId)
                .then()
                .statusCode(401);
    }

    private Map<String, Object> slot() {
        return Map.of(
                "date", "2024-03-01",
                "time", 1,
                "theme", 1
        );
    }

    private ExtractableResponse<Response> createWaiting(
            String token,
            Map<String, Object> body,
            int expectedStatus
    ) {
        return RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(body)
                .when()
                .post("/waitings")
                .then()
                .statusCode(expectedStatus)
                .extract();
    }

    private ExtractableResponse<Response> findMine(String token) {
        return RestAssured.given()
                .port(port)
                .cookie("token", token)
                .when()
                .get("/reservations-mine")
                .then()
                .statusCode(200)
                .extract();
    }

    private void deleteWaiting(
            String token,
            Long waitingId,
            int expectedStatus
    ) {
        RestAssured.given()
                .port(port)
                .cookie("token", token)
                .when()
                .delete("/waitings/" + waitingId)
                .then()
                .statusCode(expectedStatus);
    }

    private String signupAndLogin(String email) {
        RestAssured.given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "대기 회원",
                        "email", email,
                        "password", "password"
                ))
                .when()
                .post("/members")
                .then()
                .statusCode(201);

        return login(email);
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
