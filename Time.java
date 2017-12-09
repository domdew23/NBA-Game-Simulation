import java.math.BigDecimal;

public class Time {
	private BigDecimal real;
	private int discrete;

	public Time(BigDecimal real, int discrete){
		this.real = real;
		this.discrete = discrete;
	}

	public Time advance(Time interval){
		if (!(interval.real.equals(0.0))){
			return new Time(real.add(interval.real), 0); // go to the right
		} else {
			return new Time(real, discrete + interval.discrete); // go up
		}
	}

	public BigDecimal getReal(){
		return real;
	}

	public int getDiscrete(){
		return discrete;
	}

	public String toString(){
		return real.toString();
	}
}
