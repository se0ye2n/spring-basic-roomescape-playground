package roomescape.reservation;

import org.springframework.stereotype.Service;
import roomescape.member.LoginMember;
import roomescape.member.Member;
import roomescape.member.MemberDao;

import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import roomescape.member.ForbiddenException;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final MemberDao memberDao;

    public ReservationService(
            ReservationDao reservationDao,
            MemberDao memberDao
    ) {
        this.reservationDao = reservationDao;
        this.memberDao = memberDao;
    }

    public ReservationResponse save(
            ReservationRequest reservationRequest,
            LoginMember loginMember
    ) {
        Member member = findReservationMember(
                reservationRequest,
                loginMember
        );

        Reservation reservation = reservationDao.save(
                reservationRequest,
                member
        );

        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getValue()
        );
    }

    private Member findReservationMember(
            ReservationRequest reservationRequest,
            LoginMember loginMember
    ) {
        Long memberId = reservationRequest.getMemberId();

        if (memberId == null) {
            memberId = loginMember.getId();
        }

        if (!loginMember.isAdmin()
                && !loginMember.getId().equals(memberId)) {
            throw new ForbiddenException(
                    "다른 회원의 예약은 관리자만 생성할 수 있습니다."
            );
        }

        try {
            return memberDao.findById(memberId);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
    }

    public void deleteById(Long id) {
        reservationDao.deleteById(id);
    }

    public List<ReservationResponse> findAll() {
        return reservationDao.findAll().stream()
                .map(reservation -> new ReservationResponse(
                        reservation.getId(),
                        reservation.getName(),
                        reservation.getTheme().getName(),
                        reservation.getDate(),
                        reservation.getTime().getValue()
                ))
                .toList();
    }
}
