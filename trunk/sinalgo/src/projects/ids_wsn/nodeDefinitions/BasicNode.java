package projects.ids_wsn.nodeDefinitions;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import projects.ids_wsn.Utils;
import projects.ids_wsn.nodeDefinitions.routing.IRouting;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.timers.SimpleMessageTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public abstract class BasicNode extends Node{
	
	private Color myColor = Color.BLUE;
	private List<Integer> blackList = new Vector<Integer>();
	private Integer firstRoutingTtlRcv = 0;
	private IRouting routing;
	private int seqID = 0;
		

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessages(Inbox inbox) {
		preHandleMessage(inbox);
		
		while (inbox.hasNext()){
					
			Message message = inbox.next();
			
				preProcessingMessage(message);
			
			receiveMessage(message);
			
				postProcessingMessage(message);
		}
		
		postHandleMessage(inbox);
		
	}

	@Override
	public void init() {
		this.setColor(getMyColor());
		
		try {
			//Here, we have to get the routing protocol from Config.xml and inject into routing attribute
			String routingProtocol = Configuration.getStringParameter("NetworkLayer/RoutingProtocol");
			routing = Utils.StringToRoutingProtocol(routingProtocol);
			routing.setNode(this);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Chave do protocolo de roteamento não foi encontrado");
			e.printStackTrace();
		}
		
	}

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub
		
	}

	public Color getMyColor() {
		return myColor;
	}

	public void setMyColor(Color myColor) {
		this.myColor = myColor;
	}

	public List<Integer> getBlackList() {
		return blackList;
	}

	public void setBlackList(Integer item) {
		blackList.add(item);
	}
	
	public void sendMessage(Message message){
		routing.sendMessage(message);
	}
	
	public void receiveMessage(Message message){
		routing.receiveMessage(message);
	}

	public Integer getFirstRoutingTtlRcv() {
		return firstRoutingTtlRcv;
	}

	public void setFirstRoutingTtlRcv(Integer firstRoutingTtlRcv) {
		this.firstRoutingTtlRcv = firstRoutingTtlRcv;
	}
	
	@NodePopupMethod(menuText="Send a message to the Base Station")
	public void sendMessageToBaseStation(){
		this.seqID++;
		Node destino = Tools.getNodeByID(1);
		Node nextHopToDestino = routing.getBestRoute(destino);
		
		PayloadMsg msg = new PayloadMsg(destino, this, nextHopToDestino, this);
		msg.sequenceNumber = ++this.seqID;
		SimpleMessageTimer t = new SimpleMessageTimer(msg);
		t.startRelative(1, this);
	}
	
	public Boolean isNodeNextHop(Node destination){
		return routing.isNodeNextHop(destination);
	}
	
	
	/**
	 * This method is called before the Inbox iterator
	 */
	protected void preHandleMessage(Inbox inbox){}	
	
	/**
	 * This method is called before the end of the handleMessage method
	 */
	protected void postHandleMessage(Inbox inbox){}
	
	/**
	 * This method is called before the message processing in the handleMessage method
	 */
	protected void preProcessingMessage(Message message){}
	
	/**
	 * This method is called after the message processing in the Inbox iterator
	 */
	protected void postProcessingMessage(Message message){}
	
	public void beforeSendingMessage(Message message){}
	public void afterSendingMessage(Message message){}
}
