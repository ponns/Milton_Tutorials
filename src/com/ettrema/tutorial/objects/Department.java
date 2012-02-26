package com.ettrema.tutorial.objects;

public class Department {
	private Long id;
	private String Name;
	
	public Department(){}
	public Department(String name){
		 this.Name = name;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	} 
}
