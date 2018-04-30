package com.igloosec.realtime.vo;

/*************************************************** 
 * <pre> 
* 업무 그룹명: Fury
* 서브 업무명: job 정보 
* 설       명: job 정보
* 작   성  자: 이선구 [devleesk@igloosec.com]
* 작   성  일: 2018. 4. 27.
* Copyright ⓒIGLOO SEC. All Right Reserved
 * </pre> 
 ***************************************************/ 
public class JobInfo {
	// job ID - PK
	private int id;
	// job 명칭
	private String title;
	// 동작 주기(분) - 1분 ~ n분
	private int schedule;
	// 조건 정의 - Match Query DSL
	private String match;
	// 함수 정의 - Aggregation Function
	private String function;
	// 그룹핑 필드 - Aggregation Query DSL
	private String groupBy;
	// 그룹핑 결과 조건 - Aggregation Query DSL
	private String having;
	// 제한 건수 - Aggregation Size
	private int limit;
	// 통계 구분 - S(stats) | P(profile)
	private char type;
	
	public JobInfo() {
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getSchedule() {
		return schedule;
	}
	public void setSchedule(int schedule) {
		this.schedule = schedule;
	}
	public String getMatch() {
		return match;
	}
	public void setMatch(String match) {
		this.match = match;
	}
	public String getFunction() {
		return function;
	}
	public void setFunction(String function) {
		this.function = function;
	}
	public String getGroupBy() {
		return groupBy;
	}
	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}
	public String getHaving() {
		return having;
	}
	public void setHaving(String having) {
		this.having = having;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public char getType() {
		return type;
	}
	public void setType(char type) {
		this.type = type;
	}
	public JobInfo(int id, String title, int schedule, String match, String function, String groupBy, String having,
			int limit, char type) {
		super();
		this.id = id;
		this.title = title;
		this.schedule = schedule;
		this.match = match;
		this.function = function;
		this.groupBy = groupBy;
		this.having = having;
		this.limit = limit;
		this.type = type;
	}
}
