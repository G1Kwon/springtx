package hello.springtx.propagation;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
public class Log {

    @Id @GeneratedValue
    private Long id;
    private String message;

    //JPA는 기본 생성자가 필요하다
    public Log() {

    }
    public Log(String message) {
        this.message = message;
    }
}
