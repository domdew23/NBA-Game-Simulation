import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

public class Player<I, O> implements AtomicModel<I, O> {
	public static Time currentTime;
	public static Scheduler<AtomicModel> scheduler;

	private double passPercentage;
	private double twoPointPercentage;
	private double twoPointAccuracy;
	private double threePointPercentage;
	private double threePointAccuracy;
	private double turnOverPercentage;

	private int points;
	private int turnovers;
	private int feildGoalsMade;
	private int feildGoalsAttempted;
	private double feildGoalPercentage;
	private int[] eventPicker;

	private ArrayList<I> inputs;
	private ArrayList<O> outputs;
	private MathContext roundingSettings;
	private String team;
	private String name;
	private String position;
	private Token myBall;

	public Player(String team, String name, String position, double passPercentage, double twoPointPercentage, 
		double twoPointAccuracy, double threePointPercentage, double threePointAccuracy, double turnOverPercentage){
		this.team = team;
		this.name = name;
		this.position = position;

		this.passPercentage = passPercentage;
		this.twoPointPercentage = twoPointPercentage;
		this.twoPointAccuracy = twoPointAccuracy;
		this.threePointPercentage = threePointPercentage;
		this.threePointAccuracy = threePointAccuracy;
		this.turnOverPercentage = turnOverPercentage;

		this.points = 0;
		this.turnovers = 0;
		this.feildGoalsMade = 0;
		this.feildGoalsAttempted = 0;
		this.feildGoalPercentage = 0.0;
		this.eventPicker = new int[100];
		this.inputs = new ArrayList<I>();
		this.outputs = new ArrayList<O>();
		this.roundingSettings = new MathContext(3);	
		this.myBall = null;
		initEventPicker();
	}

	private void initEventPicker(){
		int size = 0;
		long turnover = Math.round(100*turnOverPercentage);
		long three = Math.round(100*threePointPercentage);
		long two = Math.round(100*twoPointPercentage);
		long pass = Math.round(100*passPercentage);

		for (int i = 0; i < turnover; i++){
			eventPicker[i] = 0;
			size++;
		}

		for (int i = size; i < three + turnover; i++){
			eventPicker[i] = 3;
			size++;
		}

		for (int i = size; i <  two + three + turnover; i++){
			eventPicker[i] = 2;
			size++;
		}

		for (int i = size; i <  eventPicker.length; i++){
			eventPicker[i] = 1;
			size++;
		}
	}

	public Token lambda(){
		return null;
	}

	public void deltaExternal(Token ball){
		myBall = ball;
		myBall.currentPossesion = team;
		Time newTime = new Time(currentTime.getReal().add(timeAdvance()), 0);
		Event event = Event.builder(newTime, "deltaInternal", this, ball).build();
		scheduler.put(event);
	}

	public void deltaInternal(){
		// pass ball or shoot or turnover (done holding)
		int random = ThreadLocalRandom.current().nextInt(0, eventPicker.length+1);
		int decision = eventPicker[random];
		switch (decision){
			case 0:
				turnovers++;
				myBall.madeShot = false;
				myBall.turnover = true;
				System.out.println("turnover");
				break;
			case 1:
				//pass
				System.out.println("pass");
				break;
			case 2:
				if (Math.random() < twoPointAccuracy){
					// make
					System.out.println("two pt make");
				} else {
					//miss
					System.out.println("two pt miss");	
				}
				break;
			case 3:
				if (Math.random() < threePointAccuracy){
					System.out.println("3 pt make");
				} else {
					System.out.println("3 pt miss");
				}
				break;
			default:
				//error
		}
	}

	public void deltaConfluent(Token ball){

	}

	public BigDecimal timeAdvance(){
		if (ThreadLocalRandom.current().nextDouble(0, 1.0) < .95){
			BigDecimal ret = new BigDecimal(ThreadLocalRandom.current().nextDouble(1.0, 8.0), roundingSettings);
			return ret;
		} else {
			BigDecimal ret = new BigDecimal(ThreadLocalRandom.current().nextDouble(1.0, 16.0), roundingSettings);
			return ret;
		}
	}

	public void addInput(I in){
		inputs.add(in);
	}

	public void addOutput(O o){
		outputs.add(o);
	}

	public String toString(){
		return name + " - " + position + " (" + team + ")";
	}
}