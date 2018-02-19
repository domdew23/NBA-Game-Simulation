import java.util.concurrent.ThreadLocalRandom;
import java.math.BigDecimal;
import java.math.MathContext;

public class main{
	private static final BigDecimal GAME_LENGTH = new BigDecimal(48 * 60);

	public static void main(String[] args){
		AtomicModel<AtomicModel, Network> knicksHoop = new Hoop<AtomicModel, Network>("Knicks Hoop");
		AtomicModel<AtomicModel, Network> hawksHoop = new Hoop<AtomicModel, Network>("Hawks Hoop");

		XMLParser parser = new XMLParser("data.xml", knicksHoop, hawksHoop);
		Token ball = new Token();

		Time currentTime = new Time(new BigDecimal(0.0), 0);
		Scheduler<AtomicModel> scheduler = new Scheduler();

		init(currentTime, scheduler, parser, knicksHoop, hawksHoop, ball);

		while (!(scheduler.isEmpty())){
			System.out.println("Global time: " + currentTime.getReal());
			Event<AtomicModel> event = scheduler.pull();
			BigDecimal e = event.time.getReal().subtract(currentTime.getReal());

			Time interval = new Time(e, 1);
			currentTime = currentTime.advance(interval);
			Ref.currentTime = currentTime;
			Player.currentTime = currentTime;
			Hoop.currentTime = currentTime;

			execute(event);
			System.out.println("Executing: " + event + "\n------------------------\n" + scheduler);
			System.out.println("current possesion: " + ball.currentPossesion);
			if (currentTime.getReal().compareTo(GAME_LENGTH) == 1) break;
		}
		System.out.println("\nKnicks " + ball.knicksTotal + " | Hawks " + ball.hawksTotal);
	}

	private static void init(Time currentTime, Scheduler<AtomicModel> scheduler, XMLParser parser, AtomicModel knicksHoop, 
		AtomicModel hawksHoop, Token ball){

		Ref.currentTime = currentTime;
		Player.currentTime = currentTime;
		Hoop.currentTime = currentTime;

		Ref.scheduler = scheduler;
		Player.scheduler = scheduler;
		Hoop.scheduler = scheduler;

		ball.firstPossesion = true;

		AtomicModel<Network, Network> ref = new Ref<Network, Network>();

		Network<AtomicModel> knicks = Network.builder().addComponents(parser.knicks).addInput(parser.knicks.get(0)).addOutput(ref).addName("Knicks").build();
		Network<AtomicModel> hawks = Network.builder().addComponents(parser.hawks).addInput(parser.hawks.get(0)).addOutput(ref).addName("Hawks").build();
		Network<AtomicModel> game = Network.builder().addComponent(knicks).addComponent(hawks).addComponent(ref).addInput(ref).addName("Game").build();
		
		knicksHoop.addOutput(knicks);
		hawksHoop.addOutput(hawks);

		ref.addInput(game);
		ref.addInput(knicks);
		ref.addInput(hawks);

		ref.addOutput(knicks);
		ref.addOutput(hawks);

		for (AtomicModel model : game.getInputs()){
			Event<AtomicModel> event = Event.builder(currentTime, "deltaExternal", model, ball).build();
			scheduler.put(event);
		}
	}

	private static void execute(Event<AtomicModel> event){
		switch (event.kind){
			case "lambda":
				event.obj.lambda();
				break;
			case "deltaExternal":
				event.obj.deltaExternal(event.token);
				break;
			case "deltaInternal":
				event.obj.lambda();
				event.obj.deltaInternal();
				break;
			case "deltaConfluent":
				event.obj.lambda();
				event.obj.deltaConfluent(event.token);
				break;
			default:
				System.out.println("Something went wrong");
				break;
		}
	}
}