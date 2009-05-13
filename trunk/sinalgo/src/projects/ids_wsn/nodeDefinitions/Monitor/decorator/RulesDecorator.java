package projects.ids_wsn.nodeDefinitions.Monitor.decorator;

import java.util.List;

import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;

public class RulesDecorator implements IMonitor {
	
	private IMonitor monitor;

	public void doInference() {
		monitor.doInference();

	}
	
	public RulesDecorator(IMonitor monitor) {
		this.monitor = monitor;		
	}

	public List<DataMessage> getDataMessage() {
		return monitor.getDataMessage();
	}

}
