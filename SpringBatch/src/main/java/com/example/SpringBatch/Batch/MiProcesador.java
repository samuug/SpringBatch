package com.example.SpringBatch.Batch;

import com.example.SpringBatch.domain.Transaccion;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MiProcesador implements ItemProcessor<Transaccion, Transaccion> {

    @Override
    public Transaccion process(Transaccion transaccion) throws Exception {
        // Validación de datos
        if (!esTransaccionValida(transaccion)) {
            // Si la transacción no es válida, puedes devolver null para indicar que debe ser filtrada.
            return null;
        }

        procesarConcurrentemente(transaccion);
        calcularDatos(transaccion);

        return transaccion;
    }

    private boolean esTransaccionValida(Transaccion transaccion) {
        // Lógica de validación, por ejemplo, verificar si los campos obligatorios están presentes
        return transaccion != null && transaccion.getMonto() > 0;
    }

    private void procesarConcurrentemente(Transaccion transaccion) {

    }

    private void calcularDatos(Transaccion transaccion) {
        double nuevoMonto = transaccion.getMonto() * 1.1; // Aumento del 10%
        transaccion.setMonto((long) nuevoMonto);
    }
}

