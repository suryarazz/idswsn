package projects.ids_wsn.nodeDefinitions.routing.fuzzy;

import java.awt.Color;
import java.util.Enumeration;
import java.util.Hashtable;
import projects.ids_wsn.Utils;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.routing.IRouting;
import projects.ids_wsn.nodes.messages.BeaconMessage;
import projects.ids_wsn.nodes.messages.EventMessage;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.FloodFindFuzzy;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.timers.SimpleMessageTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class FuzzyRouting implements IRouting {
	
	
	
	//Number of entries in Routing Table
	private Integer numBuffer = 3; 
	
	public int seqID = 0;
	
	//Fuzzy Routing Table
	protected Hashtable<Node, FuzzyRoutingEntry> fuzzyRoutingTable = new Hashtable<Node, FuzzyRoutingEntry>();
	
	//Guarantee the node-disjoint path
	protected Hashtable<Node, Integer> numHopsByNode = new Hashtable<Node, Integer>();
	
	private BasicNode node;
	
	private Boolean isMultiPathBalanced;
	
	//If multi-path os balanced, we do not have to use fuzzy inference. We have to send
	//messages to all paths. This attribute is used to store the next path that we have to choose
	private Integer sequenceSendMessage = 0;
	
	public FuzzyRouting(){
		isMultiPathBalanced = Utils.isMultiPathBalanced();		
	}

	public Node getBestRoute(Node destino) {
		FuzzyRoutingEntry fRout = fuzzyRoutingTable.get(destino);
		return fRout.getFirstActiveRoute();
	}
	
	public Node getNextRouteBalanced(Node destino) {
		FuzzyRoutingEntry fRout = fuzzyRoutingTable.get(destino);
		
		Node n = fRout.getActiveRoute(sequenceSendMessage);
		sequenceSendMessage++;
		if (sequenceSendMessage>3){
			sequenceSendMessage = 0;
		}
		
		return n;
	}

	public Boolean isNodeNextHop(Node destination) {
		return null;
	}

	public void receiveMessage(Message message) {
		if (message instanceof FloodFindDsdv){
			receiveFloodFindMessage(message);
		}else if (message instanceof FloodFindFuzzy){
			receiveFloodFindFuzzy(message);			
		}else if (message instanceof PayloadMsg){
			PayloadMsg payloadMessage = (PayloadMsg) message;
			receivePayloadMessage(payloadMessage);			
		}else if (message instanceof BeaconMessage){
			BeaconMessage beaconMessage = (BeaconMessage) message;
			receiveBeaconMessage(beaconMessage);
		}else if (message instanceof EventMessage){
			EventMessage eventMessage = (EventMessage) message;
			treatEvent(eventMessage);			
		}

	}
	
	private void receiveBeaconMessage(BeaconMessage beaconMessage) {
		
		Logging myLog = Utils.getGeneralLog();
		Boolean forward = Boolean.TRUE;
		Float energy = 0f;
		Integer numHops = 0;
		Double fsl = 0d;
		
		if (beaconMessage.forwardingNode.equals(node)){ // The message bounced back. The node must discard the msg.
			forward = Boolean.FALSE;
		}else if(beaconMessage.immediateSource.equals(node)){ // The forwarding node is retransmiting a message that the node have just transmitted.
			forward = Boolean.FALSE;
		}else {
			FuzzyRoutingEntry re = fuzzyRoutingTable.get(beaconMessage.baseStation);			
			RoutingField field = re.getRoutingField(beaconMessage.forwardingNode);
	
			if (re.containsNodeInNextHop(beaconMessage.forwardingNode)){
				energy = beaconMessage.energy;
				numHops = beaconMessage.hopsToBaseStation;
				
				//Calculating the FSL
				fsl = Utils.calculateFsl(energy, numHops);
				
				field.setNumHops(beaconMessage.hopsToBaseStation);
				field.setSequenceNumber(beaconMessage.sequenceID);
				field.setNextHop((Node)beaconMessage.forwardingNode);
				field.setFsl(fsl);
				myLog.logln("Rota;"+Tools.getGlobalTime()+";Rota Alterada;"+node.ID+";"+beaconMessage.baseStation+";"+beaconMessage.forwardingNode.ID+";"+beaconMessage.sequenceID+";"+beaconMessage.hopsToBaseStation+";"+beaconMessage.energy+";"+fsl);
			}else{
				forward = Boolean.FALSE;
			}
		}
		
		if (forward && beaconMessage.ttl > 1){ //Forward the flooding message
			
			BeaconMessage copy = (BeaconMessage) beaconMessage.clone();
			
			//We have to store the lowest energy found in the path
			if (node.getBateria().getEnergy().compareTo(copy.energy)<0){
				copy.energy = node.getBateria().getEnergy();				
			}
			
			copy.immediateSource = copy.forwardingNode;
			copy.forwardingNode = node;
			copy.ttl--;
			copy.hopsToBaseStation++;
			sendMessage(copy);
		}				
	}

	private void controlColor(){
		node.setColor(Color.YELLOW);
		Utils.restoreColorNodeTimer(node, 5);
	}
	
	private void receivePayloadMessage(PayloadMsg payloadMessage) {
		FuzzyRoutingEntry fre = null;
		
		
		if (payloadMessage.nextHop == null ){ //It's a broadcast
			if (payloadMessage.ttl > 1) {
				Boolean forward = Boolean.TRUE;
				if (payloadMessage.imediateSender.equals(node)){ // The message bounced back
					forward = Boolean.FALSE;
				}else if (payloadMessage.immediateSource.equals(node)){ // The forwarding node is retransmiting a message that the node have just transmitted.
					forward = Boolean.FALSE;					
				}
				else{
					payloadMessage.ttl--;
					payloadMessage.immediateSource = payloadMessage.imediateSender;
					payloadMessage.imediateSender = node;
				}
				if (forward){
					sendBroadcast(payloadMessage);
				}
			}
		}else if (payloadMessage.nextHop.equals(node)){
	
			fre = fuzzyRoutingTable.get(payloadMessage.baseStation);
			payloadMessage.nextHop = fre.getFirstActiveRoute();
			payloadMessage.immediateSource = payloadMessage.imediateSender;
			
			if (payloadMessage.nextHop.equals(payloadMessage.immediateSource)){
				payloadMessage.nextHop = fre.getActiveRoute(1);
			}
			payloadMessage.imediateSender = node;
			
			checkEnergyLevel();
			sendMessage(payloadMessage);
			
		}
	}
	
	private void checkEnergyLevel() {
		Logging deadLog = Utils.getDeadNodesLog();
		Float energy = node.getBateria().getEnergy();
		Boolean sendBeacon = Boolean.FALSE;
		
		if (energy.intValue() > node.energy60.intValue() && energy.intValue() < node.energy70.intValue()){
			node.setMyColor(Color.MAGENTA);
			node.setColor(Color.MAGENTA);
			if (!node.send70){
				//sendBeacon = Boolean.TRUE;
				node.send70 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() > node.energy50.intValue() && energy.intValue() < node.energy60.intValue()){
			node.setMyColor(Color.LIGHT_GRAY);
			node.setColor(Color.LIGHT_GRAY);
			if (!node.send60){
				sendBeacon = Boolean.TRUE;
				node.send60 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() > node.energy40.intValue() && energy.intValue() < node.energy50.intValue()){
			node.setMyColor(Color.GRAY);
			node.setColor(Color.GRAY);
			if (!node.send50){
				sendBeacon = Boolean.TRUE;
				node.send50 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() <= 0){
			node.setMyColor(Color.BLACK);
			node.setColor(Color.BLACK);
			node.setIsDead(Boolean.TRUE);
			deadLog.logln(Tools.getGlobalTime()+":"+node.ID);
		}
		
		if (node.getUseFuzzyRouting() && sendBeacon){
			sendBeaconMessage();
		}
	}
	
	/**
	 * This method is used to send a beacon message when the energy level is too low 
	 */
	private void sendBeaconMessage(){
		BeaconMessage beacon = new BeaconMessage(++node.beaconID, node.getRouting().getSinkNode(), node, node, node);
		//beacon.nextHop = nextHop;
		beacon.energy = node.getBateria().getEnergy();
		//this.sendBroadcast(beacon);
	}

	public void sendBroadcast(Message message) {
		sendMessage(message);
		controlColor();
	}

	public void sendMessage(Integer value) {
		
		if (node.getIsDead()){
			return;
		}
		
		Node nextHopToDestino;
		
		node.seqID++;
		Node destino = getSinkNode();
		
		if (!isMultiPathBalanced){
			nextHopToDestino = getBestRoute(destino);
		}else{
			nextHopToDestino = getNextRouteBalanced(destino); 			
		}
		
		PayloadMsg msg = new PayloadMsg(destino, node, nextHopToDestino, node);
		msg.value = value;
		msg.immediateSource = node;
		msg.sequenceNumber = ++this.seqID;
		
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(msg);
		messageTimer.startRelative(1, node);
		controlColor();
	}
	
	public void sendMessage(Message message) {		
		
		if (node.getIsDead()){
			return;
		}
		
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(message);
		messageTimer.startRelative(1, node);
		controlColor();
	}

	public void setNode(BasicNode node) {
		this.node = node;

	}
	
	
	private void receiveFloodFindFuzzy(Message message){
		Logging myLog = Utils.getGeneralLog();
		
		FloodFindFuzzy floodMsg = (FloodFindFuzzy) message;
		Boolean forward = Boolean.TRUE;
		Float energy = 0f;
		Integer numHops = 0;
		Double fsl = 0d;
		
		energy = floodMsg.energy;
		numHops = floodMsg.hopsToBaseStation;
		
		FuzzyRoutingEntry re = fuzzyRoutingTable.get(floodMsg.baseStation);
		
		if (floodMsg.immediateDestination != null){
			if (floodMsg.immediateDestination.equals(node)){ //It is a message sent by the Base Station to its neighbor
				if (re == null){
					fsl = Utils.calculateFsl(energy, numHops);
					fuzzyRoutingTable.put(floodMsg.baseStation, new FuzzyRoutingEntry(Integer.valueOf(floodMsg.sequenceID), Integer.valueOf(floodMsg.hopsToBaseStation), (Node)floodMsg.forwardingNode, Boolean.TRUE, fsl, floodMsg.index));
					myLog.logln("Rota com Indice"+floodMsg.index + ";" + Tools.getGlobalTime()+";Rota Adicionada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl);
				}
			}else{
				forward = Boolean.FALSE;
			}
		}else{	
			if (floodMsg.forwardingNode.equals(node)){ // The message bounced back. The node must discard the msg.
				forward = Boolean.FALSE;
			}else if(floodMsg.immediateSource.equals(node)){ // The forwarding node is retransmiting a message that the node have just transmitted.
				forward = Boolean.FALSE;
			}
			else if (Utils.isNeighboringNode(node, floodMsg.baseStation)){ //If the node is neighbor from Sink Node, do nothing
				forward = Boolean.FALSE;								
			}else{
				if (re == null){
					fsl = Utils.calculateFsl(energy, numHops);
					fuzzyRoutingTable.put(floodMsg.baseStation, new FuzzyRoutingEntry(Integer.valueOf(floodMsg.sequenceID), Integer.valueOf(floodMsg.hopsToBaseStation), (Node)floodMsg.forwardingNode, Boolean.TRUE, fsl, floodMsg.index));
					myLog.logln("Rota com Indice"+floodMsg.index + ";" + Tools.getGlobalTime()+";Rota Adicionada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl); 
				}else{
					RoutingField field = re.getFirstRoutingEntry(); 
					if (field.getSequenceNumber() < floodMsg.sequenceID) { //Update an existing entrie
						
						fuzzyRoutingTable.remove(floodMsg.baseStation); //Removing the old route do BaseStation
						
						fsl = Utils.calculateFsl(energy, numHops);
						//Putting the new route to the BaseStation
						fuzzyRoutingTable.put(floodMsg.baseStation, new FuzzyRoutingEntry(Integer.valueOf(floodMsg.sequenceID), Integer.valueOf(floodMsg.hopsToBaseStation), (Node)floodMsg.forwardingNode, Boolean.TRUE, fsl, floodMsg.index));
						myLog.logln("Tabela de rota zerada. Novo broadcast do Sink" + floodMsg.index + ";" + Tools.getGlobalTime()+";Rota Alterada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl);
					}else if (!re.hasRouteWithIndex(floodMsg.index) && !re.containsNodeInNextHop(floodMsg.forwardingNode)){ // If the node does not have a route with indice i and does not have a route passing by forwarding node 
						fsl = Utils.calculateFsl(energy, numHops);
						re.addField(Integer.valueOf(floodMsg.sequenceID), Integer.valueOf(floodMsg.hopsToBaseStation), (BasicNode)floodMsg.forwardingNode, Boolean.TRUE, fsl, floodMsg.index);
						myLog.logln("Rota com Indice"+floodMsg.index + ";" +Tools.getGlobalTime()+";Rota Adicionada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl);
					}else{
						forward = Boolean.FALSE;
					}
					
				}
			}
		}
		
		if (forward && floodMsg.ttl > 1){ //Forward the flooding message
			
			FloodFindFuzzy copy = (FloodFindFuzzy) floodMsg.clone();
			
			//We have to store the lowest energy found in the path
			if (node.getBateria().getEnergy().compareTo(copy.energy)<0){
				copy.energy = node.getBateria().getEnergy();				
			}
			
			copy.immediateSource = copy.forwardingNode;
			copy.forwardingNode = node;
			copy.ttl--;
			copy.hopsToBaseStation++;
			copy.immediateDestination = null;
			sendBroadcast(copy);
		}		
	}
	
	
	private void receiveFloodFindMessage(Message message){
		Logging myLog = Utils.getGeneralLog();
		
		FloodFindDsdv floodMsg = (FloodFindDsdv) message;
		Boolean forward = Boolean.TRUE;
		Float energy = 0f;
		Integer numHops = 0;
		Double fsl = 0d;
		
		if (floodMsg.forwardingNode.equals(node)){ // The message bounced back. The node must discard the msg.
			forward = Boolean.FALSE;
		}else if(floodMsg.immediateSource.equals(node)){ // The forwarding node is retransmiting a message that the node have just transmitted.
			forward = Boolean.FALSE;
		}else{
			
			energy = floodMsg.energy;
			numHops = floodMsg.hopsToBaseStation;
			
			//Calculating the FSL
			fsl = Utils.calculateFsl(energy, numHops);
			
			FuzzyRoutingEntry re = fuzzyRoutingTable.get(floodMsg.baseStation);
			
			if (re == null){
				fuzzyRoutingTable.put(floodMsg.baseStation, new FuzzyRoutingEntry(Integer.valueOf(floodMsg.sequenceID), Integer.valueOf(floodMsg.hopsToBaseStation), (Node)floodMsg.forwardingNode, Boolean.TRUE, fsl, 0));
				//numHopsByNode.put(floodMsg.baseStation, floodMsg.hopsToBaseStation);
				
				myLog.logln("Rota;"+Tools.getGlobalTime()+";Rota Adicionada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl);
				
			}else if (!re.containsNodeInNextHop(floodMsg.forwardingNode)){
				//Integer nh = numHopsByNode.get(floodMsg.baseStation);
				
				//If a ndode X receives a flood message from a node Y and node Y is more distant from BS than node X, do nothing
				//if (floodMsg.hopsToBaseStation > nh){
				//	return;
				//}
				
				if (re.getFieldsSize() < numBuffer) { 
					re.addField(Integer.valueOf(floodMsg.sequenceID), Integer.valueOf(floodMsg.hopsToBaseStation), (BasicNode)floodMsg.forwardingNode, Boolean.TRUE, fsl,0);
					myLog.logln("Rota;"+Tools.getGlobalTime()+";Rota Adicionada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl);
				}else{
					Boolean result = re.exchangeRoute(Integer.valueOf(floodMsg.sequenceID), Integer.valueOf(floodMsg.hopsToBaseStation), (BasicNode)floodMsg.forwardingNode, Boolean.TRUE, fsl, 0);
					if (result) myLog.logln("Rota;"+Tools.getGlobalTime()+";Rota Trocada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl); 
				}
				forward = Boolean.FALSE;
			}else {
				RoutingField field = re.getRoutingField(floodMsg.forwardingNode);
				if (field.getSequenceNumber() < floodMsg.sequenceID) { //Update an existing entrie
					field.setNumHops(floodMsg.hopsToBaseStation);
					field.setSequenceNumber(floodMsg.sequenceID);
					field.setNextHop((Node)floodMsg.forwardingNode);
					field.setFsl(fsl);
					myLog.logln("Rota;"+Tools.getGlobalTime()+";Rota Alterada;"+node.ID+";"+floodMsg.baseStation+";"+floodMsg.forwardingNode.ID+";"+floodMsg.sequenceID+";"+floodMsg.hopsToBaseStation+";"+floodMsg.energy+";"+fsl);
				}else{
					forward = Boolean.FALSE;
				}
			}
		}
		
		if (forward && floodMsg.ttl > 1){ //Forward the flooding message
			
			FloodFindDsdv copy = (FloodFindDsdv) floodMsg.clone();
			
			//We have to store the lowest energy found in the path
			if (node.getBateria().getEnergy().compareTo(copy.energy)<0){
				copy.energy = node.getBateria().getEnergy();				
			}
			
			copy.immediateSource = copy.forwardingNode;
			copy.forwardingNode = node;
			copy.ttl--;
			copy.hopsToBaseStation++;
			sendBroadcast(copy);
		}		
	}
	
	public void addRoute(){
		
	}
	
	public void exchangeRoute(){}

	/**
	 * Return the Sink node of the best route
	 */
	public Node getSinkNode() {
		
		Enumeration<Node> nodes = fuzzyRoutingTable.keys();
		Double fsl = null;
		Node sinkNode = null;
		
		while (nodes.hasMoreElements()){
			Node node = nodes.nextElement();
			FuzzyRoutingEntry fre = fuzzyRoutingTable.get(node);
			if (fsl == null){
				fsl = fre.getHighestFsl();
				sinkNode = node;
			}else{
				if (fre.getHighestFsl().compareTo(fsl) > 0){
					fsl = fre.getHighestFsl();
					sinkNode = node;
				}
			}
			
		}
		return sinkNode;
	}

	public void printRoutingTable() {
		Enumeration<Node> nodes = fuzzyRoutingTable.keys();
		
		Tools.clearOutput();
		while (nodes.hasMoreElements()){
			Node node = nodes.nextElement();
			FuzzyRoutingEntry fre = fuzzyRoutingTable.get(node);
			
			for (RoutingField field : fre.getRoutingFields()){
				Tools.appendToOutput("BS: "+node.ID+" / NextHop: "+field.getNextHop()+" / FSL: "+field.getFsl()+" / Ind: "+field.getIndex()+"\n");
			}	
		}
	}
	
	public void treatEvent(EventMessage message){
		if (!fuzzyRoutingTable.isEmpty()){						
			sendMessage(message.value);
			
		}
	}

	public void sendMessageWithTimer(Message message, Integer timer) {
		if (node.getIsDead()){
			return;
		}
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(message);
		messageTimer.startRelative(timer, node);
		controlColor();
		
	}

	public void sendMessageWithTimer(Integer value, Integer timer) {
		if (node.getIsDead()){
			return;
		}
		
		if (fuzzyRoutingTable.isEmpty()){
			return;
		}
		
		node.seqID++;
		Node destino = getSinkNode();
		Node nextHopToDestino = getBestRoute(destino);
		
		PayloadMsg msg = new PayloadMsg(destino, node, nextHopToDestino, node);
		msg.value = value;
		msg.immediateSource = node;
		msg.sequenceNumber = ++this.seqID;
		
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(msg);
		messageTimer.startRelative(timer, node);
		controlColor();
	}

}
