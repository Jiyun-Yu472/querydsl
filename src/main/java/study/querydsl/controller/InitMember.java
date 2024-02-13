package study.querydsl.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {

	private final InitMemberService  initmemberService;
	
	@PostConstruct 
	public void init() {
		initmemberService.init();
	}
	
	@Component
	static class InitMemberService {
		@PersistenceContext EntityManager em;
		
		@Transactional
		public void init() {
			Team teamA = new Team("teamA");
			Team teamB = new Team("teamB");
			em.persist(teamA);
			em.persist(teamB);
			
			for(int i = 0; i < 100; i++) {
				Team selectedTeam = i % 2 == 0 ? teamA : teamB;
				em.persist(new Member("member"+1, i, selectedTeam));  
			}
		}
	}
}
