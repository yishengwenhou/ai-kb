package com.itpan.backend.model.dto.document;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveNodeDTO {

    /**
     * 被移动/排序的节点 ID
     */
    @NotNull(message = "节点ID不能为空")
    private Long id;

    /**
     * 目标父节点 ID
     * 1. 如果只是在同级排序，传原父ID。
     * 2. 如果是跨文件夹移动，传新父ID。
     * 3. 如果移动到根目录，传 0。
     */
    @NotNull(message = "目标父节点不能为空")
    private Long targetParentId;

    /**
     * 新的排序值 (双精度浮点数)
     * 计算公式：(前一个节点的sort + 后一个节点的sort) / 2
     * 如果排在第一位：后一个节点sort / 2 (或 -1000)
     * 如果排在最后一位：前一个节点sort + 1000
     */
    private Double newSort;
}