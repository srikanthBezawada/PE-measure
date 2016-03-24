package org.cytoscape.peMeasure.internal.logic;

import java.util.List;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.peMeasure.internal.PEgui;
import org.cytoscape.view.model.CyNetworkView;

public class PEthread extends Thread {
    
    CyNetwork currentnetwork;
    CyNetworkView currentnetworkview;
    boolean YESb;
    PEgui gui;
    CyNetwork subNetwork = null;
    public static final String COLUMN0 = " k = 0 ";
    public static final String COLUMN1 = " k = 1 ";
    public static final String COLUMN2 = " k = 2 ";
    
    public PEthread(PEgui gui, CyNetwork currentnetwork, CyNetworkView currentnetworkview, boolean YESb) {
        this.gui = gui;
        this.currentnetwork = currentnetwork;
        this.currentnetworkview = currentnetworkview;
        this.YESb = YESb;
    }
    
    public void run(){
        gui.startComputation();
        CyTable eTable = currentnetwork.getDefaultEdgeTable();
        List<CyEdge> edgeList = currentnetwork.getEdgeList();
        eTable.createColumn(" k = 0 ", Double.class, true);
        eTable.createColumn(" k = 1 ", Double.class, true);
        eTable.createColumn(" k = 2 ", Double.class, true);
        CyRow row;
        for(CyEdge e : edgeList) {
            row = eTable.getRow(e.getSUID());
            row.set(COLUMN0, 0.5);
        }
        
        CyNode a,b;
        for(CyEdge e : edgeList){
            a = e.getSource();
            b = e.getTarget();
            
        
        }
        
        
        
        
        gui.endComputation();
    }
    
}