package roomescape.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.waiting.Waiting;
import roomescape.waiting.WaitingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MyReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public MyReservationService(
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<MyReservationResponse> findMine(Long memberId) {
        List<MyReservationResponse> responses = new ArrayList<>();

        for (Reservation reservation :
                reservationRepository.findByMember_IdOrderByIdAsc(memberId)) {
            responses.add(new MyReservationResponse(
                    reservation.getId(),
                    null,
                    reservation.getTheme().getName(),
                    reservation.getDate(),
                    reservation.getTime().getValue(),
                    "예약"
            ));
        }

        for (Waiting waiting :
                waitingRepository.findByMember_IdOrderByIdAsc(memberId)) {
            long rank = waitingRepository.countAhead(
                    waiting.getDate(),
                    waiting.getTheme().getId(),
                    waiting.getTime().getId(),
                    waiting.getId()
            ) + 1;

            responses.add(new MyReservationResponse(
                    null,
                    waiting.getId(),
                    waiting.getTheme().getName(),
                    waiting.getDate(),
                    waiting.getTime().getValue(),
                    rank + "번째 예약대기"
            ));
        }

        return responses;
    }
}
