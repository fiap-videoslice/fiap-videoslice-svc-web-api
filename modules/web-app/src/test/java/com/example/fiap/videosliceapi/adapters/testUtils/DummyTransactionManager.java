package com.example.fiap.videosliceapi.adapters.testUtils;


import com.example.fiap.videosliceapi.adapters.datasource.TransactionManager;

public class DummyTransactionManager implements TransactionManager {
    @Override
    public <T> T runInTransaction(TransactionTask<T> task) throws Exception {
        return task.run();
    }
}
