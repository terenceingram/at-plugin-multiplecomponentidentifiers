package au.gov.nla.atplugin.multiplecomponentidentifiers.editor;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import org.archiviststoolkit.swing.ATBasicComponentFactory;
import org.archiviststoolkit.model.ArchDescComponentIdentifiers;
import org.archiviststoolkit.mydomain.DomainEditorFields;
import org.archiviststoolkit.structure.ATFieldInfo;

public class ArchDescComponentIdentifiersFields extends DomainEditorFields {
	
	public ArchDescComponentIdentifiersFields() {
		initComponents();
	}

	public Component getInitialFocusComponent() {
		return null;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner non-commercial license
		mainPanel = new JPanel();
		label3 = new JLabel();
		label1 = new JLabel();
		label2 = new JLabel();
		identifierType = ATBasicComponentFactory.createTextField(detailsModel.getModel(ArchDescComponentIdentifiers.PROPERTYNAME_IDENTIFIER_TYPE), true);
		identifierLabel = ATBasicComponentFactory.createTextField(detailsModel.getModel(ArchDescComponentIdentifiers.PROPERTYNAME_IDENTIFIER_LABEL), true);
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
				"2*([150px,default], 5px), [150px,default]",
				"4*(default, 3px), default"));

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

			//---- identifierType ----
			identifierType.setColumns(4);
			identifierType.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			mainPanel.add(identifierType, cc.xywh(1, 5, 1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
			mainPanel.add(identifierLabel, cc.xy(3, 5));
			mainPanel.add(identifierValue, cc.xy(5, 5));
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
	public JTextField identifierType;
	private JTextField identifierLabel;
	private JTextField identifierValue;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
