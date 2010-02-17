package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import org.archiviststoolkit.model.ResourcesComponents;
import org.archiviststoolkit.model.validators.ResourcesComponentsValidator;

import com.jgoodies.validation.ValidationResult;

/**
 * Adds the validation rules for the table ArchDescComponentIndentifiers
 * to the ResourcesComponentsValidator defined within AT.
 * 
 * These rules are at component level.
 * 
 * @author tingram
 *
 */
public class NLA_ResourcesComponentsValidator extends ResourcesComponentsValidator {
	
	public ValidationResult validate() {
		
		// Validation Rules from AT
		ValidationResult result = super.validate();
		
		// Specifically for ArchDescComponentIdentifiers
		ResourcesComponents model = (ResourcesComponents)objectToValidate;
		ArchDescComponentIdentifierValidator validator = new ArchDescComponentIdentifierValidator(model.getArchDescComponentIdentifiers());
		result.addAllFrom(validator.validatePerComponent());
		
		return result;
	}
}
