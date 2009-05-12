package projects.ids_wsn.nodes.messages;

import projects.ids_old.monitors.PayloadMonitor;
import projects.ids_old.monitors.PayloadWarning;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * A message used to transport data from a sender to a receiver
 */
public class PayloadMsg extends Message {
	
	public Node baseStation; // BaseStation: node who should receive this msg
	public Node nextHop;
	public Node imediateSender;
	public Node sender; // The node who has crated the msg
	public int sequenceNumber; // a number to identify this msg, set by the sender 
	//public RetryPayloadMessageTimer ackTimer; // The timer set on the sender that will fire if there is no ACK returning from the destination 
	public Integer value = 0;
	public PayloadMonitor payloadMonitor;
	public PayloadWarning payloadWarning;
	public Integer ttl;
	
	
	public PayloadMsg(Node destination, Node sender) {
		this.baseStation = destination;
		this.sender = sender;
		this.imediateSender = sender;
		this.nextHop = null;
		this.value = 0;
		this.payloadMonitor = new PayloadMonitor();
		this.ttl = 30;
	}
	
	@Override
	public boolean equals(Object obj) {
		PayloadMsg msg = (PayloadMsg) obj;
		
		if (msg.baseStation == null){
			if ((msg.sender.equals(this.sender)) && (msg.value.equals(this.value))){
				return true;
			}else{
				return false;
			}			
		}else if ((msg.baseStation.equals(this.baseStation)) && (msg.sender.equals(this.sender)) &&
				(msg.value.equals(this.value))){
				return true;
			}else{
				return false;
		}
	}
	
	/**
	 * Constructor
	 * If nextHop is null then the message shall be received by all the neigboring nodes
	 * @param destination The node to send this msg to
	 * @param sender The sender who sends this msg
	 */
	public PayloadMsg(Node destination, Node sender, Node nextHop, Node immediateSrc) {
		this.baseStation = destination;
		this.sender = sender;
		this.nextHop = nextHop;
		this.imediateSender = immediateSrc;
		this.payloadMonitor = new PayloadMonitor();
		this.ttl = 30;
	}
	
	@Override
	public Message clone() {
		PayloadMsg newMessage = new PayloadMsg(this.baseStation, this.sender, this.nextHop, this.imediateSender);
		newMessage.sequenceNumber = this.sequenceNumber;
		//newMessage.ackTimer = this.ackTimer;
		newMessage.value = this.value;
		newMessage.payloadMonitor = this.payloadMonitor;
		newMessage.payloadWarning = this.payloadWarning;
		newMessage.ttl = this.ttl;
		
		return newMessage;
	}
	
	@Override
	public String toString() {
		return "Packet: Seq. Number: "+sequenceNumber+" - Sender: "+sender+" - Next hop: "+nextHop;
	}

}
