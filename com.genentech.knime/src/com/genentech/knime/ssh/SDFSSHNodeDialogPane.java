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


package com.genentech.knime.ssh;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;

/**
 * SSH Settings {@link NodeDialogPane}. Mostly copied from the implementation
 * provided by the Knime Remote Execution Node.
 * 
 * @author albertgo
 *
 */
public class SDFSSHNodeDialogPane extends NodeDialogPane {
	private final SDFSSHToolSettingsPanel m_panel = new SDFSSHToolSettingsPanel(this);

	/**
	 * The constructor of this class with no arguments and a very long java doc
	 * comment.
	 */
	public SDFSSHNodeDialogPane() {
		addTab("SDF SSH Tool Settings", m_panel);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings)
			throws InvalidSettingsException {
		SDFSSHToolSettings s = new SDFSSHToolSettings();
		m_panel.saveSettings(s);
		String msg = s.getStatusMsg();
		if (msg != null) {
			throw new InvalidSettingsException(msg);
		}
		s.save(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final DataTableSpec[] specs) throws NotConfigurableException {
		SDFSSHToolSettings s = null;
		try {
			s = new SDFSSHToolSettings(settings);
		} catch (InvalidSettingsException ise) {
		   s = new SDFSSHToolSettings();
			// ignore errors as this is called upon configure 
		   // and settings might contain invalid values
		}
		m_panel.loadSettings(s, specs[0]);
	}

}
