package roomescape.reservation;

public class MyReservationResponse {

    private final Long reservationId;
    private final String theme;
    private final String date;
    private final String time;
    private final String status;

    public MyReservationResponse(
            Long reservationId,
            String theme,
            String date,
            String time,
            String status
    ) {
        this.reservationId = reservationId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getTheme() {
        return theme;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
