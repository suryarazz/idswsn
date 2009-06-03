package projects.ids_wsn.nodeDefinitions.routing;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public interface IRouting {
	public void sendMessage(Integer value);
	public void sendMessage(Message message);
	public void sendBroadcast(Message message);
	public void receiveMessage(Message message);
	public void setNode(BasicNode node);
	public Node getBestRoute(Node destino);
	
	/**
	 * Return the sink node. 
	 * If the network uses multiple sinks, this method
	 * must return the Sink of the best route
	 * 
	 */
	public Node getSinkNode();
	
	/**
	 * This method will check wether the destination Node is the Source Node next hop or not.
	 * 
	 * @param destination Node
	 * @return Boolen
	 */
	public Boolean isNodeNextHop(Node destination);

}
