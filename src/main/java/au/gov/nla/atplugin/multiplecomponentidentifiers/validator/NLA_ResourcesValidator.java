package au.gov.nla.atplugin.multiplecomponentidentifiers.validator;

import org.archiviststoolkit.model.Resources;
import org.archiviststoolkit.model.validators.ResourcesValidator;
import org.archiviststoolkit.util.ATPropertyValidationSupport;

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
	
	private ATPropertyValidationSupport support;
	
	public ValidationResult validate() {
		
		Resources model = (Resources)objectToValidate;
		support = new ATPropertyValidationSupport(model, "Resources");
		ValidationResult result = super.validate();
		
		
		// Specifically for ArchDescComponentIdentifiers
		
		
		result.addAllFrom(support.getResult());
		
		return result;
	}

}
