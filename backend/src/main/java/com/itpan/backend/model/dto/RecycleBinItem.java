package com.itpan.backend.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 回收站项 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleBinItem {

    /** ID */
    private Long id;

    /** 类型: document - 文档, knowledgeBase - 知识库 */
    private String type;

    /** 名称 */
    private String name;

    /** 知识库ID (仅文档有值) */
    private Long kbId;

    /** 知识库名称 (仅文档有值) */
    private String kbName;

    /** 归属类型：10-个人, 20-部门, 30-公共 */
    private Integer ownerType;

    /** 归属ID */
    private Long ownerId;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deleteTime;

    /** 删除人ID */
    private Long deletedBy;

    /** 删除人名称 */
    private String deletedByName;

    /** 文件类型 (仅文档有值) */
    private String fileType;

    /** 文件大小 (仅文档有值，字节) */
    private Long fileSize;

    /** 描述 (仅知识库有值) */
    private String description;
}
