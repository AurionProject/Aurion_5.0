package gov.hhs.fha.nhinc.compliance;

public interface ComplianceChecker {

	public static final String PROPERTIES_FILE_GATEWAY = "gateway";
	
	public static final String ASSIGNED_DEVICE_CLASS_CODE = "ASSIGNED";
	
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_GENDER = "LivingSubject.administrativeGender";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_TIME = "LivingSubject.birthTime";
	public static final String SEMANTICS_TEXT_REPRESENTATION_SUBJECT_ID = "LivingSubject.id";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_NAME = "LivingSubject.name";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_ADDRESS = "Patient.addr";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_ADDRESS = "LivingSubject.BirthPlace.Addr";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_BIRTH_PLACE_NAME = "LivingSubject.BirthPlace.Place.Name";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PRINCIPAL_CARE_PROVIDER = "AssignedProvider.id";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_MAIDEN_NAME_MOTHER = "Person.MothersMaidenName";
	public static final String SEMANTICS_TEXT_REPRESENTATION_PATIENT_TELECOM = "Patient.telecom";
	
	public void update2011SpecCompliance();
	
}
