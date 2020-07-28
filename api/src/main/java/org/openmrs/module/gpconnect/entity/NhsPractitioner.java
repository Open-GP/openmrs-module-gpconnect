package org.openmrs.module.gpconnect.entity;


import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = "provider")
@EqualsAndHashCode
@ToString
@Setter
public class NhsPractitioner {

    @Id
    @Column(name="provider_id")
    Long id;


    @Column(name="sds_role_profile_id")
    public String roleProfileId;

}
