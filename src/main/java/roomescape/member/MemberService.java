package roomescape.member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        Member member = memberRepository.save(
                new Member(
                        request.getName(),
                        request.getEmail(),
                        request.getPassword(),
                        "USER"
                )
        );

        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail()
        );
    }
}
