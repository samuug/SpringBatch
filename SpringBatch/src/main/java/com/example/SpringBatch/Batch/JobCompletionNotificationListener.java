package com.example.SpringBatch.Batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("¡El trabajo ha sido completado exitosamente!");

            // Aquí puedes realizar acciones adicionales después de que el trabajo se haya completado.
            // Por ejemplo, enviar notificaciones, generar informes, etc.
        } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
            log.error("El trabajo ha fallado con excepciones: {}", jobExecution.getAllFailureExceptions());

            // Aquí puedes manejar los fallos, como enviar alertas o realizar acciones específicas.
        }
    }
}
