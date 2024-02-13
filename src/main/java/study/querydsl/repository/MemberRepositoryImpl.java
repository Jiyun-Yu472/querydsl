package study.querydsl.repository;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	
	public MemberRepositoryImpl(EntityManager em) {
		this.queryFactory = new JPAQueryFactory(em);
	}

	@Override
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

	@Override
	public Page<MemberTeamDto> searchpageComplex(MemberSearchCondition condition, Pageable pageable) {
		List<MemberTeamDto> content = queryFactory
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
		
		Long total = queryFactory
				.select(member.count())
				.from(member)
				.where(
						userNameEq(condition.getUserName()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe())
				)
				.fetchOne();
		
		return new PageImpl<>(content, pageable, total);
		//return new PageImpl<>(content, pageable, () -> countQuery.fetchOne);
	}

}
