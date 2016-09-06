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

import java.io.File;

/**
 * For Exceptions while trying to execut SSH command.
 * 
 * @author albertgo
 *
 */
public class SSHExecutionException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int status;
    private final File stdErr;
    
    public SSHExecutionException(String msg, int status, File stdErr) {
        super(msg);
        
        this.status = status;
        this.stdErr = stdErr;
    }
    
    public int getStatus() {
        return status;
    }

    public File getStdErr() {
        return stdErr;
    }
}
