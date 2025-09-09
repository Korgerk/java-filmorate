package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.stereotype.Service;

@Builder(toBuilder = true)
@Service
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre {

    @Positive
    @JsonProperty("id")
    protected int id;

    @NotBlank
    @JsonProperty("name")
    protected String name;
}