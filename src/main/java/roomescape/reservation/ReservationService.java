package roomescape.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.ForbiddenException;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberRepository;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepository;
import roomescape.time.Time;
import roomescape.time.TimeRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            TimeRepository timeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ReservationResponse save(
            ReservationRequest request,
            LoginMember loginMember
    ) {
        Member member = findReservationMember(request, loginMember);

        Time time = timeRepository.findByIdAndDeletedFalse(request.getTime())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 예약 시간입니다."
                ));

        Theme theme = themeRepository.findByIdAndDeletedFalse(request.getTheme())
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 테마입니다."
                ));

        Reservation reservation = reservationRepository.save(
                new Reservation(member, request.getDate(), time, theme)
        );

        return toResponse(reservation);
    }

    private Member findReservationMember(
            ReservationRequest request,
            LoginMember loginMember
    ) {
        Long memberId = request.getMemberId();

        if (memberId == null) {
            memberId = loginMember.getId();
        }

        if (!loginMember.isAdmin()
                && !loginMember.getId().equals(memberId)) {
            throw new ForbiddenException(
                    "다른 회원의 예약은 관리자만 생성할 수 있습니다."
            );
        }

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 회원입니다."
                ));
    }

    @Transactional
    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByOrderByIdAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getValue()
        );
    }
}
