package projects.ids_wsn.nodes.timers;

import java.awt.Color;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import sinalgo.nodes.timers.Timer;

public class RestoreColorTime extends Timer {

	@Override
	public void fire() {
		Color color = ((BasicNode) node).getMyColor();
		((BasicNode) node).setColor(color);

	}

}
