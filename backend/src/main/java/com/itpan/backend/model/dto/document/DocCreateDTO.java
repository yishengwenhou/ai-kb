package com.itpan.backend.model.dto.document;


import lombok.Data;

@Data
public class DocCreateDTO {

    private Long kbId;

    private Long parentId;

    private String title;

    private String description;

    private String type;

    private Double sort;

    private String treePath;

    private String content;

    private String fileUrl;

    private Long fileSize;

    private String fileExt;

    private String fileHash;

    private Integer status;
}
