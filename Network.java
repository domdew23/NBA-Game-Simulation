import java.util.ArrayList;
import java.math.BigDecimal;

public class Network<T> {
	private ArrayList<T> components;
	private ArrayList<T> inputs;
	private ArrayList<T> outputs;
	private String name;

	private Network(){}

	public static NetworkBuilder builder(){
		return new NetworkBuilder();
	}

	public ArrayList<T> getComponents(){
		return components;
	}

	public ArrayList<T> getInputs(){
		return inputs;
	}

	public ArrayList<T> getOutputs(){
		return outputs;
	}

	public String toString(){
		return name;
	}

	public static class NetworkBuilder<T>{
		private ArrayList<T> components = new ArrayList<T>();
		private ArrayList<T> inputs = new ArrayList<T>();
		private ArrayList<T> outputs = new ArrayList<T>();
		private String name = "Network";

		public NetworkBuilder(){}

		public NetworkBuilder addComponent(T component){
			this.components.add(component);
			return this;
		}

		public NetworkBuilder addComponents(ArrayList<T> componentList){
			for (T comp : componentList){
				this.components.add(comp);
			}
			return this;
		}

		public NetworkBuilder addName(String name){
			this.name = name;
			return this;
		}

		public NetworkBuilder addInput(T I){
			this.inputs.add(I);
			return this;
		}

		public NetworkBuilder addOutput(T O){
			this.outputs.add(O);
			return this;
		}

		public Network build(){
			Network network = new Network();
			network.components = this.components;
			network.inputs = this.inputs;
			network.outputs = this.outputs;
			network.name = this.name;
			return network;
		}
	}
}