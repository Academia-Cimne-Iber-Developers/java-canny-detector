package test;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import core.CannyCallable;

public class CannyTest {

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
		
		float low = 2.5f;
		float high = 2.8f;
			
		CannyCallable callable = new CannyCallable(img, low, high);
		
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Nucleos: "+cores);
		long start = System.currentTimeMillis();
		ExecutorService pool = Executors.newFixedThreadPool(cores);
		Future<BufferedImage> future = pool.submit(callable);
		
		try {
			edges = future.get();
			pool.shutdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
			
		File output = new File("res/edge.jpg");
		try {
			ImageIO.write(edges, "jpg", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis();
		
		System.out.println("Total time: "+(end-start)+"ms.");
		
		JFrame frame = new JFrame();
		frame.setTitle("Canny Edge Detector");
		frame.getContentPane().setLayout(new FlowLayout());
		
		JPanel container = new JPanel();
		container.add(new JLabel(new ImageIcon(edges)));
		frame.add(container);		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
