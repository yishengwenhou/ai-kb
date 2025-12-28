package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("document")
public class Document extends BaseEntity {

    /** 所属知识库ID */
    @TableField(value = "kb_id")
    private Long kbId;

    /** 文件名 (如: 2025年研发手册.pdf) */
    @TableField(value = "file_name")
    private String fileName;

    /** 文件后缀 (如: pdf, docx, txt)  */
    @TableField(value = "file_type")
    private String fileType;

    /** 物理存储路径或对象存储Key */
    @TableField(value = "file_path")
    private String filePath;

    /** 文件大小 (字节) */
    @TableField(value = "file_size")
    private Long fileSize;

    /** * 文档处理状态 
     *  对应报告中的文档管理与处理流程
     */
    @TableField(value = "status")
    private Integer status; 

    /** 文件唯一哈希 (用于秒传和去重) */
    @TableField(value = "file_hash")
    private String fileHash;

    /** * 字符总数 
     * 辅助统计，帮助评估该文档会消耗多少 Token
     */
    @TableField(value = "char_count")
    private Integer charCount;
}