import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FilterPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	private final int width = 700;
	private final int height = 400;
	private Timer timer;
	
	private JSpinner spinFp, spinFs, spinAmax, spinAmin;
	private JRadioButton butter, cheby;
	
	private Filter filter;
	private float fp=1000, fs=1500, Amax=1, Amin=25;
	
	private Color primary   = new Color(46,140,232);
	private Color secondary = new Color(214,115,28);
	
	public FilterPanel() {
		super(new BorderLayout());
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(width, height));
		setDoubleBuffered(true);
		setFocusable(true); 
		
		setComponents();
		timer = new Timer(200, this);
		timer.start();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		
		if(butter.isSelected())
			filter = Butterworth.butter(fp, fs, Amax, Amin, 1);
		else if(cheby.isSelected())
			filter = Chebyschev.cheby(fp, fs, Amax, Amin, 1);
		
		g2.setColor(secondary);
		filter.drawParameters(g2, 10, 40);
		drawGraph1(g2, 0, 20, width/2, height);
		g2.drawLine(this.width/2, 0, this.width/2, this.height); //Eixo w
		drawGraph2(g2, width/2, 20, width/2, height);
	}
	
	private void drawGraph1(Graphics2D g2, int x, int y, int w, int h) {
		//Eixos
		int x1 = (int) (x + w*0.1f);
		int x2 = (int) (x + w*0.9f);
		int y1 = (int) (y + h*0.1f);
		int y2 = (int) (y + h*0.9f);
		g2.setColor(primary);
		g2.setStroke(new BasicStroke(3));
		g2.drawString("R", x2+4, h/2);
		g2.drawString("jw", w/2, y1-4);
		g2.drawLine(x1, h/2, x2, h/2);//Eixo sigma
		g2.drawLine(w/2, y1, w/2, y2); //Eixo jw
		g2.setColor(secondary);
		filter.drawPoles(g2 , w/2, h/2);
	}
	
	private void drawGraph2(Graphics2D g2, int x, int y, int width, int height) {
		AffineTransform old = g2.getTransform();
		int x1 = (int) (x + width*0.05f);
		int x2 = (int) (x + width*0.95f);
		int y1 = (int) (y + height*0.1f);
		//Eixos
		g2.setColor(primary);
		g2.setStroke(new BasicStroke(3));
		g2.drawString("W", x2, height/2-2);
		g2.drawString("T|jw|", x1, y1-4);
		g2.drawLine(x1, height/2, x2, height/2); //Eixo w	
		g2.drawLine(x1, y1, x1, height/2);//T|jw|
		g2.setColor(secondary);
		g2.translate(x1, height/2);
		g2.drawString("Wp", filter.Wp()/(fp/20), 14);
		g2.fill(new Ellipse2D.Float(filter.Wp()/(fp/20), -2, 5, 5));
		
		for(float w=0; w<2.5*filter.Wp(); ) {
			float x3 = w/(fp/20);
			float y3 = filter.Tj(w);
			g2.fill(new Ellipse2D.Float(x3, -y3*100, 2, 2));
			
			if((fs-fp) <= 200) w+=4;
			else w+=constrain(fp);
		}
		g2.setTransform(old);
	}
	
	private int constrain(float frequency) {
		return (int) (0.03125f * Math.pow(frequency, 0.90309f));
	}
	
	private void setComponents() {
		spinFp = new JSpinner(new SpinnerNumberModel(fp, 1, 100000, 50));
		spinFs = new JSpinner(new SpinnerNumberModel(fs, 1, 100001, 50));
		spinAmax = new JSpinner(new SpinnerNumberModel(Amax, 0.1f, 2.1, 0.1f));
		spinAmin = new JSpinner(new SpinnerNumberModel(Amin, 2, 100, 1));
		addChangeListeners();
		
		butter = new JRadioButton("Butter", true);
		cheby = new JRadioButton("Cheby");
		ButtonGroup group = new ButtonGroup();
		group.add(butter);
		group.add(cheby);
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(new JLabel("fp(Hz)"));
		toolBar.add(spinFp);
		toolBar.add(new JLabel("fs(Hz)"));
		toolBar.add(spinFs);
		toolBar.add(new JLabel("Amáx(dB)"));
		toolBar.add(spinAmax);
		toolBar.add(new JLabel("Amin(dB)"));
		toolBar.add(spinAmin);
		
		toolBar.addSeparator();
		toolBar.add(butter);
		toolBar.add(cheby);
		
		this.add(toolBar, BorderLayout.NORTH);
	}
	
	
	private void addChangeListeners() {
		spinFp.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) ((JSpinner) e.getSource()).getValue();
				if(value < fs)
					fp = (float) value;
			}
		});
		
		spinFs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) ((JSpinner) e.getSource()).getValue();
				if(value > fp)
					fs = (float) value;
			}
		});
		spinAmax.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) ((JSpinner) e.getSource()).getValue();
				Amax = (float) value;
			}
		});
		spinAmin.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				double value = (double) ((JSpinner) e.getSource()).getValue();
				Amin = (float) value;
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		repaint();
	}
}
