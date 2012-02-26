package com.ettrema.tutorial.milton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream; 
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.ettrema.tutorial.objects.Department;
import com.ettrema.tutorial.objects.Document;

public class DocumentResource implements 
	PropFindableResource, GetableResource, DeletableResource, MoveableResource,
	CopyableResource, ReplaceableResource{

	private Logger log = LoggerFactory.getLogger(DocumentResource.class);
	Session session;
	Document doc;
	
	public DocumentResource(Document doc, Session session){
		this.doc = doc ;
		this.session = session;
	}
	
	@Override
	public Long getContentLength() {
		// TODO Auto-generated method stub
		return Long.parseLong(this.doc.getContent().length + "");
	}

	@Override
	public String getContentType(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getMaxAgeSeconds(Auth arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendContent(OutputStream out, Range arg1,
			Map<String, String> arg2, String arg3) throws IOException,
			NotAuthorizedException, BadRequestException {
		// TODO Auto-generated method stub
		out.write(this.doc.getContent());
	}

	@Override
	public Object authenticate(String user, String arg1) {
		// TODO Auto-generated method stub
		return user;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		// TODO Auto-generated method stub
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
		return doc.getFileName();
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
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete() { 
		// TODO Auto-generated method stub
		Transaction transaction = session.beginTransaction(); 
		session.delete(doc);
		transaction.commit(); 
		log.debug("Document {} is deleted successfully.", doc.getFileName());
	}


	@Override
	public void replaceContent(InputStream in, Long length) {
		// TODO Auto-generated method stub
		
		try {
			Transaction transaction = session.beginTransaction(); 
			byte[] bFile = new byte[Integer.parseInt(length.toString())];
			in.read(bFile);
			doc.setContent(bFile); 
			session.update(doc);
			transaction.commit(); 
			log.debug("Updated {} successfully",doc.getFileName());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Error occured while updating file {}. Error is " + e.getMessage(), doc.getFileName());
			
		}
		
	}
    
	@Override
	public void moveTo(CollectionResource parent, String newName)
			throws ConflictException {
		// TODO Auto-generated method stub
		String newDeptName = parent.getName() ;
		String newFileName = newName;
		log.debug("parent: {}, new name: {}, old Name:" + this.doc.getFileName(), newDeptName, newFileName );
		
		Transaction transaction = session.beginTransaction(); 
		doc.setFileName(newFileName);
		doc.setDeptName(newDeptName);
		session.update(doc);
		transaction.commit(); 
		log.debug("File {} moved Successfulle", newFileName);
	}
	
	@Override
	public void copyTo(CollectionResource parent, String newName) {
		// TODO Auto-generated method stub
		String newDeptName = parent.getName() ;
		String newFileName = newName;
		log.debug("parent: {}, new name: {}, old Name:" + this.doc.getFileName(), newDeptName, newFileName );
		
		Transaction transaction = session.beginTransaction(); 
		Document docx = new Document();
		docx.setFileName(newFileName);
		docx.setDeptName(newDeptName);
		docx.setContent(doc.getContent()); 
		session.save(docx);
		transaction.commit(); 
		log.debug("File {} copied Successfulle", newFileName);
	}

}