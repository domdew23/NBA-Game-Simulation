import java.math.BigDecimal;
import java.util.ArrayList;

public class Hoop<I, O> implements AtomicModel<I, O>{
	public static Time currentTime;
	public static Scheduler<AtomicModel> scheduler;

	private ArrayList<I> inputs;
	private ArrayList<O> outputs;

	private Token myBall;
	private String team;

	public Hoop(String team){
		this.team = team;
		this.inputs = new ArrayList<I>();
		this.outputs = new ArrayList<O>();
		this.myBall = null;
	}

	public Token lambda(){
		return myBall;
	}

	public void deltaInternal(){
		for (O output : outputs){
			if (Network.class.isInstance(output)){
				Network<AtomicModel> net = (Network) output;
				myBall.lastTeamWithBall = net;
				for (AtomicModel model : net.getOutputs()){
					Event<AtomicModel> event = Event.builder(currentTime, "deltaExternal", model, myBall).build();
					scheduler.put(event);
				}
			}
		}
	}

	public void deltaExternal(Token ball){
		myBall = ball;
		Time newTime = new Time(currentTime.getReal().add(timeAdvance()), 0);
		Event event = Event.builder(newTime, "deltaInternal", this, lambda()).build();
		scheduler.put(event);
	}

	public void deltaConfluent(Token ball){
		deltaExternal(ball);
		deltaInternal();
	}
	
	public BigDecimal timeAdvance(){
		return new BigDecimal(0);
	}

	public void addInput(I I){
		inputs.add(I);
	}

	public void addOutput(O O){
		outputs.add(O);
	}

	public String toString(){
		return team;
	}
}