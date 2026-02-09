package com.itpan.backend.model.dto.knowledgeBase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 知识库创建DTO
 */
@Data
public class KnowledgeBaseCreateDTO {

    @NotBlank(message = "知识库名称不能为空")
    private String name;

    private String description;

    private String introduction;

    private String coverUrl;

    private String remark;

    @NotNull(message = "可见性不能为空")
    private Integer visibility; // 0-私有, 1-公开

    @NotNull(message = "归属类型不能为空")
    private Integer ownerType; // 10-个人, 20-部门, 30-公共

    private Long ownerId;
}
