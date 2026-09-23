package roomescape.time;

import jakarta.persistence.*;

@Entity
@Table(name = "time")
public class Time {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_value", nullable = false, length = 20)
    private String value;

    @Column(nullable = false)
    private boolean deleted;

    public Time(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public Time(String value) {
        this.value = value;
    }

    public Time() {
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }
}
