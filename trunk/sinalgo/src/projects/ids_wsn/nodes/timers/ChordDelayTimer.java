package projects.ids_wsn.nodes.timers;

import projects.ids_wsn.enumerators.ChordMessageType;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;
import sinalgo.nodes.timers.Timer;

public class ChordDelayTimer extends Timer{

	public static final Integer DELAY_TIME = 100;
	
	@Override
	public void fire() {
		if (!(node instanceof BasicNode)) {
			return;
		}
	
		BasicNode basicNode = (BasicNode) node;
		
		for (MonitorNode monitor : basicNode.supervisors) {
			if (monitor.getIsDead()) {
				basicNode.sendMessageToBaseStation(ChordMessageType.SUPERVISOR_DOWN.getValue());
				UtilsChord.resetSupervisorsLists();
			}
		}
		
		this.startRelative(DELAY_TIME, basicNode);
	}

}
