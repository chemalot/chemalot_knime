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
package com.genentech.knime.commandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an extension of {@link CMDProgramDefinition} that includes the HelpText.
 * 
 * @author albertgo
 *
 */
public class InternalCMDProgramDefinition extends CMDProgramDefinition {
    private final String helpTxt;

    public static CMDProgramDefinition createCMDProgramDefinition(
            final String name, final String label, final String command, final String subFolder,
            final CMDPortType[] inPorts, final CMDPortType[] outPort,
            final String inOpt, final String outOpt, final String defOpts,
            final String helpTxt) {
        List<String> msgs = new ArrayList<String>();

        CMDProgramDefinition.validate(msgs, name, label, command, "", inPorts, outPort,
                inOpt, outOpt, defOpts);

        if (msgs.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Error in definition for %s:\n", name));
            for (String s : msgs)
                sb.append('\t').append(s).append('\n');

            throw new IllegalArgumentException(sb.toString());
        }

        return new InternalCMDProgramDefinition(name, label, command, subFolder,
                inPorts, outPort, inOpt, outOpt, defOpts, helpTxt);
    }

    protected InternalCMDProgramDefinition(final String name, final String label,
            final String command, final String subFolder, final CMDPortType[] inPorts,
            final CMDPortType[] outPort, final String inOpt,
            final String outOpt, final String defOpts, final String helpTxt) {
        super(name, label, command, subFolder, inPorts, outPort, inOpt, outOpt, defOpts);

        this.helpTxt = helpTxt;
    }

    @Override
    public String getHelpTxt() {
        return helpTxt;
    }
}
