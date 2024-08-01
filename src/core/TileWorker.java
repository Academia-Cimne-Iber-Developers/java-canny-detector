package core;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TileWorker {

    private EdgeBufferedImage imagetiled;
    private List<List<BufferedImage>> matimages;
    private List<List<BufferedImage>> matresult;
    private BufferedImage edgeimage;
    private List<List<CannyCallable>> matcallable;
    private List<List<Future<BufferedImage>>> matfuture;
    private ExecutorService pool;
    private float lowThreshold;
    private float highThreshold;
    private int threadcount;

    /*
     * Constructor de la clase TileWorker
     * 
     * @param image Imagen a procesar
     * @param w Ancho de la imagen
     * @param h Alto de la imagen
     * @param high Umbral alto
     * @param low Umbral bajo
     * 
     * @return void
     * 
     */
    public TileWorker(BufferedImage image, int w, int h, float high, float low) {
        setLowThreshold(low);
        setHighThreshold(high);
        setImagetiled(new EdgeBufferedImage(image, w, h));
        getImagetiled().createSmallImages();
        setThreadcount(getImagetiled().getTotal());
        setMatimages(convertToListOfLists(getImagetiled().getMatImg()));
        initializeLists();
        setEdgeimage(null);
        initExecutor();
    }

    /*
     * Inicializa las listas de Callable, Future y Result
     */
    private void initializeLists() {
        int rows = getImagetiled().getRow();
        int cols = getImagetiled().getCol();

        matcallable = new ArrayList<>(rows);
        matfuture = new ArrayList<>(rows);
        matresult = new ArrayList<>(rows);

        for (int i = 0; i < rows; i++) {
            matcallable.add(new ArrayList<>(cols));
            matfuture.add(new ArrayList<>(cols));
            matresult.add(new ArrayList<>(cols));
            for (int j = 0; j < cols; j++) {
                matresult.get(i).add(null);
            }
        }
    }

    /*
     * Convierte un arreglo de BufferedImage a una lista de listas de BufferedImage
     * 
     * @param array Arreglo de BufferedImage
     * 
     * @return List<List<BufferedImage>> Lista de listas de BufferedImage
     * 
     */
    private List<List<BufferedImage>> convertToListOfLists(BufferedImage[][] array) {
        List<List<BufferedImage>> result = new ArrayList<>(array.length);
        for (BufferedImage[] row : array) {
            List<BufferedImage> listRow = new ArrayList<>(row.length);
            for (BufferedImage img : row) {
                listRow.add(img);
            }
            result.add(listRow);
        }
        return result;
    }

    /*
     * Procesa la imagen con el algoritmo de Canny en la
     * matriz de imágenes
     * Luego reconstruye la imagen
     * 
     * @return void
     */
    public void processcanny() {
        loadMatcallable();
        processImages();
        rebuildimage();
    }

    /*
     * Inicializa el ExecutorService
     */
    private void initExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();    
        setPool(Executors.newFixedThreadPool(cores));
    }

    /*
     * Carga los objetos CannyCallable en la lista matcallable
     * 
     * @return void
     */
    private void loadMatcallable() {
        int rows = getImagetiled().getRow();
        int cols = getImagetiled().getCol();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matcallable.get(i).add(new CannyCallable(matimages.get(i).get(j), getLowThreshold(), getHighThreshold()));
            }
        }
    }

    /*
     * Procesa las imágenes con el algoritmo de Canny
     */
    private void processImages() {
        int rows = getImagetiled().getRow();
        int cols = getImagetiled().getCol();
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matfuture.get(i).add(pool.submit(matcallable.get(i).get(j)));
            }
        }
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                try {
                    matresult.get(i).set(j, matfuture.get(i).get(j).get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        pool.shutdown();
    }

    /*
     * Reconstruye la imagen a partir de la matriz de imágenes
     */
    private void rebuildimage() {
        getImagetiled().setMatImg(convertToArray(matresult));
        getImagetiled().joinTiles();
        setEdgeimage(getImagetiled().getSourceImg());
        setImagetiled(null);
    }

    /*
     * Convierte una lista de listas de BufferedImage a un arreglo de BufferedImage
     */
    private BufferedImage[][] convertToArray(List<List<BufferedImage>> list) {
        BufferedImage[][] result = new BufferedImage[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i).toArray(new BufferedImage[0]);
        }
        return result;
    }

	public EdgeBufferedImage getImagetiled() {
		return imagetiled;
	}

	public void setImagetiled(EdgeBufferedImage imagetiled) {
		this.imagetiled = imagetiled;
	}

	public List<List<BufferedImage>> getMatimages() {
		return matimages;
	}

	public void setMatimages(List<List<BufferedImage>> matimages) {
		this.matimages = matimages;
	}

	public List<List<CannyCallable>> getMatcallable() {
		return matcallable;
	}

	public void setMatcallable(List<List<CannyCallable>> matcallable) {
		this.matcallable = matcallable;
	}

	public ExecutorService getPool() {
		return pool;
	}

	public void setPool(ExecutorService pool) {
		this.pool = pool;
	}

	public List<List<Future<BufferedImage>>> getMatfuture() {
		return matfuture;
	}

	public void setMatfuture(List<List<Future<BufferedImage>>> matfuture) {
		this.matfuture = matfuture;
	}

	public List<List<BufferedImage>> getMatresult() {
		return matresult;
	}

	public void setMatresult(List<List<BufferedImage>> matresult) {
		this.matresult = matresult;
	}

	public BufferedImage getEdgeimage() {
		return edgeimage;
	}

	public void setEdgeimage(BufferedImage edgeimage) {
		this.edgeimage = edgeimage;
	}

	public float getLowThreshold() {
		return lowThreshold;
	}

	public void setLowThreshold(float lowThreshold) {
		this.lowThreshold = lowThreshold;
	}

	public float getHighThreshold() {
		return highThreshold;
	}

	public void setHighThreshold(float highThreshold) {
		this.highThreshold = highThreshold;
	}

	public int getThreadcount() {
		return threadcount;
	}

	public void setThreadcount(int threadcount) {
		this.threadcount = threadcount;
	}
}
