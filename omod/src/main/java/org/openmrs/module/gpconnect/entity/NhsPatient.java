package org.openmrs.module.gpconnect.entity;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "nhs_patient")
@EqualsAndHashCode
@ToString
@Setter
public class NhsPatient {
	
	@Id
	@Column(name = "patient_id")
	Long id;
	
	@Column(name = "cadaveric_donor")
	public boolean cadavericDonor;
	
	@Column(name = "nhs_number")
	public String nhsNumber;
	
	@Column(name = "nhs_number_verification_status")
	public String nhsNumberVerificationStatus;
	
	@Column(name = "ethnic_category")
	public String ethnicCategory;
}
