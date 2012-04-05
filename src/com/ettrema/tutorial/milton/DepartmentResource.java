package com.ettrema.tutorial.milton;
 
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List; 

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.*; 
import com.bradmcevoy.http.Request.Method; 
import com.bradmcevoy.http.exceptions.ConflictException;
import com.ettrema.tutorial.objects.Department;
import com.ettrema.tutorial.objects.Document;

public class DepartmentResource implements 
		PropFindableResource, CollectionResource, DeletableResource,
		PutableResource, MoveableResource {
 
	private Logger log = LoggerFactory.getLogger(DepartmentResource.class);
	Department department;
	List<DocumentResource> docs;
	Session session;
	
	public DepartmentResource(Department department, List<DocumentResource> docs, Session session){
		this.department = department;
		this.docs = docs ;
		this.session = session;
	}
	public DepartmentResource(Department department, Session session){
		this.department = department; 
		this.docs = new ArrayList<DocumentResource>();
		this.session = session;
	}
	   
	@Override
	public Object authenticate(String user, String arg1) { 
		return user;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) { 
		return true;
	}

	@Override
	public String checkRedirect(Request arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// Name of the department resourcex
		return department.getName();
	}

	@Override
	public String getRealm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return department.getId().toString();
	}
  
	@Override
	public Resource child(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() {
		// TODO Auto-generated method stub 
	    return docs;
	}
   
	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
       return null;
	}
	

	@Override
	public void delete() {
		// TODO Auto-generated method stub
		Transaction transaction = session.beginTransaction(); 
		session.delete(department);
		transaction.commit(); 
		log.debug("Department {} is deleted successfully.", department.getName());
	}
	
	@Override
	public void moveTo(CollectionResource parent, String newName)
			throws ConflictException {
		// TODO Auto-generated method stub 
		log.debug("parent: {}, new name: {}, old Name:" + this.department.getName(), parent.getName(), newName );
		
		Transaction transaction = session.beginTransaction(); 
		department.setName(newName);
		session.update(department);
		transaction.commit(); 
		log.debug("");
	}
	
	@Override
	public Resource createNew(String fileName, InputStream in, Long length,
			String contentType) throws IOException, ConflictException {
		//Begin the Hibernate transaction
		Transaction transaction = session.beginTransaction();
		
		// "Document" is a POJO bean to represent the Documents.
		// Initialize the document Object and set the parameters 
		// like filename, content
		Document doc = new Document();
		doc.setFileName(fileName);
		doc.setDeptName(this.department.getName());
		byte[] bFile = new byte[Integer.parseInt(length.toString())];
		in.read(bFile);
		doc.setContent(bFile); 
		doc.setCreatedDate(new Date());
		doc.setModifiedDate(new Date());
		
		//Save the document object and Commit the transaction
		session.save(doc); 
		transaction.commit(); 
		
		return new DocumentResource(doc,session);
	}

}
