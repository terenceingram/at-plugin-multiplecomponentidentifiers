package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import org.archiviststoolkit.model.Resources;
import org.archiviststoolkit.model.validators.ResourcesValidator;

import com.jgoodies.validation.ValidationResult;

/**
 * Adds the validation rules for the table ArchDescComponentIndentifiers
 * to the ResourcesValidator defined within AT.
 * 
 * These rules are at component level.
 * 
 * @author tingram
 *
 */
public class NLA_ResourcesValidator extends ResourcesValidator {
	
	public ValidationResult validate() {
		
		// Validation Rules from AT
		ValidationResult result = super.validate();
		
		// Specifically for ArchDescComponentIdentifiers
		Resources model = (Resources)objectToValidate;
		ArchDescComponentIdentifierValidator validator = new ArchDescComponentIdentifierValidator(model.getArchDescComponentIdentifiers());
		result.addAllFrom(validator.validatePerComponent());
		
		return result;
	}
}
