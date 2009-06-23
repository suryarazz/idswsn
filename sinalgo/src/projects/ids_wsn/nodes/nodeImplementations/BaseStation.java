package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import projects.ids_wsn.Utils;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.FloodFindFuzzy;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.timers.BaseStationMessageTimer;
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
				}
			}
		}
		
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
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg, 0);
		t.startRelative(1, this);
		this.isRouteBuild = Boolean.TRUE;
	}
	
	@NodePopupMethod(menuText="Build routing tree - Fuzzy")
	public void sendMessageFuzzyTo(){	
		
		Map<Integer, Node> mapNodes = getNeighboringNodes();
		
		for (Node n : mapNodes.values()){
			for (int x = 0; x < this.numberOfRoutes; x++){
				sendRouteMessage(x, n);
			}
		}
	}
	
	private void sendRouteMessage(Integer index, Node dst) {
		FloodFindFuzzy floodMsg = new FloodFindFuzzy(++sequenceID, this, this, this, this, index, dst);
		//FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this, this, this);
		floodMsg.energy = 500000;
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg, 1500);
		t.startRelative(index+1, this);
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
	
	private Map<Integer, Node> getNeighboringNodes(){
		Map<Integer, Node> mapNodes = new Hashtable<Integer, Node>();
		Node n = null;
		Edge e = null;
		Integer index = 0;
		Connections conn = this.outgoingConnections;
		ReusableListIterator<Edge> listConn = conn.iterator();
		
		while (listConn.hasNext()){
			e = listConn.next();
			n = e.endNode;
			mapNodes.put(index, n);
			index++;
		}
		
		return mapNodes;
	}
	

}
