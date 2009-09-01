package projects.ids_wsn.nodes.timers;

import projects.ids_wsn.enumerators.ChordMessageType;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;
import sinalgo.nodes.timers.Timer;

/**
 * A message sent to baseStation to advise there is a supervisor dead or out of the network
 * @author Alex Lacerda Ramos
 *
 */
public class ChordDelayTimer extends Timer{

	/**
	 * check whether there is any monitor down in the monitor list of a neighbor  
	 */
	@Override
	public void fire() {
		if (!(node instanceof BasicNode)) {
			return;
		}
	
		BasicNode basicNode = (BasicNode) node;
		
		for (MonitorNode monitor : basicNode.monitors) {
			if (monitor.getIsDead()) {
				basicNode.sendMessageToBaseStation(ChordMessageType.MONITOR_DOWN.getValue());
				UtilsChord.removeMonitorFromLists(monitor);//reseta a lista de monitores de todos os nós
				UtilsChord.removeTimers();//remove os timers dos nós que deixarem de ser vizinhos de monitores
			}
		}
		
		this.startRelative(BasicNode.DELAY_TIME, basicNode);//recursive repetition on every time interval
	}
}
