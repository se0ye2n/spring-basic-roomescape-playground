package roomescape.theme;

import jakarta.persistence.*;

@Entity
@Table(name = "theme")
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean deleted;

    public Theme() {
    }

    public Theme(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Theme(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}