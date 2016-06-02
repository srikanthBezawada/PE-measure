package org.cytoscape.peMeasure.internal.logic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.peMeasure.internal.CyActivator;
import org.cytoscape.peMeasure.internal.PEgui;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

/**
 * @author SrikanthB
 * This class has the algorithm implemented
 */

public class PElogic {
    
    CyNetwork currentnetwork;
    CyNetworkView currentnetworkview;
    boolean YESb;
    double reliabValue;
    PEgui gui;
    CyNetwork subNetwork = null;
    public static final String COLUMN0 = " k = 0 ";
    public static final String COLUMN1 = " k = 1 ";
    public static final String COLUMN2 = " k = 2 ";
    
    public PElogic(PEgui gui, CyNetwork currentnetwork, CyNetworkView currentnetworkview, double reliabValue, boolean YESb) {
        this.gui = gui;
        this.currentnetwork = currentnetwork;
        this.currentnetworkview = currentnetworkview;
        this.reliabValue = reliabValue;
        this.YESb = YESb;
    }
    
    public void run(){
        gui.startComputation();
        long startTime = System.currentTimeMillis();
        List<CyNode> requiredNodes = currentnetwork.getNodeList();
        List<CyEdge> requiredEdges = currentnetwork.getEdgeList();
        CyTable eTable = currentnetwork.getDefaultEdgeTable();
        List<CyEdge> edgeList = currentnetwork.getEdgeList();
        if(eTable.getColumn(COLUMN0) == null){
            eTable.createColumn(COLUMN0 , Double.class, true);
            eTable.createColumn(COLUMN1 , Double.class, true);
            eTable.createColumn(COLUMN2 , Double.class, true);
        }
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
            if(result == 0){
                result = 0.01;
            }
            if(result < reliabValue){
                requiredEdges.remove(e);
            }
            row = eTable.getRow(e.getSUID());
            row.set(COLUMN2, result);
        }
        
        createNetwork(requiredNodes, requiredEdges);
        long endTime = System.currentTimeMillis();
        long difference = endTime - startTime;
        System.out.println("Execution time for PE-measure algo: " + difference +" milli seconds");
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
    
    public void createNetwork(List<CyNode> subnodeList, List<CyEdge> subedgeList){
        // select the nodes and edges
        CyTable eTable = currentnetwork.getDefaultEdgeTable();
        List<CyEdge> elist = currentnetwork.getEdgeList();
        for(CyEdge e : elist){
            if(subedgeList.contains(e)){
                CyRow row = eTable.getRow(e.getSUID());
                row.set("selected", true);
            }
            else{
                CyRow row = eTable.getRow(e.getSUID());
                row.set("selected", false);
            }
        }
        
        // create the network
        if(YESb == true){
            NewNetworkSelectedNodesAndEdgesTaskFactory f = CyActivator.getCySwingAppAdapter().
                get_NewNetworkSelectedNodesAndEdgesTaskFactory();
            TaskIterator itr = f.createTaskIterator(currentnetwork);
            CyActivator.getCySwingAppAdapter().getTaskManager().execute(itr);
            
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PElogic.class.getName()).log(Level.SEVERE, null, ex);
            }
            // set the name of the network
            this.gui.calculatingresult("Created! Renaming the network...");
            String currentNetworkName = currentnetwork.getRow(currentnetwork).get(CyNetwork.NAME, String.class);
            Set<CyNetwork> allnetworks = CyActivator.getCyNetworkManager().getNetworkSet();
            long maxSUID = Integer.MIN_VALUE;
            for(CyNetwork net : allnetworks){
                if(net.getSUID() > maxSUID)
                    maxSUID = net.getSUID();
            }
            this.subNetwork = CyActivator.getCyNetworkManager().getNetwork(maxSUID);
            subNetwork.getRow(subNetwork).set(CyNetwork.NAME, currentNetworkName + " PE-measure " + reliabValue);         
        }
        
    }
    
}