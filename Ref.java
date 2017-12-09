import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Ref<I, O> implements AtomicModel<I, O> {
	
	public static Time currentTime;
	public static Scheduler<AtomicModel> scheduler;
	private ArrayList<I> inputs;
	private ArrayList<O> outputs;
	private boolean firstPossesion;
	private Token myBall;

	public Ref(){
		inputs = new ArrayList<I>();
		outputs = new ArrayList<O>();
		firstPossesion = false;
		myBall = null;
	}

	public Token lambda(){
		return myBall;
	}

	public void deltaExternal(Token ball){
		if (ball.firstPossesion){
			firstPossesion = true;
		}
		myBall = ball;
		Time newTime = new Time(currentTime.getReal().add(timeAdvance()), 0);
		Event event = Event.builder(newTime, "deltaInternal", this, ball).build();
		scheduler.put(event);
	}

	public void deltaInternal(){
		if (firstPossesion){
			int teamIndex = ThreadLocalRandom.current().nextInt(0,2);
			Network<AtomicModel> team = (Network) outputs.get(teamIndex);
			for (AtomicModel player : team.getInputs()){
				Event<AtomicModel> event = Event.builder(currentTime, "deltaExternal", player, lambda()).build();
				scheduler.put(event);
			}
			myBall.firstPossesion = false;
			firstPossesion = false;
		} else {
			int teamIndex = 0;
			//System.out.println("Last team with ball: " + myBall.lastTeamWithBall);
			if (myBall.lastTeamWithBall == (Network) outputs.get(0)){
				teamIndex = 1;
			} else if (myBall.lastTeamWithBall == (Network) outputs.get(1)) {
				teamIndex = 0;
			}
	
			Network<AtomicModel> team = (Network) outputs.get(teamIndex);
			for (AtomicModel player : team.getInputs()){
				Event<AtomicModel> event = Event.builder(currentTime, "deltaExternal", player, lambda()).build();
				scheduler.put(event);
			}
		}
	}

	public void deltaConfluent(Token ball){
		deltaExternal(ball);
		deltaInternal();
	}

	public BigDecimal timeAdvance(){
		return new BigDecimal(1.0);
	}

	public void addInput(I in){
		inputs.add(in);
	}

	public void addOutput(O o){
		outputs.add(o);
	}

	public String toString(){
		return "Ref";
	}
}