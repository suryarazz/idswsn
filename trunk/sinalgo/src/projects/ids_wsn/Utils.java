package projects.ids_wsn;

import projects.ids_wsn.nodeDefinitions.routing.DSDV;
import projects.ids_wsn.nodeDefinitions.routing.IRouting;

public class Utils {
	
	public static IRouting StringToRoutingProtocol(String name){
		IRouting routing = null;
		if (name.contains("DSDV")){
			routing = new DSDV();
		}
		return routing;
	}

}
