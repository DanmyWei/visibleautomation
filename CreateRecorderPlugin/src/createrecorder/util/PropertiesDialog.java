package createrecorder.util;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * unfinished properties dialog to specify parameters, and references for motion events
 * @author matt2
 *
 */
public class PropertiesDialog extends Dialog {
	
	public PropertiesDialog(Shell shell) {
		super(shell);
	}
	
	// dialog containts radio buttons for selecting between view class, activity + view class + index,  activity + id
	// and activity + view class
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
	    GridLayout layoutContainer = new GridLayout(15, true);
	    layoutContainer.marginRight = 5;
	    layoutContainer.marginLeft = 10;
	    container.setLayout(layoutContainer);
	    Button classButton = new Button(container, SWT.RADIO);
	    GridData classButtonGridData = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
	    classButton.setLayoutData(classButtonGridData);
	    Label classLabel = new Label(container, SWT.NONE);
	    classLabel.setText("view class");
	    GridData classLabelGridData = new GridData(SWT.LEFT, SWT.FILL, false, false, 4, 1);
	    classLabel.setLayoutData(classLabelGridData);
	    Text classText = new Text(container, SWT.NONE);
	    GridData classTextGridData = new GridData(SWT.LEFT, SWT.FILL, false, false, 10, 1);
	    return parent;
	}
}
