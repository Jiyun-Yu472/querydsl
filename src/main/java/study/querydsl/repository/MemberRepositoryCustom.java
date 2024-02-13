package study.querydsl.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

public interface MemberRepositoryCustom {
	List<MemberTeamDto> search(MemberSearchCondition condition);
	Page<MemberTeamDto> searchpageComplex(MemberSearchCondition condition, Pageable pageable);
}
