package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.RepetitionRule;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.RetransmissionRule;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class MonitorNode extends BasicNode implements IMonitor {
	
	private List<DataMessage> listDataMessages;
	//private Integer inferenceNumberOfPackets;
	
	//Local inference list of nodes who have bronken the repetition rule
	private List<Node> listLocalRepetitionNodes;
	
	private Integer internalBuffer;

	public void doInference() {
				
	}
	
	@Override
	public void init() {
		setMyColor(Color.RED);
		super.init();
		listDataMessages = new ArrayList<DataMessage>();
		try {
			internalBuffer = Configuration.getIntegerParameter("Monitor/Inference/InternalBuffer");
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Key Monitor/Inference/InternalBuffer not found");
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void preProcessingMessage(Message message) {
		if (message instanceof PayloadMsg){
			PayloadMsg msg = (PayloadMsg) message;
			addMessageToList(msg);			
		}
		
	}

	private void addMessageToList(PayloadMsg msg) {
		DataMessage dataMessage = new DataMessage();
		dataMessage.setClock((int)Tools.getGlobalTime());
		dataMessage.setData(msg.value);
		dataMessage.setFinalDst(msg.baseStation.ID);
		dataMessage.setIdMsg(msg.sequenceNumber);
		dataMessage.setImediateDst(msg.nextHop.ID);
		dataMessage.setImediateSrc(msg.imediateSender.ID);
		dataMessage.setSource(msg.sender.ID);
		
		listDataMessages.add(dataMessage);
		
		if (listDataMessages.size() == internalBuffer){
			applyRules();
			listDataMessages.clear();
		}
		
	}
	
	@Override
	public void beforeSendingMessage(Message message) {
		if (message instanceof PayloadMsg){
			PayloadMsg msg = (PayloadMsg) message;
			addMessageToList(msg);
		}
	}
	
	private void applyRules(){
		IMonitor rule1 = new RepetitionRule(this);
		IMonitor rule2 = new RetransmissionRule(rule1);
		rule2.doInference();
	}

	public List<DataMessage> getDataMessage() {
		return listDataMessages;
	}

	public void setListLocalRepetitionNodes(List<Node> listRepetitionNodes) {
		this.listLocalRepetitionNodes = listRepetitionNodes;
	}

	public List<Node> getListLocalRepetitionNodes() {
		return listLocalRepetitionNodes;
	}
	
	
	public void setLocalMaliciousList(Rules rule, List<Node> lista) {
		switch (rule) {
		case REPETITION:
			setListLocalRepetitionNodes(lista);						
			break;
		}
		
	}

}
