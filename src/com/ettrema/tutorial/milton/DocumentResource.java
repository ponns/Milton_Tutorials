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

	//Current Lock object - holds the details of the current lock
	CurrentLock lock;
	 
	public DocumentResource(Document doc, Session session){  
		this.doc = doc ;
		this.session = session;
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

	@Override
	public void sendContent(OutputStream out, Range arg1,
			Map<String, String> arg2, String arg3) throws IOException,
			NotAuthorizedException, BadRequestException {
		// write the contents of the file to output
		out.write(this.doc.getContent());
	}

	@Override
	public Object authenticate(String user, String arg1) {
		// always return
		return user;
	}

	@Override
	public boolean authorise(Request arg0, Method arg1, Auth arg2) {
		// always allow
		return true;
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
		// return the current lock token 
		if( this.lock == null ) return null;
		LockToken token = new LockToken();
		token.info = this.lock.lockInfo;
		token.timeout = new LockTimeout( this.lock.seconds );
		token.tokenId = this.lock.lockId;

		return token;
	
	}

	@Override
	public LockResult lock(LockTimeout timeout, LockInfo lockInfo) throws NotAuthorizedException {
		
		LockTimeout.DateAndSeconds lockedUntil = timeout.getLockedUntil( 60l, 3600l );

		// create a new lock
		this.lock = new CurrentLock( lockedUntil.date, UUID.randomUUID().toString(), lockedUntil.seconds, lockInfo );

		LockToken token = new LockToken();
		token.info = lockInfo;
		token.timeout = new LockTimeout( lockedUntil.seconds );
		token.tokenId = this.lock.lockId;

		return LockResult.success( token );	
	}

	@Override
	public LockResult refreshLock(String token) throws NotAuthorizedException {
		
		// reset the current token
		if( lock == null )
			throw new RuntimeException( "not locked" );
		if( !lock.lockId.equals( token ) )
		    throw new RuntimeException( "invalid lock id" );
		this.lock = lock.refresh(); 
		
		//create the lock token with new details
		LockToken lockToken = new LockToken();
		lockToken.info = lock.lockInfo;
		lockToken.timeout = new LockTimeout( lock.seconds );
		lockToken.tokenId = lock.lockId;

		return LockResult.success( lockToken );
 
	}

	@Override
	public void unlock(String tokenId) throws NotAuthorizedException {
		// unlock
		if( lock == null ) {
		    log.warn( "request to unlock not locked resource" );
		    return;
		}
		
		if( !lock.lockId.equals( tokenId ) ){
			log.warn( "Invalid Lock Token" );
			throw new RuntimeException( "Invalid lock token" );
		}
		    
		this.lock = null;
	}

	@Override
	public void setProperties(Fields arg0) {
		// This method is necessary because of PropPatchableResource
		// but actual property update needs to be done....
		// TODO what is the necessity for implementing  PropPatchableResource class ?
		
	}
  
	class CurrentLock {

	    final Date lockedUntil;
	    final String lockId;
	    final long seconds;
	    final LockInfo lockInfo;

	    public CurrentLock( Date lockedUntil, String lockId, long seconds, LockInfo lockInfo ) {
	        this.lockedUntil = lockedUntil;
	        this.lockId = lockId;
	        this.seconds = seconds;
	        this.lockInfo = lockInfo;
	    }

	    CurrentLock refresh() {
	        Date dt = Utils.addSeconds( Utils.now(), seconds );
	        return new CurrentLock( dt, lockId, seconds, lockInfo );
	    }
	}

	// return the all property names as list
	@Override
	public List<QName> getAllPropertyNames() {
		// Since namespace is blank here. Just the local part of the property name
		
		List<QName> localPropNames = new ArrayList<QName>();
		Iterator<?> i = this.doc.getAllProperties().entrySet().iterator();
		
		while(i.hasNext()){
			Map.Entry<String, String> pair = (Map.Entry<String, String>) i.next() ;
			localPropNames.add( new QName(pair.getKey()) );
		}
		 
		return localPropNames;
	}

	// return the value of the property
	@Override
	public Object getProperty(QName name) {
		return this.doc.getAllProperties().get(name.getLocalPart());
	}

	// return the metadata of the property
	@Override
	public PropertyMetaData getPropertyMetaData(QName name) {
		
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