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

import core.EdgeBufferedImage;

public class TileTest {

	public static void main(String[] args) {
		BufferedImage img = null;
		
		try
		{
			img = ImageIO.read(new File("res/lena.png"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		EdgeBufferedImage tiled = new EdgeBufferedImage(img, 19, 19);
		long start = System.currentTimeMillis();
		tiled.createSmallImages();
		tiled.joinTiles();
		
		long end = System.currentTimeMillis();
		
		System.out.println("Tiempo total: "+(end-start)+"ms.");
		BufferedImage result = tiled.getSourceImg();
		
		File output = new File("res/tiled.jpg");
		try {
			ImageIO.write(result, "jpg", output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		
		JPanel container = new JPanel();
		container.add(new JLabel(new ImageIcon(result)));
		
		frame.add(container);		
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
