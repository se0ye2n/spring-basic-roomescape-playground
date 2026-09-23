package roomescape.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MyReservationService {

    private final ReservationRepository reservationRepository;

    public MyReservationService(
            ReservationRepository reservationRepository
    ) {
        this.reservationRepository = reservationRepository;
    }

    public List<MyReservationResponse> findMine(Long memberId) {
        return reservationRepository
                .findByMember_IdOrderByIdAsc(memberId)
                .stream()
                .map(reservation -> new MyReservationResponse(
                        reservation.getId(),
                        reservation.getTheme().getName(),
                        reservation.getDate(),
                        reservation.getTime().getValue(),
                        "예약"
                ))
                .toList();
    }
}
