package projects.ids_wsn.nodeDefinitions.routing;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public interface IRouting {
	public void sendMessage(Message message);
	public void sendBroadcast(Message message);
	public void receiveMessage(Message message);
	public void setNode(BasicNode node);
	public Node getBestRoute(Node destino);

}
