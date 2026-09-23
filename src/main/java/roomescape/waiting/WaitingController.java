package roomescape.waiting;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.LoginMember;

import java.net.URI;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> create(
            @RequestBody WaitingRequest request,
            LoginMember loginMember
    ) {
        WaitingResponse response = waitingService.save(
                request, loginMember
        );

        return ResponseEntity
                .created(URI.create("/waitings/" + response.getId()))
                .body(response);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            LoginMember loginMember
    ) {
        waitingService.delete(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
