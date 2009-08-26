package projects.ids_wsn.nodeDefinitions.dht;

import java.util.List;

import projects.ids_wsn.nodes.nodeImplementations.FingerEntry;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;

public interface IDHT {
	
	public MonitorNode getNextNodeInChordRing();
	
	public void setNextNodeInChordRing(MonitorNode nextNodeInChordRing);

	public MonitorNode getPreviousNodeInChordRing();
	
	public void setPreviousNodeInChordRing(MonitorNode previoiusNodeInChordRing);
	
	public void updateFingerTable();
	
	public void createFingerTable();
	
	public Integer getStart(int index);

	public MonitorNode findSucessor(Integer hashKey);
	
	public MonitorNode findClosestPredecessor(Integer hashKey);
	
	public List<FingerEntry> getFingerTable();

	public void setFingerTable(List<FingerEntry> fingerTable);
}
