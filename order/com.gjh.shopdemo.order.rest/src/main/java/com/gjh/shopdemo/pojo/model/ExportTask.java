package com.gjh.shopdemo.pojo.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("export_task")
public class ExportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String taskName;
    private String taskType;
    private String queryParams;
    private String fileUrl;
    private Integer status;
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime finishTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}