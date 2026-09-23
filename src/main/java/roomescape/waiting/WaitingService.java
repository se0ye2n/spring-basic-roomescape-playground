package roomescape.waiting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.ForbiddenException;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            TimeRepository timeRepository,
            ThemeRepository themeRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public WaitingResponse save(
            WaitingRequest request,
            LoginMember loginMember
    ) {
        if (request.getDate() == null
                || request.getTime() == null
                || request.getTheme() == null) {
            throw new IllegalArgumentException(
                    "날짜, 시간, 테마를 입력해주세요."
            );
        }

        String date = LocalDate.parse(request.getDate()).toString();

        Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 회원입니다."
                ));

        Time time = timeRepository.findByIdAndDeletedFalse(request.getTime())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 예약 시간입니다."
                ));

        Theme theme = themeRepository.findByIdAndDeletedFalse(request.getTheme())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 테마입니다."
                ));

        Reservation reservation = reservationRepository
                .findByDateAndTheme_IdAndTime_Id(
                        date, theme.getId(), time.getId()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "예약 가능한 시간입니다. 바로 예약해주세요."
                ));

        if (reservation.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException(
                    "본인이 예약한 시간에는 대기할 수 없습니다."
            );
        }

        if (waitingRepository
                .existsByMember_IdAndDateAndTheme_IdAndTime_Id(
                        member.getId(), date, theme.getId(), time.getId()
                )) {
            throw new IllegalArgumentException(
                    "이미 대기 신청한 시간입니다."
            );
        }

        Waiting waiting = waitingRepository.saveAndFlush(
                new Waiting(member, date, time, theme)
        );

        long waitingNumber = waitingRepository.countAhead(
                date, theme.getId(), time.getId(), waiting.getId()
        ) + 1;

        return new WaitingResponse(waiting.getId(), waitingNumber);
    }

    @Transactional
    public void delete(Long waitingId, LoginMember loginMember) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 예약 대기입니다."
                ));

        if (!waiting.getMember().getId().equals(loginMember.getId())) {
            throw new ForbiddenException(
                    "본인의 예약 대기만 취소할 수 있습니다."
            );
        }

        waitingRepository.delete(waiting);
    }
}