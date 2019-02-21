
public class Medio {

	public int getMedio (int a , int b, int c) {
		int value=0;
		if (a<=b && b<=c) return value=b;
		if (b<=a && a<=c) return value=a;
		if (b<=c && c<=a) return value=c;
		return value;
	}
	
	
	

}
