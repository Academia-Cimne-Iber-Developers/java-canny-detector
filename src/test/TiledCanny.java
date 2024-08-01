package test;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import core.TileWorker;

public class TiledCanny {

	public static void main(String[] args) {

		BufferedImage img = null;
		BufferedImage edges = null;
		
		try
		{
			img = ImageIO.read(new File("res/lena.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		long start = System.currentTimeMillis();
		
		int w = 256;
		int h = 256;
		float low = 9.4f;
		float high = 9.7f;
		
		TileWorker worker = new TileWorker(img, w, h, high, low);
		worker.processcanny();
		
		edges = worker.getEdgeimage();
		
		File output = new File("res/resultado.jpg");
		try {
			ImageIO.write(edges, "jpg", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		long end = System.currentTimeMillis();
		
		System.out.println("Total time: "+(end-start)+"ms.");
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		JPanel container = new JPanel();
		container.add(new JLabel(new ImageIcon(edges)));
		frame.add(container);		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
