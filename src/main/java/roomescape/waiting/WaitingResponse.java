package roomescape.waiting;

public class WaitingResponse {

    private Long id;
    private long waitingNumber;

    public WaitingResponse() {
    }

    public WaitingResponse(Long id, long waitingNumber) {
        this.id = id;
        this.waitingNumber = waitingNumber;
    }

    public Long getId() {
        return id;
    }

    public long getWaitingNumber() {
        return waitingNumber;
    }
}
