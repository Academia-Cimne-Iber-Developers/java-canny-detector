package core;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class CannyCallable implements Callable<BufferedImage> {

	private BufferedImage source;
	private BufferedImage edge;
	private CannyEdgeDetector detector;
	private float low, high;
	
	/*
	 * Constructor de la clase
	 * 
	 * @param source: imagen a procesar
	 * @param low: umbral mínimo del detector
	 * @param high: umbral máximo del detector
	 * 
	 * Inicializa el detector con los umbrales
	 * y la imagen a procesar
	 * 
	 * @return void
	 */
	public CannyCallable(BufferedImage source, float low, float high){
		this.setSource(source); 				 
		this.detector = new CannyEdgeDetector();
		this.setLow(low);
		this.setHigh(high);
		initDetector();
	}
	
	/*
	 * Inicializa el detector con los umbrales
	 * 
	 * @return void
	 */
	private void initDetector(){
		detector.setLowThreshold(getLow());
		detector.setHighThreshold(getHigh());
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 * 
	 * Metodo que se ejecuta al correr el hilo
	 * Al finalizar el hilo devuelve un objeto
	 * en este caso la imagen de bordes
	 * 
	 * @return BufferedImage
	 * 
	 */
	@Override
	public BufferedImage call() {
		
		this.detector.setSourceImage(getSource());
		this.detector.process();
		setEdge(this.detector.getEdgesImage());	
		return getEdge();
	}

	public BufferedImage getSource() {
		return source;
	}

	public void setSource(BufferedImage source) {
		this.source = source;
	}

	public BufferedImage getEdge() {
		return edge;
	}

	public void setEdge(BufferedImage edge) {
		this.edge = edge;
	}

	public float getLow() {
		return low;
	}

	public void setLow(float low) {
		this.low = low;
	}

	public float getHigh() {
		return high;
	}

	public void setHigh(float high) {
		this.high = high;
	}

}
