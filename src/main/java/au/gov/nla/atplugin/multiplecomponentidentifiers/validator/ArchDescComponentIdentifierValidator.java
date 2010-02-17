package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import java.util.Set;

import org.archiviststoolkit.model.ArchDescComponentIdentifiers;
import org.archiviststoolkit.model.validators.ATAbstractValidator;
import org.archiviststoolkit.util.ATPropertyValidationSupport;

import com.jgoodies.validation.Severity;
import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.message.SimpleValidationMessage;
import com.jgoodies.validation.util.ValidationUtils;

/**
 * Validation rules for the table ArchDescComponentIndentifiers.
 * 
 * @author tingram
 *
 */
public class ArchDescComponentIdentifierValidator extends ATAbstractValidator {
	
	private ArchDescComponentIdentifiers model;
	private ATPropertyValidationSupport support;
	Set<ArchDescComponentIdentifiers> identifiers;
	
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
		
		//Rule 8
		checkEqualNumberOfStartAndEndChild();
		
		//Rule 9
		
		return support.getResult();
	}
	
	/**
	 * Component level validation. If present, each start child must have a
	 * corresponding end child.
	 */
	private void checkEqualNumberOfStartAndEndChild() {
		int startchildCount = 0;
		int endchildCount = 0;
		for (ArchDescComponentIdentifiers identifier : identifiers) {
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("startchild")) startchildCount++;
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("endchild")) endchildCount++;
		}
		if (startchildCount != endchildCount) support.addSimpleError("Number of start child and end child identifiers does not match.");
	}
	
	/**
	 * Component level validation. 
	 */
	private void checkOnlyOnePersistentIdentifier() {
		int count = 0;
		for (ArchDescComponentIdentifiers identifier : identifiers) {
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("persistent identifier")) count++;
		}
		if (count > 1) support.addSimpleError("Only one persistent identifier can be assigned per component.");
	}
	
	/**
	 * Component level validation. 
	 */
	private void checkOnlyOneSubunitNumber() {
		int count = 0;
		for (ArchDescComponentIdentifiers identifier : identifiers) {
			if (!ValidationUtils.isBlank(identifier.getIdentifierType()) && 
					identifier.getIdentifierType().equals("subunit number")) count++;
		}
		if (count > 1) support.addSimpleError("Only one subunit number can be assigned per component.");
	}
	
	/**
	 * Component level validation. 
	 */
	private void checkOnlyOneChildrange() {
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
	private void checkForChildrange() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("childrange")) {
			if (ValidationUtils.isBlank(model.getIdentifierLabel())) support.addSimpleError("Child range identifier requires a label.");
		}
	}
	
	/**
	 * Identifier validation.
	 */
	private void checkForSubunitNumber() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("subunit number")) {
			if (ValidationUtils.isBlank(model.getIdentifierLabel())) support.addSimpleError("Subunit number requires a label.");
		}	
	}

	/**
	 * Identifier validation.
	 */
	private void componentIdentifierIsMandatory() {
		if (ValidationUtils.isBlank(model.getComponentIdentifier())) support.addSimpleError("Identifier is mandatory.");
	}
	
	/**
	 * Identifier validation.
	 */
	private void validatePersistentIdentifierPattern() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("persistent identifier") || 
				model.getIdentifierType().equalsIgnoreCase("altpersistent identifier") || 
				model.getIdentifierType().equalsIgnoreCase("startchild") || 
				model.getIdentifierType().equalsIgnoreCase("endchild")) {
			
			PersistentIdentifier identifier = new PersistentIdentifier();
			if (!identifier.validate(model.getComponentIdentifier())) support.addSimpleError("Identifier Value " + model.getComponentIdentifier() + " does not match a valid persistent identifier pattern.");
			
		}
	}
}
