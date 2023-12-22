package com.example.SpringBatch.taskExecutor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class TaskExecutorConfigurations {

    @Bean
    @Qualifier("batchTaskExecutor")
    public TaskExecutor batchTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
