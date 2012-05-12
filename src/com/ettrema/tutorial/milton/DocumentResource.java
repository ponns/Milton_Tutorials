package com.ettrema.tutorial.milton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream; 
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

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
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.bradmcevoy.property.MultiNamespaceCustomPropertyResource;
import com.bradmcevoy.property.PropertySource;
import com.bradmcevoy.property.PropertySource.PropertyAccessibility;
import com.bradmcevoy.property.PropertySource.PropertyMetaData;
import com.ettrema.http.fs.SimpleLockManager;
import com.ettrema.http.fs.SimpleSecurityManager;
import com.ettrema.tutorial.objects.Department;
import com.ettrema.tutorial.objects.Document;

public class DocumentResource implements GetableResource,
	PropFindableResource, DeletableResource, MoveableResource,
	CopyableResource, ReplaceableResource, LockableResource , 
	PropPatchableResource, MultiNamespaceCustomPropertyResource {

	//Initialize the Logger object
	private Logger log = LoggerFactory.getLogger(DocumentResource.class);
	Session session; 
	Document doc;
	MyResourceFactory resourceFactory;
  	 
	public DocumentResource(Document doc, Session session){  
		this.doc = doc ;
		this.session = session;
	}
	
	public DocumentResource(Document doc, Session session, MyResourceFactory resourceFactory ){  
		this.doc = doc ;
		this.session = session;
		this.resourceFactory = resourceFactory ;
	}
	
	@Override
	public Long getContentLength() {
		// Return the content length
		return Long.parseLong(this.doc.getContent().length + "");
	}

	@Override
	public String getContentType(String arg0) {
		return null;
	}

	@Override
	public Long getMaxAgeSeconds(Auth arg0) { 
		return null;
	}
	
	// false -  not locked recursively and hence it can be deleted.
	// true - this resource or child resource is locked. Hence delete request 
	//    should not be completed.
	public boolean isLockedOutRecursive(Request request){
		if (request == null ){
			return false;
		} 
		LockToken token = this.getCurrentLock();
		if( token != null ) {
			Auth auth = request.getAuthorization();
			String lockedByUser = token.info.lockedByUser;
			if( lockedByUser == null ) {
				log.warn( "Resource is locked with a null user. Ignoring the lock" );
				return false;
			} else if( !lockedByUser.equals( auth.getUser() ) ) {
				log.info( "fail: lock owned by: " + lockedByUser + " not by " + auth.getUser() );
				String value = request.getIfHeader();
				if( value != null ) {
					if( value.contains( "opaquelocktoken:" + token.tokenId + ">" ) ) {
						log.info( "Contained valid token. so is unlocked" );
						return false;
					}
				}
				return true; 
			}
		}
		return false;
	}

	@Override
	public void sendContent(OutputStream out, Range arg1,
			Map<String, String> arg2, String arg3) throws IOException,
			NotAuthorizedException, BadRequestException {
		// write the contents of the file to output
		out.write(this.doc.getContent());
	}

	@Override
	public Object authenticate(String user, String pwd) {
		// always return
		return resourceFactory.getSecurityManager().authenticate(user, pwd);
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth auth) {
		if (auth == null){
			return false;
		}else{
			return true;	
		} 
	}

	@Override
	public String checkRedirect(Request arg0) {
		// no redirect
		return null;
	}

	@Override
	public Date getModifiedDate() {
		// return the modified date
		return doc.getModifiedDate();
	}

	@Override
	public String getName() {
		// return the File name
		return doc.getFileName();
	}

	@Override
	public String getRealm() {
		return resourceFactory.getSecurityManager().getRealm("");
	}

	@Override
	public String getUniqueId() {
		// make sure to return the unique name to identify the resource
		return this.doc.getFileName();
	}

	@Override
	public Date getCreateDate() {
		// Return the created date
		return doc.getCreatedDate();
	}

	@Override
	public void delete() { 
		// Delete the document
		Transaction transaction = session.beginTransaction(); 
		session.delete(doc);
		transaction.commit(); 
		log.debug("Document {} is deleted successfully.", doc.getFileName());
	}


	@Override
	public void replaceContent(InputStream in, Long length) {
		// Update the document 
		try {
			Transaction transaction = session.beginTransaction(); 
			byte[] bFile = new byte[Integer.parseInt(length.toString())];
			in.read(bFile);
			doc.setContent(bFile); 
			doc.setModifiedDate(new Date());
			session.update(doc);
			transaction.commit(); 
			log.debug("Updated {} successfully",doc.getFileName());
		} catch (IOException e) { 
			log.error("Error occured while updating file {}. Error is " + e.getMessage(), doc.getFileName());
		}
	}
    
	@Override
	public void moveTo(CollectionResource parent, String newName)
			throws ConflictException {
		// Move
		String newDeptName = parent.getName() ;
		String newFileName = newName;
		log.debug("parent: {}, new name: {}, old Name:" + this.doc.getFileName(), newDeptName, newFileName );
		
		Transaction transaction = session.beginTransaction(); 
		doc.setFileName(newFileName);
		doc.setDeptName(newDeptName);
		doc.setCreatedDate(new Date());
		session.update(doc);
		transaction.commit(); 
		log.debug("File {} moved Successfulle", newFileName);
	}
	
	@Override
	public void copyTo(CollectionResource parent, String newName) {
		// Copy
		String newDeptName = parent.getName() ;
		String newFileName = newName;
		log.debug("parent: {}, new name: {}, old Name:" + this.doc.getFileName(), newDeptName, newFileName );
		
		Transaction transaction = session.beginTransaction(); 
		Document docx = new Document();
		docx.setFileName(newFileName);
		docx.setDeptName(newDeptName);
		docx.setContent(doc.getContent()); 
		docx.setCreatedDate(new Date());
		session.save(docx);
		transaction.commit(); 
		log.debug("File {} copied Successfulle", newFileName);
	}

	@Override
	public LockToken getCurrentLock() {
		return this.resourceFactory.getLockManager().getCurrentToken(this);
	}

	@Override
	public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
		return this.resourceFactory.getLockManager().lock(timeout, lockInfo, this); 
	}

	@Override
	public LockResult refreshLock(String token) throws NotAuthorizedException {
		 return this.resourceFactory.getLockManager().refresh(token, this); 
	}

	@Override
	public void unlock(String tokenId) throws NotAuthorizedException {
		this.resourceFactory.getLockManager().unlock(tokenId, this);
	}

	@Override
	public void setProperties(Fields arg0) {
		// This method is necessary because of PropPatchableResource
		// but actual property update needs to be done....
		// TODO what is the necessity for implementing  PropPatchableResource class ?
		
	}
    
	// return the all property names as list
	@Override
	public List<QName> getAllPropertyNames() {
		// Since namespace is blank here. Just the local part of the property name
		log.debug("Inside getallprop names...");
		List<QName> localPropNames = new ArrayList<QName>();
		Iterator<?> i = this.doc.getAllProperties().entrySet().iterator();
		
		while(i.hasNext()){
			Map.Entry<String, String> pair = (Map.Entry<String, String>) i.next() ;
			localPropNames.add( new QName(pair.getKey()) );
		}
		 
		log.debug("localPropNames length:" + localPropNames.size());
		return localPropNames;
	}

	// return the value of the property
	@Override
	public Object getProperty(QName name) {
		log.debug("Get the specific property");
		return this.doc.getAllProperties().get(name.getLocalPart());
	}

	// return the metadata of the property
	@Override
	public PropertyMetaData getPropertyMetaData(QName name) {
		log.debug("Get the property metadata");
		//check whether property is present
		if (this.doc.getAllProperties().containsKey(name.getLocalPart()) ){
			
			// if it is modified or created date, then retun the datatype as date
			if ( name.getLocalPart().equals("ModifiedDate") 
					|| name.getLocalPart().equals("CreatedDate") ) {
				return new PropertyMetaData( PropertyAccessibility.READ_ONLY, Date.class );
			}else
				return new PropertyMetaData( PropertyAccessibility.WRITABLE, String.class );
			
		}else{
			return PropertyMetaData.UNKNOWN;
		}
		
	}

	//update the property
	@Override
	public void setProperty(QName name, Object value) {
		  
		Transaction transaction = session.beginTransaction(); 
		this.doc.setModifiedDate(new Date());
		this.doc.setProperty(name.getLocalPart(), (String) value) ;
		session.update(this.doc);
		transaction.commit(); 
		log.debug("Property {} updated Successfully", name.getLocalPart() );
		
	}
 

}