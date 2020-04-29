package org.openmrs.module.gpconnect.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "patient")
public class GPConnectPatient {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long patient_id;
}
