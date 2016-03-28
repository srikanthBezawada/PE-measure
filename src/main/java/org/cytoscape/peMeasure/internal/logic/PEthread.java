package org.cytoscape.peMeasure.internal.logic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        eTable.createColumn(COLUMN0 , Double.class, true);
        eTable.createColumn(COLUMN1 , Double.class, true);
        eTable.createColumn(COLUMN2 , Double.class, true);
        CyRow row;
        for(CyEdge e : edgeList) {
            row = eTable.getRow(e.getSUID());
            row.set(COLUMN0, 0.5);
            //row.set(COLUMN1, 0);
            //row.set(COLUMN2, 0);
        }
        
        CyNode a,b;
        LinkedHashMap<CyEdge, CyEdge> reqMap;
        double result=1;
        double x,y;
        
        for(CyEdge e : edgeList){
            result = 1;
            a = e.getSource();
            b = e.getTarget();
            reqMap = findCommonEdges(a, b, edgeList);
            if(reqMap.isEmpty()){
            } else{
                for(Map.Entry<CyEdge, CyEdge> entry : reqMap.entrySet()){
                    row = eTable.getRow(entry.getKey().getSUID());
                    x = row.get(COLUMN0, Double.class);
                    row = eTable.getRow(entry.getValue().getSUID());
                    y = row.get(COLUMN0, Double.class);
                    result = result*(1-x*y);
                }
            }
            result = 1-result;
            row = eTable.getRow(e.getSUID());
            row.set(COLUMN1, result);
        }
        
        for(CyEdge e : edgeList){
            result = 1;
            a = e.getSource();
            b = e.getTarget();
            reqMap = findCommonEdges(a, b, edgeList);
            if(reqMap.isEmpty()){
            } else {
                for(Map.Entry<CyEdge, CyEdge> entry : reqMap.entrySet()){
                    row = eTable.getRow(entry.getKey().getSUID());
                    x = row.get(COLUMN1, Double.class);
                    row = eTable.getRow(entry.getValue().getSUID());
                    y = row.get(COLUMN1, Double.class);
                    result = result*(1-x*y);
                }
            }
            result = 1-result;
            row = eTable.getRow(e.getSUID());
            row.set(COLUMN2, result);
        }
            
        gui.endComputation();
    }
    
    public LinkedHashMap<CyEdge, CyEdge> findCommonEdges(CyNode a, CyNode b, List<CyEdge> edgeList){
        List<CyNode> comList = currentnetwork.getNeighborList(a, CyEdge.Type.ANY);
        LinkedHashMap<CyEdge, CyEdge> reqMap = new LinkedHashMap<CyEdge, CyEdge>();
        CyEdge e1,e2;
        for(CyNode x : comList) {
            if(currentnetwork.getNeighborList(b, CyEdge.Type.ANY).contains(x)) {
                e1 = currentnetwork.getConnectingEdgeList(a, x, CyEdge.Type.ANY).get(0);
                e2 = currentnetwork.getConnectingEdgeList(b, x, CyEdge.Type.ANY).get(0);
                reqMap.put(e1, e2);
            } 
        }
       
        return reqMap; 
    }
    
}