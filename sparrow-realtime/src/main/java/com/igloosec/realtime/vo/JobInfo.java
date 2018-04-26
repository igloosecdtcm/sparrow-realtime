package com.igloosec.realtime.vo;

public class JobInfo {
	private String id;
	private String query;
	private String aggs;
	private int size;
	private String sort;
	private String type;
	public JobInfo() {
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getAggs() {
		return aggs;
	}
	public void setAggs(String aggs) {
		this.aggs = aggs;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public JobInfo(String id, String query, String aggs, int size, String sort, String type) {
		super();
		this.id = id;
		this.query = query;
		this.aggs = aggs;
		this.size = size;
		this.sort = sort;
		this.type = type;
	}
}
