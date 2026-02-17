package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 文档内容实体类
 * 存储文档的正文内容，与Document表通过id一对一关联
 *
 * @author IT潘
 * @since 2026-02-17
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("document_content")
public class DocumentContent {

    /**
     * 重写id字段，使用NONE类型表示不自增
     * 因为document_content表的id直接使用文档ID作为主键
     */
    @TableId(type = IdType.NONE)
    private Long id;

    /** 文档正文内容(HTML/JSON格式) */
    @TableField(value = "content")
    private String content;

    /** 纯HTML内容用于展示(可选) */
    @TableField(value = "content_html")
    private String contentHtml;

    @TableField(value = "update_by")
    private Long updateBy;

    @TableField(value = "update_time",fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}