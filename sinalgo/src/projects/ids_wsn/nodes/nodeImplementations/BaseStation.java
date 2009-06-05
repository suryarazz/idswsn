package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.timers.BaseStationMessageTimer;
import projects.ids_wsn.nodes.timers.RestoreColorBSTime;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class BaseStation extends Node {
	
	private Integer sequenceID = 0;

	private Boolean isRouteBuild = Boolean.FALSE;
	
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
						controlColor();					
				}
			}
		}
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
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
	
	@NodePopupMethod(menuText="Build routing tree")
	public void sendMessageTo(){	
		FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this, this, this);
		floodMsg.energy = 500000;
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg, 0);
		t.startRelative(1, this);
		this.isRouteBuild = Boolean.TRUE;
	}
	
	@NodePopupMethod(menuText="Build routing tree - Fuzzy")
	public void sendMessageFuzzyTo(){	
		FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this, this, this);
		floodMsg.energy = 500000;
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg, 1000);
		t.startRelative(1, this);
		this.isRouteBuild = Boolean.TRUE;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Base Station "+this.ID;
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "BS";
		//super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.WHITE);
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
	

}
