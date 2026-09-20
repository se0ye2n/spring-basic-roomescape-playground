package roomescape.member;

import io.jsonwebtoken.JwtException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final MemberDao memberDao;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginService(
            MemberDao memberDao,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberDao = memberDao;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("이메일과 비밀번호를 입력해주세요.");
        }

        try {
            Member member = memberDao.findByEmailAndPassword(
                    request.getEmail(),
                    request.getPassword()
            );

            return jwtTokenProvider.createToken(member);
        } catch (EmptyResultDataAccessException e) {
            throw new UnauthorizedException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }
    }

    public LoginMember findLoginMember(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        try {
            Long memberId = jwtTokenProvider.getMemberId(token);
            Member member = memberDao.findById(memberId);

            return new LoginMember(
                    member.getId(),
                    member.getName(),
                    member.getEmail(),
                    member.getRole()
            );
        } catch (JwtException | IllegalArgumentException
                 | EmptyResultDataAccessException e) {
            throw new UnauthorizedException("유효하지 않은 로그인 정보입니다.");
        }
    }
}
