package img;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import gui.MainFrame;
import gui.Operation;

public class XML {
	private Image i;
	private Map<String,Operation>o;
	public XML(Image im,Map<String,Operation>oo) {
		i=im;
		o=oo;
	}
	/*
	 * ispisuje funkciju u .fun format
	 */
	static public void XMLFunctionWriter(String dirpath,Operation o) {
		String name=o.getName();
		if(o.isBasic())name+=2;
		String path=dirpath+"\\"+name+".fun";
		XMLOutputFactory xmlof=XMLOutputFactory.newInstance();
		try {
			XMLStreamWriter xmlw=xmlof.createXMLStreamWriter(new FileOutputStream(path),"UTF-8");
			xmlw.writeStartDocument("UTF-8", "1.0");
		    xmlw.writeCharacters("\n");
		    o.xmlWrite(xmlw);
		    xmlw.writeEndDocument();
		    xmlw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * parsira funkciju iz xmlReadera / parsira putanje i poziva parsiranje svake od tih funkcija posebno
	 */
	private void parseOperations(XMLStreamReader xmlr) throws XMLStreamException {
		while(xmlr.hasNext()) {
			switch(xmlr.next()) {
			case XMLStreamReader.START_ELEMENT:
				if(xmlr.getLocalName().equals("path")) {
					xmlr.next();
					String p=xmlr.getText();
					if(o.get(p)==null) {
						Operation op=Operation.parse(p, this.o);
						o.put(op.getName(),op);
					}
					xmlr.next(); //kraj
				}
				break;
			case XMLStreamReader.END_ELEMENT:
				return;
			} 
				
		}
	}
	/*
	 * parsira sliku / poziva parsiranje layera selekcija i funkcija
	 */
	private void parseImage(XMLStreamReader xmlr) throws XMLStreamException, Errors  {
		int count=0;
		while (xmlr.hasNext()) {
			int type=xmlr.next();
			switch(type) {
			case XMLStreamReader.START_ELEMENT:
				String name=xmlr.getLocalName();
				System.out.println(name+" tekuca stvar koja se cita");
				if(name.equals("layers")) {
					i.parseLayers(xmlr);
				}
				if(name.equals("selections")) {
					i.parseSelections(xmlr);
				}
				if(name.equals("functions")) {
					parseOperations(xmlr);
				}
				break;
			case XMLStreamReader.END_ELEMENT: //pazi!!!
				return;
			}
		}
		//parseOperationPaths(xmlr,ops);
	}
	/*
	 * proverava da li je ispravan format fajla
	 * da li je dobra ekstenzija
	 * baca gresku ako nije
	 * 
	 */
	public void checkXML(String pathXML) throws Errors{
		Pattern p=Pattern.compile("^.*.xml$");
		Matcher mat=p.matcher(pathXML);
		if(mat.matches()) {
			return;
		}
		else throw new Errors("The extension must be .xml");
	}
	
	/*
	 * glavna Reader metoda koja poziva sve ostale
	 * formira xmlR, proverava da li je putanja ispravna
	 */
	public void XMLReader(String pathXML) throws Errors {
		if(i==null) return;
		XMLInputFactory xmlif=XMLInputFactory.newInstance();
		XMLStreamReader xmlr;
		checkXML(pathXML);
			try {
				xmlr=xmlif.createXMLStreamReader(new FileInputStream(pathXML),"UTF-8");
				while (xmlr.hasNext()) {
					int type=xmlr.next();
					switch(type) {
					case XMLStreamReader.START_ELEMENT:
						String name=xmlr.getLocalName();
						if(name.equals("image")) {
							String r=xmlr.getAttributeValue(null, "height");
							i.setHeight(Integer.parseInt(r));
							r=xmlr.getAttributeValue(null, "width");
							i.setWidth(Integer.parseInt(r));
							//System.out.println("slika visina sirina:" +i.getHeight()+" "+i.getWidth());
							parseImage(xmlr);
						}
						break;
					case XMLStreamReader.END_ELEMENT:
						break;
					}
				}
				xmlr.close();
			} catch (FileNotFoundException | XMLStreamException e) {
				// TODO Auto-generated catch block
				throw new Errors("Path is not valid, file does not exist");
			}
			
	}
	/*
	 * vrsi citav ispis
	 */
	public void XMLWriter(String pathXML) throws Errors{
		if(i==null) return;
		checkXML(pathXML);
		try {
			XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
			XMLStreamWriter xmlw=xmlof.createXMLStreamWriter(new FileOutputStream(pathXML),"UTF-8");
			xmlw.writeStartDocument("UTF-8", "1.0");
		    xmlw.writeCharacters("\n");
		    xmlw.writeStartElement("image");
		    xmlw.writeAttribute("width", i.getWidth()+"");
		    xmlw.writeAttribute("height", i.getHeight()+"");
		    xmlw.writeStartElement("layers");
		    /*
		     * ispisuje layere onim redosledom kojim su se umetali
		     */
		    i.getLayers().values().stream().sorted((l1,l2)->{
		    	return l1.getID()-l2.getID();
		    }).forEach(l->{
		    	String k=l.getName();
		    	Formater f=new BMP();
				String path="data\\"+k+".bmp";
				File file=new File(path);
				Image i=new Image(null, l.getHeight(), l.getWidth(), l.getPixels());
				f.setImage(i);
				f.save(path);
				try {
					xmlw.writeStartElement("layer");
					xmlw.writeAttribute("active", (l.isVisible()?1:0)+"");
					xmlw.writeAttribute("changeable", (l.isActive()?1:0)+"");
					xmlw.writeCharacters("\n");//zbog kompatibilnosti sa c++
				    xmlw.writeStartElement("opacity");
				    xmlw.writeCharacters(l.getOpacity()+"");
				    xmlw.writeEndElement();
				    xmlw.writeCharacters("\n");//takodje kompatibilnost
				    xmlw.writeStartElement("path");
				    xmlw.writeCharacters(System.getProperty("user.dir")+"\\"+path);
				    xmlw.writeEndElement();
				    xmlw.writeEndElement();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
		    });
			xmlw.writeEndElement();
			xmlw.writeStartElement("selections");
			/*
			 * ispisuje selekcije
			 */
			int icount=i.getSelections().size();
			i.getSelections().values().stream().forEach(s->{
				try {
					
					xmlw.writeStartElement("selection");
					xmlw.writeAttribute("changeable", (s.isActive()?1:0)+"");
					xmlw.writeStartElement("name");
					xmlw.writeCharacters(s.getName());
					xmlw.writeEndElement();
					xmlw.writeStartElement("rectangles");
					s.getRectangles().stream().forEach(r->{
						try {
							xmlw.writeStartElement("rectangle");
							xmlw.writeStartElement("x");
							xmlw.writeCharacters(r.getX()+"");
							xmlw.writeEndElement();
							xmlw.writeStartElement("y");
							xmlw.writeCharacters(r.getY()+"");
							xmlw.writeEndElement();
							xmlw.writeStartElement("w");
							xmlw.writeCharacters(r.getXd()-r.getX()+"");
							xmlw.writeEndElement();
							xmlw.writeStartElement("h");
							xmlw.writeCharacters(r.getYd()-r.getY()+"");
							xmlw.writeEndElement();
							xmlw.writeEndElement();
							
						} catch (XMLStreamException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					});
					xmlw.writeEndElement();
					xmlw.writeEndElement();
				} catch (XMLStreamException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
			System.out.println(icount+"je broj selekcija");
			xmlw.writeEndElement();
			xmlw.writeStartElement("functions");
			/*
			 * ispisuje funkcije
			 */
			if(o!=null) o.values().stream().filter(o->{
				return !o.isBasic();
			}).forEach(e->{
				/*
				 * putanjaaaa*/
				String path=System.getProperty("user.dir")+"\\data";
				XMLFunctionWriter(path, e);
				try {
					xmlw.writeStartElement("path");
					xmlw.writeCharacters(path+"\\"+e.getName()+".fun");
					xmlw.writeEndElement();
				} catch (XMLStreamException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			});
			xmlw.writeEndElement();
			xmlw.writeEndElement();
			xmlw.writeEndDocument();
		    xmlw.close();
		
		} catch (FileNotFoundException|XMLStreamException e) {
			// TODO Auto-generated catch block
			throw new Errors("Path is not valid");
		} 
	}
	public static void main(String []s) {
		
	
	}
}
