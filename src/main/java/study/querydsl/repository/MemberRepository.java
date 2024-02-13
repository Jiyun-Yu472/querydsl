package study.querydsl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import study.querydsl.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
	List<Member> findByUserName(String userName);
}
