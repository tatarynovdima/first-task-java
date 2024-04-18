package org.example.model.book;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.example.model.author.Author;

import javax.validation.constraints.*;
import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @NotEmpty(message = "Title must not be empty")
    private String title;

    @NotNull(message = "Publish year must not be null")
    @PastOrPresent(message = "Publish year must be in the past or present")
    @JsonFormat(pattern = "yyyy")
    private Date publishYear;

    @NotNull(message = "Genres list must not be null")
    private Genre genres;

    @NotNull(message = "Author must not be null")
    private Author author;

    @Size(max = 1000, message = "Description must not exceed {max} characters")
    private String description;
}