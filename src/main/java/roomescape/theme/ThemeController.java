package roomescape.theme;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class ThemeController {

    private final ThemeRepository themeRepository;

    public ThemeController(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @PostMapping("/themes")
    public ResponseEntity<Theme> createTheme(@RequestBody Theme theme) {
        Theme newTheme = themeRepository.save(
                new Theme(theme.getName(), theme.getDescription())
        );

        return ResponseEntity
                .created(URI.create("/themes/" + newTheme.getId()))
                .body(newTheme);
    }

    @GetMapping("/themes")
    public ResponseEntity<List<Theme>> list() {
        return ResponseEntity.ok(
                themeRepository.findByDeletedFalseOrderByIdAsc()
        );
    }

    @DeleteMapping("/themes/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeRepository.softDeleteById(id);
        return ResponseEntity.noContent().build();
    }
}
