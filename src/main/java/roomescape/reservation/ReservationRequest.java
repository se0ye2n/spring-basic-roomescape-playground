package roomescape.reservation;

public class ReservationRequest {
    private String date;
    private Long theme;
    private Long time;

    public String getDate() {
        return date;
    }

    public Long getTheme() {
        return theme;
    }

    public Long getTime() {
        return time;
    }

    private Long memberId;

    public Long getMemberId() {
        return memberId;
    }
}
