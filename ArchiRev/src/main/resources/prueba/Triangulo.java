
/***
 * Triangulo().setI.setJ.setK.getTipo()
 * Nos sobra el resto de mutantes
 */




import java.io.Serializable;
  
public class Triangulo implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int i, j, k;
    public int tipo;
    public static final int ESCALENO=1, ISOSCELES=2, EQUILATERO=3, NO_TRIANGULO=0;
    
    public Triangulo()
    {
    }
    
    public void setI(int v) throws IllegalArgumentException {
        if (v<=0) throw new IllegalArgumentException();
        i=v;  
    }
    
    public void setJ(int v) throws IllegalArgumentException {
        if (v<=0) throw new IllegalArgumentException();
        j=v; 
    }
    
    public void setK(int v) throws IllegalArgumentException {
        if (v<=0) throw new IllegalArgumentException();
        k=v; 
    }
        
    /**
     * 
     * @return 1 if scalene; 2 if isosceles; 3 if equilateral; 0 if not a triangle
     */
    public int getTipo() {
        if (i==j) { tipo=tipo+1; }
        if (i==k) { tipo=tipo+2; }
        if (j==k) { tipo=tipo+3; }
        
        if (i<=0 || j<=0 || k<=0) {
            tipo=Triangulo.NO_TRIANGULO;
            return tipo;
        } 
        if (tipo==0) 
        {
            if (i+j<=k || j+k<=i || i+k<=j) {
                tipo=Triangulo.NO_TRIANGULO;
                return tipo;
            } else {
                tipo=Triangulo.ESCALENO;
                return tipo;
            }
        }
        if (tipo>3) {
            tipo=Triangulo.EQUILATERO;
            return tipo;
        } else if (tipo==1 && i+j>k) 
        {
            tipo=Triangulo.ISOSCELES;
            return tipo;
        } else if (tipo==2 && i+k>j) 
        {
            tipo=Triangulo.ISOSCELES;
            return tipo;
        } else if (tipo==3 && j+k>i) 
        {
            tipo=Triangulo.ISOSCELES;
            return tipo;
        } else {
            tipo=Triangulo.NO_TRIANGULO;
            return tipo;
        }
    }
    
}
