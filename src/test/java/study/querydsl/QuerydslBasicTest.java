package study.querydsl;

import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
	@Autowired EntityManager em;
	JPAQueryFactory queryFactory; 
	
	@BeforeEach
	public void before() {
		queryFactory = new JPAQueryFactory(em);
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);
		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
	}
	
	@Test
	public void startQueryDsl() {
		Member findMember = queryFactory
				.select(member)
				.from(member)
				.where(member.userName.eq("member1"))
				.fetchOne();
		
		assertThat(findMember.getUserName()).isEqualTo("member1");
	}
	
	@Test
	public void search() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.userName.eq("member1")
					.and(member.age.eq(10)))
			.fetchOne();
		
		assertThat(findMember.getUserName()).isEqualTo("member1");
	}
	
	@Test
	public void searchAndParam() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
					member.userName.eq("member1"),
					member.age.eq(10)
			)
			.fetchOne();
		
		assertThat(findMember.getUserName()).isEqualTo("member1");
	}
	
	@Test
	public void resultFetch() {
		List<Member> fetch = queryFactory
				.selectFrom(member)
				.fetch();
		
		Member fetchOne = queryFactory
				.selectFrom(member)
				.where(member.userName.eq("member1"))
				.fetchOne();
		
		Member fetchFirst = queryFactory
				.selectFrom(member)
				.fetchFirst();
	}
	
	@Test
	public void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));
		
		List<Member> findMember = queryFactory
				.selectFrom(member)
				.where(member.age.eq(100))
				.orderBy(member.age.desc(), member.userName.asc().nullsLast())
				.fetch();
		
		assertThat(findMember.get(0).getUserName()).isEqualTo("member5");
		assertThat(findMember.get(1).getUserName()).isEqualTo("member6");
		assertThat(findMember.get(2).getUserName()).isNull();;
	}
	
	@Test
	public void paging() {
		List<Member> findMember = queryFactory
				.selectFrom(member)
				.orderBy(member.age.desc())
				.offset(1) //start from 0
				.limit(2)
				.fetch();
		
		assertThat(findMember.size()).isEqualTo(2);
	}
	
	@Test
	public void aggregation() {
		Tuple findMember = queryFactory
				.select(
						member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max(),
						member.age.min())
				.from(member)
				.fetchOne();
		
		assertThat(findMember.get(member.count())).isEqualTo(4);
		assertThat(findMember.get(member.age.sum())).isEqualTo(100);
		assertThat(findMember.get(member.age.avg())).isEqualTo(25);
		assertThat(findMember.get(member.age.max())).isEqualTo(40);
		assertThat(findMember.get(member.age.min())).isEqualTo(10);
	}
	
	@Test
	public void groups() {
		List<Tuple> findMember = queryFactory
				.select(
						team.name,
						member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				.fetch();
		
		Tuple teamA = findMember.get(0);
		Tuple teamB = findMember.get(1);
		
		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);
		
		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}
	
	@Test
	public void join() {
		List<Member> findMember = queryFactory
				.selectFrom(member) 
				.join(member.team, team)
				.where(team.name.eq("teamA"))
				.fetch();
		
		assertThat(findMember)
			.extracting("userName")
			.containsExactly("member1", "member2");
	}
	
	@Test
	public void thetaJoin() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		
		List<Member> result = queryFactory
				.select(member)
				.from(member, team)
				.where(member.userName.eq(team.name))
				.fetch();
	}
	
	@Test
	public void joinOnFiltering() {
		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				.leftJoin(member.team, team).on(team.name.eq("teamA"))
				.fetch();
		 
		for (Tuple tuple : result) {
//			System.out.println("tuple :: " + tuple);
		}
	}
	
	@Test
	public void joinOnNoRelation() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));
		
		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				.leftJoin(team).on(member.userName.eq(team.name))
				.fetch();
		
		for (Tuple tuple : result) {
//			System.out.println("tuple :: " + tuple);
		}
	}
	
	@Autowired EntityManagerFactory emf;
	
	@Test
	public void fetchJoinUse() {
		
		Member findMember = queryFactory
				.selectFrom(member)
				.join(member.team, team).fetchJoin() 
				.limit(1)
				.fetchOne();
		
		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 적용").isTrue();
	}
	
	@Test
	public void subQuery() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> findMember = queryFactory
				.selectFrom(member)
				.where(member.age.eq(
						JPAExpressions
								.select(memberSub.age.max())
								.from(memberSub)
				))
				.fetch();
		
		assertThat(findMember).extracting("age").containsExactly(40);
	}
	
	@Test
	public void subQueryGoe() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> findMember = queryFactory
				.selectFrom(member)
				.where(member.age.goe(
						JPAExpressions
								.select(memberSub.age.avg())
								.from(memberSub)
				))
				.fetch();
		
		assertThat(findMember).extracting("age").containsExactly(30, 40);
	}
	
	@Test
	public void subQueryIn() {
		QMember memberSub = new QMember("memberSub");
		
		List<Member> findMember = queryFactory
				.selectFrom(member)
				.where(member.age.in(
						select(memberSub.age)
								.from(memberSub)
								.where(memberSub.age.gt(10))
				))
				.fetch();
		
		assertThat(findMember).extracting("age").containsExactly(20, 30, 40);
	}
	
	@Test
	public void selectSubQuery() {
		QMember memberSub = new QMember("memberSub");
		
		List<Tuple> results = queryFactory
				.select(member.userName,
						select(memberSub.age.avg())
						.from(memberSub))
				.from(member)
				.fetch();
		
		for (Tuple tuple : results) {
//			System.out.println("tuple :: " + tuple);
		}
	}
	
	@Test
	public void basicCase() {
		List<String> results = queryFactory
				.select(member.age
						.when(10).then("열살")
						.when(20).then("스무살")
						.otherwise("기타")) 
				.from(member)
				.fetch();
		
		for (String string : results) {
//			System.out.println("String :: " + string);
		}
	}
	
	@Test
	public void complexCase() {
		List<String> results = queryFactory
				.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0~20age")
						.when(member.age.between(21, 30)).then("21~30age")
						.otherwise("기타" )) 
				.from(member)
				.fetch();
		
		for (String string : results) {
//			System.out.println("String :: " + string);
		}
	}
	
	@Test
	public void constant() {
		List<Tuple> results = queryFactory
				.select(member.userName
						, Expressions.constant("A")) 
				.from(member)
				.fetch();
		
		for (Tuple string : results) {
//			System.out.println("Tuple :: " + string);
		}
	}
	
	@Test
	public void concat() {
		List<String> results = queryFactory
				.select(member.userName.concat("_").concat(member.age.stringValue())) 
				.from(member)
				.fetch();
		
		for (String string : results) {
			System.out.println("String :: " + string);
		}
	}
	
	@Test
	public void simplePorjection() {
		List<String> results = queryFactory
				.select(member.userName) 
				.from(member)
				.fetch();
		
		for (String string : results) {
			System.out.println("String :: " + string);
		}
	}
	
	@Test
	public void tuplePorjection() {
		List<Tuple> results = queryFactory
				.select(member.userName,
						member.age) 
				.from(member)
				.fetch();
		 
		for (Tuple tuple : results) {
			System.out.println("Tuple :: " + tuple.get(member.userName));
			System.out.println("Tuple :: " + tuple.get(member.age));
		}
	}
	
	@Test
	public void findDtoByJPQL() {
		List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.userName, m.age) from Member m", MemberDto.class)
				.getResultList();
	}
	
	@Test
	public void findDtoBySetter() {
		List<MemberDto> result = queryFactory
				.select(Projections.bean(MemberDto.class, 
						member.userName, 
						member.age))
				.from(member)
				.fetch();
	}
	
	@Test
	public void findDtoByField() {
		List<MemberDto> result = queryFactory
				.select(Projections.fields(MemberDto.class, 
						member.userName, 
						member.age))
				.from(member)
				.fetch();
	}
	
	@Test
	public void findDtoByConstructor() {
		QMember memberSub = new QMember("memberSub");
		
		List<MemberDto> result = queryFactory
				.select(Projections.constructor(MemberDto.class, 
						member.userName.as("userName"),  
//						member.age,
						Expressions.as(
								select(memberSub.age.max())
								.from(memberSub), "age"
						)
				))
				.from(member)
				.fetch();
	}
	
	@Test
	public void findDtoByQueryProtection() {
		List<MemberDto> result = queryFactory
				.select(new QMemberDto(member.userName, member.age)) //dto가 querydsl을 의존하게 만드는 방식
				.from(member)
				.fetch();
	}
	
	@Test
	public void dynamicQueryBooleanBuilder() {
		String userNameParam = "member1";
		Integer ageParam = 10;
		
		List<Member> result = searchMember1(userNameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}
	
	private List<Member> searchMember1(String  userNameCond, Integer ageCond) {
		BooleanBuilder builder =new BooleanBuilder();
		if(userNameCond != null) {
			builder.and(member.userName.eq(userNameCond)); 
		}
		
		if(ageCond != null) {
			builder.and(member.age.eq(ageCond));
		}
		
		return queryFactory
				.selectFrom(member)
				.where(builder)
				.fetch();
	}
	
	@Test
	public void dynamicQueryWhereParam() {
		String userNameParam = "member1";
		Integer ageParam = 10;
		
		List<Member> result = searchMember2(userNameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember2(String  userNameCond, Integer ageCond) {
		return queryFactory
				.selectFrom(member)
				.where(userNameEq(userNameCond), ageEq(ageCond))
				.fetch();
	}

	//반환값으로 BooleanExpression도 사용하능(Predicate의 구현체, and or 연산가능  e.g.userNameEq(p1).and(ageEq(p2)) 
	private Predicate userNameEq(String userNameCond) {
		if(userNameCond == null) { return null; }
		
		return member.userName.eq(userNameCond);
	}
	
	private Predicate ageEq(Integer ageCond) {
		return ageCond == null ? null : member.age.eq(ageCond);
	}

	@Test
	public void bulkUpdate() {
		queryFactory
				.update(member)
				.set(member.userName, "비회원")
				.where(member.age.lt(28))
				.execute();
		
		em.flush();
		em.clear();
	}
	
	@Test
	public void bulkDelete() {
		long count = queryFactory
				.delete(member)
				.where(member.age.gt(18))
				.execute();
	}
	
	@Test
	public void sqlFunction() {
		List<String> result = queryFactory
				.select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.userName, "member", "M"))
				.from(member)
				.fetch();
	}
	
	@Test
	public void sqlFunction2() {
		List<String> result = queryFactory
				.select(member.userName)
				.from(member)
				.where(member.userName.eq(member.userName.lower()))
				.fetch();
	}
}
























