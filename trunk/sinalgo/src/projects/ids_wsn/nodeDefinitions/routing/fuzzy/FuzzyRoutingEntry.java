package projects.ids_wsn.nodeDefinitions.routing.fuzzy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import projects.ids_wsn.comparators.FslComparator;
import projects.ids_wsn.enumerators.Order;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import sinalgo.nodes.Node;

public class FuzzyRoutingEntry {
	private List<RoutingField> fields = new ArrayList<RoutingField>();
	
	public FuzzyRoutingEntry (Integer seq, Integer numHops, Node nextHop, Boolean active, Double fsl){
		addField(seq, numHops, nextHop, active, fsl);
				
	}
	
	public void addField(Integer seq, Integer numHops, Node nextHop, Boolean active, Double fsl){
		RoutingField r = new RoutingField(seq, numHops, nextHop, active, fsl);
		fields.add(r);		
	}
	
	/**
	 * Get the route with the first route with the highest fsl
	 * Routes with lower fsl are best
	 * 
	 * @return
	 */
	public Node getFirstActiveRoute(){		
		
		Node node = null;
		Collections.sort(fields, new FslComparator(Order.DESC));
		
		RoutingField rf = fields.get(0);
		
		node = rf.getNextHop();
		
		return node;
	}
	
	public Integer getFieldsSize(){
		return fields.size();
	}
	
	public Node getBestRoute(Node node){
		return getFirstActiveRoute();
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
	
	/**
	 * This method search the route with the lowest fsl and exchange by a new one
	 * Routes with high fsl are better
	 * 
	 * @param seq
	 * @param numHops
	 * @param nextHop
	 * @param active
	 * @param fsl
	 */
	public Boolean exchangeRoute(Integer seq, Integer numHops, BasicNode nextHop, Boolean active, Double fsl){
		
		Boolean result = Boolean.FALSE;
		
		RoutingField r = new RoutingField(seq, numHops, nextHop, active, fsl);
		
		//
		Collections.sort(fields, new FslComparator(Order.ASC));
		
		RoutingField rOld = fields.get(0);
		
		if (r.getFsl().compareTo(rOld.getFsl()) > 0){
			fields.remove(0);
			fields.add(r);
			result = Boolean.TRUE;
		}
		
		return result;
		
	}
	
	public Double getLowestFsl(){
		Collections.sort(fields, new FslComparator(Order.ASC));
		RoutingField rf = fields.get(0);
		
		return rf.getFsl();
	}
	
	public Double getHighestFsl(){
		Collections.sort(fields, new FslComparator(Order.DESC));
		RoutingField rf = fields.get(0);
		
		return rf.getFsl();
	}
}
