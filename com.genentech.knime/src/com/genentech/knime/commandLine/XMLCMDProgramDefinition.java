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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.genentech.knime.Settings;

/**
 * {@link CMDProgramDefinition} that is read from XML element.
 * 
 * @author albertgo
 *
 */
public class XMLCMDProgramDefinition extends CMDProgramDefinition {
    private final String helpText;

    private XMLCMDProgramDefinition(final String name, final String label, final String command,
            final String subFolder, final CMDPortType[] inPorts, final CMDPortType[] outPort,
            final String inOpt, final String outOpt, final String defOpts,
            final String helpFile) {
        super(name, label, command, subFolder, inPorts, outPort, inOpt, outOpt, defOpts);

        this.helpText = readHelpFile(helpFile);
    }

    @SuppressWarnings("unchecked")
    static CMDProgramDefinition createXMLProgram(final Element cmdElement) {
        List<String> msgs = new ArrayList<String>();

        String name = cmdElement.getAttributeValue("name");
        if (name == null || name.length() == 0)
            throw new Error("Invalid empty name in:\n" + cmdElement.toString());

        String cmd  = cmdElement.getAttributeValue("command");
        if( cmd == null ) cmd = name;
        
        String in = "";
        String out = "";
        String def = "";
        CMDPortType[] iPorts = new CMDPortType[0];
        CMDPortType[] oPorts = iPorts;

        Pattern trimPat = Pattern.compile("^\\s+",Pattern.MULTILINE);
        
        for (Element ele : (List<Element>) cmdElement.getChildren()) {
            String ename = ele.getName();

            if ("IO".equals(ename)) {
                String tmp = ele.getAttributeValue("in");
                in = tmp == null ? "" : tmp;

                tmp = ele.getAttributeValue("out");
                out = tmp == null ? "" : tmp;

            } else if ("default".equals(ename)) {
                String tmp = ele.getText();
                if( tmp != null ) {
                    tmp = trimPat.matcher(tmp).replaceAll("").trim();
                }
                if( tmp == null || tmp.length() == 0 )
                    tmp = ele.getAttributeValue("value");
                def = tmp == null ? "" : tmp;

            } else if ("ports".equals(ename)) {
                iPorts = parsePorts(ele.getAttributeValue("in"), msgs);
                oPorts = parsePorts(ele.getAttributeValue("out"), msgs);
            }
        }

        String lbl  = cmdElement.getAttributeValue("label");
        if( lbl == null ) { 
            lbl = name;
            
            // add " " after sdf
            if( lbl.startsWith("sdf") 
                && ((iPorts.length > 0 && iPorts[0] == CMDPortType.SDF)
                    || (oPorts.length > 0 && oPorts[0] == CMDPortType.SDF))) {
                lbl = "sdf " + lbl.substring(3);
            }
            
            // remove suffix
            int li = lbl.lastIndexOf('.');
            if( li >1 ) lbl = lbl.substring(0,li);
        }
        
        String folder = cmdElement.getAttributeValue("subfolder");
        if( folder == null ) folder = "";
        folder = folder.trim();

        String n = name.replaceAll("\\.[^.]+$", "");
        String hf = HELPFilePath + "/" + n + ".txt";
        CMDProgramDefinition.validate(msgs, n, lbl, cmd, folder, iPorts, oPorts, in, out, def);

        if (msgs.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Error in definition for %s:\n", n));
            for (String s : msgs)
                sb.append('\t').append(s).append('\n');

            XMLOutputter xmlOut = new XMLOutputter();
            sb.append(xmlOut.outputString(cmdElement)).append('\n');

            throw new IllegalArgumentException(sb.toString());
        }

        return new XMLCMDProgramDefinition(n, lbl, cmd, folder, iPorts, oPorts, in, out, def, hf);
    }

    static private CMDPortType[] parsePorts(final String portStr,
            final List<String> msgs) {
        CMDPortType[] iPorts;

        if (portStr == null || portStr.trim().length() == 0)
            return new CMDPortType[0];

        String[] ps = portStr.trim().split(" +");
        iPorts = new CMDPortType[ps.length];
        for (int i = 0; i < ps.length; i++) {
            try {
                iPorts[i] = CMDPortType.toPortType(ps[i]);
            } catch (IllegalArgumentException e) {
                msgs.add(String.format("Invalid port type %s", ps[i]));
            }
        }
        return iPorts;
    }

    @Override
    public String getHelpTxt() {
        return helpText;
    }
    
    private static final String readHelpFile(String helpFile) {
        try {
            InputStream in;
            if( helpFile.toLowerCase().startsWith("http://"))
                in = new FileInputStream(Settings.cacheToFile(
                           new URL(helpFile),Pattern.compile("text/.*",Pattern.CASE_INSENSITIVE)));
            else
                in = new FileInputStream(helpFile);
            
            in = new BufferedInputStream(in);
            String ret = streamToString(in);
            in.close();
            return ret;

        } catch (IOException e) {
            return e.getMessage();
        }
    }

    /** @return empty string if nothing in strm **/
    public static String streamToString(final InputStream strm)
            throws IOException {
        StringBuilder sb = new StringBuilder(2000);
        byte[] buf = new byte[2048];

        int len;
        while ((len = strm.read(buf)) > -1)
            sb.append(new String(buf, 0, len));

        strm.close();
        return sb.toString();
    }
}

