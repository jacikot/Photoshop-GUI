package gui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
public class Operation implements Cloneable{
	protected String name;
	protected boolean basic;
	protected boolean noArguments;
	protected String arg="void";
	//dohvata kompozitnu operaciju sa imenom na osnovu path
	//ako path nije odgovarajuci vraca null
	public static CompositeOperation getNewComposite(String path) {
		Pattern p=Pattern.compile("^.*\\\\([^\\.]*).fun$");
		Matcher mat=p.matcher(path);
		if(mat.matches()) {
			return new CompositeOperation(mat.group(1));
		}
		else return null;
	}
	public List<Operation> getOperations(){
		return null;
	}

	public Operation(String n, boolean isBasic,boolean noarg) {
		name=n;
		basic=isBasic;
		noArguments=noarg;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isBasic() {
		return basic;
	}
	public boolean isNoArguments() {
		return noArguments;
	}
	@Override
	public String toString() {
		return name+"("+arg+")";
	}
	/*
	 * postavlja argument ako je potrebno
	 */
	public void setArguments(String a) {
		if(isNoArguments()) return;
		try {
			Integer.parseInt(a);
			arg=a;
		}
		catch(NumberFormatException e) {
			arg=1+"";
		}
	}
	/*
	 * ispisuje listu operacija koje su sadrzane u to operaciji
	 * za basic operaciju to je uvek no components
	 */
	public void printComponents(java.awt.List l) {
		l.removeAll();
		l.add("no components");
	}
	//sluzi kako bi ispisala osnovnu operaciju u xml
	//poziva se iz kompozitne ili iz omotaca osnovne
	public void xmlWritepom(XMLStreamWriter xmlw) throws XMLStreamException {
		 xmlw.writeStartElement("function");
		    xmlw.writeAttribute("name", name);
			if(!noArguments) {
				xmlw.writeCharacters("\n"); //kompatibilnost
				xmlw.writeStartElement("argument");
				xmlw.writeCharacters(arg);
				xmlw.writeEndElement();
				xmlw.writeCharacters("\n");
			}
			xmlw.writeEndElement();
	}
	/*
	 * ispisuje funkciju u njen .fun fajl - osnovne se ispisuju sa omotacem 
	 * kompozitne operacije kako bi c++ mogao da ih ucita i izvrsi
	 * c++ gleda to kao kompozitne operacije sa jednim clanom u listi
	 * takav clan se nakon izvrsavanja brise i ne ucitava ga java pri vracanju konteksta
	 */
	public void xmlWrite(XMLStreamWriter xmlw) throws XMLStreamException {
		xmlw.writeStartElement("function");
	    xmlw.writeAttribute("name", name+2);
	    xmlw.writeCharacters("\n");
	    xmlw.writeStartElement("members");
	    xmlw.writeCharacters("\n");
	    xmlWritepom(xmlw);
	    xmlw.writeCharacters("\n");
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n");
		xmlw.writeEndElement();
	}
	/*
	 * operacije su cloneable
	 */
	@Override
	public Operation clone() {
		// TODO Auto-generated method stub
		Operation o=null;
		try {
			o= (Operation) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return o;
	}
	//parsira operaciju
	public void parseOp(XMLStreamReader xmlr,Map<String, Operation>ops) throws XMLStreamException {
		if(!noArguments) {
			xmlr.next();//novi red izmedju arguments i operacije
			if(xmlr.next()==XMLStreamReader.START_ELEMENT) {
				if(xmlr.getLocalName().equals("argument")) {
					xmlr.next();
					arg=xmlr.getText();
					xmlr.next();
					xmlr.next();//novi red kraj argumenata i kraj funkcije postoji ako ima argumenata
				}
			}
		}
		xmlr.next();//kraj fje
	}
	/*
	 * parsira obicnu operaciju bez members sa ili bez argumenata
	 */
	public static Operation parse(String pathXML, Map<String, Operation>ops) {
		XMLInputFactory xmlif=XMLInputFactory.newInstance();
		XMLStreamReader xmlr;
		Operation o=null;
		try {
				xmlr=xmlif.createXMLStreamReader(new FileInputStream(pathXML),"UTF-8");
				while(xmlr.hasNext()) {
					switch(xmlr.next()) {
					case XMLStreamReader.START_ELEMENT:
						if(xmlr.getLocalName().equals("function")) {
							String name=xmlr.getAttributeValue(null, "name");
							if(ops.get(name)==null) {
								o=new CompositeOperation(name);
								o.parseOp(xmlr,ops);
								ops.put(name,o);
							}
							else {
								xmlr.close();
								return ops.get(name);
								
							}
						}
						break;
					case XMLStreamReader.END_ELEMENT:
						xmlr.close();
						return o;
					}
				}
				
			} catch (FileNotFoundException | XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return o;
	}
	
	
	
}

class CompositeOperation extends Operation implements Cloneable{
	private List<Operation> operations=new ArrayList<Operation>(); 
	public CompositeOperation(String n) {
		super(n, false, true);
		// TODO Auto-generated constructor stub
	}
	public void addOperation(Operation o) {
		operations.add(o);
	}
	public void addList(List<Operation> lo) {
		operations=lo;
	}
	public List<Operation> getOperations(){
		return operations;
	}
	/*
	 * ispisuje sve komponente u listu na glavnom prozoru
	 */
	public void printComponents(java.awt.List l) {
		l.removeAll();
		if(operations.isEmpty()) System.out.println("empty");
		operations.stream().forEach(o->{
			l.add(o.toString());
		});
	}
	/*
	 * vrsi kloniranje liste operacija koje sadrzi tako sto za svaku poziva clone
	 * to je potrebno jer ako je clan osnovna operacija
	 * potrebno je klonirati i argument
	 */
	@Override
	public CompositeOperation clone()  {
		CompositeOperation co=null;
		co=(CompositeOperation)super.clone();
		List<Operation> old=co.operations;
		co.operations=new ArrayList<Operation>();
		for(int i=0;i<old.size();i++) {
			co.operations.add(old.get(i).clone());
		}
		return co;
	}
	/*
	 * ispisuje operaciju
	 */
	public void xmlWritepom(XMLStreamWriter xmlw)  throws XMLStreamException{
		xmlw.writeStartElement("function");
	    xmlw.writeAttribute("name", name);
	    xmlw.writeCharacters("\n"); //zbog kompatibilnosti
		xmlw.writeStartElement("members");
		operations.stream().forEach(o->{
			try {
				o.xmlWritepom(xmlw);
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		xmlw.writeEndElement();
		xmlw.writeCharacters("\n"); //kompatibilnost
		xmlw.writeEndElement();
	}
	/*
	 * parsira operaciju
	 */
	public void parseOp(XMLStreamReader xmlr,Map<String, Operation>ops) throws XMLStreamException {
		xmlr.next();//za novi red izmedju ime funkcije i members
		if(xmlr.next()==XMLStreamReader.START_ELEMENT) {
			if(xmlr.getLocalName().equals("members")) {
				loop:while(xmlr.hasNext()) {
					switch(xmlr.next()) {
					case XMLStreamReader.START_ELEMENT:
						String name=xmlr.getAttributeValue(null, "name");
						Operation o=null;
						if(ops.get(name)==null) {
							o=new CompositeOperation(name);
							ops.put(name, o);
						}
						else if(ops.get(name).isBasic()) o=new Operation(name,true,ops.get(name).noArguments);
						else o=new CompositeOperation(name);
						o.parseOp(xmlr, ops);
						operations.add(o);
						break;
						
					case XMLStreamReader.END_ELEMENT:
						break loop;
				}
			}
				
			} 
		}
		xmlr.next();//razmak izmedju members i funkcije
		xmlr.next();//kraj funkcije
	}
	/*
	 * to nema omotac kao osnovna operacija pa je ispisuje samo sa pom
	 */
	public void xmlWrite(XMLStreamWriter xmlw) throws XMLStreamException {
		xmlWritepom(xmlw);
	}
	
}