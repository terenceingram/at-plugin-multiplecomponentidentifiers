/*
 * Created by JFormDesigner on Fri Jan 15 16:38:47 EST 2010
 */

package au.nla.gov.atplugin.multiplecomponentidentifiers;

import java.awt.*;
import javax.swing.*;
import com.jgoodies.forms.layout.*;
import org.archiviststoolkit.mydomain.*;
import org.archiviststoolkit.model.*;

/**
 * @author Terence Ingram
 */
public class ComponentIdentifiersPanel extends JPanel {
	
	private DomainObject domainObject;
	
	public ComponentIdentifiersPanel(DomainObject domainObject) {
		this.domainObject = domainObject;
		initComponents();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Gerard Clifton
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		multipleComponentIdentifiersTable = new DomainSortableTable(ArchDescComponentIdentifiers.class);
		addButton = new JButton();
		panel1 = new JPanel();
		editButton = new JButton();
		removeButton = new JButton();

		//======== this ========
		setMinimumSize(new Dimension(635, 408));
		setPreferredSize(new Dimension(750, 520));
		setBackground(new Color(200, 205, 232));

		// JFormDesigner evaluation mark
		setBorder(new javax.swing.border.CompoundBorder(
			new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
				"JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
				java.awt.Color.red), getBorder())); addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});

		setLayout(new GridBagLayout());

		//---- label1 ----
		label1.setText("Component Idenifiers");
		label1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
		add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane1 ========
		{
			scrollPane1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane1.setPreferredSize(new Dimension(650, 250));
			scrollPane1.setFont(new Font("Trebuchet MS", Font.PLAIN, 13));
			scrollPane1.setViewportView(multipleComponentIdentifiersTable);
		}
		add(scrollPane1, new GridBagConstraints(0, 1, 2, 12, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//---- addButton ----
		addButton.setText("Add");
		add(addButton, new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 5), 0, 0));

		//======== panel1 ========
		{
			panel1.setBackground(new Color(200, 205, 232));
			panel1.setLayout(new GridLayout());

			//---- editButton ----
			editButton.setText("Edit");
			panel1.add(editButton);

			//---- removeButton ----
			removeButton.setText("Remove");
			panel1.add(removeButton);
		}
		add(panel1, new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Gerard Clifton
	private JLabel label1;
	private JScrollPane scrollPane1;
	private JTable multipleComponentIdentifiersTable;
	private JButton addButton;
	private JPanel panel1;
	private JButton editButton;
	private JButton removeButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
