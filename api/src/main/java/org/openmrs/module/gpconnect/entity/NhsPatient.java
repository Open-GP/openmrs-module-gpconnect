package org.openmrs.module.gpconnect.entity;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity(name = "nhs_patient")
@EqualsAndHashCode
@ToString
@Setter
public class NhsPatient {
	
	@Id
	@Column(name = "patient_id")
	Long id;
	
	@Column(name = "nhs_number")
	public String nhsNumber;
	
	@Column(name = "nhs_number_verification_status")
	public String nhsNumberVerificationStatus;
	
	@Column(name = "ethnic_category")
	public String ethnicCategory;
	
	@Column(name = "residential_status")
	public String residentialStatus;
	
	@Column(name = "treatment_category")
	public String treatmentCategory;
	
	@Column(name = "registration_start")
	public Date registrationStart;
	
	@Column(name = "registration_end")
	public Date registrationEnd;
	
	@Column(name = "registration_type")
	public String registrationType;
	
	@Column(name = "preferred_branch")
	public String preferredBranch;
	
	@Column(name = "death_notification_status")
	public String deathNotificationStatus;
}
