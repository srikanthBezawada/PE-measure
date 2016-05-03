package org.cytoscape.peMeasure.internal;

import java.util.Properties;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class PEcore {
    
    private static PEgui gui;
    public PEcore(){
        gui = createPEgui();
    }

    public PEgui createPEgui(){
        gui = new PEgui(this);
        CyActivator.getCyServiceRegistrar().registerService(gui, CytoPanelComponent.class, new Properties());
        CytoPanel cytopanelwest = CyActivator.getCyDesktopService().getCytoPanel(CytoPanelName.WEST);
        int index = cytopanelwest.indexOfComponent(gui);
        cytopanelwest.setSelectedIndex(index);
        return gui;
    }
    
    public void closeStartMenu() {
        CyActivator.getCyServiceRegistrar().unregisterService(gui, CytoPanelComponent.class);
    }
    
    public static PEgui getPEgui(){
        return gui;
    }
    
}