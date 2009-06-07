package projects.ids_wsn.nodeDefinitions;

import java.awt.Color;
import java.awt.Graphics;
import java.util.List;
import java.util.Vector;

import projects.ids_wsn.Utils;
import projects.ids_wsn.nodeDefinitions.energy.EnergyMode;
import projects.ids_wsn.nodeDefinitions.energy.IEnergy;
import projects.ids_wsn.nodeDefinitions.routing.IRouting;
import projects.ids_wsn.nodes.messages.BeaconMessage;
import projects.ids_wsn.nodes.timers.RepeatSendMessageTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public abstract class BasicNode extends Node{
	
	private Color myColor = Color.BLUE;
	private List<Integer> blackList = new Vector<Integer>();
	private Integer firstRoutingTtlRcv = 0;
	private IRouting routing;
	public int seqID = 0;
	public int beaconID = 0;
	private IEnergy bateria;
	
	private Float energy60 = 0f;
	private Float energy50 = 0f;
	private Float energy40 = 0f;
	private Float energy20 = 0f;
	
	private Boolean send60 = Boolean.FALSE;
	private Boolean send50 = Boolean.FALSE;
	private Boolean send40 = Boolean.FALSE;
	//private Boolean send20 = Boolean.FALSE;
	
		

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleMessages(Inbox inbox) {
		preHandleMessage(inbox);
		
		//Spent energy due to the listening mode
		this.bateria.spend(EnergyMode.LISTEN);
		
		while (inbox.hasNext()){
					
			Message message = inbox.next();
			
			//Message processing
			this.bateria.spend(EnergyMode.RECEIVE);
			
				preProcessingMessage(message);
			
			routing.receiveMessage(message);
			
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
		
		try {
			//Here, we have to get the battery implementation from Config.xml and inject into battery attribute
			String energyModel = Configuration.getStringParameter("Energy/EnergyModel");
			bateria = Utils.StringToEnergyModel(energyModel);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Energy Model not found");
			e.printStackTrace();
		}
		
		energy60 = this.getBateria().getInitialEnergy() * 0.6f;
		energy50 = this.getBateria().getInitialEnergy() * 0.5f;
		energy40 = this.getBateria().getInitialEnergy() * 0.4f;
		energy20 = this.getBateria().getInitialEnergy() * 0.2f;
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
		Float energy = this.getBateria().getEnergy();
		
		
		if (energy.intValue() > energy50.intValue() && energy.intValue() < energy60.intValue()){
			this.setMyColor(Color.MAGENTA);
			this.setColor(Color.MAGENTA);
			if (!send60){
				sendBeaconMessage();
				send60 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() > energy40.intValue() && energy.intValue() < energy50.intValue()){
			this.setMyColor(Color.GRAY);
			this.setColor(Color.GRAY);
			if (!send50){
				sendBeaconMessage();
				send50 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() > energy20.intValue() && energy.intValue() < energy40.intValue()){
			this.setMyColor(Color.DARK_GRAY);
			this.setColor(Color.DARK_GRAY);
			if (!send40){
				sendBeaconMessage();
				send40 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() <= 0){
			this.setMyColor(Color.BLACK);
			this.setColor(Color.BLACK);
		}
	}
	
	/**
	 * This method is used to send a beacon message when the energy level is too low 
	 */
	private void sendBeaconMessage(){
		BeaconMessage beacon = new BeaconMessage(++beaconID, this.routing.getSinkNode(), this, this, this);
		beacon.energy = this.getBateria().getEnergy();
		routing.sendBroadcast(beacon);
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

	public Integer getFirstRoutingTtlRcv() {
		return firstRoutingTtlRcv;
	}

	public void setFirstRoutingTtlRcv(Integer firstRoutingTtlRcv) {
		this.firstRoutingTtlRcv = firstRoutingTtlRcv;
	}
	
	@NodePopupMethod(menuText="Send a message to the Base Station")
	public void sendMessageToBaseStation(){
		routing.sendMessage(10);
	}
	
	@NodePopupMethod(menuText="Send continuously a message to the Base Station per Round")
	public void sendMessageToBaseStationPerRound(){
		RepeatSendMessageTimer t = new RepeatSendMessageTimer(10);
		t.startRelative(1, this);
	}
	
	
	
	@NodePopupMethod(menuText="Print energy")
	public void printEnergy(){
		Tools.appendToOutput("Total spent energy: "+getBateria().getTotalSpentEnergy()+"\n");
		Tools.appendToOutput("Energy left: "+getBateria().getEnergy()+"\n");
	}
	
	public Boolean isNodeNextHop(Node destination){
		return routing.isNodeNextHop(destination);
	}
	
	@NodePopupMethod(menuText="Print Routing Table")
	public void printRoutingTable(){
		routing.printRoutingTable();		
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

	public IEnergy getBateria() {
		return bateria;
	}

	public void setBateria(IEnergy bateria) {
		this.bateria = bateria;
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		//super.drawAsDisk(g, pt, highlight, 10);
		String text = String.valueOf(this.ID);
		super.drawNodeAsDiskWithText(g, pt, highlight, text, 8, Color.WHITE);
	}

	public IRouting getRouting() {
		return routing;
	}
}
