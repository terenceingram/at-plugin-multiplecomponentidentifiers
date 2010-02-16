package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import org.archiviststoolkit.model.ResourcesComponents;
import org.archiviststoolkit.model.validators.ResourcesComponentsValidator;
import org.archiviststoolkit.util.ATPropertyValidationSupport;

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
	
private ATPropertyValidationSupport support;
	
	public ValidationResult validate() {
		
		ResourcesComponents model = (ResourcesComponents)objectToValidate;
		support = new ATPropertyValidationSupport(model, "Resource Component");
		ValidationResult result = super.validate();
		
		
		// Specifically for ArchDescComponentIdentifiers
		
		
		result.addAllFrom(support.getResult());
		
		return result;
	}

}
