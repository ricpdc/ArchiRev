package flip;
public interface iFlip {
	 public int flip(int i);
}
public class foo implements iFlip {
	 public foo(){}
	 public flip(int i) {
		 return i * -1;
	 }
}
 
public class FlipClient {
		 public static void main(String[] args) {
		 foo f= new foo();
		 iFlip g=(iFlip) f;
		 f.flip(100);
	 }
}

