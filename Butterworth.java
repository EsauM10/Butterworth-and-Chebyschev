import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class Butterworth implements Filter{
	
	Pole poles[];
	float E;  //Maxima variacao da banda passante
	float Aws;//Atenuacao
	float Wp; //Frequencia de corte
	float Ws; //Frequencia de bloqueio
	int gain;//Ganho DC do filtro
	
	public Butterworth(Pole poles[], float E, float Aws, float Wp, float Ws, int gain) {
		this.poles = poles;
		this.E     = E;
		this.Aws   = Aws;
		this.Wp    = Wp;
		this.Ws    = Ws;
		this.gain  = gain;
	}
	
	
	public static Butterworth butter(float fp, float fs, float Amax, float Amin, int gain) {
		int N = 0;
		float ratio = fs/fp;
		float E = (float) Math.sqrt(Math.pow(10, (Amax/10)) - 1);
		float Aws = 1;
		float Wp = (float) (2*Math.PI*fp);
		float Ws = (float) (2*Math.PI*fs);
		
		//Obtem a ordem do filtro
		for(int i=1; Aws < Amin; i++) {
			Aws = (float) (10 * Math.log10(1+(E*E)*Math.pow(ratio, 2*i)));
			N = i;
		}
		
		//Obtem os polos
		Pole poles[] = new Pole[N];
		int theta = 90 - 180/(2*N);
		
		for(int i=0; i<N; i++, theta-=180/N) {
			int modTheta= (int) Math.abs(theta);
			poles[i] = new Pole(modTheta, modTheta);
			//System.out.printf("Wo(-cos(%d)%sjsin(%d)\n", modTheta, (theta>0)? "+":"-", modTheta);
		}
		return new Butterworth(poles, E, Aws, Wp, Ws, gain);
	}
	
	
	public int order() {return poles.length;}

	@Override
	public float Wp() {return Wp;}
	
	/**
	 * Obtem a funcao de magnitude do filtro T|jw|
	 */
	public float Tj(float w) {
		int N = order();
		return (float) (1f/(Math.sqrt(1 + E*E * Math.pow(w/Wp, 2*N))));
	}
	
	public float W0() { return (float) (Wp*Math.pow((1/E), 1/order()));}
	
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
		
		int N = (order() < 200) ? order():200;	
		float theta = -180f/(2*N);
		for(int i=0; i<N; i++, theta=-180f/N) {
			g2.rotate(Math.toRadians(theta));
			g2.drawLine(0, 0, 0, -(width/2+25));
			g2.drawString("X", -3, -(width/2+26));
		}
		g2.setTransform(old);
	}
	
	/**
	 * Obtem a funcao de transferencia do filtro
	 * @return Uma String com a funcao de transferencia
	 */
	public String Ts() {
		int N = order();
		String function = "";
		String bar = "";
		
		//Funcao de Transferencia
		System.out.printf("\nT(s) = %d*Wo^%d\n", gain, N);
		if(N % 2 != 0) {
			bar += "_____";
			function += "(S+Wo)";
		}
		
		for(int i=0; i<N/2; i++) {
			float angle = (float) (2*Math.cos(Math.toRadians(poles[i].real)));
			function += String.format("(S²+%.3fWoS+Wo²)", (float) Math.abs(angle));
			bar = bar + "_________________";
		}
		System.out.println(bar);
	
		return function;
	}
	
}
