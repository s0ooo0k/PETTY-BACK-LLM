package io.github.petty.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
public class CommentRequest {
    @NotBlank
    @Size(max=500)
    private String content;
}