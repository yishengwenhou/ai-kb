package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(("knowledge_base"))
public class KnowledgeBase extends BaseEntity {
    /** 知识库名称 */
    @TableField(value = "name")
    private String name;        

    /** 知识库描述：描述该库存放哪类知识，甚至可以作为 AI 检索时的 Prompt 参考 */
    @TableField(value = "description")
    private String description; 

    /** * 访问权限：
     * 0-私有 (仅创建者/负责人)
     * 1-公开 (全员)
     * 2-指定部门 (需关联部门ID)
     */
    @TableField(value = "visibility")
    private Integer visibility; 

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
}