package io.github.petty.community.dto;

import io.github.petty.community.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.util.List;
import io.github.petty.community.dto.PostImageRequest;

@Getter
@Setter
public class PostRequest {
    @Size(max=100)
    private String title;
    @Size(max=2000)
    private String content;
    private Post.PostType postType;
    @Size(max=50)
    private String petName;
    private String petType;
    @Size(max=100)
    private String region;
    private Boolean isResolved;
    private List<PostImageRequest> images;
}
