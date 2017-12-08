import java.util.concurrent.ThreadLocalRandom;
import java.math.BigDecimal;
public class main{
	public static void main(String[] args){
		XMLParser parser = new XMLParser("data.xml");

		Time currentTime = new Time(new BigDecimal(0.0), 0);
		Scheduler<AtomicModel> scheduler = new Scheduler();
		Ref.currentTime = currentTime;
		Ref.scheduler = scheduler;
		Player.currentTime = currentTime;
		Player.scheduler = scheduler;

		Token ball = new Token();
		ball.firstPossesion = true;

		AtomicModel<Network, Network> ref1 = new Ref();

		Network<AtomicModel> knicks = Network.builder().addComponents(parser.knicks).addInput(parser.knicks.get(0)).build();
		Network<AtomicModel> hawks = Network.builder().addComponents(parser.hawks).addInput(parser.hawks.get(0)).build();

		Network<AtomicModel> refs = Network.builder().addComponent(ref1).addInput(ref1).build();
		Network<Network> game = Network.builder().addComponent(refs).addComponent(knicks).addComponent(hawks).addInput(refs).build();
		
		ref1.addInput(game);
		ref1.addInput(knicks);
		ref1.addInput(hawks);
		
		ref1.addOutput(knicks);
		ref1.addOutput(hawks);

		for (Network<AtomicModel> network : game.getInputs()){
			for (AtomicModel model : network.getInputs()){
				Event<AtomicModel> event = Event.builder(currentTime, "deltaExternal", model, ball).build();
				scheduler.put(event);
			}
		}

		System.out.println(scheduler);

		while (!(scheduler.isEmpty())){
			System.out.println("Global time: " + currentTime.getReal());
			Event<AtomicModel> event = scheduler.pull();
			BigDecimal e = event.time.getReal().subtract(currentTime.getReal());

			Time interval = new Time(e, 1);
			currentTime = currentTime.advance(interval);
			Ref.currentTime = currentTime;
			Player.currentTime = currentTime;

			execute(event);
			System.out.println("------------------------\n" + scheduler);
		}
	}

	private static void couple(AtomicModel I, AtomicModel O){
		I.addOutput(O);
		O.addInput(I);
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
		System.out.println("Executing " + event);
	}
}