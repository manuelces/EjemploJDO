package com.jdo.server.bean;

import com.jdo.server.bean.PMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JDOCursorHelper;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

@Api(name = "contactinfoendpoint", namespace = @ApiNamespace(ownerDomain = "jdo.com", ownerName = "jdo.com", packagePath = "server.bean"))
public class ContactInfoEndpoint {

	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listContactInfo")
	public CollectionResponse<ContactInfo> listContactInfo(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<ContactInfo> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(ContactInfo.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<ContactInfo>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (ContactInfo obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<ContactInfo> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	@ApiMethod(name = "getContactInfo")
	public ContactInfo getContactInfo(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		ContactInfo contactinfo = null;
		try {
			contactinfo = mgr.getObjectById(ContactInfo.class, id);
		} finally {
			mgr.close();
		}
		return contactinfo;
	}

	@ApiMethod(name = "insertContactInfo")
	public ContactInfo insertContactInfo(ContactInfo contactinfo) {
		PersistenceManager mgr = getPersistenceManager();
		try {			
			mgr.makePersistent(contactinfo);
		} finally {
			mgr.close();
		}
		return contactinfo;
	}

	@ApiMethod(name = "updateContactInfo")
	public ContactInfo updateContactInfo(ContactInfo contactinfo) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsContactInfo(contactinfo)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(contactinfo);
		} finally {
			mgr.close();
		}
		return contactinfo;
	}


	@ApiMethod(name = "removeContactInfo")
	public void removeContactInfo(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			ContactInfo contactinfo = mgr.getObjectById(ContactInfo.class, id);
			mgr.deletePersistent(contactinfo);
		} finally {
			mgr.close();
		}
	}

	private boolean containsContactInfo(ContactInfo contactinfo) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(ContactInfo.class, contactinfo.getKey());
		} catch (javax.jdo.JDOObjectNotFoundException ex) {
			contains = false;
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}

}
