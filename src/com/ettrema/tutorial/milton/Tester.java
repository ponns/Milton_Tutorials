package com.ettrema.tutorial.milton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import org.hibernate.*; 
import org.hibernate.cfg.Configuration;

import com.ettrema.tutorial.objects.Department;
import com.ettrema.tutorial.objects.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tester {
 
	private Logger log = LoggerFactory.getLogger(Tester.class);
	
	public void save(){
		 SessionFactory sessionFactory =  new Configuration().configure().buildSessionFactory();
		 Session session = sessionFactory.openSession(); 
		 Transaction transaction = session.beginTransaction();
		  
		 List existingDepartments = session.createCriteria(Department.class).list();
		 if( existingDepartments == null || existingDepartments.size() == 0) {
			 log.debug("Departments not exists. Recreating them...");
			 Department deptIT = new Department("Information Technologies"); 
			 session.save(deptIT); 	
			 Department deptFin = new Department("Finance"); 
			 session.save(deptFin); 
			 Department deptHR = new Department("Human Resources"); 
			 session.save(deptHR); 
		 }else{
			 log.debug("Departments are already there...count is {}", existingDepartments.size() );
			  
		 }
		 
		 List existingDocuments = session.createCriteria(Document.class).list();
		 if( existingDocuments == null || existingDocuments.size() == 0) {
			 Document doc = new Document();
			 doc.setFileName("test.pdf");
			 doc.setDeptName("Finance");
			 File file = new File("C:\\A181137\\tmp\\BusTimings.pdf");
			 byte[] bFile = new byte[(int) file.length()];
			 try{
				 FileInputStream fileInputStream = new FileInputStream(file);
				 fileInputStream.read(bFile);
				 fileInputStream.close();
			 } catch (Exception e) {
				 e.printStackTrace();
			 } 
			 doc.setContent(bFile); 
			 session.save(doc); 
		 }else{
			 log.debug("Documents are already there.. count is {}", existingDocuments.size());
			 Document doc = (Document) existingDocuments.get(0);
			 
			 try{
				 FileOutputStream fos = new FileOutputStream("C:\\A181137\\tmp\\"+doc.getFileName());
				 fos.write(doc.getContent());
				 fos.close();
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
		 
		 transaction.commit();
		 session.close(); 
	}
	
	public static void main(String[] args) {
		System.out.println("Started...");
		Tester t = new Tester();
		t.save();
		System.out.println("Completed...");
	}

}
