package projects.ids_wsn.enumerators;

public enum ChordMessageType {
	// a partir do numero 101
	ANSWER_MONITOR_ID(101),
	SEND_TO_SUPERVISOR(102),
	NOTIFY_NEIGHBORS(103),
	SUPERVISOR_DOWN(104);
	
	private Integer value;
	
	private ChordMessageType(Integer value) {
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}
}
