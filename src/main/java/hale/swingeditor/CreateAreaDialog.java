package main.java.hale.swingeditor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import main.java.hale.Game;
import main.java.hale.loading.JSONOrderedObject;
import main.java.hale.loading.SaveWriter;
import main.java.hale.resource.ResourceManager;
import main.java.hale.resource.ResourceType;
import main.java.hale.util.Logger;

/**
 * A widget for creating a new area within a campaign
 *
 * @author jared
 */

public class CreateAreaDialog extends JDialog
{
    private JButton ok, cancel;

    private JTextField idField, nameField;
    private JSpinner widthSpinner, heightSpinner, visibilitySpinner;
    private JCheckBox exploredCheckBox;

    public CreateAreaDialog(JFrame parent)
    {
        super(parent, "Create New Area", true);
        this.setResizable(false);

        JPanel content = new JPanel(new GridBagLayout());
        getContentPane().add(content);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);

        c.gridx = 0;
        c.gridy = 0;
        content.add(new JLabel("ID"), c);

        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        idField = new JTextField();
        idField.getDocument().addDocumentListener(new NameChangedListener());
        content.add(idField, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;
        content.add(new JLabel("Name"), c);

        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 3;
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(new NameChangedListener());
        content.add(nameField, c);

        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        content.add(new JLabel("Size"), c);

        c.gridx++;
        widthSpinner = new JSpinner(new SpinnerNumberModel(20, 5, 200, 1));
        content.add(widthSpinner, c);

        c.gridx++;
        content.add(new JLabel(" x "), c);

        c.gridx++;
        heightSpinner = new JSpinner(new SpinnerNumberModel(20, 5, 200, 1));
        content.add(heightSpinner, c);

        c.gridx = 0;
        c.gridy++;
        content.add(new JLabel("Visibility"), c);

        c.gridx++;
        visibilitySpinner = new JSpinner(new SpinnerNumberModel(8, 1, 20, 1));
        content.add(visibilitySpinner, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 4;
        exploredCheckBox = new JCheckBox("Explored");
        content.add(exploredCheckBox, c);

        c.gridx = 1;
        c.gridy++;
        c.gridwidth = 1;
        ok = new JButton(new OKAction());
        ok.setEnabled(false);
        content.add(ok, c);

        c.gridx++;
        c.gridwidth = 2;
        cancel = new JButton(new CancelAction());
        content.add(cancel, c);

        pack();
    }

    private void createArea()
    {
        if (!isValidToCreateArea()) return;

        JSONOrderedObject areaData = new JSONOrderedObject();
        areaData.put("name", nameField.getText());
        areaData.put("width", widthSpinner.getValue());
        areaData.put("height", heightSpinner.getValue());
        areaData.put("visibilityRadius", visibilitySpinner.getValue());
        areaData.put("explored", exploredCheckBox.isSelected());
        areaData.put("tileset", "unified");
        areaData.put("layers", new JSONOrderedObject());

        String outputLocation = "campaigns/" + Game.curCampaign.getID() + "/areas/" +
                idField.getText() + ResourceType.JSON.getExtension();

        try {
            PrintWriter out = new PrintWriter(new File(outputLocation));
            SaveWriter.writeJSON(areaData, out);
            out.close();
        } catch (Exception e) {
            Logger.appendToErrorLog("Error writing new area to disk", e);
        }

        EditorManager.addLogEntry("Created area: " + outputLocation);
        ResourceManager.addCampaignResource("areas/" + idField.getText() + ResourceType.JSON.getExtension());
        EditorManager.updateCampaign();

        setVisible(false);
        dispose();
    }

    private boolean isValidToCreateArea()
    {
        if (idField.getText().length() == 0) return false;

        // if name is alphanumeric or underscore
        if (!idField.getText().matches("^[a-zA-Z0-9_-]*$")) return false;

        if (nameField.getText().length() == 0) return false;

        return true;
    }

    private void checkValidToCreateArea()
    {
        ok.setEnabled(isValidToCreateArea());
    }

    private class NameChangedListener implements DocumentListener
    {
        @Override
        public void changedUpdate(DocumentEvent e)
        {
            checkValidToCreateArea();
        }

        @Override
        public void insertUpdate(DocumentEvent e)
        {
            checkValidToCreateArea();
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
            checkValidToCreateArea();
        }
    }

    private class CancelAction extends AbstractAction
    {
        public CancelAction()
        {
            super("Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            setVisible(false);
            dispose();
        }
    }

    private class OKAction extends AbstractAction
    {
        public OKAction()
        {
            super("Create");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            createArea();
        }
    }
}
