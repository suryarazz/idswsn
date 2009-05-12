package projects.ids_wsn.nodes.nodeImplementations;

import java.util.List;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class MonitorNode extends BasicNode implements IMonitor {
	
	private List<DataMessage> listDataMessages;

	public void doInference() {
	}
	
	@Override
	protected void postProcessingMessage(Message message) {
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
		
	}
	
	@Override
	public void beforeSendingMessage(Message message) {
		
		//We also have to store the messages that the Monitors are forwarding, in order
		//to correctly correlate the messages
		if (message instanceof PayloadMsg){
			PayloadMsg msg = (PayloadMsg) message;
			addMessageToList(msg);
		}
	}

}
