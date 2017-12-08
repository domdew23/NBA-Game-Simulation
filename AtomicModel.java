/* Component */
import java.math.BigDecimal;

public interface AtomicModel<I,O>{

	public Token lambda();

	/* internal state transition function */
	public void deltaInternal();

	/* external state transition function */
	public void deltaExternal(Token token);

	public void deltaConfluent(Token token);
	
	public BigDecimal timeAdvance();

	public void addInput(I I);

	public void addOutput(O O);

}