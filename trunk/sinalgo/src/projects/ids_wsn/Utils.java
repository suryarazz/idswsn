package projects.ids_wsn;

import projects.ids_wsn.nodeDefinitions.routing.DSDV;
import projects.ids_wsn.nodeDefinitions.routing.IRouting;
import projects.ids_wsn.nodes.timers.RestoreColorTime;
import sinalgo.nodes.Node;

public class Utils {
	
	public static IRouting StringToRoutingProtocol(String name){
		IRouting routing = null;
		if (name.contains("DSDV")){
			routing = new DSDV();
		}
		return routing;
	}
	
	public static void restoreColorNodeTimer(Node node, Integer time){
		RestoreColorTime restoreColorTime = new RestoreColorTime();
		restoreColorTime.startRelative(time, node);
	}

}
