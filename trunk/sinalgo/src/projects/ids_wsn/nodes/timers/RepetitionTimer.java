package projects.ids_wsn.nodes.timers;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

public class RepetitionTimer extends Timer {
	Message msg;
	int interval;
	
	public RepetitionTimer(Message msg, int interval){
		this.msg = msg;
		this.interval = interval;
	}

	@Override
	public void fire() {
		((BasicNode)node).sendMessage(msg);
		this.startRelative(interval, node); //Recursive start of the time
	}

}