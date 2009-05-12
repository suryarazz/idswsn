package projects.ids_wsn.nodeDefinitions.routing;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.PayloadMsgDsdv;
import projects.ids_wsn.nodes.timers.SimpleMessageTimer;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class DSDV implements IRouting {
	
	private final static Integer NUM_MAX_ENTRIES_PER_ROUTE = 3; 
	
	//Node who is using the protocol
	private BasicNode node;
	
	public void setNode(BasicNode node) {
		this.node = node;
		
	}
	
	public class MultiRoutingEntry {
		public List<Integer> sequenceNumber = new ArrayList<Integer>();
		public List<Integer> numHops = new ArrayList<Integer>();
		public List<Node> nextHops = new ArrayList<Node>();
		public List<Boolean> active = new ArrayList<Boolean>();
		
		public MultiRoutingEntry(Integer seqNumb, Integer numHops, Node node){
			this.sequenceNumber.add(seqNumb);
			this.numHops.add(numHops);
			this.nextHops.add(node);
			this.active.add(Boolean.TRUE);
		}
		
		public Node getFirstActiveRoute(){
			for (int i = 0; i < active.size(); i++){
				if (active.get(i) == Boolean.TRUE){
					return nextHops.get(i);
				}
			}
			return nextHops.get(0);
		}
		
		public void addElements(Integer seqNumb, Integer numHops, Node node){
			this.sequenceNumber.add(seqNumb);
			this.numHops.add(numHops);
			this.nextHops.add(node);
			this.active.add(Boolean.TRUE);
		}
	}
	
	
	
	public int seqID = 0;
	
	//Routing Table with multiple routes
	protected Hashtable<Node, MultiRoutingEntry> multiRoutingTable = new Hashtable<Node, MultiRoutingEntry>();

	public void receiveMessage(Message message) {
		
		if (message instanceof FloodFindDsdv){ 
			receiveFloodFindMsg(message);
			
		}else if (message instanceof PayloadMsgDsdv){ //It's a payload message
			PayloadMsgDsdv payloadMessage = (PayloadMsgDsdv) message;
			receivePayloadMessage(payloadMessage);
		}else{ //It's an event
			//Only route the message if the routing table is not null;
			//treatEvent(message, this);
		}
	}
	
	private void receiveFloodFindMsg(Message message) {
		FloodFindDsdv floodMsg = (FloodFindDsdv) message;
		Boolean forward = Boolean.TRUE;
		
		if (floodMsg.forwardingNode.equals(this)){ // The message bounced back. The node must discard the msg.
			forward = Boolean.FALSE;
		}else{
			MultiRoutingEntry re = multiRoutingTable.get(floodMsg.baseStation);
			if (re == null){
				multiRoutingTable.put(floodMsg.baseStation, new MultiRoutingEntry(floodMsg.sequenceID, floodMsg.hopsToBaseStation, floodMsg.forwardingNode));
				node.setFirstRoutingTtlRcv(floodMsg.ttl);
				System.out.println("puting a route to BS in Routing Table: "+floodMsg.baseStation.ID);
			}else if (!re.nextHops.contains(floodMsg.forwardingNode)){
				if (re.nextHops.size() < NUM_MAX_ENTRIES_PER_ROUTE) { 
						re.addElements(floodMsg.sequenceID, floodMsg.hopsToBaseStation, floodMsg.forwardingNode);
				}
				forward = Boolean.FALSE;
			}else {
				Integer ind = re.nextHops.indexOf(floodMsg.forwardingNode);
				if (re.sequenceNumber.get(ind) < floodMsg.sequenceID) { //Update an existing entrie
					re.numHops.set(ind, floodMsg.hopsToBaseStation);
					re.sequenceNumber.set(ind,floodMsg.sequenceID);
					re.nextHops.set(ind, floodMsg.forwardingNode);							
				}else{
					forward = Boolean.FALSE;
				}
			}
		}
		
		if (forward && floodMsg.ttl > 1){ //Forward the flooding message
			
			FloodFindDsdv copy = floodMsg.getRealClone();
			copy.forwardingNode = node;
			copy.ttl--;
			copy.hopsToBaseStation++;
			sendBroadcast(copy);
		}
	}
	
	private void receivePayloadMessage(PayloadMsgDsdv payloadMessage) {
		MultiRoutingEntry re = null;
		
		
		if (payloadMessage.nextHop == null ){ //It's a broadcast
			if (payloadMessage.ttl > 1) {
				Boolean forward = Boolean.TRUE;
				if (payloadMessage.imediateSender.equals(this)){ // The message bounced back
					forward = Boolean.FALSE;
				}else{
					payloadMessage.ttl--;
					payloadMessage.imediateSender = node;
				}
				if (forward){
					sendBroadcast(payloadMessage);
				
				}
			}
		}else if (payloadMessage.nextHop.equals(this)){
			//The node receives the packet. It must spends the energy to receive
			
			//this.setColor(Color.YELLOW);
			re = multiRoutingTable.get(payloadMessage.baseStation);
			payloadMessage.nextHop = re.getFirstActiveRoute();
			payloadMessage.imediateSender = node;
			
			sendMessage(payloadMessage);
			
			//bateria.spent(EnergyMode.SEND);
			//sendBroadcast(payloadMessage);
		}
	}

	public void sendMessage(Message message) {
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(message);
		messageTimer.startRelative(1, node);
	}

	public void sendBroadcast(Message message) {
		sendMessage(message);		
	}

}
