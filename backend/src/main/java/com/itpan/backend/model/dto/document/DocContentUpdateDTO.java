package com.itpan.backend.model.dto.document;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class DocContentUpdateDTO {
    private Long id;

    private String content;

    private String contentHtml;
}
