package projects.ids_wsn.nodeDefinitions.dht;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodes.nodeImplementations.FingerEntry;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;
import sinalgo.nodes.Node;

public class Chord implements IDHT {

	private MonitorNode monitor;
	
	/**
	 * Rules which the monitor(supervisor) is responsible for.
	 */
	private Set<Rules> supervisedRules;

	/**
	 * Stores the distributed hash table containing the keys(ID's of violated
	 * rules), the datum(ID's of the monitor responsible for the respective
	 * rule) and some other information or interest according to the <b>Chord
	 * Protocol</b>.
	 */
	private List<FingerEntry> fingerTable;

	/**
	 * Refers to the following node in the formation of the Chord Protocol Ring
	 */
	private MonitorNode nextNodeInChordRing;

	/**
	 * Refers to the previous node in the formation of the Chord Protocol Ring
	 */
	private MonitorNode previousNodeInChordRing;
	
	/**
	 * When a monitor node becomes a supervisor node, this map is used to store
	 * the lists of malicious nodes received from the
	 * <code>mapLocalMaliciousNodes</code> of other monitor nodes.
	 */
	private Map<Rules, List<Node>> mapExternalMaliciousNode;

	public Chord(MonitorNode monitor) {
		this.monitor = monitor;
		supervisedRules = new HashSet<Rules>();
		fingerTable = new ArrayList<FingerEntry>();
	}
	
	public void updateFingerTable() {
		//TODO implement updateFingerTable when a node joins or extis the network
	}
	
	public void createFingerTable(){
		for (int index = 0; index < UtilsChord.CHORD_RING_SIZE; index++) {
			System.out.println("criando finger indice: "+index);
			Integer startHash = getStart(index);
			System.out.println("start hash: " + startHash);
			
			MonitorNode sucessorNode = findSucessor(startHash);
			System.out.println("sucessor hash: " + sucessorNode.getHashID());
			System.out.println("----------------------------------------------");
			FingerEntry fingerEntry = new FingerEntry(this.monitor.ID, index, startHash, sucessorNode);
			
			fingerTable.add(fingerEntry);
		}
	}
	
	public Integer getStart(int index){
	/*----------(hashID + 2.pow(index)) mod 2.pow(chord_ring_size)----------*/
		Integer amount = new Double(Math.pow(2, index)).intValue();
		Integer mod = new Double(Math.pow(2, UtilsChord.CHORD_RING_SIZE)).intValue();
		
		return (this.monitor.getHashID() + amount) % mod;
	}
		

	public MonitorNode findSucessor(Integer hashKey) {
		if(isBetween(hashKey, this.monitor.getHashID(), nextNodeInChordRing.getHashID()) || hashKey == nextNodeInChordRing.getHashID()){
			return nextNodeInChordRing;
		}else{
			MonitorNode fromMyFingerTable = findInMyFingerTable(hashKey);
			if(fromMyFingerTable != null){
				return fromMyFingerTable;
			}
			System.out.println(this.monitor.getHashID() + " is gonna call FIND_CLOSEST_PREDECESSOR for key " + hashKey);
			MonitorNode closestPredecessor = findClosestPredecessor(hashKey);
			System.out.println("closest: " + closestPredecessor.getHashID());
			return closestPredecessor.getDht().findSucessor(hashKey);
		}
	}

	private MonitorNode findInMyFingerTable(Integer hashKey) {
		for (int i = fingerTable.size()-1; i >= 0; i--) {
			FingerEntry fingerEntry = fingerTable.get(i);
			MonitorNode sucessorFinger = fingerEntry.getSucessorNode(); 
			if(sucessorFinger.getHashID() == hashKey){
				return sucessorFinger;
			}
		}
		
		return null;
	}

	public boolean isBetween(Integer hashKey, Integer before, Integer after) {
		if(before == after){
			return (hashKey != before);
		}
		
		if(before < after){
			return hashKey.compareTo(before) > 0 && hashKey.compareTo(after) < 0;
			
		}
		
		Integer minID = 0;
		Integer maxID = new Double(Math.pow(2, UtilsChord.CHORD_RING_SIZE)).intValue();
		
		return (((before != maxID) && (hashKey > before) && hashKey<=maxID) || 
				(minID != after) && (hashKey >= minID) && (hashKey < after));

	}

	public MonitorNode findClosestPredecessor(Integer hashKey) {
		for (int i = fingerTable.size()-1; i >= 0; i--) {
			FingerEntry fingerEntry = fingerTable.get(i);
			MonitorNode sucessorFinger = fingerEntry.getSucessorNode(); 
			if(sucessorFinger.getHashID().compareTo(this.monitor.getHashID()) > 0 && sucessorFinger.getHashID().compareTo(hashKey) < 0){
				return sucessorFinger;
			}
		}
		
		return this.nextNodeInChordRing;
	}
	
	public Set<Rules> getSupervisedRules() {
		return supervisedRules;
	}

	public void setSupervisedRules(Set<Rules> supervisedRules) {
		this.supervisedRules = supervisedRules;
	}

	public List<FingerEntry> getFingerTable() {
		return fingerTable;
	}

	public void setFingerTable(List<FingerEntry> fingerTable) {
		this.fingerTable = fingerTable;
	}

	public void setMapExternalMaliciousNode(
			Map<Rules, List<Node>> mapExternalMaliciousNode) {
		this.mapExternalMaliciousNode = mapExternalMaliciousNode;
	}

	public Map<Rules, List<Node>> getMapExternalMaliciousNode() {
		return mapExternalMaliciousNode;
	}

	public void addExternalMaliciousList(Rules rule,
			List<Node> externalMalicious) {
		if (mapExternalMaliciousNode.containsKey(rule)) {
			List<Node> maliciousNodes = mapExternalMaliciousNode.get(rule);
			externalMalicious.addAll(maliciousNodes);
		}
		mapExternalMaliciousNode.put(rule, externalMalicious);
		
		//TODO se buffer estiver cheio correlacionar os nós maliciosos
	}

	public void addSupervisedRules(Rules rule) {
		this.supervisedRules.add(rule);
	}

	public void removeSupervisedRule(Rules rule) {
		this.supervisedRules.remove(rule);
	}

	public MonitorNode getMonitor() {
		return monitor;
	}

	public void setMonitor(MonitorNode monitor) {
		this.monitor = monitor;
	}

	public MonitorNode getNextNodeInChordRing() {
		return nextNodeInChordRing;
	}

	public void setNextNodeInChordRing(MonitorNode nextNodeInChordRing) {
		this.nextNodeInChordRing = nextNodeInChordRing;
	}

	public MonitorNode getPreviousNodeInChordRing() {
		return previousNodeInChordRing;
	}

	public void setPreviousNodeInChordRing(MonitorNode previousNodeInChordRing) {
		this.previousNodeInChordRing = previousNodeInChordRing;
	}
}
