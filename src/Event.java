import java.math.BigDecimal;

public class Event<T> implements Comparable<Event<T>>{
	public Time time;
	public Token token;
	public String kind; // input or output
	public T obj;

	private Event(){
	}

	public static<T>EventBuilder builder(Time t, String k, T o, Token tok){
		return new EventBuilder<T>(t, k, o, tok);
	}

	public int compareTo(Event<T> other){
		return time.getReal().compareTo(other.time.getReal());
	}

	public String toString(){
		return "Time: " + time + " | Kind: " + kind + " | Object: " + obj;
	}

	public static class EventBuilder<T>{
		private Time time;
		private String kind;
		private T obj;
		private Token token;

		public EventBuilder(Time t, String k, T o, Token tok){
			time = t;
			kind = k;
			obj = o;
			token = tok;
		}

		public EventBuilder addParameter(int q){
			return this;
		}

		public Event build(){
			Event<T> event = new Event<T>();
			event.time = this.time;
			event.kind = this.kind;
			event.obj = this.obj;
			event.token = this.token;
			return event;
		}
	}
}