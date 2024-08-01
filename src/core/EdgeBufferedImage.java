package core;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class EdgeBufferedImage {

	private BufferedImage sourceImg;
	private BufferedImage[][] matImg;
	private int row, col, total,
				tilew, tileh,
				padw, padh,
				width, height;
	private static int BORDER = 8;
	
	public EdgeBufferedImage(BufferedImage image, int w, int h){
		this.sourceImg = image;

		/*
		 * Determino cantidad de filas y columnas
		 * dividiendo las dimensiones de la imagen
		 * por las dimensiones w y h que son ancho y alto
		 * del mosaico, respectivamente
		 */
		this.setCol((image.getWidth()/w));
		this.setRow((image.getHeight()/h));
		
		this.setWidth(image.getWidth());
		this.setHeight(image.getHeight());
		
		this.setTilew(w);
		this.setTileh(h);
		
		this.setPadw(getWidth() - (getTilew() * (getCol())));
		this.setPadh(getHeight() - (getTileh()) * (getRow()));
		
		if (getPadh() == getTileh()) setPadh(0);
		if (getPadw() == getTilew()) setPadw(0);
		
		if (getPadh() != 0) setRow(getRow()+1);
		if (getPadw() != 0) setCol(getCol()+1);
		
		this.setTotal(getCol()*getRow());
		
		this.setMatImg(new BufferedImage[row][col]);
	}
	
	public BufferedImage getSourceImg(){
		return this.sourceImg;
	}
	
	public void setSourceImg(BufferedImage image){
		this.sourceImg = image;
	}
	
    public BufferedImage[][] getMatImg() {
		return matImg;
	}

	public void setMatImg(BufferedImage[][] matImg) {
		this.matImg = matImg;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getTilew() {
		return tilew;
	}

	public void setTilew(int tilew) {
		this.tilew = tilew;
	}

	public int getTileh() {
		return tileh;
	}

	public void setTileh(int tileh) {
		this.tileh = tileh;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getPadw() {
		return padw;
	}

	public void setPadw(int padw) {
		this.padw = padw;
	}

	public int getPadh() {
		return padh;
	}

	public void setPadh(int padh) {
		this.padh = padh;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	/*
	 * Método que crea las imágenes más pequeñas
	 * a partir de la imagen original
	 * 
	 */
	public void createSmallImages() {
		
		int rows = getRow();
		int columns = getCol();
		int smallWidth = getTilew();
		int smallHeight = getTileh();
		int padh = 0;
		int padw = 0;
        
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {

				if ((col == columns - 1) && !(getPadw() == 0)){
					padw = getPadw();
					smallWidth = padw;
				}
				if ((row == rows - 1) && !(getPadh() == 0)){
					padh = getPadh();
					smallHeight = padh;
				}
					
				int posx = col * getTilew();
				int posy = row * getTileh();

				matImg[row][col] = getSourceImg().getSubimage(posx,	posy, smallWidth, smallHeight);
            }
			padh = 0;
			padw = 0;
			smallWidth = getTilew();
			smallHeight = getTileh();	
		}
		
		//Agrando las dimensiones de las piezas contando el borde a agregar
		setTilew(getTilew() + BORDER * 2);
		setTileh(getTileh() + BORDER * 2);
		
		//Agrego bordes negros a todas las piezas de la matriz
		for (int row = 0; row < rows; row++){
			for (int col = 0; col < columns; col++){
				matImg[row][col] = addBorders(getMatImg()[row][col]);
			}
		}
		
  }
	
	public void joinTiles(){
		
		setTilew(getTilew()-BORDER*2);
		setTileh(getTileh()-BORDER*2);
		
		int rows = getRow();
		int columns = getCol();
		int smallWidth = getTilew();
		int smallHeight = getTileh();
		int padh = 0;
		int padw= 0;
		
		BufferedImage comb = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) comb.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(Color.BLACK);
		
		/*
		 * Corto el excedente a todas las imágenes
		 * dentro de la matriz de imágenes.
		 * Teóricamente este metodo se corre luego de 
		 * tener la matriz de imagenes de bordes detectados
		 * con bordes extras.
		 */
		for (int row = 0; row < rows; row++){
			for (int col = 0; col < columns; col++){
			matImg[row][col] = cutBorders(getMatImg()[row][col]);
			}
		}
		
		/*
		 * En este ciclo empiezo a juntar todas las imágenes
		 * ya recortadas en una única imagen que será el
		 * resultado a devolver
		 */
		
		for (int row = 0; row < rows; row++){
			for (int col = 0; col < columns; col++){
				
				BufferedImage piece = getMatImg()[row][col];
					
				if ((col == columns - 1) && !(getPadw() == 0)){
					padw = getPadw();
					smallWidth = padw;
				}
				if ((row == rows - 1) && !(getPadh() == 0)){
					padh = getPadh();
					smallHeight = padh;
				}
						
				int posx = col * getTilew();
				int posy = row * getTileh();
				
				g.drawImage(piece, posx, posy, smallWidth, smallHeight, null);
				g.drawRect(posx, posy, smallWidth, smallHeight);
				
			}
			padh = 0;
			padw = 0;
			smallWidth = getTilew();
			smallHeight = getTileh();
		}
		g.dispose();
		
		setSourceImg(comb);
	}
	
	/*
	 * Dibuja la imagen en un lienzo más grande con bordes
	 * negros.
	 * 
	 */
	public BufferedImage addBorders(BufferedImage source){
		
		BufferedImage comb = new BufferedImage(source.getWidth()+BORDER*2, source.getHeight()+BORDER*2, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) comb.getGraphics();
		g.setColor(Color.GREEN);
				
		g.drawImage(source, BORDER, BORDER, null);
		g.drawRect(BORDER, BORDER, getTilew()-BORDER*2, getTileh()-BORDER*2);
		g.dispose();
		return comb;	
	}
	
	/*
	 * Corta los bordes excedentes de la imagen
	 * 
	 */
	public BufferedImage cutBorders(BufferedImage source){
		
		BufferedImage result = null;
		result = source.getSubimage(BORDER, BORDER, source.getWidth() - BORDER * 2,
																source.getHeight() - BORDER * 2);
		return result;	
	}
	

}
