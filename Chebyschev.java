import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class Chebyschev implements Filter{
	
	Pole poles[];
	float E;
	float Aws;
	float Wp;
	float Ws;
	int gain;
	
	public Chebyschev(Pole poles[], float E, float Aws, float Wp, float Ws, int gain) {
		this.poles = poles;
		this.E = E;
		this.Aws = Aws;
		this.Wp = Wp;
		this.Ws = Ws;
		this.gain = gain;
	}
	
	
	public static Chebyschev cheby(float fp, float fs, float Amax, float Amin, int gain) {
		int N = 0;
		float ratio = fs/fp;
		float E = (float) Math.sqrt(Math.pow(10, (Amax/10)) - 1);
		float Aws = 0;
		float Wp = (float) (2*Math.PI*fp);
		float Ws = (float) (2*Math.PI*fs);
		float acosh = (float) arccosh(ratio);
		
		//Obtem a ordem do filtro
		for(int i=1; Aws < Amin; i++) {
			float cosh = (float) Math.cosh(i*acosh);
			Aws = (float) (10*Math.log10(1+(E*E)*(cosh*cosh)));
			N = i;
		}
		float arg2 = (float) (arcsinh(1/E)/N);
		float cosh = (float) Math.cosh(arg2);
		float senh = (float) Math.sinh(arg2);
		Pole poles[] = new Pole[N];
		
		//Obtem os polos
		for(int k=1; k<=N; k++) {
			float arg1 = (float) ((2*k-1)*Math.PI)/(2*N);
			float real = (float) Math.sin(arg1) * senh;
			float img  = (float) Math.cos(arg1) * cosh;
			poles[k-1] = new Pole(real, img);
		}
		return new Chebyschev(poles, E, Aws, Wp, Ws, gain);
	}
	

	public int order() {return poles.length;}
	
	public float Wp() {return Wp;}
	
	/**
	 * Obtem a funcao de magnitude do filtro T|jw|
	 */
	public float Tj(float w) {
		int N = order();
		if(w <= Wp) {
			float cos = (float) Math.cos(N*Math.acos(w/Wp));
			return (float) (1f/Math.sqrt(1 + E*E * cos*cos));
		}
		float cosh = (float) Math.cosh((N*arccosh(w/Wp)));
		return (float) (1f/Math.sqrt(1 + E*E * cosh*cosh));
	}
	
	public void drawParameters(Graphics2D g2, int x, int y) {
		g2.drawString("E = "     + E, x, y);
		g2.drawString("A(ws) = " + Aws + " dB", x, y+20);
		g2.drawString("Wp = " 	 + Wp + " rad/s", x, y+40);
		g2.drawString("Ws = " 	 + Ws + " rad/s", x, y+60);
		g2.drawString("Ordem = " + order(), x, y+80);
	}
	
	public void drawPoles(Graphics2D g2, int width, int height) {
		AffineTransform old = g2.getTransform();
		g2.setStroke(new BasicStroke(2.5f));
		g2.translate(width, height);
		
		int N = order();
		int factor = (int) (height*0.5f);
		//int y=20;
		for(int k=0; k<N; k++) {
			float real = poles[k].real;
			float img  = poles[k].img;
			g2.draw(new Line2D.Float(0, 0, -real*factor*2, -img*factor));
			g2.fill(new Ellipse2D.Float(-real*factor*2-3, -img*factor-3, 6, 6));
			
			/*String pole = String.format("P%d = Wp(%.4f%s%.4fj)", k+1, -real, (img>0)? "+":"",img);
			g2.drawString(pole, 10, y);
			y+=15;*/
		}
		g2.setTransform(old);
	}
	
	/*
	public void Ts() {
		int N = poles.length;
		//Funcao de Transferencia
		float coef = (float) (E*Math.pow(2, N-1));
		System.out.printf("\n\nT(s) = %d*Wp^%d\n", gain, N);
		System.out.println("___________________________________________");
		System.out.printf("%.3f", coef);
		if (N % 2 != 0) {
			float realPole = poles[N/2].real;
			System.out.printf("(S+%.4fWp)", realPole);
		}
		for(int i=0; i<N/2; i++) {
			float real = poles[i].real;
			float img  = poles[i].img;
			System.out.printf("(S�+%.4fWpS+%.4fWp�)", 2*real, (real*real + img*img));
		}
	}*/
	
	
	
	
	private static double arccosh(double x) {
		return Math.log(x + Math.sqrt(x*x - 1.0));
	}
	
	private static double arcsinh(double x) {
		if(Math.abs(x) > 1.0e10) 
			return (x > 0) ? 0.69314718055995+Math.log(Math.abs(x)):
							-0.69314718055995+Math.log(Math.abs(x));
		
		double lnOnePlusX = Math.log(1.0 + Math.abs(x) + x*x / 
									(1.0 + Math.sqrt(1.0 + x*x)));
		return (x == 0.0) ? 0.0 : ((x > 0.0) ? lnOnePlusX : -lnOnePlusX);
	}
	
}
