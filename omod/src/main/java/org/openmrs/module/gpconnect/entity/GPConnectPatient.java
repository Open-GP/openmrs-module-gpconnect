package org.openmrs.module.gpconnect.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "patient")
public class GPConnectPatient {
	
	@Id
	@Column(name = "patient_id")
	private Long id;
}