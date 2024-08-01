package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import core.BenchmarkCanny;
import core.TileWorker;

public class WindowUI extends JFrame
						implements ActionListener{

	private JPanel mainPanel;
	private JPanel imgPanel;
	private JPanel buttonPanel;
	
	private JLabel imgsrc, imgtgt;
	
	private JButton open;
	private JButton save;
	private JButton convert;
	private JButton settings;
	private JButton bench;
	
	private JFileChooser fc;
	private BufferedImage result;
	private BufferedImage source;
	private BufferedImage scaledimg;
	
	private static int WIDTHTMB = 480;
	private static int HEIGHTTMB = 320;
	
	private TileWorker canny;
	private float high;
	private float low;
	private int tilewidth;
	private int tileheight;
	
	public WindowUI(){
		setTitle("Detector de Bordes Canny");
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		result = null;
		source = null;
		setTilewidth(256);
		setTileheight(256);
		setLow(7.4f);
		setHigh(8.7f);
		
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(1080, 480));
		
		imgPanel = new JPanel(new FlowLayout());
		imgsrc = new JLabel(new ImageIcon("res/default.jpg"));
		imgsrc.setBorder(new EmptyBorder(5, 0, 5, 5));
		imgsrc.setText("Original");
		imgsrc.setHorizontalTextPosition(SwingConstants.CENTER);
		imgsrc.setVerticalTextPosition(SwingConstants.BOTTOM);
		
		imgtgt = new JLabel(new ImageIcon("res/default.jpg"));
		imgtgt.setBorder(new EmptyBorder(5, 5, 5, 0));
		imgtgt.setText("Canny");
		imgtgt.setHorizontalTextPosition(SwingConstants.CENTER);
		imgtgt.setVerticalTextPosition(SwingConstants.BOTTOM);
		
		imgPanel.add(imgsrc);
		imgPanel.add(imgtgt);
		
		mainPanel.add(imgPanel, BorderLayout.NORTH);
		
		buttonPanel = new JPanel(new FlowLayout());
		open = new JButton("Abrir");
		open.addActionListener(this);
		buttonPanel.add(open);
		
		convert = new JButton("Convertir");
		convert.addActionListener(this);
		buttonPanel.add(convert);
		
		save = new JButton("Guardar");
		save.addActionListener(this);
		buttonPanel.add(save);
		
		settings = new JButton("Ajustes");
		settings.addActionListener(this);
		buttonPanel.add(settings);
		
		bench = new JButton("Benchmark");
		bench.addActionListener(this);
		buttonPanel.add(bench);
		
		fc = new JFileChooser();
		
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
	
		add(mainPanel);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == open){
			int returnVal = fc.showOpenDialog(WindowUI.this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION){
				File file = fc.getSelectedFile();
				try {
					source = ImageIO.read(file);
				} catch (IOException e1) {e1.printStackTrace();	}

				scaledimg = ScaledImage(source, WIDTHTMB, HEIGHTTMB);
				
				imgsrc.setIcon(new ImageIcon(scaledimg));
			}
		}
		
		if (e.getSource() == convert){
				
				canny = new TileWorker(scaledimg,
						getTilewidth(), getTileheight(),
						getHigh(), getLow());
				
				canny.processcanny();			
				setResult(canny.getEdgeimage());			
				
				imgtgt.setIcon(new ImageIcon(getResult()));			
		}
		
		if (e.getSource() == save){
			
			canny = new TileWorker(source,
					getTilewidth(), getTileheight(),
					getHigh(), getLow());

			canny.processcanny();
			setResult(canny.getEdgeimage());	
			
			int ret = fc.showSaveDialog(null);
			if (ret == JFileChooser.APPROVE_OPTION){
				
				try {
					ImageIO.write(getResult(), "jpg", fc.getSelectedFile());
				} catch (IOException e1) {e1.printStackTrace();}
			}
		}
		
		if (e.getSource() == settings) setValues();

		if (source == null) return;
		
		if ((e.getSource() == bench) && (source != null)) {
    bench.setEnabled(false);
    BenchmarkDialog dialog = new BenchmarkDialog(this);
    
    int steps = 20;
    int repetitions = 5;
    
    BenchmarkCanny benchmarker = new BenchmarkCanny(source, getHigh(), getLow(), steps, repetitions);
    
    SwingWorker<List<BenchmarkCanny.BenchmarkResult>, Void> worker = new SwingWorker<List<BenchmarkCanny.BenchmarkResult>, Void>() {
        @Override
        protected List<BenchmarkCanny.BenchmarkResult> doInBackground() throws Exception {
            return benchmarker.runBenchmark(dialog);
        }

        @Override
        protected void done() {
            try {
                List<BenchmarkCanny.BenchmarkResult> results = get();
                dialog.addResult("\nResumen de mejores tiempos:");
                for (core.BenchmarkCanny.BenchmarkResult result : results) {
                    dialog.addResult(String.format("Ancho: %d Alto: %d - Mejor tiempo: %dms", 
                        result.width, result.height, result.bestTime));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                dialog.addResult("Error al ejecutar el benchmark: " + ex.getMessage());
            } finally {
                dialog.benchmarkCompleted();
                bench.setEnabled(true);
            }
        }
    };
    worker.execute();
    dialog.setVisible(true);
}
}
	
	public void setValues(){
		JTextField highT = new JTextField();
		JTextField lowT = new JTextField();
		JTextField width = new JTextField();
		JTextField height = new JTextField();
		
		highT.setText(getHigh()+"");
		lowT.setText(getLow()+"");
		width.setText(getTilewidth()+"");
		height.setText(getTileheight()+"");
		
		String high = "Umbral alto:";
		String low = "Umbral bajo:";
		String w = "Ancho pieza:";
		String h = "Alto pieza:";
		
		Object[] message = {high, highT,
							low, lowT,
							w, width,
							h, height};
		
		int option = JOptionPane.showConfirmDialog(null, message, "Ajustes", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION){
			setHigh(Float.parseFloat(highT.getText()));
			setLow(Float.parseFloat(lowT.getText()));
			setTileheight(Integer.parseInt(height.getText()));
			setTilewidth(Integer.parseInt(width.getText()));
		}
		
	}
	
public void benchmark(BenchmarkDialog dialog) {
    int width = source.getWidth();
    int height = source.getHeight();

    for (int i = 0; i < 20; i++) {
        float percent = 1 - (i * 0.05f);
        int newW = (int) (width * percent);
        int newH = (int) (height * percent);

        setTilewidth(newW);
        setTileheight(newH);

        canny = new TileWorker(source, getTilewidth(), getTileheight(), getHigh(), getLow());

        long start = System.currentTimeMillis();
        canny.processcanny();
        long end = System.currentTimeMillis();
        
        dialog.addResult(String.format("Ancho: %d Alto: %d Hilos: %d", newW, newH, canny.getThreadcount()));
        dialog.addResult(String.format("Tiempo %dms.", (end - start)));
    }
}
	
	
	public void setResult(BufferedImage image){
		this.result = image;
	}
	
	public BufferedImage getResult(){
		return result;
	}

	public float getHigh() {
		return high;
	}

	public float getLow() {
		return low;
	}

	public void setLow(float low) {
		this.low = low;
	}

	public void setHigh(float high) {
		this.high = high;
	}

	private BufferedImage ScaledImage(Image srcImg, int w, int h){
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		SwingUtilities.invokeAndWait(new Runnable(){
			@Override
			public void run(){
				WindowUI window = new WindowUI();
				window.pack();
				window.setLocationRelativeTo(null);
				window.setVisible(true);
			}
		});

	}

	public int getTilewidth() {
		return tilewidth;
	}

	public void setTilewidth(int tilewidth) {
		this.tilewidth = tilewidth;
	}

	public int getTileheight() {
		return tileheight;
	}

	public void setTileheight(int tileheight) {
		this.tileheight = tileheight;
	}
}