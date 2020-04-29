package org.openmrs.module.gpconnect.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "nhs_patient")
public class NhsPatient {
	
	@Id
	@Column(name = "patient_id")
	Long id;
	
	@Column(name = "cadaveric_donor")
	public boolean cadavericDonor;
}
