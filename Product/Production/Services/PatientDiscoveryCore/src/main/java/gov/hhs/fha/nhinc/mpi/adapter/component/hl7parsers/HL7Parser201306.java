/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.mpi.adapter.component.hl7parsers;

import gov.hhs.fha.nhinc.mpilib.Address;
import gov.hhs.fha.nhinc.mpilib.Identifier;
import gov.hhs.fha.nhinc.mpilib.Patient;
import gov.hhs.fha.nhinc.mpilib.Patients;
import gov.hhs.fha.nhinc.mpilib.PersonName;
import gov.hhs.fha.nhinc.mpilib.PhoneNumber;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.transform.subdisc.HL7Constants;
import gov.hhs.fha.nhinc.transform.subdisc.HL7DataTransformHelper;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hl7.v3.ADExplicit;
import org.hl7.v3.ActClassControlAct;
import org.hl7.v3.AdxpExplicitCity;
import org.hl7.v3.AdxpExplicitCountry;
import org.hl7.v3.AdxpExplicitPostalCode;
import org.hl7.v3.AdxpExplicitState;
import org.hl7.v3.AdxpExplicitStreetAddressLine;
import org.hl7.v3.CD;
import org.hl7.v3.CE;
import org.hl7.v3.COCTMT090003UV01AssignedEntity;
import org.hl7.v3.COCTMT090300UV01AssignedDevice;
import org.hl7.v3.COCTMT150002UV01Organization;
import org.hl7.v3.COCTMT150003UV03Organization;
import org.hl7.v3.CS;
import org.hl7.v3.CommunicationFunctionType;
import org.hl7.v3.EntityClassDevice;
import org.hl7.v3.II;
import org.hl7.v3.INT;
import org.hl7.v3.MCCIMT000100UV01Receiver;
import org.hl7.v3.MCCIMT000100UV01Sender;
import org.hl7.v3.MCCIMT000300UV01Acknowledgement;
import org.hl7.v3.MCCIMT000300UV01Agent;
import org.hl7.v3.MCCIMT000300UV01Device;
import org.hl7.v3.MCCIMT000300UV01Organization;
import org.hl7.v3.MCCIMT000300UV01Receiver;
import org.hl7.v3.MCCIMT000300UV01Sender;
import org.hl7.v3.MFMIMT700711UV01AuthorOrPerformer;
import org.hl7.v3.MFMIMT700711UV01Custodian;
import org.hl7.v3.MFMIMT700711UV01QueryAck;
import org.hl7.v3.PNExplicit;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01ControlActProcess;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01Subject1;
import org.hl7.v3.PRPAIN201306UV02MFMIMT700711UV01Subject2;
import org.hl7.v3.PRPAMT201310UV02OtherIDs;
import org.hl7.v3.PRPAMT201310UV02Patient;
import org.hl7.v3.PRPAMT201310UV02Person;
import org.hl7.v3.PRPAMT201310UV02QueryMatchObservation;
import org.hl7.v3.PRPAMT201310UV02Subject;
import org.hl7.v3.ParticipationTargetSubject;
import org.hl7.v3.TELExplicit;
import org.hl7.v3.TSExplicit;
import org.hl7.v3.XActMoodIntentEvent;
import org.hl7.v3.XParticipationAuthorPerformer;

/**
 *
 * @author Jon Hoppesch
 */
public class HL7Parser201306 {

    private static final Logger LOG = Logger.getLogger(HL7Parser201306.class);
    private static final String PROPERTY_FILE = "adapter";
    private static final String PROPERTY_NAME = "assigningAuthorityId";
    private static final String DEFAULT_AA_OID = "1.1";
    
    /**
     * Method to build a PRPAIN201306UV02 froma given list of Patients and a PRPAIN201305UV02 object.
     *
     * @param patients A list of patients from which to build the PRPAIN201306UV02 for.
     * @param query the PRPAIN201305UV02 object
     * @return a PRPAIN201306UV02 object
     */
    public static PRPAIN201306UV02 buildMessageFromMpiPatient(Patients patients, PRPAIN201305UV02 query) {
        LOG.trace("Entering HL7Parser201306.BuildMessageFromMpiPatient method...");
        
        PRPAIN201306UV02 msg = buildMessageFromMpiPatientMatchHcidIdentifiers(patients, query, null);

        LOG.trace("Exiting HL7Parser201306.BuildMessageFromMpiPatient method...");
        return msg;
    }
    
    /**
     * Method to build a PRPAIN201306UV02 from given list of Patients and a PRPAIN201305UV02 object.
     *
     * @param patients A list of patients from which to build the PRPAIN201306UV02 for.
     * @param query the PRPAIN201305UV02 object
     * @param sOrgOid Organization OID to match when putting in the identifier. 
     * @return a PRPAIN201306UV02 object
     */
    public static PRPAIN201306UV02 buildMessageFromMpiPatientMatchHcidIdentifiers(Patients patients, PRPAIN201305UV02 query, String sOrgOid) {
        LOG.trace("Entering HL7Parser201306.buildMessageFromMpiPatientMatchHcidIdentifiers method...");
        
        LOG.trace("Called buildMessageFromMpiPatientMatchHcidIdentifiers with Organization OID = " + sOrgOid);

        PRPAIN201306UV02 msg = new PRPAIN201306UV02();

        // Set up message header fields
        msg.setITSVersion("XML_1.0");

        II id = new II();
        try {
            id.setRoot(PropertyAccessor.getInstance().getProperty(PROPERTY_FILE, PROPERTY_NAME));
        } catch (PropertyAccessException e) {
            LOG.error(
                "PropertyAccessException - Default Assigning Authority property not defined in adapter.properties",
                e);
            id.setRoot(DEFAULT_AA_OID);
            // CONNECT environment corrupt; return error response
            // return BuildMessageForError(<ERROR_CODE>, query);
        }
        id.setExtension(MessageIdGenerator.generateMessageId());
        msg.setId(id);

        // Set up the creation time string
        String timestamp = "";
        try {
            GregorianCalendar today = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

            timestamp =
                String.valueOf(today.get(GregorianCalendar.YEAR))
                + String.valueOf(today.get(GregorianCalendar.MONTH) + 1)
                + String.valueOf(today.get(GregorianCalendar.DAY_OF_MONTH))
                + String.valueOf(today.get(GregorianCalendar.HOUR_OF_DAY))
                + String.valueOf(today.get(GregorianCalendar.MINUTE))
                + String.valueOf(today.get(GregorianCalendar.SECOND));
        } catch (Exception e) {
            LOG.error("Exception when creating XMLGregorian Date");
            LOG.error(" message: " + e.getMessage());
        }

        TSExplicit creationTime = new TSExplicit();
        creationTime.setValue(timestamp);
        msg.setCreationTime(creationTime);

        II interactionId = new II();
        interactionId.setRoot("2.16.840.1.113883.1.6");
        interactionId.setExtension("PRPA_IN201306UV02");
        msg.setInteractionId(interactionId);

        CS processingCode = new CS();
        processingCode.setCode("P");
        msg.setProcessingCode(processingCode);

        CS processingModeCode = new CS();
        processingModeCode.setCode("T");
        msg.setProcessingModeCode(processingModeCode);

        CS ackCode = new CS();
        ackCode.setCode("NE");
        msg.setAcceptAckCode(ackCode);

        msg.getAcknowledgement().add(createAck(query));

        // Set the receiver and sender
        msg.getReceiver().add(createReceiver(query.getSender()));
        msg.setSender(createSender(query.getReceiver().get(0)));

        msg.setControlActProcess(createControlActProcess(patients, query, sOrgOid));

        LOG.trace("Exiting HL7Parser201306.buildMessageFromMpiPatientMatchHcidIdentifiers method...");
        return msg;
    }

    private static PRPAIN201306UV02MFMIMT700711UV01ControlActProcess createControlActProcess(Patients patients,
            PRPAIN201305UV02 query, String sOrgOid) {
        PRPAIN201306UV02MFMIMT700711UV01ControlActProcess controlActProcess =
            new PRPAIN201306UV02MFMIMT700711UV01ControlActProcess();

        controlActProcess.setMoodCode(XActMoodIntentEvent.EVN);
        controlActProcess.setClassCode(ActClassControlAct.CACT);
        CD code = new CD();
        code.setCode("PRPA_TE201306UV");
        code.setCodeSystem("2.16.840.1.113883.1.6");
        controlActProcess.setCode(code);

        if ((patients != null) && (patients.size() > 0)) {
            for (Patient patient : patients) {
                controlActProcess.getSubject().add(createSubject(patient, query, sOrgOid));
            }
        } else {
            LOG.info("createControlActProcess - No patients found to create subject");
        }

        // Add in query parameters
        if (query.getControlActProcess() != null && query.getControlActProcess().getQueryByParameter() != null
            && query.getControlActProcess().getQueryByParameter().getValue() != null) {
            controlActProcess.setQueryByParameter(query.getControlActProcess().getQueryByParameter());
        }

        controlActProcess.setQueryAck(createQueryAck(query));

        // Set original QueryByParameter in response
        controlActProcess.setQueryByParameter(query.getControlActProcess().getQueryByParameter());
        
		// Create the AuthorOrPerformer block and add it to ControlActProcess
		MFMIMT700711UV01AuthorOrPerformer authorOrPerformer = new MFMIMT700711UV01AuthorOrPerformer();
        authorOrPerformer.setTypeCode(XParticipationAuthorPerformer.AUT);

        COCTMT090300UV01AssignedDevice assignedDevice = new COCTMT090300UV01AssignedDevice();
        II id = new II();
        try {
            id.setRoot(PropertyAccessor.getInstance().getProperty(PROPERTY_FILE, PROPERTY_NAME));
        } catch (PropertyAccessException e) {
            LOG.error(
                "PropertyAccessException - Default Assigning Authority property not defined in adapter.properties",
                e);
            id.setRoot(DEFAULT_AA_OID);
            // CONNECT environment corrupt; return error response
            // return BuildMessageForError(<ERROR_CODE>, query);
        }
        
        assignedDevice.setClassCode(HL7Constants.ASSIGNED_DEVICE_CLASS_CODE);
        assignedDevice.getId().add(id);

        javax.xml.namespace.QName xmlqname = new javax.xml.namespace.QName("urn:hl7-org:v3", "assignedDevice");
        JAXBElement<COCTMT090300UV01AssignedDevice> assignedDeviceJAXBElement = new JAXBElement<COCTMT090300UV01AssignedDevice>(xmlqname, COCTMT090300UV01AssignedDevice.class, assignedDevice);

        authorOrPerformer.setAssignedDevice(assignedDeviceJAXBElement);
        controlActProcess.getAuthorOrPerformer().add(authorOrPerformer);        

		return controlActProcess;
    }

    private static MFMIMT700711UV01QueryAck createQueryAck(PRPAIN201305UV02 query) {
        MFMIMT700711UV01QueryAck result = new MFMIMT700711UV01QueryAck();

        if (query.getControlActProcess() != null && query.getControlActProcess().getQueryByParameter() != null
            && query.getControlActProcess().getQueryByParameter().getValue() != null
            && query.getControlActProcess().getQueryByParameter().getValue().getQueryId() != null) {
            result.setQueryId(query.getControlActProcess().getQueryByParameter().getValue().getQueryId());
        }

        CS respCode = new CS();
        respCode.setCode("OK");
        result.setQueryResponseCode(respCode);

        return result;
    }

    private static PRPAIN201306UV02MFMIMT700711UV01Subject1 createSubject(Patient patient, PRPAIN201305UV02 query, String sOrgOid) {
        PRPAIN201306UV02MFMIMT700711UV01Subject1 subject = new PRPAIN201306UV02MFMIMT700711UV01Subject1();

        subject.getTypeCode().add("SUBJ");

        subject.setRegistrationEvent(createRegEvent(patient, query, sOrgOid));

        return subject;
    }

    private static PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent createRegEvent(Patient patient,
            PRPAIN201305UV02 query, String sOrgOid) {
        PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent regEvent =
            new PRPAIN201306UV02MFMIMT700711UV01RegistrationEvent();
        regEvent.getMoodCode().add("EVN");
        regEvent.getClassCode().add("REG");
        II id = new II();
        id.getNullFlavor().add("NA");
        regEvent.getId().add(id);

        CS statusCode = new CS();
        statusCode.setCode("active");

        regEvent.setStatusCode(statusCode);

        regEvent.setSubject1(createSubject1(patient, query, sOrgOid));

        regEvent.setCustodian(createCustodian());

        return regEvent;
    }

    private static MFMIMT700711UV01Custodian createCustodian() {
        MFMIMT700711UV01Custodian result = new MFMIMT700711UV01Custodian();
        result.getTypeCode().add("CST");
        result.setAssignedEntity(createAssignEntity());

        return result;
    }

    private static COCTMT090003UV01AssignedEntity createAssignEntity() {
        COCTMT090003UV01AssignedEntity assignedEntity = new COCTMT090003UV01AssignedEntity();
        assignedEntity.setClassCode(HL7Constants.ASSIGNED_DEVICE_CLASS_CODE);
        II id = new II();
        id.setRoot(HomeCommunityMap.formatHomeCommunityId(HomeCommunityMap.getLocalHomeCommunityId()));
        assignedEntity.getId().add(id);
        CE ce = new CE();
        ce.setCode("NotHealthDataLocator");
        ce.setCodeSystem("1.3.6.1.4.1.19376.1.2.27.2");
        assignedEntity.setCode(ce);

        return assignedEntity;
    }

    private static PRPAIN201306UV02MFMIMT700711UV01Subject2 createSubject1(Patient patient, PRPAIN201305UV02 query, String sOrgOid) {
        PRPAIN201306UV02MFMIMT700711UV01Subject2 subject = new PRPAIN201306UV02MFMIMT700711UV01Subject2();
        subject.setTypeCode(ParticipationTargetSubject.SBJ);
        // Add in patient
        subject.setPatient(createPatient(patient, query, sOrgOid));

        return subject;
    }

    private static PRPAMT201310UV02Patient createPatient(Patient patient, PRPAIN201305UV02 query, String sOrgOid) {
        PRPAMT201310UV02Patient subjectPatient = new PRPAMT201310UV02Patient();

        subjectPatient.getClassCode().add("PAT");

        CS statusCode = new CS();
        statusCode.setCode("SD");
        subjectPatient.setStatusCode(statusCode);

        // Add in patient id
        subjectPatient.getId().add(createSubjectId(patient, sOrgOid));

        // Add in patient person
        subjectPatient.setPatientPerson(createPatientPerson(patient, query));

        // Add in provider organization
        subjectPatient.setProviderOrganization(createProviderOrg(patient));

        // Add in query match observation
        subjectPatient.getSubjectOf1().add(createSubjectOf1());

        return subjectPatient;
    }

    private static PRPAMT201310UV02Subject createSubjectOf1() {
        PRPAMT201310UV02Subject result = new PRPAMT201310UV02Subject();

        result.setQueryMatchObservation(createQueryMatch());
        return result;
    }

    private static PRPAMT201310UV02QueryMatchObservation createQueryMatch() {
        PRPAMT201310UV02QueryMatchObservation queryMatch = new PRPAMT201310UV02QueryMatchObservation();
        queryMatch.getMoodCode().add("EVN");
        queryMatch.getClassCode().add("CASE");
        CD code = new CD();
        code.setCode("IHE_PDQ");
        queryMatch.setCode(code);

        INT intValue = new INT();
        //CHECKSTYLE:OFF
        intValue.setValue(BigInteger.valueOf(100));
        //CHECKSTYLE:ON
        queryMatch.setValue(intValue);

        return queryMatch;
    }

    private static JAXBElement<COCTMT150003UV03Organization> createProviderOrg(Patient patient) {
        COCTMT150003UV03Organization org = new COCTMT150003UV03Organization();
        org.setDeterminerCode("INSTANCE");
        org.setClassCode("ORG");
        II id = new II();

        if (patient.getIdentifiers() != null && patient.getIdentifiers().size() > 0
            && patient.getIdentifiers().get(0).getOrganizationId() != null
            && patient.getIdentifiers().get(0).getOrganizationId().length() > 0) {
            id.setRoot(HomeCommunityMap.formatHomeCommunityId(patient.getIdentifiers().get(0).getOrganizationId()));
        }
        org.getId().add(id);

        org.getContactParty().add(null);

        javax.xml.namespace.QName xmlqname = new javax.xml.namespace.QName("urn:hl7-org:v3", "providerOrganization");
        JAXBElement<COCTMT150003UV03Organization> result =
            new JAXBElement<COCTMT150003UV03Organization>(xmlqname, COCTMT150003UV03Organization.class, org);

        return result;
    }

    private static II createSubjectId(Patient patient, String sOrgOid) {
        II id = new II();

        if ((patient != null) &&
        	(NullChecker.isNotNullish(patient.getIdentifiers()))) {
        	
        	Identifier oMatchingId = null;
        	
		    // If there is an organization ID, then locate the ID associated with that organization OID and use that
		    // or the primary ID.
		    //------------------------------------------------------------------------------------------------------
		    if (NullChecker.isNotNullish(sOrgOid)) {
		    	for (Identifier oId : patient.getIdentifiers()) {
		    		if (NullChecker.isNotNullish(oId.getOrganizationId()) &&
		    			sOrgOid.equals(oId.getOrganizationId())) {
		    			oMatchingId = oId;
		    			LOG.info("Found a Patient Id in the search result that matches the receiving organization's OID: " + sOrgOid);
		    			break;
		    		}
		    	}
		    }
		    
		    if (oMatchingId == null) {
		    	oMatchingId = patient.getIdentifiers().get(0);
		    }

            if (NullChecker.isNotNullish(oMatchingId.getOrganizationId())) {
                LOG.info("Setting Patient Id root in 201306: " + oMatchingId.getOrganizationId());
                id.setRoot(HomeCommunityMap.formatHomeCommunityId(oMatchingId.getOrganizationId()));
            }

            if (NullChecker.isNotNullish(oMatchingId.getId())) {
                LOG.info("Setting Patient Id extension in 201306: " + oMatchingId.getId());
                id.setExtension(oMatchingId.getId());
            }
        }
        else {
        	LOG.info("Patient Id was NOT set - there were no matching identifiers in the search information.");
        }
        	

        return id;
    }

    private static JAXBElement<PRPAMT201310UV02Person> createPatientPerson(Patient patient, PRPAIN201305UV02 query) {
        PRPAMT201310UV02Person person = new PRPAMT201310UV02Person();

        // Set classCode
        person.getClassCode().add("PSN");

        // Set determinerCode
        person.setDeterminerCode("INSTANCE");

        // Set the Subject Gender
        if (patient.getGender() != null && patient.getGender().length() > 0) {
            person.setAdministrativeGenderCode(createGender(patient));
        }

        // Set the Subject Name
        if (patient.getNames().size() > 0) {
            for (PersonName name : patient.getNames()) {
                person.getName().add(createSubjectName(name));
            }
        } else {
            person.getName().add(createSubjectName(patient));
        }

        // Set the Birth Time
        if (patient.getDateOfBirth() != null && patient.getDateOfBirth().length() > 0) {
            person.setBirthTime(createBirthTime(patient));
        }

        // Set the Address
        if (patient.getAddresses().size() > 0) {
            for (Address add : patient.getAddresses()) {
                person.getAddr().add(createAddress(add));
            }
        }

        // Set the phone Numbers
        if (patient.getPhoneNumbers().size() > 0) {
            for (PhoneNumber number : patient.getPhoneNumbers()) {
                TELExplicit tele = HL7DataTransformHelper.createTELExplicit(number.getPhoneNumber());

                person.getTelecom().add(tele);
            }
        }

        // Set the SSN
        if (patient.getSSN() != null && patient.getSSN().length() > 0) {
            person.getAsOtherIDs().add(createSsnId(patient));
        }

        // Put all of the identifiers into the "OtherIDs" field.
        //------------------------------------------------------
        if ((patient.getIdentifiers() != null) &&
            (patient.getIdentifiers().size() > 0))
        {
            List<PRPAMT201310UV02OtherIDs> asOtherIds = createOtherIds(patient);
            if ((asOtherIds != null) &&
                (asOtherIds.size() > 0))
            {
                person.getAsOtherIDs().addAll(asOtherIds);
            }
        }
        
        javax.xml.namespace.QName xmlqname = new javax.xml.namespace.QName("urn:hl7-org:v3", "patientPerson");
        JAXBElement<PRPAMT201310UV02Person> result =
            new JAXBElement<PRPAMT201310UV02Person>(xmlqname, PRPAMT201310UV02Person.class, person);

        return result;
    }

    /**
     * This method returns all of the identifiers in a way that they can be 
     * added into the AsOtherIdentifiers list.  
     * 
     * @param patient The retrieved information for the patient.
     * @return The list of OtherIds that was constructed.
     */
    private static List<PRPAMT201310UV02OtherIDs> createOtherIds (Patient patient)
    {
        ArrayList<PRPAMT201310UV02OtherIDs> listIds = new ArrayList<PRPAMT201310UV02OtherIDs>();

        if ((patient != null) &&
            (patient.getIdentifiers() != null) &&
            (patient.getIdentifiers().size() > 0))
        {
            for (Identifier singleId : patient.getIdentifiers())
            {
                if ((singleId.getOrganizationId() != null) &&
                    (singleId.getOrganizationId().length() > 0) &&
                    (singleId.getId() != null) &&
                    (singleId.getId().length() > 0))
                {
                    PRPAMT201310UV02OtherIDs asOtherId = new PRPAMT201310UV02OtherIDs();

                    // TODO: Temporary assignment until actual value can be determined
                    asOtherId.getClassCode().add("SD");
                    II ii = new II();
                    asOtherId.getId().add(ii);
                    ii.setRoot(singleId.getOrganizationId());
                    ii.setExtension(singleId.getId());
                    COCTMT150002UV01Organization scopingOrg = new COCTMT150002UV01Organization();
                    II scopingOrgIi = new II();
                    scopingOrgIi.setRoot(ii.getRoot());
                    scopingOrg.getId().add(scopingOrgIi);
                    asOtherId.setScopingOrganization(scopingOrg);
                    listIds.add(asOtherId);
                    LOG.debug("HL7Parser201306.createOtherIds: Added 'otherId' with root:" + ii.getRoot() + ", Extension: " + ii.getExtension());
                }
            }   // for (Identifier singleId : patient.getIdentifiers())
        }   // if ((patient != null) && ...
        
        return listIds;
    }

    private static PRPAMT201310UV02OtherIDs createSsnId(Patient patient) {
        PRPAMT201310UV02OtherIDs otherIds = new PRPAMT201310UV02OtherIDs();

        otherIds.getClassCode().add("SD");

        // Set the SSN
        if (patient.getSSN() != null && patient.getSSN().length() > 0) {
            II ssn = new II();
            ssn.setExtension(patient.getSSN());
//            ssn.setRoot("2.16.840.1.113883.4.1");
            ssn.setRoot(HL7Constants.SSN_ID_ROOT);
            LOG.info("Setting Patient SSN in 201306: " + patient.getSSN());
            otherIds.getId().add(ssn);

            COCTMT150002UV01Organization scopingOrg = new COCTMT150002UV01Organization();
            scopingOrg.setClassCode("ORG");
            scopingOrg.setDeterminerCode("INSTANCE");
            II orgId = new II();
            orgId.setRoot(ssn.getRoot());
            scopingOrg.getId().add(orgId);
            otherIds.setScopingOrganization(scopingOrg);
        }

        return otherIds;
    }

    private static TSExplicit createBirthTime(Patient patient) {
        TSExplicit birthTime = new TSExplicit();

        if (patient.getDateOfBirth() != null && patient.getDateOfBirth().length() > 0) {
            LOG.info("Setting Patient Birthday in 201306: " + patient.getDateOfBirth());
            birthTime.setValue(patient.getDateOfBirth());
        }

        return birthTime;
    }

    private static PNExplicit createSubjectName(Patient patient) {
        if (patient.getNames().size() > 0) {
            return createSubjectName(patient.getNames().get(0));
        }

        return createSubjectName(new PersonName());
    }

    private static PNExplicit createSubjectName(PersonName personName) {
        org.hl7.v3.ObjectFactory factory = new org.hl7.v3.ObjectFactory();
        PNExplicit name = (factory.createPNExplicit());

        String lastName = personName.getLastName();
        String firstName = personName.getFirstName();
        String middleName = personName.getMiddleName();
        String prefix = personName.getTitle();
        String suffix = personName.getSuffix();

        name = HL7DataTransformHelper.createPNExplicit(firstName, middleName, lastName, prefix, suffix);

        return name;
    }

    private static CE createGender(Patient patient) {
        CE gender = new CE();

        if (patient.getGender() != null && patient.getGender().length() > 0) {
            LOG.info("Setting Patient Gender in 201306: " + patient.getGender());
            gender.setCode(patient.getGender());
        }
        return gender;
    }

    private static MCCIMT000300UV01Acknowledgement createAck(PRPAIN201305UV02 query) {
        MCCIMT000300UV01Acknowledgement ack = new MCCIMT000300UV01Acknowledgement();
        ack.setTypeId(query.getInteractionId());

        CS typeCode = new CS();
        typeCode.setCode("AA");

        ack.setTypeCode(typeCode);

        return ack;
    }

    private static MCCIMT000300UV01Receiver createReceiver(MCCIMT000100UV01Sender querySender) {
        MCCIMT000300UV01Receiver receiver = new MCCIMT000300UV01Receiver();
        String app = null;
        String oid = null;

        receiver.setTypeCode(CommunicationFunctionType.RCV);

        if (querySender.getDevice() != null && NullChecker.isNotNullish(querySender.getDevice().getId())
            && querySender.getDevice().getId().get(0) != null
            && NullChecker.isNotNullish(querySender.getDevice().getId().get(0).getRoot())) {
            app = querySender.getDevice().getId().get(0).getRoot();
        }

        if (querySender.getDevice() != null
            && querySender.getDevice().getAsAgent() != null
            && querySender.getDevice().getAsAgent().getValue() != null
            && querySender.getDevice().getAsAgent().getValue().getRepresentedOrganization() != null
            && querySender.getDevice().getAsAgent().getValue().getRepresentedOrganization().getValue() != null
            && NullChecker.isNotNullish(querySender.getDevice().getAsAgent().getValue()
            .getRepresentedOrganization().getValue().getId())
            && querySender.getDevice().getAsAgent().getValue().getRepresentedOrganization().getValue().getId().get(
            0) != null
            && NullChecker.isNotNullish(querySender.getDevice().getAsAgent().getValue()
            .getRepresentedOrganization().getValue().getId().get(0).getRoot())) {
            oid =
                querySender.getDevice().getAsAgent().getValue().getRepresentedOrganization().getValue().getId()
                .get(0).getRoot();
        }

        MCCIMT000300UV01Device receiverDevice = new MCCIMT000300UV01Device();
        receiverDevice.setDeterminerCode(HL7Constants.RECEIVER_DETERMINER_CODE);
        receiverDevice.setClassCode(EntityClassDevice.DEV);
        LOG.debug("Setting receiver device id (applicationId) to query sender's device id " + app);
        receiverDevice.getId().add(HL7DataTransformHelper.IIFactory(app));

        MCCIMT000300UV01Agent agent = new MCCIMT000300UV01Agent();
        MCCIMT000300UV01Organization org = new MCCIMT000300UV01Organization();
        org.setClassCode(HL7Constants.ORG_CLASS_CODE);
        org.setDeterminerCode(HL7Constants.RECEIVER_DETERMINER_CODE);
        II id = HL7DataTransformHelper.IIFactory(oid);
        org.getId().add(id);

        javax.xml.namespace.QName xmlqnameorg =
            new javax.xml.namespace.QName("urn:hl7-org:v3", "representedOrganization");
        JAXBElement<MCCIMT000300UV01Organization> orgElem =
            new JAXBElement<MCCIMT000300UV01Organization>(xmlqnameorg, MCCIMT000300UV01Organization.class, org);
        agent.setRepresentedOrganization(orgElem);
        agent.getClassCode().add(HL7Constants.AGENT_CLASS_CODE);

        javax.xml.namespace.QName xmlqnameagent = new javax.xml.namespace.QName("urn:hl7-org:v3", "asAgent");
        JAXBElement<MCCIMT000300UV01Agent> agentElem =
            new JAXBElement<MCCIMT000300UV01Agent>(xmlqnameagent, MCCIMT000300UV01Agent.class, agent);

        receiverDevice.setAsAgent(agentElem);

        receiver.setDevice(receiverDevice);

        return receiver;
    }

    private static MCCIMT000300UV01Sender createSender(MCCIMT000100UV01Receiver queryReceiver) {
        MCCIMT000300UV01Sender sender = new MCCIMT000300UV01Sender();
        String app = null;
        String oid = null;

        sender.setTypeCode(CommunicationFunctionType.SND);

        if (queryReceiver.getDevice() != null && NullChecker.isNotNullish(queryReceiver.getDevice().getId())
            && queryReceiver.getDevice().getId().get(0) != null
            && NullChecker.isNotNullish(queryReceiver.getDevice().getId().get(0).getRoot())) {
            app = queryReceiver.getDevice().getId().get(0).getRoot();
        }

        if (queryReceiver.getDevice() != null
            && queryReceiver.getDevice().getAsAgent() != null
            && queryReceiver.getDevice().getAsAgent().getValue() != null
            && queryReceiver.getDevice().getAsAgent().getValue().getRepresentedOrganization() != null
            && queryReceiver.getDevice().getAsAgent().getValue().getRepresentedOrganization().getValue() != null
            && NullChecker.isNotNullish(queryReceiver.getDevice().getAsAgent().getValue()
            .getRepresentedOrganization().getValue().getId())
            && queryReceiver.getDevice().getAsAgent().getValue().getRepresentedOrganization().getValue().getId()
            .get(0) != null
            && NullChecker.isNotNullish(queryReceiver.getDevice().getAsAgent().getValue()
            .getRepresentedOrganization().getValue().getId().get(0).getRoot())) {
            oid =
                queryReceiver.getDevice().getAsAgent().getValue().getRepresentedOrganization().getValue().getId()
                .get(0).getRoot();
        }

        MCCIMT000300UV01Device senderDevice = new MCCIMT000300UV01Device();
        senderDevice.setDeterminerCode(HL7Constants.SENDER_DETERMINER_CODE);
        senderDevice.setClassCode(EntityClassDevice.DEV);
        LOG.debug("Setting sender device id (applicationId) to query receiver's device id " + app);
        senderDevice.getId().add(HL7DataTransformHelper.IIFactory(app));

        MCCIMT000300UV01Agent agent = new MCCIMT000300UV01Agent();
        MCCIMT000300UV01Organization org = new MCCIMT000300UV01Organization();
        org.setClassCode(HL7Constants.ORG_CLASS_CODE);
        org.setDeterminerCode(HL7Constants.SENDER_DETERMINER_CODE);
        II id = HL7DataTransformHelper.IIFactory(oid);
        org.getId().add(id);

        javax.xml.namespace.QName xmlqnameorg =
            new javax.xml.namespace.QName("urn:hl7-org:v3", "representedOrganization");
        JAXBElement<MCCIMT000300UV01Organization> orgElem =
            new JAXBElement<MCCIMT000300UV01Organization>(xmlqnameorg, MCCIMT000300UV01Organization.class, org);
        agent.setRepresentedOrganization(orgElem);
        agent.getClassCode().add(HL7Constants.AGENT_CLASS_CODE);

        javax.xml.namespace.QName xmlqnameagent = new javax.xml.namespace.QName("urn:hl7-org:v3", "asAgent");
        JAXBElement<MCCIMT000300UV01Agent> agentElem =
            new JAXBElement<MCCIMT000300UV01Agent>(xmlqnameagent, MCCIMT000300UV01Agent.class, agent);

        senderDevice.setAsAgent(agentElem);

        sender.setDevice(senderDevice);

        return sender;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ADExplicit createAddress(Address add) {
        org.hl7.v3.ObjectFactory factory = new org.hl7.v3.ObjectFactory();
        ADExplicit result = (factory.createADExplicit());
        List addrlist = result.getContent();

        if (add != null) {
            if (add.getStreet1() != null && add.getStreet1().length() > 0) {
                AdxpExplicitStreetAddressLine street = new AdxpExplicitStreetAddressLine();
                street.setContent(add.getStreet1());

                addrlist.add(factory.createADExplicitStreetAddressLine(street));
            }

            if (add.getStreet2() != null && add.getStreet2().length() > 0) {
                AdxpExplicitStreetAddressLine street = new AdxpExplicitStreetAddressLine();
                street.setContent(add.getStreet2());

                addrlist.add(factory.createADExplicitStreetAddressLine(street));
            }
            if (add.getCity() != null && add.getCity().length() > 0) {
                AdxpExplicitCity city = new AdxpExplicitCity();
                city.setContent(add.getCity());

                addrlist.add(factory.createADExplicitCity(city));
            }
            if (add.getState() != null && add.getState().length() > 0) {
                AdxpExplicitState state = new AdxpExplicitState();
                state.setContent(add.getState());

                addrlist.add(factory.createADExplicitState(state));
            }
            if (add.getZip() != null && add.getZip().length() > 0) {
                AdxpExplicitPostalCode zip = new AdxpExplicitPostalCode();
                zip.setContent(add.getZip());

                addrlist.add(factory.createADExplicitPostalCode(zip));
            }
            if (!StringUtils.isBlank(add.getCountry())) {
                AdxpExplicitCountry country = new AdxpExplicitCountry();
                country.setContent(add.getCountry());

                addrlist.add(factory.createADExplicitCountry(country));
            }
        }

        return result;
    }
}
