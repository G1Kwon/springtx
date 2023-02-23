package hello.springtx.propagation;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;
import java.rmi.UnexpectedException;

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

    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        //외부 트랜잭션만 물리 트랜잭션을 시작한다.
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        //내부 트랜잭션은 외부 트랜잭션에 참여만 하지 시작을 하지 않는다. (로그 참고)
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 커밋");
        //내부 트랜잭션에서는 물리 트랜잭션 커밋이 일어나지 않는다.
        txManager.commit(inner);

        log.info("외부 트랜잭션 커밋");
        //외부 트랜잭션만 물리 트랜잭션을 커밋한다.
        txManager.commit(outer);
    }

    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer);
    }

    @Test
    void innter_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 롤백");
        //Participating transaction failed - marking existing transaction as rollback-only 마킹
        txManager.rollback(inner);

        //Global transaction is marked as rollback-only but transactional code requested commit
        log.info("외부 트랜잭션 커밋");
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }
}
