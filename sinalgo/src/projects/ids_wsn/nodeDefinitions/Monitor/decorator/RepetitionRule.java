package projects.ids_wsn.nodeDefinitions.Monitor.decorator;

import java.util.ArrayList;
import java.util.List;

import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;

public class RepetitionRule extends RulesDecorator {

	public RepetitionRule(IMonitor monitor) {
		super(monitor);
	}
	
	@Override
	public void doInference() {
		applayRepetitionRule();				
		super.doInference();
	}
	
	private void applayRepetitionRule(){
		Integer tamBuffer = getDataMessage().size();
		List<DataMessage> listTemp = new ArrayList<DataMessage>();
		DataMessage data1;
		DataMessage data2;
		
		for (int x=0; x<tamBuffer-1;x++){
			data1 = getDataMessage().get(x);
			
			if (listTemp.contains(data1)){
				continue;
			}
			
			for (int y=x+1; y<tamBuffer;y++){
				data2 = getDataMessage().get(y);
				if (data1.equals(data2)){
					listTemp.add(data1);
					break;
				}
			}
			
		}
	}

}
