package roomescape.time;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TimeService {

    private final TimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public TimeService(
            TimeRepository timeRepository,
            ReservationRepository reservationRepository
    ) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<AvailableTime> getAvailableTime(String date, Long themeId) {
        List<Reservation> reservations =
                reservationRepository.findByDateAndTheme_Id(date, themeId);

        return timeRepository.findByDeletedFalseOrderByIdAsc().stream()
                .map(time -> new AvailableTime(
                        time.getId(),
                        time.getValue(),
                        reservations.stream().anyMatch(
                                reservation -> reservation.getTime()
                                        .getId().equals(time.getId())
                        )
                ))
                .toList();
    }

    public List<Time> findAll() {
        return timeRepository.findByDeletedFalseOrderByIdAsc();
    }

    @Transactional
    public Time save(Time time) {
        return timeRepository.save(new Time(time.getValue()));
    }

    @Transactional
    public void deleteById(Long id) {
        timeRepository.softDeleteById(id);
    }
}
