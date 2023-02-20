package hello.springtx.propagation;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("Transaction Initiate");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("Transaction Commit Start");
        txManager.commit(status);
        log.info("Transaction Commit Complete");
    }

    @Test
    void rollback() {
        log.info("Transaction Initiate");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("Transaction Rollback Start");
        txManager.rollback(status);
        log.info("Transaction Rollback Complete");
    }

    @Test
    void double_commit() {
        log.info("Transaction 1 Initiate");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("Transaction 1  Commit Start");
        txManager.commit(tx1);

        log.info("Transaction 2 Initiate");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("Transaction 2  Commit Start");
        txManager.commit(tx2);

        log.info("Transaction Commit Complete");
    }

    @Test
    void double_commit_rollback() {
        log.info("Transaction 1 Initiate");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("Transaction 1  Commit Start");
        txManager.commit(tx1);

        log.info("Transaction 2 Initiate");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("Transaction 2  Rollback Start");
        txManager.rollback(tx2);

        log.info("Transaction Commit Complete");
    }
}
