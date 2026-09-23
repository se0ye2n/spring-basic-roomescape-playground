package roomescape;

import roomescape.member.AdminOnly;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @AdminOnly
    @GetMapping("/admin")
    public String admin() {
        return "admin/index";
    }

    @AdminOnly
    @GetMapping("/admin/reservation")
    public String adminReservation() {
        return "admin/reservation";
    }

    @AdminOnly
    @GetMapping("/admin/theme")
    public String adminTheme() {
        return "admin/theme";
    }

    @AdminOnly
    @GetMapping("/admin/time")
    public String adminTime() {
        return "admin/time";
    }

    @GetMapping("/")
    public String reservation() {
        return "reservation";
    }

    @GetMapping("/reservation-mine")
    public String myReservation() {
        return "reservation-mine";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }
}
