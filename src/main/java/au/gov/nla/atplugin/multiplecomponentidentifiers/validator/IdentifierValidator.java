package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import org.archiviststoolkit.model.ArchDescComponentIdentifiers;
import org.archiviststoolkit.model.validators.ATAbstractValidator;
import org.archiviststoolkit.util.ATPropertyValidationSupport;

import com.jgoodies.validation.ValidationResult;
import com.jgoodies.validation.util.ValidationUtils;

/**
 * Validation rules for the table ArchDescComponentIndentifiers.
 * These rules are specifically for a single Identifier.
 * 
 * @author tingram
 *
 */
public class IdentifierValidator extends ATAbstractValidator {
	
	private ArchDescComponentIdentifiers model;
	private ATPropertyValidationSupport support;
	
	public IdentifierValidator() {
	}
	
	public IdentifierValidator(ArchDescComponentIdentifiers archDescComponentIdentifiers) {
		this.objectToValidate = archDescComponentIdentifiers;
	}
	
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
		
		return support.getResult();
	}
	
	private void checkForChildrange() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("childrange")) {
			if (ValidationUtils.isBlank(model.getIdentifierLabel())) support.addSimpleError("Child range identifier requires a label.");
		}
	}

	private void checkForSubunitNumber() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("subunit number")) {
			if (ValidationUtils.isBlank(model.getIdentifierLabel())) support.addSimpleError("Subunit number requires a label.");
		}	
	}

	private void componentIdentifierIsMandatory() {
		if (ValidationUtils.isBlank(model.getComponentIdentifier())) support.addSimpleError("Identifier is mandatory.");
	}
	
	
	private void validatePersistentIdentifierPattern() {
		if (ValidationUtils.isBlank(model.getIdentifierType())) {
			// Do nothing
		} else if (model.getIdentifierType().equalsIgnoreCase("persistent identifier") || 
				model.getIdentifierType().equalsIgnoreCase("altpersistent identifier") || 
				model.getIdentifierType().equalsIgnoreCase("startchild") || 
				model.getIdentifierType().equalsIgnoreCase("endchild")) {
			// Validate the PI structure
		}
	}
	
	
}
