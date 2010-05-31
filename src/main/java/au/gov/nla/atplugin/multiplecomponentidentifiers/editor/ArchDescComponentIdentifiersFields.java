package au.gov.nla.atplugin.multiplecomponentidentifiers.editor;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import org.archiviststoolkit.swing.ATBasicComponentFactory;
import org.archiviststoolkit.model.ArchDescComponentIdentifiers;
import org.archiviststoolkit.mydomain.DomainEditorFields;

/**
 * Screen to adding / editing the details of the Multiple Component
 * Identifiers.
 * 
 * @author tingram
 *
 */
public class ArchDescComponentIdentifiersFields extends DomainEditorFields {
	
	public ArchDescComponentIdentifiersFields() {
		initComponents();
		// Ensure that the drop downs have a default selection
		// otherwise if not it causes the entire application to hang
		// with a thread running in the background.
		identifierTypeList.setSelectedIndex(0);
		identifierLabelList.setSelectedIndex(0);
	}

	public Component getInitialFocusComponent() {
		return identifierTypeList;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		mainPanel = new JPanel();
		label3 = new JLabel();
		label1 = new JLabel();
		label2 = new JLabel();
		identifierTypeList = ATBasicComponentFactory.createComboBox(detailsModel, ArchDescComponentIdentifiers.PROPERTYNAME_IDENTIFIER_TYPE, ArchDescComponentIdentifiers.class);
		identifierLabelList = ATBasicComponentFactory.createComboBox(detailsModel, ArchDescComponentIdentifiers.PROPERTYNAME_IDENTIFIER_LABEL, ArchDescComponentIdentifiers.class);
		identifierValue = ATBasicComponentFactory.createTextField(detailsModel.getModel(ArchDescComponentIdentifiers.PROPERTYNAME_COMPONENT_IDENTIFIER), true);
		CellConstraints cc = new CellConstraints();

		//======== this ========
		setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
		setBackground(new Color(200, 205, 232));
		setLayout(new FormLayout(
			"default:grow",
			"top:default:grow"));

		//======== mainPanel ========
		{
			mainPanel.setBorder(Borders.DLU4_BORDER);
			mainPanel.setOpaque(false);
			mainPanel.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			mainPanel.setBackground(new Color(200, 205, 232));
			mainPanel.setLayout(new FormLayout(
				"max(min;150px), 1px, max(min;150px), 1px, max(default;150px)",
				"default, default, default, default, default, default"));

			//---- label3 ----
			label3.setText("Identifier Type");
			label3.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			mainPanel.add(label3, cc.xywh(1, 3, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));

			//---- label1 ----
			label1.setText("Identifier Label");
			mainPanel.add(label1, cc.xy(3, 3));

			//---- label2 ----
			label2.setText("Identifier Value");
			mainPanel.add(label2, cc.xy(5, 3));

			//---- identifierTypeList ----
			identifierTypeList.setOpaque(false);
			identifierTypeList.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			mainPanel.add(identifierTypeList, cc.xywh(1, 4, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));

			//---- identifierLabelList ----
			identifierLabelList.setOpaque(false);
			identifierLabelList.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			mainPanel.add(identifierLabelList, cc.xywh(3, 4, 1, 1, CellConstraints.LEFT, CellConstraints.DEFAULT));
			mainPanel.add(identifierValue, cc.xy(5, 4));
		}
		add(mainPanel, cc.xy(1, 1));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JPanel mainPanel;
	private JLabel label3;
	private JLabel label1;
	private JLabel label2;
	public JComboBox identifierTypeList;
	public JComboBox identifierLabelList;
	private JTextField identifierValue;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
