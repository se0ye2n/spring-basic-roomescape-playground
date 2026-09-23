package roomescape.reservation;

public class MyReservationResponse {

    private Long reservationId;
    private Long waitingId;
    private String theme;
    private String date;
    private String time;
    private String status;

    public MyReservationResponse() {
    }

    public MyReservationResponse(
            Long reservationId,
            Long waitingId,
            String theme,
            String date,
            String time,
            String status
    ) {
        this.reservationId = reservationId;
        this.waitingId = waitingId;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getWaitingId() {
        return waitingId;
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
