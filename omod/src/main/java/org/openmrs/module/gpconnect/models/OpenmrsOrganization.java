package org.openmrs.module.gpconnect.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.openmrs.Auditable;
import org.openmrs.OpenmrsObject;
import org.openmrs.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity(name = "organization")
@EqualsAndHashCode
@ToString
@Getter
public class OpenmrsOrganization implements OpenmrsObject, Auditable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "organization_id")
	Integer id;
	
	@Column
	String uuid;
	
	@Column
	String name;
	
	@Override
	public Integer getId() {
		return this.id;
	}
	
	@Override
	public void setId(Integer integer) {
		this.id = integer;
	}
	
	@Override
	public String getUuid() {
		return this.uuid;
	}
	
	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public User getCreator() {
		return null;
	}
	
	@Override
	public void setCreator(User user) {
		
	}
	
	@Override
	public Date getDateCreated() {
		return null;
	}
	
	@Override
	public void setDateCreated(Date date) {
		
	}
	
	@Override
	public User getChangedBy() {
		return null;
	}
	
	@Override
	public void setChangedBy(User user) {
		
	}
	
	@Override
	public Date getDateChanged() {
		return null;
	}
	
	@Override
	public void setDateChanged(Date date) {
		
	}
}
