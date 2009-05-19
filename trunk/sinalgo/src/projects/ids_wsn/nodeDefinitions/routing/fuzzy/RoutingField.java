package projects.ids_wsn.nodeDefinitions.routing.fuzzy;

import projects.ids_wsn.nodeDefinitions.BasicNode;

public class RoutingField {
	private Integer sequenceNumber;
	private Integer numHops;
	private BasicNode nextHop;
	private Boolean active;
	private Double fsl;
	
	public RoutingField(Integer seq, Integer numHops, BasicNode nextHop, Boolean active, Double fsl){
		this.sequenceNumber = seq;
		this.numHops = numHops;
		this.nextHop = nextHop;
		this.active = active;
		this.fsl = fsl;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setNumHops(Integer numHops) {
		this.numHops = numHops;
	}

	public void setNextHop(BasicNode nextHop) {
		this.nextHop = nextHop;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setFsl(Double fsl) {
		this.fsl = fsl;
	}

	public Integer getNumHops() {
		return numHops;
	}

	public BasicNode getNextHop() {
		return nextHop;
	}

	public Boolean getActive() {
		return active;
	}

	public Double getFsl() {
		return fsl;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

}
