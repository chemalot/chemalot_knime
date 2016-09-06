/*
    The chemalot-knime package provides a framework to execute commandline
    programs that read and wrie SDF files on a remote host from the KNIME
    graphical pipelining platform. 
    Copyright (C) 2016 Genentech Inc.

    This file is part of chemalot-knime.

    chemalot-knime is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    chemalot-knime is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with chemalot-knime.  If not, see <http://www.gnu.org/licenses/>.

*/
package com.genentech.knime.settings;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;

import com.genentech.knime.Settings;

/**
 * Setting node.
 * 
 * This node reads a java property file and makes all properties stored in the
 * file available as flow varaibles on the output port.
 * 
 * The settings are read into a Map in {@link Settings}.
 * 
 * albertgo @ Genentech
 */
public class GNESettingsNodeModel extends NodeModel {

	public GNESettingsNodeModel() {
		super(new PortType[]{}, new PortType[]{FlowVariablePortObject.TYPE});
	}
	
	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
			throws InvalidSettingsException {
		pushVariableMap();
		return new PortObjectSpec[]{FlowVariablePortObjectSpec.INSTANCE};
	}
	
	@Override
	protected PortObject[] execute(PortObject[] inObjects, ExecutionContext exec)
			throws Exception {
		pushVariableMap();
		return new PortObject[]{FlowVariablePortObject.INSTANCE};
	}
	
	private void pushVariableMap() {
		for (Map.Entry<String, String> e : Settings.GNEProperties.entrySet()) {
			pushFlowVariableString(e.getKey(), e.getValue());
		}
		
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no op
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// no op
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// no op
	}

	@Override
	protected void validateSettings(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// no op
	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings)
			throws InvalidSettingsException {
		// no op
	}

	@Override
	protected void reset() {
		// no op
	}

}
