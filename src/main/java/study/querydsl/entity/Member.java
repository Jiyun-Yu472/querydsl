package study.querydsl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@ToString(of = {"id", "userName", "age"})
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {
	@Id @GeneratedValue
	@Column(name = "member_id")
	long id;
	private String userName;
	private int age;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;
	
	public Member(String userName) {
		this.userName = userName;
		this.age = 0;
	}
	
	public Member(String userName, int age) {
		this.userName = userName;
		this.age = age;
		this.team = null;
	}
	
	public Member(String userName, int age, Team team) {
		this.userName = userName;
		this.age = age;
		 if(team != null) {
			 changeTeam(team);
		 }
	}
	
	public void changeTeam(Team team) {
		this.team = team;
		team.getMembers().add(this);
	}
}
