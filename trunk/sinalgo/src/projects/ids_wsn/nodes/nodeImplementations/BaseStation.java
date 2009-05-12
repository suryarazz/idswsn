package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.timers.SimpleMessageTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;

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
		// TODO Auto-generated method stub
		
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
		FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this);
		SimpleMessageTimer t = new SimpleMessageTimer(floodMsg);
		t.startRelative(1, this);
		this.isRouteBuild = Boolean.TRUE;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Base Station";
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "BS";
		//super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.WHITE);
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 11, Color.WHITE);
	}
	

}
