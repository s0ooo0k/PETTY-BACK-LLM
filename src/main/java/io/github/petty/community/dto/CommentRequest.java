package io.github.petty.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;

@Getter
@Setter
public class CommentRequest {
    @NotBlank
    @Size(max=500)
    private String content;
}