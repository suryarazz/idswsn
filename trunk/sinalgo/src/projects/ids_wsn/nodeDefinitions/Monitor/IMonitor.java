package projects.ids_wsn.nodeDefinitions.Monitor;

import java.util.List;

public interface IMonitor {
	public void doInference();
	public List<DataMessage> getDataMessage();
}
