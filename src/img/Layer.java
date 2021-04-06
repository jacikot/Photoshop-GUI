package img;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Layer {
	private boolean active=false;
	private boolean visible=false;
	private int opacity=100;
	private int height;
	private int width;
	private static int ID=0;
	private int id=ID++; //odredjuje redosled umetanja
	private PIX[][]pixels;
	private String name=null;
	public Layer(Image i,String n) throws Errors {
		if(i==null) throw new Errors("Image unavaliable");
		height=i.getHeight();
		width=i.getWidth();
		pixels=i.getPixels();
		name=n;
	}
	public int getID() {
		return id;
	}
	public String getName() {
		return name;
	}
	/*
	 * parsira jedan layer iz xmlr
	 */
	public Layer(XMLStreamReader xmlr) throws Errors, XMLStreamException {
		String s=xmlr.getAttributeValue(null, "changeable");
		active=(s.equals("1")?true:false);
		s=xmlr.getAttributeValue(null, "active");
		visible=(s.equals("1")?true:false);
		xmlr.next(); //brise novi red izmedju layer i opacity
		int type=xmlr.next();
		if(type==XMLStreamReader.START_ELEMENT) {
			String nm=xmlr.getLocalName();
			if(nm.equals("opacity")) {
				if(xmlr.next()==XMLStreamReader.CHARACTERS) {
					opacity=Integer.parseInt(xmlr.getText());
				}
				if(xmlr.next()==XMLStreamReader.END_ELEMENT) {}
			}
		}
		xmlr.next(); //izmedju opacity i path
		if(xmlr.next()==XMLStreamReader.START_ELEMENT) {
			String nm=xmlr.getLocalName();
			if(nm.equals("path")) {
				if(xmlr.next()==XMLStreamReader.CHARACTERS) {
					String path=xmlr.getText();
					Formater f=Formater.getInstance(path);
					f.open(path);
					height=f.getIm().getHeight();
					width=f.getIm().getWidth();
					pixels=f.getIm().getPixels();
					name=f.getFileName(path);
					System.out.println("ime layera:"+name);
				}
				if(xmlr.next()==XMLStreamReader.END_ELEMENT) {}
			}
		}
	}
		
		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public int getOpacity() {
		return opacity;
	}
	public void setOpacity(int opacity) {
		this.opacity = opacity;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public PIX[][] getPixels() {
		return pixels;
	}
	public void setPixels(PIX[][] pixels) {
		this.pixels = pixels;
	}
	

}
