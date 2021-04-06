package img;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import gui.Operation;
public class Image {
	private native void loadImageXML(String pathXML,String outputPath);
	private BufferedImage i;
	private int height;
	private int width;
	private PIX[][]pixels;
	private Map<String,Layer> layers;
	private Map<String,Selection>selections;
	private static final String pathXML=System.getProperty("user.dir")+"\\data\\state.xml";
	public Image(BufferedImage bi, int h, int w, PIX[][]p) {
		i=bi;
		height=h;
		width=w;
		pixels=p;
		layers=new HashMap<String,Layer>();
		selections=new HashMap<String,Selection>();
	}
	/*
	 * inicijalno stvara se prazna slika ucitavanjem pocetne slike iz data foldera
	 */
	public Image() throws Errors {
		Formater f=new BMP();
		f.open(System.getProperty("user.dir")+"\\data\\slikaprazna100100.bmp");
		i=f.getIm().getI();
		height=f.getIm().getHeight();
		width=f.getIm().getWidth();
		pixels=f.getIm().getPixels();
		layers=new HashMap<String,Layer>();
		selections=new HashMap<String,Selection>();
	}
	public boolean existsLayer(String name) {
		return layers.containsKey(name);
	}
	public boolean existsSelection(String name) {
		return selections.containsKey(name);
	}
	public boolean noLayers() {
		return layers.isEmpty();
	}
	public Map<String,Selection> getSelections(){
		return selections;
	}
	public boolean noSelection() {
		return selections.isEmpty();
	}
	public Selection getSelection(String name) {
		return selections.get(name);
	}
	public void addSelection(String name) {
		selections.put(name, new Selection(name));
	}
	public void deleteSelection(String name) {
		selections.remove(name);
	}
	public void setLayer(String name,Layer l) {
		layers.put(name, l);
	}
	public Layer getLayer(String name) {
		return layers.get(name);
	}
	public void deleteLayer(String name) {
		layers.remove(name);
	}
	public BufferedImage getI() {
		return i;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public PIX[][] getPixels() {
		return pixels;
	}
	public Map<String,Layer> getLayers(){
		return layers;
	}
	public void makeXML(Map<String,Operation>ops) {
		XML writer=new XML(this,ops);
		try {
			writer.XMLWriter(pathXML);
		} catch (Errors e) {
			e.printStackTrace();
		}
	}
	//ucitava sliku sa odgovarajuce putanje
	public void load(String path) throws Errors {
	
		Formater f=new BMP();
		f.open(path);
		Image i=f.getIm();
		pixels=i.getPixels();
		height=i.getHeight();
		width=i.getWidth();
		this.i=i.getI();
			
	}
	/*
	 * radi se preko native metode koja cita stanje projekta u c++
	 * vrsi merge slike (svih layera)
	 * i tu sliku iscrtava
	 * 
	 */
	public void update(Map<String,Operation>ops) {
		try {
			makeXML(ops);
		    System.loadLibrary("Dll1");
		    loadImageXML(pathXML,System.getProperty("user.dir")+"\\data\\pom.bmp");
		    load(System.getProperty("user.dir")+"\\data\\pom.bmp");
		} catch(Errors e) {}
	}
	/*
	 * parsira selekciju iz xmlReadera
	 */
	public void parseSelections(XMLStreamReader xmlr) throws XMLStreamException {
		selections.clear();
		int i=0;
		while (xmlr.hasNext()) {
			int type=xmlr.next();
			switch(type) {
			case XMLStreamReader.START_ELEMENT:
				String name=xmlr.getLocalName();
				if(name.equals("selection")) {
					Selection s=new Selection(xmlr);
					selections.put(s.getName(),s );
					System.out.println("ucitana selekcija "+s.getName());
					i++;
				}
				break;
			case XMLStreamReader.END_ELEMENT:
				if(--i==0) {
					break;
				}
				else {
					return;
				}
			}
		}
	}
	/*
	 * parsira layere iz xmlr
	 */
	public void parseLayers(XMLStreamReader xmlr) throws XMLStreamException, Errors {
		layers.clear();
		int i=0;
		while (xmlr.hasNext()) {
			int type=xmlr.next();
			switch(type) {
			case XMLStreamReader.START_ELEMENT:
				String name=xmlr.getLocalName();
				if(name.equals("layer")) {
					Layer l=new Layer(xmlr);
					System.out.println("layer: "+l.getName()+" ");
					layers.put(l.getName(),l );
					i++;
				}
				break;
			case XMLStreamReader.END_ELEMENT: 
				//kada dodje do dva end to znaci da je pokupio end od poslednjeg layera i end od layers
				if(--i==0) {
					break;
				}
				else {
					return;
				}
			case XMLStreamReader.CHARACTERS:
				System.out.println(xmlr.getText()+"je procitano");
			}
		}
	}

}
