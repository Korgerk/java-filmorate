package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MpaRating {

    @Positive
    @JsonProperty("id")
    protected int id;

    @NotBlank
    @JsonProperty("name")
    protected String name;
}