package projects.ids_wsn.nodeDefinitions.energy;

public interface IEnergy {
	public void spend(EnergyMode mode);
	
	public Float getTotalSpentEnergy();
}
