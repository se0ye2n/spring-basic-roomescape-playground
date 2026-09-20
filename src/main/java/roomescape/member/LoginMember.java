package roomescape.member;

public class LoginMember {

    private final Member member;

    public LoginMember(Long id, String name, String email, String role) {
        this.member = new Member(id, name, email, role);
    }

    public Long getId() {
        return member.getId();
    }

    public String getName() {
        return member.getName();
    }

    public String getEmail() {
        return member.getEmail();
    }

    public String getRole() {
        return member.getRole();
    }

    public boolean isAdmin() {
        return member.isAdmin();
    }
}
