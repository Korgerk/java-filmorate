package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MpaRating {
    private int id;
    private String name;
}