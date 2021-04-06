package img;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Selection {
	public static class Rectangle {
		private int x,y,xd,yd;

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getXd() {
			return xd;
		}

		public void setXd(int xd) {
			this.xd = xd;
		}

		public int getYd() {
			return yd;
		}

		public void setYd(int yd) {
			this.yd = yd;
		}
		/*
		 * parsira pravougaonik iz xmlReadera
		 */
		public void XMLparse(XMLStreamReader xmlr ) throws NumberFormatException, XMLStreamException {
			while(xmlr.hasNext()) {
				switch(xmlr.next()) {
				case XMLStreamReader.START_ELEMENT:
					if(xmlr.getLocalName().equals("x")) {
						xmlr.next();
						setX(Integer.parseInt(xmlr.getText()));
						xmlr.next();//za kraj
						break;
					}
					if(xmlr.getLocalName().equals("y")) {
						xmlr.next();
						setY(Integer.parseInt(xmlr.getText()));
						xmlr.next();//za kraj
						break;
					}
					if(xmlr.getLocalName().equals("w")) {
						xmlr.next();
						int w=Integer.parseInt(xmlr.getText());
						setXd(w+x);
						xmlr.next();//za kraj
						break;
					}
					if(xmlr.getLocalName().equals("h")) {
						xmlr.next();
						int h=Integer.parseInt(xmlr.getText());
						setYd(h+y);
						xmlr.next();//za kraj
					}
					break;
				case XMLStreamReader.END_ELEMENT:
					//kraj rectangle
					return;
				}
		}
	}
		
	}
	private List <Rectangle> rectangles=new ArrayList<Rectangle>();
	private String name;
	private boolean active=false;
	public void setActive(boolean b) {
		active=b;
	}
	/*
	 * parsira selekciju, za svaki pravougaonik zove svoju metodu za njih
	 */
	public Selection(XMLStreamReader xmlr) throws XMLStreamException {
		String s=xmlr.getAttributeValue(null, "changeable");
		active=(s.equals("1")?true:false);
		int type=0;
		do {
			type=xmlr.next();
			if(type==XMLStreamReader.START_ELEMENT) {
				String nm=xmlr.getLocalName();
				if(nm.equals("name")) {
					if(xmlr.next()==XMLStreamReader.CHARACTERS) name=xmlr.getText();
					if(xmlr.next()==XMLStreamReader.END_ELEMENT) {
						continue;
					}
				}
				if(nm.equals("rectangles")) {
					while (xmlr.hasNext()) {
						switch(xmlr.next()) {
						case XMLStreamReader.START_ELEMENT:
							if(xmlr.getLocalName().equals("rectangle")) {
								Rectangle r=new Rectangle();
								r.XMLparse(xmlr);
								rectangles.add(r);
							}
							break;
						case XMLStreamReader.END_ELEMENT:
							return;
						}
					}
				}
			}
		}
		while(type!=XMLStreamReader.END_ELEMENT);
		
	}
	public Selection(String n) {
		name=n;
	}
	public List<Rectangle> getRectangles() {
		return rectangles;
	}
	public void addRectangle(Rectangle r) {
		rectangles.add(r);
	}
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return active;
	}
	
	
}
