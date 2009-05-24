package projects.ids_wsn;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import projects.ids_wsn.nodeDefinitions.energy.IEnergy;
import projects.ids_wsn.nodeDefinitions.energy.simple.SimpleEnergy;
import projects.ids_wsn.nodeDefinitions.routing.DSDV;
import projects.ids_wsn.nodeDefinitions.routing.IRouting;
import projects.ids_wsn.nodeDefinitions.routing.fuzzy.FuzzyRouting;
import projects.ids_wsn.nodes.timers.RestoreColorTime;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class Utils {
	
	public static IRouting StringToRoutingProtocol(String name){
		IRouting routing = null;
		if (name.contains("DSDV")){
			routing = new DSDV();
		}else if (name.contains("Fuzzy")){
			routing = new FuzzyRouting();
		}
		return routing;
	}
	
	public static IEnergy StringToEnergyModel(String name){
		IEnergy energy = null;
		if (name.contains("Simple")){
			energy = new SimpleEnergy();
		}
		return energy;
	}
	
	public static void restoreColorNodeTimer(Node node, Integer time){
		RestoreColorTime restoreColorTime = new RestoreColorTime();
		restoreColorTime.startRelative(time, node);
	}
	
	public static Double calculateFsl(Double energy, Integer numHops){
		Double fsl = 0d;
		
		String fileName = "fcl/routing.fcl";
		FIS fis = FIS.load(fileName, true);
		
		if (fis == null){
			System.err.println("Can't load file: '" + fileName + "'");
			new Exception();
		}
		
		//show rules set
		FunctionBlock fb = fis.getFunctionBlock(null);
		
		//set inputs
		fb.setVariable("energy", energy);
		fb.setVariable("path_length", numHops);		
		
		fb.evaluate();
		
		fsl = fb.getVariable("psl").defuzzify();
		
		return fsl;
	}
	
	public static Logging getGeneralLog(){
		String logFile = "";
		try {
			logFile = Configuration.getStringParameter("LogFiles/General");
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("General log file not found");
			e.printStackTrace();
		}
		return Logging.getLogger(logFile);
	}
}
