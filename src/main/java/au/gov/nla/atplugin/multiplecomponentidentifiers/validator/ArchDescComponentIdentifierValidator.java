package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.archiviststoolkit.model.ArchDescComponentIdentifiers;
import org.archiviststoolkit.model.validators.ATAbstractValidator;
import org.archiviststoolkit.util.ATPropertyValidationSupport;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.util.ValidationUtils;

/**
 * Validation rules for the table ArchDescComponentIndentifiers.
 * 
 * @author tingram
 *
 */
public class ArchDescComponentIdentifierValidator extends ATAbstractValidator {
	
	protected ArchDescComponentIdentifiers model;
	protected ATPropertyValidationSupport support;
	protected Set<ArchDescComponentIdentifiers> identifiers;
	protected PersistentIdentifier persistentIdentifier;
	
	public ArchDescComponentIdentifierValidator() {
	}
	
	/**
	 * This constructor used by related classes (Resources, ResourcesComponents) to
	 * perform component level validation.
	 * 
	 * @param identifiers
	 */
	public ArchDescComponentIdentifierValidator(Set<ArchDescComponentIdentifiers> identifiers) {
		this.identifiers = identifiers;
	}
	
	/**
	 * Standard validate method that is used within AT.
	 */
	public ValidationResult validate() {
		
		model = (ArchDescComponentIdentifiers)objectToValidate;
		support = new ATPropertyValidationSupport(model, "Mulitple Component Identifiers");
		persistentIdentifier = new PersistentIdentifier();
		
		//Rule 1
		componentIdentifierIsMandatory();
		
		//Rule 2
		validatePersistentIdentifierPattern();
		
		//Rule 5
		checkForSubunitNumber();
		
		//Rule 7
		checkForChildrange();
		
		checkForStringLengths(model, support);
		return support.getResult();
	}
	
	/**
	 * Validation rules used by related classes (Resources, ResourcesComponents) to
	 * perform component level validation.
	 * 
	 * @return
	 */
	public ValidationResult validatePerComponent() {
		support = new ATPropertyValidationSupport(model, "Mulitple Component Identifiers");
		
		if (identifiers.isEmpty()) return support.getResult();
		
		//Rule 3
		checkOnlyOnePersistentIdentifier();
		
		//Rule 4
		checkOnlyOneSubunitNumber();
		
		//Rule 6
		checkOnlyOneChildrange();
		
		//Rule 8, 9 &10
		checkStartAndEndChildPairs();
		
		return support.getResult();
	}
	
	/**
	 * Component level validation. Performs three validations:
	 * 1) Check for equal number of start / endchild identifiers
	 * 2) Check that the start and end children have matching pi prefixes
	 * 3) Check that startchild number must be less or equal to the endchild number.
	 */
	protected void checkStartAndEndChildPairs() {
		
		List<String> startChildList = new ArrayList<String>();
		List<String> endChildList = new ArrayList<String>();
		
		for (ArchDescComponentIdentifiers identifier : identifiers) {
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("startchild")) startChildList.add(identifier.getComponentIdentifier());
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("endchild")) endChildList.add(identifier.getComponentIdentifier());
		}
		
		if (startChildList.size() != endChildList.size()) {
			//Rule 8
			support.addSimpleError("Number of start child and end child identifiers does not match.");
		} else {
			// Start and End Child are in pairs match each pair to validate
			for (int index=0; index < startChildList.size(); index++) {
				String startChild = startChildList.get(index);
				String endChild = endChildList.get(index);
				
				if (!getPiPrefix(startChild).equals(getPiPrefix(endChild))) {
					//Rule 9
					support.addSimpleError("Start child " + startChild + " and end child " + endChild + " identifier patterns do not match.");
				} else if (getChildNumberFromPi(startChild) > getChildNumberFromPi(endChild)) {
					//Rule 10
					support.addSimpleError("The end child identifier " + endChild + " is not greater than or equal to corresponding start child " + startChild + " identifier.");
				}
			}	
		}
	}
	
	/**
	 * Extracts the pi prefix from a pi i.e.
	 * returns nla.ms-ms9915-1.2 from nla.ms-ms9915-1.2-27
	 * 
	 * Rule must return the pi before the final separator of either
	 * "." or "-"
	 * 
	 * @param identifier
	 * @return
	 */
	protected String getPiPrefix(String identifier) {
		if (identifier.lastIndexOf(".") > identifier.lastIndexOf("-")) {
			return identifier.substring(0, identifier.lastIndexOf("."));
		} else {
			return identifier.substring(0, identifier.lastIndexOf("-"));
		}
	}
	
	/**
	 * Extracts the child number prefix from a pi i.e.
	 * returns 27 from nla.ms-ms9915-1.2-27
	 * 
	 * Rule must return number after the final separator of either
	 * "." or "-"
	 * 
	 * @param identifier
	 * @return
	 */
	protected int getChildNumberFromPi(String identifier) {
		if (identifier.lastIndexOf(".") > identifier.lastIndexOf("-")) {
			return Integer.valueOf(identifier.substring(identifier.lastIndexOf(".") + 1));
		} else {
			return Integer.valueOf(identifier.substring(identifier.lastIndexOf("-") + 1));
		}
	}
	
	/**
	 * Component level validation. Checks that there is only ONE type of "persistent identifier"
	 * in the list of identifiers.
	 */
	protected void checkOnlyOnePersistentIdentifier() {
		int count = 0;
		for (ArchDescComponentIdentifiers identifier : identifiers) {
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("persistent identifier")) count++;
		}
		if (count > 1) support.addSimpleError("Only one persistent identifier can be assigned per component.");
	}
	
	/**
	 * Component level validation. Checks that there is only ONE type of "subunit number"
	 * in the list of identifiers.
	 */
	protected void checkOnlyOneSubunitNumber() {
		int count = 0;
		for (ArchDescComponentIdentifiers identifier : identifiers) {
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("subunit number")) count++;
		}
		if (count > 1) support.addSimpleError("Only one subunit number can be assigned per component.");
	}
	
	/**
	 * Component level validation. Checks that there is only ONE type of "childrange"
	 * in the list of identifiers.
	 */
	protected void checkOnlyOneChildrange() {
		int count = 0;
		for (ArchDescComponentIdentifiers identifier : identifiers) {
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("childrange")) count++;
		}
		if (count > 1) support.addSimpleError("Only one childrange can be assigned per component.");
	}
	
	/**
	 * Identifier validation.
	 */
	protected void checkForChildrange() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("childrange")) {
			if (ValidationUtils.isBlank(model.getIdentifierLabel())) support.addSimpleError("Child range identifier requires a label.");
		}
	}
	
	/**
	 * Identifier validation.
	 */
	protected void checkForSubunitNumber() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("subunit number")) {
			if (ValidationUtils.isBlank(model.getIdentifierLabel())) support.addSimpleError("Subunit number requires a label.");
		}	
	}

	/**
	 * Identifier validation.
	 */
	protected void componentIdentifierIsMandatory() {
		if (ValidationUtils.isBlank(model.getComponentIdentifier())) support.addSimpleError("Identifier is mandatory.");
	}
	
	/**
	 * Identifier validation.
	 */
	protected void validatePersistentIdentifierPattern() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("persistent identifier") || 
				model.getIdentifierType().equalsIgnoreCase("altpersistent identifier") || 
				model.getIdentifierType().equalsIgnoreCase("startchild") || 
				model.getIdentifierType().equalsIgnoreCase("endchild")) {
			
			if (!persistentIdentifier.validate(model.getComponentIdentifier())) support.addSimpleError("Identifier Value " + model.getComponentIdentifier() + " does not match a valid persistent identifier pattern.");
			
		}
	}
}
