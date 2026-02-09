package com.itpan.backend.model.dto.knowledgeBase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 知识库更新DTO
 */
@Data
public class KnowledgeBaseUpdateDTO {

    @NotNull(message = "知识库ID不能为空")
    private Long id;

    @NotBlank(message = "知识库名称不能为空")
    private String name;

    private String description;

    private String introduction;

    private String coverUrl;

    private String remark;

    private Integer visibility; // 0-私有, 1-公开

    private Integer status; // 0-就绪, 1-处理中, 2-已归档
}
