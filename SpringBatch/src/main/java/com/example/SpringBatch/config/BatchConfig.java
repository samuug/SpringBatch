package com.example.SpringBatch.config;

import com.example.SpringBatch.Batch.JobCompletionNotificationListener;
import com.example.SpringBatch.domain.Transaccion;
import com.example.SpringBatch.repository.TransaccionRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Value("${spring.batch.input}")
    private Resource[] resources;
    @Autowired
    private LocalContainerEntityManagerFactoryBean entityManagerFactory;

    @Bean
    public FlatFileItemReader<Transaccion> reader() {
        FlatFileItemReader<Transaccion> reader = new FlatFileItemReader<>();
        reader.setResource(resources[0]); // Puedes extender esto para manejar múltiples archivos
        reader.setLineMapper(new DefaultLineMapper<Transaccion>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("monto");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Transaccion>() {{
                setTargetType(Transaccion.class);
            }});
        }});
        return reader;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("spring_batch");
        taskExecutor.setConcurrencyLimit(10); // Número máximo de hilos concurrentes
        return taskExecutor;
    }

    @Bean
    public JpaItemWriter<Transaccion> writer() {
        JpaItemWriter<Transaccion> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory((EntityManagerFactory) entityManagerFactory);
        return writer;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(dataSource);
        jobRepositoryFactoryBean.setTransactionManager(transactionManager);
        jobRepositoryFactoryBean.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        jobRepositoryFactoryBean.afterPropertiesSet();
        return jobRepositoryFactoryBean.getObject();
    }

    @Bean
    public Step step(ItemReader<Transaccion> reader, ItemProcessor<Transaccion, Transaccion> processor,
                     ItemWriter<Transaccion> writer, @Qualifier("batchTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("step", jobRepository)
                .<Transaccion, Transaccion>chunk(5000, transactionManager)
                .reader(reader)
                .faultTolerant()
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Job processJob(JobCompletionNotificationListener listener, Step step) {
        return new JobBuilder("processJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step)
                .end()
                .build();
    }
}
