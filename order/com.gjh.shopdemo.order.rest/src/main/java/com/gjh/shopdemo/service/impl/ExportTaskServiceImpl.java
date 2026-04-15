package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.ExportTaskMapper;
import com.gjh.shopdemo.pojo.model.ExportTask;
import com.gjh.shopdemo.service.ExportTaskService;
import org.springframework.stereotype.Service;

@Service
public class ExportTaskServiceImpl extends ServiceImpl<ExportTaskMapper, ExportTask> implements ExportTaskService {
}