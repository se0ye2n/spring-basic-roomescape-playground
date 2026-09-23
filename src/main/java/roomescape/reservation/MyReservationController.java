package roomescape.reservation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.LoginMember;

import java.util.List;

@RestController
public class MyReservationController {

    private final MyReservationService myReservationService;

    public MyReservationController(
            MyReservationService myReservationService
    ) {
        this.myReservationService = myReservationService;
    }

    @GetMapping("/reservations-mine")
    public List<MyReservationResponse> findMine(LoginMember loginMember) {
        return myReservationService.findMine(loginMember.getId());
    }
}
