package com.ivf.companion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumCommentRequest {

    @NotBlank(message = "Comment content is required")
    private String content;
}
