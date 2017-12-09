public class Scheduler<T>{

	private int DEFAULT_INIT_SIZE = 50;
	private Event<T>[] events;
	private int size;

	public Scheduler(int initSize){
		events = new Event[initSize + 1];
		size = 0;
	}

	public Scheduler(){
		events = new Event[DEFAULT_INIT_SIZE + 1];
		size = 0;
	}

	public boolean put(Event<T> e){
		try {
			if (size == events.length - 1){
				reSize();
			}
			events[++size] = e;
			check(size);
		} catch (Exception ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private void reSize(){
		Event<T>[] old = events;
		events = new Event[events.length * 2];
		System.arraycopy(old, 1, events, 1, size);
	}

	private int greater(int i, int j){
		return events[i].compareTo(events[j]);
	}

	private void siftUp(int i, int j){
		Event<T> tmp = events[i];
		events[i] = events[j];
		events[j] = tmp;
	}

	private void siftDown(int k){
		/* check it has a child - if so sift to the bottom of the heap */
		while(2 * k <= size){
			int j = 2 * k;
			if (j < size && greater(j, j + 1) == 1){
				j++;
			} 

			if (!(greater(k, j) == 1)){
				break;
			}

			siftUp(k, j);
			k = j;
		}
	}

	private void check(int k){
		/* check if root or greater than parent - if so swap with parent*/
		while (k > 1 && greater(k/2, k) == 1){
			siftUp(k, k/2);
			k /= 2;
		}
	}

	public boolean contains(Event<T> e){
		if (isEmpty()){
			return false;
		}

		for (Event<T> tmp : events){
			if (tmp == e){
				return true;
			}
		}
		return false;
	}

	public Event peek(){
		/* return the root element */
		if (isEmpty()){
			throw new NullPointerException("Cannot peek an empty list.");
		}
		return events[1];
	}

	public Event<T> pull(){
		if (isEmpty()){
			throw new NullPointerException("Cannot poll an empty queue.");
		}

		/* swap root and bottom node - store root - sift root back to the bottom */
		checkMerge();
		siftUp(1, size);
		Event<T> e = events[size--];
		events[size + 1] = null;
		siftDown(1);
		return e;
	}

	public boolean remove(Event<T> e){
		if (isEmpty() || !contains(e)){
			return false;
		}

		for (int i = 1; i < size + 1; i++){
			if (events[i] == e){
				siftUp(i, size);
				Event<T> tmp = events[size--];
				events[size + 1] = null;
				siftDown(i);
				return true;
			}
		}
		return false;
	}

	public Event<T> find(String kind, T obj){
		for (Event<T> e : events){
			if (e != null && e.kind == kind && e.obj == obj){
				return e;
			}
		}
		return null;
	}

	public void clear(){
		for (Event<T> e : events){
			if (e != null) remove(e);
		}
	}

	public int size(){
		return size;
	}

	public boolean isEmpty(){
		return size == 0;
	}

	public Event[] getEvents(){
		return events;
	}

	private void checkMerge(){
		for (Event<T> e : events){
			if (e != null && e != peek()){
				if (peek().time.getReal().compareTo(e.time.getReal()) == 0 && peek().obj == e.obj){
					merge(peek(), e);
				}	
			}
		}
	}

	/* never compare floating point values for equality - don't use floats */
	private void merge(Event<T> one, Event<T> two){
		remove(one);
		remove(two);

		Token t = null;
		if (one.token != null){
			t = one.token;
		} else if (two.token != null){
			t = two.token;
		} else {
			throw new NullPointerException("Something went wrong while merging.");
		}
		System.out.println("deltaConfluent!"); try {Thread.sleep(20000);}catch(InterruptedException e){}
		Event<T> event = Event.builder(one.time, "deltaConfluent", one.obj, t).build();
		put(event);
	}

	public String toString(){
		String retVal = "";
		for (int i = 1; i < size + 1; i++){
			retVal += "Event " + i + ": " + events[i] + "\n";
		}
		return retVal;
	}
}