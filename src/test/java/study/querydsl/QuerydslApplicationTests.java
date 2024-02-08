package study.querydsl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;


@SpringBootTest
@Transactional
class QuerydslApplicationTests {
	@Autowired EntityManager em;
		
	@Test
	void contextLoads() {
	}

}
