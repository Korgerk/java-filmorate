package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Builder.Default
    private Set<Long> friends = new HashSet<>();

    @Builder.Default
    private Set<Long> likes = new HashSet<>();

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    private Long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "\\S+")
    private String login;

    private String name;

    @Past
    @NotNull
    private LocalDate birthday;

    public Map<String, Object> toMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("email", email);
        values.put("login", login);
        values.put("name", name);
        values.put("birthday", birthday);
        return values;
    }
}