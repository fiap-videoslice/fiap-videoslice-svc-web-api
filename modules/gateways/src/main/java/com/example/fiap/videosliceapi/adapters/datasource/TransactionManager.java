package com.example.fiap.videosliceapi.adapters.datasource;

import java.util.function.Supplier;

public interface TransactionManager {

    /**
     * Permite ao serviço de negócio declarar que uma sequência de operações deve ser executada em
     * uma transação ("tudo ou nada").
     * <br />
     * Os adapters de infraestrutura serão responsáveis por fornecer uma implementação apropriada
     * que respeite as propriedades esperadas da Transação.
     */
    <T> T runInTransaction(TransactionTask<T> task) throws Exception;

    @FunctionalInterface
    interface TransactionTask<T> {
        T run() throws Exception;
    }
}
