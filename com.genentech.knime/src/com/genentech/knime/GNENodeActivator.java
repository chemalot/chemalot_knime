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
package com.genentech.knime;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jsch.core.IJSchService;
import org.knime.core.node.NodeLogger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle  within Eclipse.
 */
public class GNENodeActivator extends Plugin {

    /** The plug-in ID. */
    public static final String PLUGIN_ID = "com.genentech.knime";
    
    // The shared instance
    private static GNENodeActivator plugin;
    private IJSchService m_ijschService;

    /**
     * The constructor
     */
    public GNENodeActivator() {
        // empty
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        NodeLogger.getLogger(GNENodeActivator.class).info(
                "Starting Plug-in " + PLUGIN_ID);
        BundleContext bundleContext = getBundle().getBundleContext();
        
        // disable knime update sites so that gne update site is the only one
        if( Settings.DISABLEKnimeUpdateSites ) removeKnimeRepositories();
        
        @SuppressWarnings("unchecked")
        ServiceReference<IJSchService> service = (ServiceReference<IJSchService>) 
                bundleContext.getServiceReference(IJSchService.class.getName());
        
        m_ijschService = bundleContext.getService(service);
    }

    
    /** disable update sites that include "knime.org" updates are provided by 
     * genentech update site
     */
    private void removeKnimeRepositories() {
        final ProvisioningUI provUI = ProvisioningUI.getDefaultUI();
        URI[] repositories = provUI.getRepositoryTracker()
                .getKnownRepositories(provUI.getSession());

        List<URI> toRemove = new ArrayList<URI>();
        for (URI uri : repositories) {
            if (uri.getHost() != null && uri.getHost().contains(".knime.org")) {
                toRemove.add(uri);
            }
        }

        provUI.getRepositoryTracker()
                .removeRepositories(toRemove.toArray(new URI[toRemove.size()]),
                        provUI.getSession());
    }

    /**
     * @return the JSch service.
     */
    public IJSchService getIJSchService() {
        return m_ijschService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        m_ijschService = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static GNENodeActivator getDefault() {
        return plugin;
    }

}
