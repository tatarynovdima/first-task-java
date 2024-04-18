package org.example.model.author;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    @Pattern(regexp = "^[A-Z][a-z]*(\\s(([a-z]{1,3})|(([a-z]+\\')?[A-Z][a-z]*)))*$", message = "Bad formed author name")
    @Size(min = 2, message = "Author name must have at least {min} characters")
    @NotEmpty(message = "Author name must not be empty")
    private String name;

    @NotNull(message = "Author age must not be null")
    private int age;

    @Pattern(regexp = "^[A-Z][a-z]*$", message = "Nationality must start with an uppercase letter and contain only letters")
    @NotEmpty(message = "Nationality must not be empty")
    private String nationality;

    @NotNull(message = "Birthday must not be null")
    @Past(message = "Birthday must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
}