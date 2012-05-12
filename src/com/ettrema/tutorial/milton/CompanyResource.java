package com.ettrema.tutorial.milton;
 
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.tutorial.objects.Department;

public class CompanyResource implements PropFindableResource, 
				CollectionResource, MakeCollectionableResource{
	private Logger log = LoggerFactory.getLogger(CompanyResource.class);
	List<DepartmentResource> allDepartments;
	
	Session session;
	
	public CompanyResource(List<DepartmentResource> allDepartments, Session session){
		this.allDepartments = allDepartments ;
		this.session = session;
	}
	
	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object authenticate(String user, String password) {
		// always allow
		return user;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth auth) {
		// Always return true so that no authorization required.
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource child(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Resource> getChildren() {
		// Return all Departments 
		return this.allDepartments ;
	}

	@Override
	public CollectionResource createCollection(String name)
			throws NotAuthorizedException, ConflictException { 
		//Begin the Hibernate transaction
		Transaction transaction = session.beginTransaction();
		 
		//Create the Criteria with the conditional Expression to get the 
		//Department details from Database
		Criteria crit = session.createCriteria(Department.class);
		crit.add(Expression.eq("Name", name));
		List list = crit.list();
		
		// Check whether Department already exists
		if( list == null || list.size() == 0 ) {
			// if not, Create a new one.
			log.debug("Department {} not found. Hence creating it.", name);
			Department dept = new Department(name); 
			session.save(dept);
			transaction.commit();
			DepartmentResource deptResource = new DepartmentResource(dept, session);
			return deptResource;
		} else { 
			// If already present, Just the log the message
			log.debug("Department {} already exists ", name);  
			return null ;
		} 
	}


}
