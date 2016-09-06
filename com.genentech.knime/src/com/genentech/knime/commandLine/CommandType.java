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

/**
 * A commandType describes the Command Line Knime Node based on the number of input
 * and output nodes.
 * 
 * @author albertgo
 *
 */
public enum CommandType
{  /** Only creates a single sdf file */
   GENERATOR,   
   
   /** reads sdf and writes sdf */
   PROCESSOR,
   
   /** only reads sdf on port */
   CONSUMER,
   
   /** multiple in and/or out sdf ports */
   OTHER;
   
   static CommandType toCommandType(String pName)
   {  return CommandType.valueOf(pName.toUpperCase());
   }
}
