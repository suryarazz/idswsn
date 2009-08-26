package projects.ids_wsn.enumerators;

public enum ChordMessageType {
	// a partir do numero 101
	ANSWER_MONITOR_ID(101);
	
	private Integer value;
	
	private ChordMessageType(Integer value) {
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}
}
