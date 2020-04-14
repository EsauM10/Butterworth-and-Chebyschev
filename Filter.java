import java.awt.Graphics2D;

public interface Filter {
	
	public int order();
	public float Wp();
	public float Tj(float W);
	public void drawParameters(Graphics2D g2, int x, int y);
	public void drawPoles(Graphics2D g2, int x, int y);
}
