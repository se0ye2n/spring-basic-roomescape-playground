package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {

    @LocalServerPort
    private int port;

    @Test
    void 일단계() {
        String token = createToken("admin@email.com", "password");

        assertThat(token).isNotBlank();

        ExtractableResponse<Response> response =
                RestAssured.given().port(port).log().all()
                        .cookie("token", token)
                        .when().get("/login/check")
                        .then().log().all()
                        .statusCode(200)
                        .extract();

        assertThat(response.jsonPath().getString("name"))
                .isEqualTo("어드민");
    }

    @Test
    void 이단계() {
        String token = createToken("admin@email.com", "password");

        Map<String, String> params = new HashMap<>();
        params.put("date", "2024-03-02");
        params.put("time", "1");
        params.put("theme", "1");

        ExtractableResponse<Response> response =
                RestAssured.given().port(port).log().all()
                        .contentType(ContentType.JSON)
                        .cookie("token", token)
                        .body(params)
                        .when().post("/reservations")
                        .then().log().all()
                        .statusCode(201)
                        .extract();

        assertThat(response.jsonPath().getString("name"))
                .isEqualTo("어드민");

        params.put("memberId", "2");
        params.put("time", "2");

        ExtractableResponse<Response> adminResponse =
                RestAssured.given().port(port).log().all()
                        .contentType(ContentType.JSON)
                        .cookie("token", token)
                        .body(params)
                        .when().post("/reservations")
                        .then().log().all()
                        .statusCode(201)
                        .extract();

        assertThat(adminResponse.jsonPath().getString("name"))
                .isEqualTo("브라운");
    }

    @Test
    void 로그인_없이_예약하면_실패한다() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "date", "2024-03-01",
                        "time", "1",
                        "theme", "1"
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 유효하지_않은_토큰으로_예약하면_실패한다() {
        RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .cookie("token", "invalid-token")
                .body(Map.of(
                        "date", "2024-03-01",
                        "time", "1",
                        "theme", "1"
                ))
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 삼단계() {
        String brownToken = createToken("brown@email.com", "password");
        String adminToken = createToken("admin@email.com", "password");

        String[] adminPaths = {
                "/admin",
                "/admin/reservation",
                "/admin/theme",
                "/admin/time"
        };

        for (String path : adminPaths) {
            RestAssured.given().port(port).log().all()
                    .cookie("token", brownToken)
                    .when().get(path)
                    .then().log().all()
                    .statusCode(403);

            RestAssured.given().port(port).log().all()
                    .cookie("token", adminToken)
                    .when().get(path)
                    .then().log().all()
                    .statusCode(200);
        }
    }

    @Test
    void 로그인_없이_관리자_페이지에_접근하면_실패한다() {
        RestAssured.given().port(port).log().all()
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 유효하지_않은_토큰으로_관리자_페이지에_접근하면_실패한다() {
        RestAssured.given().port(port).log().all()
                .cookie("token", "invalid-token")
                .when().get("/admin")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    void 로그인_없이_일반_페이지에_접근할_수_있다() {
        RestAssured.given().port(port).log().all()
                .when().get("/")
                .then().log().all()
                .statusCode(200);
    }

    private String createToken(String email, String password) {
        return RestAssured.given().port(port).log().all()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "email", email,
                        "password", password
                ))
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("token");
    }

    @ParameterizedTest
    @CsvSource({
            "DELETE, /reservations/1",
            "POST, /themes",
            "DELETE, /themes/1",
            "POST, /times",
            "DELETE, /times/1"
    })
    void 관리자_API는_비로그인과_일반_사용자를_차단한다(
            String method,
            String path
    ) {
        Map<String, String> body = Map.of(
                "name", "새 테마",
                "description", "테마 설명",
                "value", "21:00"
        );

        RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(body)
                .when().request(method, path)
                .then().statusCode(401);

        String token = createToken("brown@email.com", "password");

        RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(body)
                .when().request(method, path)
                .then().statusCode(403);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 동명이인도_회원_ID로_구분하여_예약한다() {
        Long anotherBrownId = RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "name", "브라운",
                        "email", "another-brown@email.com",
                        "password", "password"
                ))
                .when().post("/members")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        String adminToken = createToken("admin@email.com", "password");

        long timeId = 1L;
        for (Long memberId : new Long[]{2L, anotherBrownId}) {
            Long reservationId = RestAssured.given().port(port)
                    .contentType(ContentType.JSON)
                    .cookie("token", adminToken)
                    .body(Map.of(
                            "memberId", memberId,
                            "date", "2024-03-02",
                            "time", timeId++,
                            "theme", 1
                    ))
                    .when().post("/reservations")
                    .then().statusCode(201)
                    .extract().jsonPath().getLong("id");

            Long savedMemberId = jdbcTemplate.queryForObject(
                    "SELECT member_id FROM reservation WHERE id = ?",
                    Long.class,
                    reservationId
            );

            assertThat(savedMemberId).isEqualTo(memberId);
        }
    }

    @Test
    void 일반_회원은_다른_회원으로_예약할_수_없다() {
        String token = createToken("brown@email.com", "password");

        RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(Map.of(
                        "memberId", 1,
                        "date", "2024-03-02",
                        "time", 1,
                        "theme", 1
                ))
                .when().post("/reservations")
                .then().statusCode(403);
    }
}
