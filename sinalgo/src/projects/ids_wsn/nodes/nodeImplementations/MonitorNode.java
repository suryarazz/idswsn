package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import projects.ids_wsn.enumerators.ChordMessageType;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.IntervalRule;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.RepetitionRule;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.RetransmissionRule;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodeDefinitions.dht.Chord;
import projects.ids_wsn.nodeDefinitions.dht.IDHT;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.FloodFindFuzzy;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * Node responsible for monitoring your neighbors through the promiscuous
 * listening mode. Stores informations of interest, processing them due to
 * specified rules. It can act like a <b>Supervisor Node</b>. In this case, it
 * becomes able to correlate evidences discovered by others monitor nodes. Each
 * supervisor node is responsible for correlating a specific sub-set of rules.
 * 
 * @author Marcus Vinícius Lemos<br/>
 * @changes Alex Lacerda Ramos
 */
public class MonitorNode extends BasicNode implements IMonitor {

	/**
	 * Stores the SHA-1 Hash ID of the node
	 */
	private Integer hashID;
	/**
	 * It stores the internal messages buffer size (<code>dataMessages</code>).
	 */
	public static Integer INTERNAL_BUFFER;

	/**
	 * Messages intercepted by the monitor node. When the messages buffer is
	 * full, the rules must be applied to the messages in order to find
	 * malicious nodes.
	 */
	private List<DataMessage> dataMessages;

	/**
	 * Map that contains a list of possible malicious nodes according to each
	 * rule. This is a local map, hence it contains only the nodes watched by
	 * this monitor node.
	 */
	private Map<Rules, List<Node>> mapLocalMaliciousNodes;

	private Integer hashLength = 1024;
	
	private Integer hashChain[] = new Integer[hashLength];
	
	private IDHT dht;
	
	static {
		try {
			INTERNAL_BUFFER = Configuration
					.getIntegerParameter("Monitor/Inference/InternalBuffer");
		} catch (CorruptConfigurationEntryException e) {
			Tools
					.appendToOutput("Key Monitor/Inference/InternalBuffer not found");
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		setMyColor(Color.RED);
		super.init();
		this.hashID = UtilsChord.generateSHA1(this.ID);
		this.dht = new Chord(this);
		mapLocalMaliciousNodes = new HashMap<Rules, List<Node>>();
		dataMessages = new ArrayList<DataMessage>();
		initHashChain();
	}

	@Override
	protected void preProcessingMessage(Message message) {
		if (message instanceof PayloadMsg) {
			PayloadMsg msg = (PayloadMsg) message;
			addMessageToList(msg);
		}
	}
	
	@Override
	protected void postProcessingMessage(Message message) {
		if(message instanceof FloodFindDsdv || message instanceof FloodFindFuzzy){
			this.sendMessageToBaseStation(ChordMessageType.ANSWER_MONITOR_ID.getValue());
		}
	}
	
	private void addMessageToList(PayloadMsg msg) {
		DataMessage dataMessage = new DataMessage();
		dataMessage.setClock((int) Tools.getGlobalTime());
		dataMessage.setData(msg.value);
		dataMessage.setFinalDst(msg.baseStation.ID);
		dataMessage.setIdMsg(msg.sequenceNumber);
		dataMessage.setImediateDst(msg.nextHop.ID);
		dataMessage.setImediateSrc(msg.imediateSender.ID);
		dataMessage.setSource(msg.sender.ID);

		dataMessages.add(dataMessage);

		if (dataMessages.size() == INTERNAL_BUFFER) {
			applyRules();
			dataMessages.clear();
		}
	}

	@Override
	public Boolean beforeSendingMessage(Message message) {
		if (message instanceof PayloadMsg) {
			PayloadMsg msg = (PayloadMsg) message;
			addMessageToList(msg);
		}
		return Boolean.TRUE;
	}
	
	

	private void applyRules() {
		IMonitor rule1 = new RepetitionRule(this);
		IMonitor rule2 = new RetransmissionRule(rule1);
		IMonitor rule3 = new IntervalRule(rule2);
		rule3.doInference();

		this.sendMaliciousNodesToSupervisor();
	}

	@SuppressWarnings("unused")
	private void sendMaliciousNodesToSupervisor() {

		for (Rules rule : mapLocalMaliciousNodes.keySet()) {

			Integer hashKey = UtilsChord.generateSHA1(rule.name());
			MonitorNode sucessorNode = this.getDht().findSucessor(hashKey);

			// TODO enviar os maliciosos para o supervisor ou é melhor enviar as
			// mensagens??

			List<Node> maliciousNodes = mapLocalMaliciousNodes.get(rule);

			// TODO usar o protocolo de roteamento para enviar a lista de nós

			// TODO criar classe de assinatura para armazenar a lista de
			// maliciosos e o no que enviou essa lista, etc...
			// Substituir mapExternalMaliciousList por um mapa de assinaturas

//			sucessorNode.addExternalMaliciousList(rule, maliciousNodes);
		}
	}
	
	public void doInference() {
		
	}
	public void addLocalMaliciousList(Rules rule, List<Node> lista) {
		mapLocalMaliciousNodes.put(rule, lista);
	}
	
	@NodePopupMethod(menuText="Print Finger Table")
	public void printFingerTable(){
		Tools.appendToOutput("\nnode: " + this.ID + " ( "+ this.hashID +" )");
		
		List<FingerEntry> fingerTable = this.getDht().getFingerTable();
		for (FingerEntry fingerEntry : fingerTable) {
			Tools.appendToOutput("\n"+fingerEntry.getIndex()+" | "+fingerEntry.getStartHash()+" --> "+fingerEntry.getSucessorNode().getHashID());
		}
	}
	
	@NodePopupMethod(menuText="Print Ring Information")
	public void printRingInfomation(){
		Tools.appendToOutput("\nnode: " + this.ID + " ( "+ this.hashID +" )");
		
		Tools.appendToOutput("\nnext: "+this.getDht().getNextNodeInChordRing().ID+" (hash: " + this.getDht().getNextNodeInChordRing().getHashID() + ")");
		Tools.appendToOutput("\nprevious: "+this.getDht().getPreviousNodeInChordRing().ID+" (hash: " + this.getDht().getPreviousNodeInChordRing().getHashID() + ")");
		Tools.appendToOutput("\n-----------------");
	}

	/*----------------------------------------------------
	---------------- GETTTERS AND SETTERS ----------------
	----------------------------------------------------*/

	public List<DataMessage> getDataMessages() {
		return dataMessages;
	}

	public void setDataMessages(List<DataMessage> dataMessages) {
		this.dataMessages = dataMessages;
	}

	public Map<Rules, List<Node>> getMapLocalMaliciousNodes() {
		return mapLocalMaliciousNodes;
	}

	public void setMapLocalMaliciousNodes(
			Map<Rules, List<Node>> mapLocalMaliciousNodes) {
		this.mapLocalMaliciousNodes = mapLocalMaliciousNodes;
	}

	public Integer getMonitorID() {
		return this.ID;
	}
	
	private void initHashChain(){
		Random rnd = Tools.getRandomNumberGenerator();
		for (int i = 0; i<hashLength; i++){
			hashChain[i] = rnd.nextInt();
		}
	}

	@Override
	public List<DataMessage> getDataMessage() {
		return null;
	}

	@Override
	public void setLocalMaliciousList(Rules rule, List<Node> lista) {
		
	}
	
	public Integer getHashID() {
		return hashID;
	}
	
	public void setHashID(Integer hashID) {
		this.hashID = hashID;
	}
	
	public IDHT getDht() {
		return dht;
	}
	
	public void setDht(IDHT dht) {
		this.dht = dht;
	}
}
