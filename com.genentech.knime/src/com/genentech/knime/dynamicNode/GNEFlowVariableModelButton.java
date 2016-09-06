package com.genentech.knime.dynamicNode;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.FlowVariableModelButton;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.workflow.FlowVariable;

/**
 * 
 * @author gabriel
 */
@SuppressWarnings("serial")
public abstract class GNEFlowVariableModelButton extends FlowVariableModelButton {
	
	private final NodeDialogPane m_parent;

    // small hack to be called after the flow-variable button-dialog has been opened and something has actually changed!
	public GNEFlowVariableModelButton(final NodeDialogPane parent, final FlowVariableModel fvm) {
		super(fvm);
	    fvm.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(final ChangeEvent evt) {
	            final Map<String, FlowVariable> availableFlowVariables = parent.getAvailableFlowVariables();
	            FlowVariable flowVariable = availableFlowVariables.get(fvm.getInputVariableName());
	            if (flowVariable != null) {
	            	setTextField(flowVariable.getStringValue());                  	
	            }
	            FlowVariableModel wvm = (FlowVariableModel)evt.getSource();
	            setFieldEditable(!wvm.isVariableReplacementEnabled());
	        }
	    });
		m_parent = parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final AtomicBoolean changed = new AtomicBoolean(false);
        ChangeListener cl = new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent ce) {
                changed.set(true);
            }
        };
        final FlowVariableModel fvm = getFlowVariableModel();
        fvm.addChangeListener(cl);
        super.actionPerformed(e);
        fvm.removeChangeListener(cl);
        if (changed.get() && fvm.isVariableReplacementEnabled()) {
            Map<String, FlowVariable> availableFlowVariables = m_parent.getAvailableFlowVariables();
            FlowVariable flowVariable = availableFlowVariables.get(fvm.getInputVariableName());
            if (flowVariable != null) {
            	setTextField(flowVariable.getStringValue());
                FlowVariableModel wvm = ((FlowVariableModelButton) e.getSource()).getFlowVariableModel();
                setFieldEditable(!wvm.isVariableReplacementEnabled());
            }
        }
    }
    
    public abstract void setTextField(final String s);
    
    public abstract void setFieldEditable(final boolean bool);
}

