package roomescape.member;

import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginService(
            MemberRepository memberRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "이메일과 비밀번호를 입력해주세요."
            );
        }

        Member member = memberRepository.findByEmailAndPassword(
                request.getEmail(),
                request.getPassword()
        ).orElseThrow(() -> new UnauthorizedException(
                "이메일 또는 비밀번호가 올바르지 않습니다."
        ));

        return jwtTokenProvider.createToken(member);
    }

    public LoginMember findLoginMember(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        try {
            Long memberId = jwtTokenProvider.getMemberId(token);

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new UnauthorizedException(
                            "유효하지 않은 로그인 정보입니다."
                    ));

            return new LoginMember(
                    member.getId(),
                    member.getName(),
                    member.getEmail(),
                    member.getRole()
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException(
                    "유효하지 않은 로그인 정보입니다."
            );
        }
    }
}
