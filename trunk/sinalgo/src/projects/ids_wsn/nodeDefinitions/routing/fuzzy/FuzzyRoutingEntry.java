package projects.ids_wsn.nodeDefinitions.routing.fuzzy;

import java.util.ArrayList;
import java.util.List;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import sinalgo.nodes.Node;

public class FuzzyRoutingEntry {
	private List<RoutingField> fields = new ArrayList<RoutingField>();
	
	public FuzzyRoutingEntry (Integer seq, Integer numHops, BasicNode nextHop, Boolean active, Double fsl){
		addField(seq, numHops, nextHop, active, fsl);
				
	}
	
	public void addField(Integer seq, Integer numHops, BasicNode nextHop, Boolean active, Double fsl){
		RoutingField r = new RoutingField(seq, numHops, nextHop, active, fsl);
		fields.add(r);		
	}
	
	public Node getFirstActiveRoute(){
		Node node = null;
		for (RoutingField field : fields){
			if (field.getActive()){
				node = field.getNextHop();
			}
		}
		return node;
	}
	
	public Integer getFieldsSize(){
		return fields.size();
	}
	
	public Node getBestRoute(Node node){
		return null;
	}
	
	public Boolean containsNodeInNextHop(Node node){
		Boolean result = false;
		
		for (RoutingField field : fields){
			if (field.getNextHop().equals(node)){
				result = true;
				break;
			}
		}
		return result;
	}
	
	public RoutingField getRoutingField(Node node){
		RoutingField rf = null;
		for (RoutingField field : fields){
			if (field.getNextHop().equals(node)){
				rf = field;
			}
		}
		return rf;
	}
}
