package com.itpan.backend.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentVO {

    // --- 核心 ID ---
    private Long id;
    private Long kbId;
    private Long parentId;

    // 【新增】路径，用于面包屑导航 (如: "0/1/5/")
    private String treePath;

    // --- 展示信息 ---
    private String title;
    private String icon;
    private String description;

    // 【新增】文档封面图
    private String coverUrl;

    // --- 逻辑控制 ---
    private String type;       // doc, sheet, file, folder
    private Double sort;
    private Boolean hasChildren;

    // 【新增】辅助字段，方便前端判断 (后端判断 type.equals("folder"))
    private Boolean isFolder;

    // --- 内容与统计 (详情页用) ---
    // 【新增】核心正文 (列表页为null，详情页有值)
    private String content;
    // 【新增】字数统计
    private Integer charCount;

    // --- 文件特有属性 (仅 type=file 时有值) ---
    private String fileUrl;
    private Long fileSize;
    private String fileExt;

    // --- 状态与审计 ---
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // --- 用户信息 ---
    private String createName;
    private String updateName;
}