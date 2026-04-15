package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.ExportTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exportTask")
public class ExportTaskController {

    @Autowired
    private ExportTaskService exportTaskService;
}