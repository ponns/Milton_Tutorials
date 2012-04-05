package com.ettrema.tutorial.objects;

import java.util.Date;
import java.util.HashMap;

public class Document {
	private Long id;
	private String FileName;
	private String DeptName;
	private Date ModifiedDate;
	private Date CreatedDate;
	private String Title;
	private String KeyWords;

	private byte[] content;
	
	//convenient method to return all the properties as name/value pairs
	public HashMap<String, Object> getAllProperties(){ 
		HashMap<String, Object> localProps = new HashMap<String, Object>();
		
		localProps.put("ModifiedDate", this.ModifiedDate);
		localProps.put("CreatedDate", this.CreatedDate);
		localProps.put("Title", this.Title);
		localProps.put("KeyWords", this.KeyWords);
		
		return localProps;
	}
	
	public void setProperty(String name, String value){
		if (name.equals("Title")) this.setTitle(value);
		if (name.equals("KeyWords")) this.setKeyWords(value);
	}
	   
	public String getTitle() {
		return Title;
	}
	public void setTitle(String title) {
		Title = title;
	}
	public String getKeyWords() {
		return KeyWords;
	}
	public void setKeyWords(String keyWords) {
		KeyWords = keyWords;
	} 
	public Date getModifiedDate() {
		return ModifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		ModifiedDate = modifiedDate;
	}
	public Date getCreatedDate() {
		return CreatedDate;
	}
	public void setCreatedDate(Date createdDate) {
		CreatedDate = createdDate;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}
	public String getDeptName() {
		return DeptName;
	}
	public void setDeptName(String deptName) {
		DeptName = deptName;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	
}
