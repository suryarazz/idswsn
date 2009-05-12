package projects.ids_wsn.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * The message used to determine a route between two nodes using
 * incremental flooding.
 * This message requires the read-only policy. 
 */
public class FloodFindDsdv extends Message {

	/**
	 * True if this is a find-message, false if it is the answer-message 
	 * that returns from the destination when the flooding was successful.
	 */
	public boolean isFindMessage = true; 

	/**
	 * The TTL for this message when it's being sent as a find-msg
	 */
	public int ttl;
	
	/**
	 * The Reference to the BaseStation
	 */
	public Node baseStation;
	
	/**
	 * The node who is forwarding the flood message
	 */
	public Node forwardingNode;
	
	
	/**
	 * Number of hops to BaseStation 
	 */
	public int hopsToBaseStation;
	
	/**
	 * Sequence ID of this message 
	 */
	public int sequenceID; 
	
	/**
	 * The node to which a route should be established. 
	 */
	//public Node destination;
	
	/**
	 * Default constructor. 
	 */
	public FloodFindDsdv(int seqID, Node baseStation, Node forwardingNode) {
		ttl = 500; // initial TTL
		isFindMessage = true;
		hopsToBaseStation = 0;
		sequenceID = seqID;
		this.baseStation = baseStation;
		this.forwardingNode = forwardingNode;
	}
	
	@Override
	public Message clone() {
		// This message requires a read-only policy
		return this;
	}
	
	
	/**
	 * @return A real clone of this message, i.e. a new message object
	 */
	public FloodFindDsdv getRealClone() {
		FloodFindDsdv msg = new FloodFindDsdv(this.sequenceID, this.baseStation, this.forwardingNode);
		msg.ttl = this.ttl;
		msg.isFindMessage = this.isFindMessage;
		msg.hopsToBaseStation = this.hopsToBaseStation;
		msg.forwardingNode = this.forwardingNode;
		return msg;
	}
}