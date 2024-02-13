package study.querydsl.repository;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

@Repository
public class MemberJpaRepository {
	private final EntityManager em;
	private final JPAQueryFactory queryFactory;
	
	public MemberJpaRepository(EntityManager em) {
		this.em = em;
		this.queryFactory = new JPAQueryFactory(em);
	}
	
	public void save(Member member) {
		em.persist(member);
	}
	
	public Optional<Member> findById(Long id) {
		Member findMember = em.find(Member.class, id);
		return Optional.ofNullable(findMember);
	}
	
	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}
	
	public List<Member> findAll_Querydsl() {
		return queryFactory
				.selectFrom(member)
				.fetch();
	}
	
	public List<Member> findByUserName(String userName) {
		return em.createQuery("select m from Member m where userName = :userName", Member.class)
				.setParameter("userName", userName)
				.getResultList();
	}
	
	public List<Member> findByUserName_Querydsl(String userName) {
		return queryFactory
				.selectFrom(member)
				.where(member.userName.eq(userName))
				.fetch();
	}
	
	public List<MemberTeamDto> searchBybuilder(MemberSearchCondition condition) {
		
		BooleanBuilder builder = new BooleanBuilder();
		if(StringUtils.hasText(condition.getUserName())) {
			builder.and(member.userName.eq(condition.getUserName()));
		}
		
		if(StringUtils.hasText(condition.getTeamName())) {
			builder.and(team.name.eq(condition.getTeamName()));
		}
		
		if(condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}
		
		if(condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}
		
		return queryFactory
					.select(new QMemberTeamDto(
							member.id.as("memberId"), 
							member.userName, 
							member.age, 
							team.id.as("teamId"), 
							team.name.as("teamName") 
					))
					.from(member)
					.leftJoin(member.team, team)
					.where(builder)
					.fetch();
	}
	
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return queryFactory
				.select(new QMemberTeamDto(
						member.id.as("memberId"), 
						member.userName, 
						member.age, 
						team.id.as("teamId"), 
						team.name.as("teamName") 
				))
				.from(member)
				.leftJoin(member.team, team)
				.where(
						userNameEq(condition.getUserName()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				)
				.fetch();
	}

	private Predicate userNameEq(String userName) {
		return !StringUtils.hasText(userName) ? null : member.userName.eq(userName);
	}
	
	private Predicate teamNameEq(String teamName) {
		return !StringUtils.hasText(teamName)? null : team.name.eq(teamName);
	}

	private Predicate ageGoe(Integer ageGoe) {
		return ageGoe == null ? null : member.age.goe(ageGoe);
	}
	
	private Predicate ageLoe(Integer ageLoe) {
		return ageLoe == null ? null : member.age.loe(ageLoe);
	}
}
