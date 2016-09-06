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

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.genentech.knime.Settings;

/**
 * Stores Settings for Knime To SDF Knime Node.
 * 
 * @author albertgo
 *
 */
public class KnimeSDFCMDBridgeSettings {

    private static final String CFG_STRUCT_COL = "struct_col";
    static final String CFG_LOCAL_EX_DIR = "localEDir";
    static final String CFG_REMOTE_EX_DIR = "remoteEDir";
    private final String m_StructCol;
    private final String m_localExchangeDir;
    private final String m_remoteExchangeDir;
    
    public KnimeSDFCMDBridgeSettings(String structColumn, 
            String localExchangeDir, String remoteExchangeDir)
    {   this.m_StructCol = structColumn;
        this.m_localExchangeDir = localExchangeDir;
        this.m_remoteExchangeDir = remoteExchangeDir;
    }
    
    
    public String getStructColumn() {
        return m_StructCol;
    }


    public String getLocalExchangeDir() {
        return m_localExchangeDir;
    }

    public String getRemoteExchangeDir() {
        return m_remoteExchangeDir;
    }

    public static KnimeSDFCMDBridgeSettings loadFromModel(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        String structCol = settings.getString(CFG_STRUCT_COL);
        String lDir = settings.getString(CFG_LOCAL_EX_DIR);
        String rDir = settings.getString(CFG_REMOTE_EX_DIR);

        return new KnimeSDFCMDBridgeSettings(structCol, lDir, rDir);
    }

    public static KnimeSDFCMDBridgeSettings loadFromDialog(final NodeSettingsRO settings) {
        String structCol = settings.getString(CFG_STRUCT_COL, "MOLFILE");
        String lDir = Settings.getExchangeLocalDir();
        lDir = settings.getString(CFG_LOCAL_EX_DIR, lDir);
        
        
        String rDir = Settings.getExchangeRemoteDir();
        settings.getString(CFG_REMOTE_EX_DIR, rDir);

        return new KnimeSDFCMDBridgeSettings(structCol, lDir, rDir);
    }

    public static void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        KnimeSDFCMDBridgeSettings kSet = loadFromDialog(settings);
        String lsDir = kSet.getLocalExchangeDir();
        if( lsDir == null )
            throw new InvalidSettingsException(
                    "Cannot access local exchange directory: " + lsDir);
        
        if( kSet.getRemoteExchangeDir() == null )
            throw new InvalidSettingsException(
                    "Remote exchange directory not set.");
    }

    public void save(final NodeSettingsWO settings) {
        settings.addString(CFG_STRUCT_COL, getStructColumn());
        settings.addString(CFG_LOCAL_EX_DIR, getLocalExchangeDir());
        settings.addString(CFG_REMOTE_EX_DIR, getRemoteExchangeDir());
    }
}
