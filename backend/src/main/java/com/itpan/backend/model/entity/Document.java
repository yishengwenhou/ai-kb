package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 知识库节点实体类
 * 支持文档、表格、思维导图、文件夹等多种类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("document")
public class Document extends BaseEntity {

    /** 所属知识库ID */
    @TableField(value = "kb_id")
    private Long kbId;

    /** 父节点ID (0表示根目录) */
    @TableField(value = "parent_id")
    private Long parentId;

    /** 节点标题/文件名 */
    @TableField(value = "title")
    private String title;

    /** 图标(Emoji或图片URL) */
    @TableField(value = "icon")
    private String icon;

    /** 简介/描述 */
    @TableField(value = "description")
    private String description;

    /** 节点类型: doc(文档), sheet(表格), mind(思维导图), file(上传的实体文件), folder(纯文件夹) */
    @TableField(value = "type")
    private String type;

    /** 排序值(双精度，支持中间插入) */
    @TableField(value = "sort")
    private Double sort;

    /** 层级路径(如: 0/10/55/)，加速查询 */
    @TableField(value = "tree_path")
    private String treePath;

    /** 文档富文本内容(JSON/HTML) - 仅 type='doc/sheet' 时有效 */
    @TableField(value = "content")
    private String content;

    /** 文件存储路径/OSS Key - 仅 type='file' 时有效 */
    @TableField(value = "file_url")
    private String fileUrl;

    /** 文件大小(字节) - 仅 type='file' 时有效 */
    @TableField(value = "file_size")
    private Long fileSize;

    /** 文件扩展名(pdf, docx) - 仅 type='file' 时有效 */
    @TableField(value = "file_ext")
    private String fileExt;

    /** 文件Hash(用于秒传) - 仅 type='file' 时有效 */
    @TableField(value = "file_hash")
    private String fileHash;

    /** 状态: 0正常 1回收站 */
    @TableField(value = "status")
    private Integer status;
}