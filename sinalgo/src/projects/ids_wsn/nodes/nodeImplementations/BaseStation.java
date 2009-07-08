package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import projects.ids_wsn.comparators.EnergyComparator;
import projects.ids_wsn.enumerators.Order;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.FloodFindFuzzy;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.timers.BaseStationMessageTimer;
import projects.ids_wsn.nodes.timers.BaseStationRepeatMessageTimer;
import projects.ids_wsn.nodes.timers.RestoreColorBSTime;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.storage.ReusableListIterator;

public class BaseStation extends Node {
	
	private Integer sequenceID = 0;

	private Boolean isRouteBuild = Boolean.FALSE;
	
	private Integer countReceivedMessages;
	
	private Integer numberOfRoutes;
	
	private Integer broadcastID = 0;
	
	private Boolean printReceivedMessage =  Boolean.FALSE;
	
	public Boolean getIsRouteBuild() {
		return isRouteBuild;
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()){
			Message message = inbox.next();
			
			if (message instanceof PayloadMsg){
				PayloadMsg payloadMessage = (PayloadMsg) message;
				if ((payloadMessage.nextHop == null) ||
						(payloadMessage.nextHop.equals(this))){
						countReceivedMessages++;
						controlColor();		
						if (printReceivedMessage){
							Tools.appendToOutput("ID: "+payloadMessage.sender.ID+" /Msg: "+payloadMessage.sequenceNumber+" /Timer: "+Tools.getGlobalTime()+"\n");
						}
				}
			}
		}
	}
	
	
	@NodePopupMethod(menuText="Enable/Disable the printing of received messages")
	public void enableDisableImpMsg(){
		Tools.clearOutput();
		if (printReceivedMessage) 
			printReceivedMessage = Boolean.FALSE; 
		else 
			printReceivedMessage = Boolean.TRUE;
	}
	
	@NodePopupMethod(menuText="Print the number of received messages")
	public void printCountReceivedMessages(){
		//Tools.clearOutput();
		Tools.appendToOutput(this+": "+this.countReceivedMessages+" messages\n");
	}

	@Override
	public void init() {
		this.countReceivedMessages = 0;
		
		try {
			//Here, we have to get the Number of Routes Value from Config.xml and inject into numberOfRoutes attribute
			String number = Configuration.getStringParameter("NetworkLayer/NumbersOfRoutesFuzzy");
			this.numberOfRoutes = Integer.valueOf(number);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("NumbersOfRoutesFuzzy key not founf in Config.xml");
			e.printStackTrace();
		}
	}

	@Override
	public void neighborhoodChange() {}

	@Override
	public void postStep() {}

	@Override
	public void preStep() {}
	
	@NodePopupMethod(menuText="Build routing tree - DSDV")
	public void sendMessageTo(){	
		FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this, this, this);
		floodMsg.energy = 500000;
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg);
		t.startRelative(1, this);
		this.isRouteBuild = Boolean.TRUE;
	}
	
	@NodePopupMethod(menuText="Build routing tree - Fuzzy")
	public void sendMessageFuzzyTo(){	
		BaseStationRepeatMessageTimer t = new BaseStationRepeatMessageTimer(1500);
		t.startRelative(1, this);
		
	}
	
	public void prepareSendRouteMessage(){
		List<BasicNode> listNodes = getNeighboringNodes();
		broadcastID++;
		
		int x = 0;
		for (Node n : listNodes){
			x = x + 1;
			sendRouteMessage(x, n, broadcastID);
		}
	}
	
	private void sendRouteMessage(Integer index, Node dst, int broadcastID) {
		FloodFindFuzzy floodMsg = new FloodFindFuzzy(broadcastID, this, this, this, this, index, dst, broadcastID);
		//FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this, this, this);
		floodMsg.energy = 500000;
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg);
		t.startRelative((index+1)*2, this);
		this.isRouteBuild = Boolean.TRUE;
		
	}

	@Override
	public String toString() {
		return "Base Station "+this.ID;
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "BS";
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 11, Color.WHITE);
	}
	
	private void controlColor(){
		this.setColor(Color.YELLOW);
		RestoreColorBSTime restoreColorTime = new RestoreColorBSTime();
		restoreColorTime.startRelative(5, this);
	}
	
	public Integer getSequenceID(){
		return ++sequenceID;
	}
	
	private List<BasicNode> getNeighboringNodes(){
		List<BasicNode> listNodes = new ArrayList<BasicNode>();
		Node n = null;
		Edge e = null;
		Integer index = 0;
		Connections conn = this.outgoingConnections;
		ReusableListIterator<Edge> listConn = conn.iterator();
		
		while (listConn.hasNext()){
			e = listConn.next();
			n = e.endNode;
			if (n instanceof BasicNode)
				listNodes.add((BasicNode)n);
			index++;
		}
		
		Collections.sort(listNodes,new EnergyComparator(Order.DESC));
		return listNodes;
	}
	

}
