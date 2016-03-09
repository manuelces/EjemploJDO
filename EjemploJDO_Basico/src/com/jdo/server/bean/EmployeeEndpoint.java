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

@Api(name = "employeeendpoint", namespace = @ApiNamespace(ownerDomain = "jdo.com", ownerName = "jdo.com", packagePath = "server.bean"))
public class EmployeeEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listEmployee")
	public CollectionResponse<Employee> listEmployee(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		PersistenceManager mgr = null;
		Cursor cursor = null;
		List<Employee> execute = null;

		try {
			mgr = getPersistenceManager();
			Query query = mgr.newQuery(Employee.class);
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				HashMap<String, Object> extensionMap = new HashMap<String, Object>();
				extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
				query.setExtensions(extensionMap);
			}

			if (limit != null) {
				query.setRange(0, limit);
			}

			execute = (List<Employee>) query.execute();
			cursor = JDOCursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Employee obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Employee> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getEmployee")
	public Employee getEmployee(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		Employee employee = null;
		try {
			employee = mgr.getObjectById(Employee.class, id);
		} finally {
			mgr.close();
		}
		return employee;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param employee the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertEmployee")
	public Employee insertEmployee(Employee e) {
		PersistenceManager mgr = getPersistenceManager();
		try {		
			
		    //ContactInfo ci = e.getContactInfo();
		    //ContactInfo cii=new ContactInfoEndpoint().insertContactInfo(ci);
		    //e.setContactInfo(ci);
			mgr.makePersistent(e);
		} finally {
			mgr.close();
		}
		return e;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param employee the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateEmployee")
	public Employee updateEmployee(Employee employee) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			if (!containsEmployee(employee)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.makePersistent(employee);
		} finally {
			mgr.close();
		}
		return employee;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeEmployee")
	public void removeEmployee(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		try {
			Employee employee = mgr.getObjectById(Employee.class, id);
			mgr.deletePersistent(employee);
		} finally {
			mgr.close();
		}
	}

	private boolean containsEmployee(Employee employee) {
		PersistenceManager mgr = getPersistenceManager();
		boolean contains = true;
		try {
			mgr.getObjectById(Employee.class, employee.getId());
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
