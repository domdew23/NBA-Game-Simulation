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
	private int[] eventMap;

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
		this.eventMap = new int[100];
		this.inputs = new ArrayList<I>();
		this.outputs = new ArrayList<O>();
		this.roundingSettings = new MathContext(3);	
		this.myBall = null;
		initEventMap();
	}

	private void initEventMap(){
		int size = 0;
		long turnover = Math.round(100*turnOverPercentage);
		long three = Math.round(100*threePointPercentage);
		long two = Math.round(100*twoPointPercentage);
		long pass = Math.round(100*passPercentage);

		for (int i = 0; i < turnover; i++){
			eventMap[i] = 0;
			size++;
		}

		for (int i = size; i < three + turnover; i++){
			eventMap[i] = 3;
			size++;
		}

		for (int i = size; i <  two + three + turnover; i++){
			eventMap[i] = 2;
			size++;
		}

		for (int i = size; i <  eventMap.length; i++){
			eventMap[i] = 1;
			size++;
		}
	}

	public Token lambda(){
		return myBall;
	}

	public void deltaExternal(Token ball){
		scheduler.remove(scheduler.find("deltaInternal", this));
		myBall = ball;
		Time newTime = new Time(currentTime.getReal().add(timeAdvance()), 0);
		
		if (currentTime.getReal().remainder(new BigDecimal(24.0)).compareTo(new BigDecimal(21.0)) == 1){
			myBall.lowShotClock = true;
			newTime = currentTime;
		} else {
			myBall.lowShotClock = false;
		}

		Event event = Event.builder(newTime, "deltaInternal", this, myBall).build();
		scheduler.put(event);
	}

	public void deltaInternal(){
		Event<AtomicModel> event = null;
		int random = ThreadLocalRandom.current().nextInt(0, eventMap.length);
		int decision = eventMap[random];

		if (myBall.lowShotClock) decision = ThreadLocalRandom.current().nextInt(2, 4);
		if (myBall.steal) decision = 0; myBall.steal = false; scheduler.remove(scheduler.find("deltaExternal", this));

		switch (decision){
			case 0:
				turnovers++;
				myBall.madeShot = false;
				myBall.turnover = true;
				for (O output : outputs){
					if (Hoop.class.isInstance(output)){
						event = Event.builder(currentTime, "deltaExternal", output, lambda()).build();
						scheduler.put(event);
						break;
					}
				}
				break;
			case 1:
				//pass
				int index = ThreadLocalRandom.current().nextInt(0, outputs.size());
				AtomicModel reciever = (AtomicModel) outputs.get(index);
				while (Hoop.class.isInstance(reciever)){
					index = ThreadLocalRandom.current().nextInt(0, outputs.size());
					reciever = (AtomicModel) outputs.get(index);
				}
				event = Event.builder(currentTime, "deltaExternal", reciever, lambda()).build();
				scheduler.put(event);
				break;
			case 2:
				if (Math.random() < twoPointAccuracy){
					myBall.madeShot = true;
					points += 2;
					myBall.pointValue = 2;
					feildGoalsAttempted++;
					feildGoalsMade++;
					if (team.equals("Knicks")) myBall.knicksTotal += 2;
					if (team.equals("Hawks")) myBall.hawksTotal += 2;
					for (O output : outputs){
						if (Hoop.class.isInstance(output)){
							event = Event.builder(currentTime, "deltaExternal", output, lambda()).build();
							scheduler.put(event);
							break;
						}
					}
				} else {
					myBall.madeShot = false;
					feildGoalsAttempted++;
					for (O output : outputs){
						if (Hoop.class.isInstance(output)){
							event = Event.builder(currentTime, "deltaExternal", output, lambda()).build();
							scheduler.put(event);
							break;
						}
					}
				}
				break;
			case 3:
				if (Math.random() < threePointAccuracy){
					myBall.madeShot = true;
					myBall.pointValue = 3;
					points += 3;
					feildGoalsAttempted++;
					feildGoalsMade++;
					if (team.equals("Knicks")) myBall.knicksTotal += 3;
					if (team.equals("Hawks")) myBall.hawksTotal += 3;
					for (O output : outputs){
						if (Hoop.class.isInstance(output)){
							event = Event.builder(currentTime, "deltaExternal", output, lambda()).build();
							scheduler.put(event);
							break;
						}
					}
				} else {
					myBall.madeShot = false;
					feildGoalsAttempted++;
					for (O output : outputs){
						if (Hoop.class.isInstance(output)){
							event = Event.builder(currentTime, "deltaExternal", output, lambda()).build();
							scheduler.put(event);
							break;
						}
					}
				}
				break;
			default: break;
				//error
		}
		if (feildGoalsAttempted > 0) feildGoalPercentage = ((double) feildGoalsMade/(double) feildGoalsAttempted);
	}

	public void deltaConfluent(Token ball){
		Event<AtomicModel> event = null;
		if (Math.random() < twoPointAccuracy){
			myBall.madeShot = true;
			myBall.pointValue = 1;
			points += 1;
			if (team.equals("Knicks")) myBall.knicksTotal += 1;
			if (team.equals("Hawks")) myBall.hawksTotal += 1;
			for (O output : outputs){
				if (Hoop.class.isInstance(output)){
					event = Event.builder(currentTime, "deltaExternal", output, lambda()).build();
					scheduler.put(event);
					break;
				}
			}
		} else {
			myBall.madeShot = false;
			for (O output : outputs){
				if (Hoop.class.isInstance(output)){
					event = Event.builder(currentTime, "deltaExternal", output, lambda()).build();
					scheduler.put(event);
					break;
				}
			}
		}
	}

	public BigDecimal timeAdvance(){
		BigDecimal ret = null;
		if (ThreadLocalRandom.current().nextDouble(0, 1.0) < .95){
			ret = new BigDecimal(ThreadLocalRandom.current().nextDouble(1.0, 8.0), roundingSettings);
		} else {
			ret = new BigDecimal(ThreadLocalRandom.current().nextDouble(1.0, 16.0), roundingSettings);
		}
		return ret;
	}

	public void addInput(I in){
		inputs.add(in);
	}

	public void addOutput(O o){
		outputs.add(o);
	}

	public String toString(){
		BigDecimal fg = new BigDecimal(feildGoalPercentage*100, roundingSettings);
		return name + " - " + position + " (" + team + ") " + points + " " + fg + "% " + turnovers + " TO";
	}
}