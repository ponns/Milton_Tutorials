package com.ettrema.tutorial.milton;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;  
import com.ettrema.tutorial.objects.Department;
import com.ettrema.tutorial.objects.Document;

public class MyResourceFactory implements ResourceFactory{
	private Logger log = LoggerFactory.getLogger(MyResourceFactory.class);
	
	SessionFactory sessionFactory;
	Session session;
	
	public MyResourceFactory(){
		sessionFactory =  new Configuration().configure().buildSessionFactory(); 
		session = sessionFactory.openSession();
	}
	
	@Override
	public Resource getResource(String host, String p) { 
		Path path = Path.path(p).getStripFirst();
		log.debug("getResource: " + path);
		if( path.isRoot() ) {
			return null;
		} else if( path.getLength() == 1 ) {
			return this.getCompanyResource(); 
		} else if( path.getLength() == 2 ) {
			return this.getDepartmentResource(path.getName());
		} else if( path.getLength() == 3 ) {
			return this.getDocumentResource(path.getName());
		}
		return this.getCompanyResource();
	}
	
	public CompanyResource getCompanyResource(){
		
		List<DepartmentResource> allDepartments = new ArrayList<DepartmentResource>();
		//session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		List existingDepartments = session.createCriteria(Department.class).list();
		if( existingDepartments == null || existingDepartments.size() == 0) {
			 log.debug("Departments do not exists. Recreating them..."); 
			 DepartmentResource deptResource;
			 
			 Department deptIT = new Department("Information Technology"); 
			 session.save(deptIT); 	
			 deptResource = new DepartmentResource(deptIT, session);
			 allDepartments.add(deptResource);
			 
			 Department deptFin = new Department("Finance"); 
			 session.save(deptFin); 
			 deptResource = new DepartmentResource(deptFin, session);
			 allDepartments.add(deptResource);
			 
			 Department deptHR = new Department("Human Resources"); 
			 session.save(deptHR); 
			 deptResource = new DepartmentResource(deptHR, session);
			 allDepartments.add(deptResource);
			 
		}else{
			 log.debug("Number of Departments: {}", existingDepartments.size() );
			 for(Object o: existingDepartments){
				 DepartmentResource deptResource = new DepartmentResource((Department)o, session);
				 allDepartments.add(deptResource);
			 }
		}
		transaction.commit();
		//session.close();
		
		CompanyResource company = new CompanyResource(allDepartments, session);
		return company;
	}
	
	public DepartmentResource getDepartmentResource(String deptName){
		log.debug("Departname to select is {}", deptName);
		DepartmentResource deptResource ;
		
		//session = sessionFactory.openSession();
		//Transaction transaction = session.beginTransaction();
		
		Criteria crit = session.createCriteria(Department.class);
		crit.add(Expression.eq("Name", deptName));
		
		List list = crit.list(); 
		if( list == null || list.size() == 0 ) {
			log.debug("Department {} not found.", deptName);
			return null;
		} else {
			Department dept = (Department) list.get(0);
			log.debug("Department {} found: " + dept.getName()); 
			deptResource = new DepartmentResource(dept, this.getAllDocuments(dept.getName()), session);
		}
		
		//transaction.commit();
		//session.close();
		
		return deptResource;
		
	}

	public List<DocumentResource> getAllDocuments(String deptName){
		
		List<DocumentResource> docs = new ArrayList<DocumentResource>();
		
		log.debug("Documents to be selected for department {}", deptName); 
		//session = sessionFactory.openSession();
		//Transaction transaction = session.beginTransaction();
		
		Criteria crit = session.createCriteria(Document.class);
		crit.add(Expression.eq("DeptName", deptName));
		
		List list = crit.list(); 
		if( list == null || list.size() == 0 ) {
			log.debug("Documents not found for department {}", deptName); 
		} else { 
			log.debug("{} Documents found for  department {}",  list.size(),deptName);
			for(Object o: list ){
				docs.add(  new DocumentResource((Document)o,session));
			} 
		}
		
		//transaction.commit();
		//session.close();
		
		return docs;
	}
	
	public DocumentResource getDocumentResource(String docName){
		log.debug("Document to select is {}", docName);
		DocumentResource docResource;
		//session = sessionFactory.openSession();
		//Transaction transaction = session.beginTransaction();
		
		Criteria crit = session.createCriteria(Document.class);
		crit.add(Expression.eq("FileName", docName));
		
		List list = crit.list(); 
		if( list == null || list.size() == 0 ) {
			log.debug("Document {} not found.", docName);
			return null;
		} else {
			Document doc = (Document) list.get(0);
			log.debug("Document {} found: " + doc.getFileName());
			docResource = new DocumentResource(doc, session);
		}
		
		//transaction.commit();
		//session.close();
		
		return docResource;
	}

}
