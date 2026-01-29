package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(("knowledge_base"))
public class KnowledgeBase extends BaseEntity {
    /** 知识库名称 */
    @TableField(value = "name")
    private String name;        

    /** 知识库描述：描述该库存放哪类知识，甚至可以作为 AI 检索时的 Prompt 参考 */
    @TableField(value = "description")
    private String description;

    /** * 向量数据库中的集合名称
     * 对应报告中的 Qdrant/Chroma 存储 
     */
    @TableField(value = "vector_collection_name")
    private String vectorCollectionName;

    /** * 知识库状态
     * 0-就绪, 1-处理中(如正在大批量导入文档), 2-已归档
     */
    @TableField(value = "status")
    private Integer status;
    
    /** 备注信息 (继承自 BaseEntity 或在此定义) */
    @TableField(value = "remark")
    private String remark;

    /** 封面图 URL */
    @TableField(value = "cover_url")
    private String coverUrl;

    /** 知识库简介 */
    @TableField(value = "introduction")
    private String introduction;

    /** 归属类型：10-个人, 20-部门, 30-公共 */
    @TableField("owner_type")
    private Integer ownerType;

    /** 归属ID */
    @TableField("owner_id")
    private Long ownerId;

    /** * 可见性
     * 0: 私有 (仅Owner和授权成员可见)
     * 1: 外部公开 (例如个人知识库对全公司公开，部门知识库对全公司公开)
     */
    @TableField("visibility")
    private Integer visibility;

}