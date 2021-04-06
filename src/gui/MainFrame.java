package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Stroke;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.color.*;
import img.*;


public class MainFrame extends Frame {
	//komponente MainFrame
	private slika c=null;
	private Image image=null;
	private Label errors=null;
	private Choice layernames=null;
	private Choice selectionnames=null;
	private Choice operationnames=null;
	private TextField opacity=null;
	private TextField arg=null;
	private Checkbox active=null;
	private Checkbox visible=null;
	private Checkbox configure=null;
	private Checkbox activeSelection=null;
	private Button changeLayer=null;
	private Button deleteLayer=null;
	private Button deleteSelection=null;
	private Button execute=null;
	private Button del=null;
	private Button saveFun=null;
	private Panel east=null;
	private Panel west=null;
	private List comp=null;
	private boolean isSaved=false;
	//korisnicki direktorijum
	static String projectPath=System.getProperty("user.dir");
	//mapa operacija
	private Map<String,Operation> operations=new HashMap<String,Operation>();
	
	//getter
	public  Map<String,Operation> getOperations(){
		return operations;
	}
	/*
	 * klasa koja prosiruje platno na kojem ce se iscrtavati slika i selekcije
	 */
	class slika extends Canvas{
		Selection.Rectangle drawing=null; //kvadrat koji se trenutno iscrtava 
		public slika() {
			/*
			 * MouseListener za iscrtavanje i dodavanje pravougaonika
			 * u selekciji
			 */
			addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent arg0) {
					if(configure.getState()&&configure.isEnabled()) {
						//ako smo u konf rezimu postavljaju se pocetne koordinate
						drawing=new Selection.Rectangle();
						drawing.setX(arg0.getX());
						drawing.setY(arg0.getY());
						drawing.setXd(arg0.getX());
						drawing.setYd(arg0.getY());
						
					}
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
					//ako smo u konf rezimu postavljaju se krajnje koordinate
					//menjamo ih sa pocetnim ako su manje od pocetnih
					if(configure.getState()&&configure.isEnabled()&&drawing!=null) {
						if(arg0.getX()<drawing.getX()) {
							drawing.setXd(drawing.getX());
							drawing.setX(arg0.getX());
						}
						else drawing.setXd(arg0.getX());
						if(arg0.getY()<drawing.getY()) {
							drawing.setYd(drawing.getY());
							drawing.setY(arg0.getY());
						}
						else drawing.setYd(arg0.getY());
						String n=selectionnames.getSelectedItem();
						Selection s=image.getSelection(n);
						s.addRectangle(drawing);
						drawing=null;
						repaint();
					}
				}
				
			});
			
		}
		/*
		 * vrsi iscrtavanje aktivne selekcije
		 * poziva se iz paint za canvas
		 */
		private void paintRectangles(Graphics g) {
			Color c=g.getColor();
			if(image.getSelection(selectionnames.getSelectedItem()).isActive())g.setColor(Color.RED);
			else g.setColor(Color.BLACK);
			String sn=selectionnames.getSelectedItem();
			Selection s=image.getSelection(sn);
			Graphics2D gg = (Graphics2D)g;
			s.getRectangles().stream().forEach(r->{
				Rectangle2D rect = new Rectangle2D.Float(r.getX(), r.getY(), r.getXd()-r.getX(), r.getYd()-r.getY() );
				float[] dash = { 5F, 5F };
				Stroke dashedStroke = new BasicStroke( 2F, BasicStroke.CAP_SQUARE,
				BasicStroke.JOIN_MITER, 3F, dash, 0F );
				gg.fill( dashedStroke.createStrokedShape( rect ) );
			});
			g.setColor(c);
		}
		/*
		 * AWT nit iscrtava sliku
		 */
		@Override
		public void paint(Graphics g) {
			// TODO Auto-generated method stub
			if(image!=null) {
				g.drawImage(image.getI(), 0, 0, this);
				setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
				if(configure.getState()&&configure.isEnabled()) paintRectangles(g);
				
			}
			else {
				setBackground(Color.LIGHT_GRAY);
			}
		
		}
	}
	/*
	 * klasa dijaloga za eksportovanje slike u bmp ili pam formatu
	 */
	class DialogSave extends Dialog{
		private void setPanel() {
			Panel p=new Panel(new GridLayout(3,1));
			TextField tf2=new TextField();
			Panel pftf=new Panel();
			pftf.add(tf2);
			tf2.setPreferredSize(new Dimension(100,20));
			Button save=new Button("Save");
			Button cancel=new Button("Cancel");
			Label l=new Label("Enter path to the file",Label.CENTER);
			p.add(l);
			p.add(pftf);
			Panel p2=new Panel();
			p2.add(save);
			p2.add(cancel);
			p.add(p2);
			add(p);
			/*
			 * pritiskom na button save vrsi se cuvanje tekuce slike
			 */
			save.addActionListener(e->{
				try {
					//cita se tekst iz textfielda
					String path=tf2.getText();
					
					//uzima se instanca odgovarajuceg formatera na osnovu putanje
					//koriste se regex-i za matchovanje ekstenzije
					
					Formater f=Formater.getInstance(path);
					//ako slika jos nije postavljna baca se izuzetak
					
					if(image==null) throw new Errors("no image to save");
					
					//postavlja sliku koju je potrebno sacvati u image polje 
					//objekta formatera
					f.setImage(image);
					//vrsi cuvanje te slike u odgovarajucem formatu
					f.save(path);
					isSaved=true;
				}
				catch(Errors ee) {
					errors.setText(ee.toString());
					errors.setForeground(Color.RED);
				}
				
				
			});
			//cancel button gasi prozor
			cancel.addActionListener(e->{
				dispose();
			});
			
		}
		
		public DialogSave(Frame par) {
			super(par,"Save",true);
			setBounds(300,300,300,300);
			setPanel();
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					dispose();
					
				}
				
			
			});
			setVisible(true);
			// TODO Auto-generated constructor stub
		}
		
	}
	/*
	 * dijalog koji sluzi za dodavanje layera
	 * 
	 */
	class DialogAdd extends Dialog{
		private void setPanel() {
			Panel p=new Panel(new GridLayout(5,1));
			TextField tf=new TextField();
			Panel pftf=new Panel();
			pftf.add(tf);
			tf.setPreferredSize(new Dimension(100,20));
			Button open=new Button("Open");
			Button cancel=new Button("Cancel");
			Label l=new Label("Enter path to the file",Label.CENTER);
			Label l2=new Label("Enter layer name",Label.CENTER);
			TextField tf2=new TextField();
			tf2.setPreferredSize(new Dimension(100,20));
			Panel pftf2=new Panel();
			pftf2.add(tf2);
			p.add(l2);
			p.add(pftf2);
			p.add(l);
			p.add(pftf);
			Panel p2=new Panel();
			p2.add(open);
			p2.add(cancel);
			p.add(p2);
			add(p);
			/*
			 * vrsi dodavanje i ucitavanje layera
			 */
			open.addActionListener(e->{
				try {
					//dohvata text iz odgovarajuceg textfielda koji predstavlja putanju
					String path=tf.getText();
					Formater f;
					
					//ukoliko ime layera vec postoji baca se greska
					if(image.existsLayer(tf2.getText())) throw new Errors("Layer name exists");
					//dohvata instancu odg formatera na osnovu putanje i proverava ekstenziju
					
					f = Formater.getInstance(path);
					
					//otvara sliku sa tom ekstenzijom, ako slika ne postoji takodje baca izuzetak
					
					f.open(path);
					
					/*
					 * ako je to prvi layer ucitan postavlja tekucu sliku 
					 * ako nije prvi layer samo dodaje layer u objekat slike
					 * 
					 * */
					if(image==null) image=f.getIm();
					else {
						//azurira velicinu slike popunjavanjem praznim pikselima
						if(image.getHeight()<f.getIm().getHeight())image.setHeight(f.getIm().getHeight());
						if(image.getWidth()<f.getIm().getWidth()) image.setWidth(f.getIm().getWidth());
					}
					//dodaje layer
					image.setLayer(tf2.getText(), new Layer(f.getIm(),tf2.getText()));
					
					//dodaj ime layera u listu dostupnih
					
					layernames.add(tf2.getText());
					
					//omogucava menjanje i brisanje layera koje je ako nema layera onemoguceno
					
					changeLayer.setEnabled(true);
					deleteLayer.setEnabled(true);
					execute.setEnabled(true);
					isSaved=false;
					
				}
				 catch (Errors e1) {
					//ispisuje obavestenje na errors labeli
					errors.setText(e1.toString());
					errors.setForeground(Color.RED);
				 }
			});
			cancel.addActionListener(e->{
				dispose();
			});
		}
		public DialogAdd(Frame par) {
			super(par,"Add layer",true);
			setBounds(300,300,300,300);
			setPanel();
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent e) {
					dispose();
				}
				
			
			});
			setVisible(true);
		}
		
	}
	/*
	 * dijalog za dodavanje selekcija
	 */
	class DialogSelect extends Dialog{

		public DialogSelect(Frame f) {
			super(f, "Selections", true);
			setBounds(300,300,300,300);
			Panel p=new Panel(new GridLayout(3,1));
			p.add(new Label("Enter new selection name",Label.CENTER));
			TextField tf=new TextField();
			tf.setPreferredSize(new Dimension(100,20));
			Button b=new Button("Add");
			Button cancel=new Button("Cancel");
			/*
			 * dodavanje selekcije
			 */
			b.addActionListener(e->{
				//ako nema slike ne utice
				try {
					if(image==null) return;
					//ako postoji vec selekcija o tome se obavestava korisnik
					
					if(image.existsSelection(tf.getText())) throw new Errors("Selection name exists");
					
					//dodaje se selekcija i azurira se lista na prozoru
					
					image.addSelection(tf.getText());
					selectionnames.add(tf.getText());
					
					//omogucava se dugmad koja je neaktivna zbog nepostojanja selekcije
					
					configure.setEnabled(true);
					activeSelection.setEnabled(true);
					deleteSelection.setEnabled(true);
				}
				catch(Errors e1) {
					errors.setText(e1.toString());
					errors.setForeground(Color.RED);
				}
			});
			Panel p3=new Panel();
			p3.add(tf);
			p.add(p3);
			Panel p2=new Panel();
			
			p2.add(b);
			p2.add(cancel);
			cancel.addActionListener(e->{
				dispose();
			});
			p.add(p2);
			add(p);
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent arg0) {
					dispose();
				}
				
			});
			setVisible(true);
			// TODO Auto-generated constructor stub
		}
		
	}
	/*
	 * dijalog za ucitavanje konteksta iz xml
	 */
	class DialogLoadContext extends Dialog{

		public DialogLoadContext(Frame arg0) {
			super(arg0,"Import project",true);
			// TODO Auto-generated constructor stub
			setBounds(300,300,300,300);
			Panel p=new Panel(new GridLayout(3,1));
			p.add(new Label("Enter project path",Label.CENTER));
			TextField tf=new TextField();
			tf.setPreferredSize(new Dimension(100,20));
			Button b=new Button("Import");
			Button cancel=new Button("Cancel");
			cancel.addActionListener(e->{
				dispose();
			});
			/*
			 * vrsi se importovnje konteksta
			 */
			b.addActionListener(e->{
				//otvara sa xml sa slikom i operacijama u koje se treba smestiti kontekst
				XML xml=new XML(image,operations);
				try {
					/*
					 * vrsi se citanje iz xml
					 * greska je ako fajl ne postoji ili je ekstenzija pogresna
					*/
					xml.XMLReader(tf.getText());
					System.out.println("procitao xml");
					
					//vrsi se update svega sto se prikazuje na ekanu
					
					updateContext();
					isSaved=false;
				} catch (Errors ee) {
					errors.setText(ee.toString());
					errors.setForeground(Color.RED);
				}
			});
			Panel p3=new Panel();
			p3.add(tf);
			p.add(p3);
			Panel p2=new Panel();
			
			p2.add(b);
			p2.add(cancel);
			p.add(p2);
			add(p);
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent arg0) {
					dispose();
				}
				
			});
			setVisible(true);
		}
		
	}
	/*
	 * cuva kontekst programa koji je trenutno aktivan
	 * 
	 */
	class DialogStoreContext extends Dialog{

		public DialogStoreContext(Frame arg0) {
			super(arg0,"Export project",true);
			// TODO Auto-generated constructor stub
			setBounds(300,300,300,300);
			Panel p=new Panel(new GridLayout(3,1));
			p.add(new Label("Enter file path (xml)",Label.CENTER));
			TextField tf=new TextField();
			tf.setPreferredSize(new Dimension(100,20));
			Button b=new Button("Export");
			Button cancel=new Button("Cancel");
			cancel.addActionListener(e->{
				dispose();
			});
			/*
			 * vrsi eksportovanje projekta
			 */
			b.addActionListener(e->{
				//formira xml objekat nad odgovarajucom slikom i operacijama
				XML xml=new XML(image,operations);
				try {
					//onemogucava komponente koje su od znacaja za izmenu slike
					disableComponents();
					b.setEnabled(false);
					
					//vrsi citanje iz odgovarajuceg fajla sa uneto putanjom
					//baca gresku ako je problem u formatu fajla ili fijal ne postoji
					
					xml.XMLWriter(tf.getText());
					
					//omogucava komponente
					b.setEnabled(true);
					enableComponents();
				} catch (Errors ee) {
					errors.setText(ee.toString());
					errors.setForeground(Color.RED);
				}
			});
			Panel p3=new Panel();
			p3.add(tf);
			p.add(p3);
			Panel p2=new Panel();
			
			p2.add(b);
			p2.add(cancel);
			p.add(p2);
			add(p);
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent arg0) {
					dispose();
				}
				
			});
			setVisible(true);
		}
		
	}
	/*
	 * kreira meni 
	 */
	private void createMenu() {
		MenuBar mb=new MenuBar();
		Menu m1=new Menu("File");
		MenuItem mi1=new MenuItem("Save Image");
		MenuItem mi2=new MenuItem("Open Layer");
		MenuItem mi3=new MenuItem("Add Selection");
		MenuItem mi4=new MenuItem("Import Project");
		MenuItem mi5=new MenuItem("Export Project");
		m1.add(mi2);
		m1.add(mi1);
		m1.add(mi3);
		m1.add(mi4);
		m1.add(mi5);
		mb.add(m1);
		setMenuBar(mb);
		mi1.addActionListener(e->{
			new DialogSave(this);
		});
		mi2.addActionListener(e->{
			new DialogAdd(this);
		});
		mi3.addActionListener(e->{
			new DialogSelect(this);
		});
		mi4.addActionListener(e->{
			new DialogLoadContext(this);
		});
		mi5.addActionListener(e->{
			new DialogStoreContext(this);
		});
		
		
	}
	/*
	 * vrsi onemogucavanje komponenti koje su od znacaja za izmenu slike
	 */
	private void disableComponents() {
		selectionnames.setEnabled(false);
		configure.setEnabled(false);
		activeSelection.setEnabled(false);
		changeLayer.setEnabled(false);
		deleteLayer.setEnabled(false);
		deleteSelection.setEnabled(false);
		execute.setEnabled(false);
	}
	/*
	 * omogucava iste te komponente
	 */
	private void enableComponents() {
		if(!image.noLayers()) {
			changeLayer.setEnabled(true);
			deleteLayer.setEnabled(true);
			execute.setEnabled(true);
		}
		selectionnames.setEnabled(true);
		if(!image.noSelection()) {
			configure.setEnabled(true);
			activeSelection.setEnabled(true);
			deleteSelection.setEnabled(true);
		}
	}
	/*
	 * vrsi update slike i dimenzija
	 * radi se preko native metode koja cita stanje projekta u c++
	 * vrsi merge slike 
	 * i tu sliku iscrtava
	 * 
	 */
	private void setPicture() {
		image.update(operations);
		MainFrame.this.setBounds(0,0,image.getWidth()+east.getWidth()+west.getWidth()+15,image.getHeight()+errors.getHeight()+68);
		MainFrame.this.setResizable(false);
		c.setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
		isSaved=false;
		c.repaint();
	}
	
	/*
	 * kreira panel za layere
	 */
			
	private void createLayerPanel() {
		Panel p=new Panel();
		Label name=new Label("Layers",Label.CENTER);
		layernames=new Choice();
		
		/*
		 * izmenom oznacenog layera u listi menja se
		 * vrednost polja u tom layeru tako da odgovaraju trenutno izabranom
		*/ 
		
		layernames.addItemListener(e->{
			Layer cur=image.getLayer((layernames.getSelectedItem()));
			opacity.setText(cur.getOpacity()+"");
			active.setState(cur.isActive());
			visible.setState(cur.isVisible());
			
		});
		
		opacity=new TextField();
		opacity.setPreferredSize(new Dimension(80,20));
		opacity.setText(100+"");
		active=new Checkbox("active");
		/*
		 * postavlja dati layer na aktivan za izvrsavanje operacija
		 */
		active.addItemListener(e->{
			if(!image.getLayers().isEmpty()) {
				image.getLayer(layernames.getSelectedItem()).setActive(active.getState());
			}
		});
		visible=new Checkbox("visible");
		changeLayer=new Button("Set");
		changeLayer.setEnabled(false);
		
		/*
		 * button vrsi izmenu layera sa unetim vrednostima
		 * postavlja visible i opacity
		 * vrsi update slike
		 */
		changeLayer.addActionListener(e->{
			//ako slika nije postavljena nema uticaja
			if(image==null) return;
			
			//onemogucava odgovarajuce kompnente
			disableComponents();
			
			String n=layernames.getSelectedItem();
			Layer l=image.getLayer(n);
			boolean old=l.isVisible();
			l.setActive(active.getState());
			l.setVisible(visible.getState());
			int x=0;
			//provarava da li je opacity lepo ucitana ako nije ispisuje to korisniku
			 try { 
				 x=Integer.parseInt(opacity.getText());
			 } catch(NumberFormatException | NullPointerException  ex) { 
		    	errors.setText("Opacity is not valid");
		    	errors.setForeground(Color.RED); 
		    	enableComponents();
		    	return;
			 } 
			 //proverava vrednost opacity da li je odgovarajucim vrednostima
			if(x>100||x<0) {
				errors.setText("Arguments failed");
				errors.setForeground(Color.RED);
				enableComponents();
				return;
			}
			//izvrsava merge samo ako se promenio ili opacity ili visible
			if(l.isVisible()!=old||x!=l.getOpacity()) {
				l.setOpacity(Byte.parseByte(opacity.getText()));
				execute.setEnabled(true);
				setPicture();
			}
			//reaktivira komponente
			enableComponents();
			isSaved=false;
		});
		
		deleteLayer=new Button("Delete");
		deleteLayer.setEnabled(false);
		/*
		 * brise Layer koji je trenutno aktivan u listi
		 * ako ne postoji nijedan layer ovo dugnem je onemoguceno
		 */
		deleteLayer.addActionListener(e->{
			if(image==null) return;
			String n=layernames.getSelectedItem();
			Layer l=image.getLayer(n);
			//brise se i fizicki objekat i ime u listi
			image.deleteLayer(n);
			layernames.remove(n);
			//ako je bio vidljiv vrsi se update slike
			if(l.isVisible()) {
				image.update(operations);
				c.repaint();
			}
			if(image.noLayers()) {
				//ako nma vise layera onemogucava se odg button
				execute.setEnabled(false);
				changeLayer.setEnabled(false);
				deleteLayer.setEnabled(false);
			}
			isSaved=false;
		});
		
		p.add(new Label("opacity:"));
		p.add(opacity);
		Panel layers=new Panel(new GridLayout(7,1));
		layers.add(name);
		layers.add(layernames);
		layers.add(p);
		layers.add(active);
		layers.add(visible);
		layers.add(changeLayer);
		layers.add(deleteLayer);
		layers.setBackground(Color.PINK);
		east.add(layers);
	}
	/*
	 * kreira panel za selekcije
	 */
	private void createSelectionPanel() {
		Panel p=new Panel(new GridLayout(5,1));
		Label l=new Label("Selections",Label.CENTER);
		p.add(l);
		selectionnames=new Choice();
		
		//ako se menja ime u listi menja se i da li je aktivna ili ne selekcija
		selectionnames.addItemListener(e->{
			activeSelection.setState(image.getSelection(selectionnames.getSelectedItem()).isActive());
			c.repaint();
		});
		p.add(selectionnames);
		activeSelection=new Checkbox("active");
		/*
		 * menja se boja prikazivanja pravougaonika
		 */
		activeSelection.addItemListener(e->{
			image.getSelection(selectionnames.getSelectedItem()).setActive(activeSelection.getState());
			activeSelection.setState(image.getSelection(selectionnames.getSelectedItem()).isActive());
			c.repaint();
		});
		configure=new Checkbox("configure mode");
		configure.setEnabled(false);
		//u config modu se vrsi i crtanje i dodavanje kvadrata
		configure.addItemListener(e->{
			c.repaint();
		});
		p.add(configure);
		p.add(activeSelection);
		deleteSelection=new Button("delete");
		activeSelection.setEnabled(false);
		deleteSelection.setEnabled(false);
		/*
		 * brise odabranu selekciju
		 * ako nema selekcija ovo je onemoguceno
		 */
		deleteSelection.addActionListener(e->{
			if(image==null) return;
			String n=selectionnames.getSelectedItem();
			//brise je i iz slike i iz liste
			selectionnames.remove(n);
			image.deleteSelection(n);
			if(image.noSelection()) {
				configure.setEnabled(false);
				activeSelection.setEnabled(false);
				deleteSelection.setEnabled(false);
			}
			c.repaint();
		});
		p.add(deleteSelection);
		p.setBackground(Color.CYAN);
		east.add(p);
		
	}
	/*
	 * dijalog za dodavanje funkcija
	 */
	class DialogFunctions extends Dialog{
		private List li=new List();
		private List lo=new List();
		private Button b=new Button("Delete");
		private Button b2=new Button("Finished");
		//temp je trenutno aktivna lista operacija dodatih u funkciju koja se pravi
		private java.util.List<Operation> temp=new ArrayList<Operation>();
		public DialogFunctions(Frame arg0) {
			super(arg0,"Functions",true);
			setBounds(250, 250, 400, 400);
			//dodaju se operacije koje trnutnopostoje
			operations.keySet().stream().forEach(k->{
				li.add(k);
			});
			Label l=new Label("Create composite function",Label.CENTER);
			Panel north=new Panel(new GridLayout(2,1));
			north.setBackground(Color.GRAY);
			north.add(l);
			Panel n=new Panel();
			n.add(new Label("name:"));
			TextField tf=new TextField();
			tf.setPreferredSize(new Dimension(100,20));
			n.add(tf);
			north.add(n);
			add(north,BorderLayout.NORTH);
			Panel p=new Panel(new GridLayout(1,2));
			Panel pli=new Panel(new BorderLayout());
			pli.add(li,BorderLayout.CENTER);
			Panel pli2=new Panel();
			pli2.add(new Label("argument:"));
			TextField arg=new TextField();
			arg.setPreferredSize(new Dimension(100,20));
			pli2.add(arg);
			pli.add(pli2,BorderLayout.SOUTH);
			p.add(pli);
			/*
			 * duplim klikom na listu dodaje se
			 * odgoarajuca funkcija sa odgovarajucim argumentom
			 * ako argument nije validan postavlja podrazumevano 1
			 */
			li.addActionListener(e->{
				String name=e.getActionCommand();
				Operation o=operations.get(name);
				Operation newo=null;
				//ako je ugradjena operacija samo seformira njen objekat
				if(o.isBasic()) {
					newo=new Operation(name,true,o.isNoArguments());
					newo.setArguments(arg.getText());
					
				}
				else newo=o.clone(); 
				/*ako je kompozitna formira se klon operacije iz mape
				 * ne moze da se koristi konstruktor ovde jer konstruktor pravi praznu listu clanova
				 * operacije su cloneable
				*/
				temp.add(newo); //dodaje u privremenu listu
				lo.add(newo.toString()); //dodaje u izlaznu listu ispis na ekranu
			});
			Panel p2=new Panel(new BorderLayout());
			p2.add(lo,BorderLayout.CENTER);
			Panel buttons=new Panel();
			buttons.add(b);
			buttons.add(b2);
			p2.add(buttons,BorderLayout.SOUTH);
			p.add(p2);
			add(p,BorderLayout.CENTER);
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent arg0) {
					dispose();
				}
				
			});
			/*
			 * brise trenutno aktivnu operaciju
			 * ako ne postoji nijedna dodata nema uticaja
			 */
			b.addActionListener(e->{
				if(temp.isEmpty()) return; 
				temp.remove(lo.getSelectedIndex());
				lo.remove(lo.getSelectedItem());
				
			});
			/*
			 * formira kompozitnu operaciju sastojanu od prethodnih
			 * dodaje je u operacije, u listu operacija u glavnom prozoru i u 
			 * listu dostupnih u dijalogu
			 */
			b2.addActionListener(e->{
				CompositeOperation o=new CompositeOperation(tf.getText());
				o.addList(temp);
				if(temp.isEmpty()) System.out.println("i on je prazan");
				temp=new ArrayList<Operation>();
				operations.put(tf.getText(), o);
				li.add(tf.getText());
				operationnames.add(tf.getText());
				lo.removeAll();
			});
			setVisible(true);
			
		}
		
	}
	/*
	 * formira panel za funkcije
	 */
	private void createFunctionPanel() {
		west=new Panel(new GridLayout(7,1));
		west.setBackground(Color.ORANGE);
		Label op=new Label("Operations",Label.CENTER);
		west.add(op);
		operationnames=new Choice();
		//sve postojece operacije dodaje
		operations.keySet().stream().forEach(k->{
			operationnames.add(k);
		});
		
		west.add(operationnames);
		Panel argu=new Panel();
		Panel pathArg=new Panel();
		Label ll=new Label("arg:");
		Label ll2=new Label("dir path:");
		pathArg.add(ll2);
		argu.add(ll);
		del=new Button("Delete");
		Button add=new Button("Add");
		Button load=new Button("Load");
		del.setEnabled(false);
		saveFun=new Button("Save");
		saveFun.setEnabled(false);
		Panel adddel=new Panel();
		adddel.add(del);
		adddel.add(add);
		adddel.add(saveFun);
		adddel.add(load);
		west.add(adddel);
		arg=new TextField();
		arg.setPreferredSize(new Dimension(100,20));
		argu.add(arg);
		TextField pathSave=new TextField();
		pathSave.setPreferredSize(new Dimension(100,20));
		pathArg.add(pathSave);
		west.add(argu);
		west.add(pathArg);
		comp=new List();
		comp.add("no components");
		west.add(comp);
		execute=new Button("Execute");
		west.add(execute);
		
		add(west,BorderLayout.WEST);
		//promena trenutno prikazivane operacije menja dostupnost polja za argument
		//menja dostupnost brisanja i ono sto pise u listi koja prikazuje sadrzaj komp fje
		operationnames.addItemListener(e->{
			Operation o=operations.get(operationnames.getSelectedItem());
			if (o.isNoArguments()){
				arg.setEnabled(false);
			}
			else arg.setEnabled(true);
			o.printComponents(comp);
			if(o.isBasic()) {del.setEnabled(false);saveFun.setEnabled(false);}
			else {del.setEnabled(true);saveFun.setEnabled(true);}
		});
		
		// brise funkciju iz operacija i liste 
		//azurira zavisne komponente
		del.addActionListener(e->{
			operations.remove(operationnames.getSelectedItem());
			operationnames.remove(operationnames.getSelectedItem());
			operations.get(operationnames.getSelectedItem()).printComponents(comp);
			arg.setEnabled(!operations.get(operationnames.getSelectedItem()).isNoArguments());
		});
		load.addActionListener(e->{
			try {
				Operation o=Operation.getNewComposite(pathSave.getText());
				if(o==null) throw new Errors("file format must be .fun");
				if(operations.containsKey(o.getName())) throw new Errors("function exists in operation map");
				if(!new File(pathSave.getText()).exists()) throw new Errors("file does not exists");
				o=Operation.parse(pathSave.getText(), operations);
				operationnames.add(o.getName());
			}
			catch(Errors e1) {
				errors.setForeground(Color.RED);
				errors.setText(e1.getMessage());
			}
			//operationnames.add
		});
		saveFun.addActionListener(e->{
			Operation o=operations.get(operationnames.getSelectedItem());
			if(o.isBasic()) return;
			if(!new File(pathSave.getText()).exists()) {
				errors.setForeground(Color.RED);
				errors.setText("directory does not exist");
				return;
			}
			XML.XMLFunctionWriter(pathSave.getText(), o);
		});
		//otvara prozor za dodavanje
		add.addActionListener(e->{
			new DialogFunctions(this);
			
		});
		execute.setEnabled(false);
		/*
		 * pokrece izvravanje funkcije
		 */
		execute.addActionListener(e->{
			try {
				String path=projectPath+"\\data\\state.xml";
				//cuva kontekst programa u state.xml
				XML xml=new XML(image,operations);
				xml.XMLWriter(path);
				//dohvata trazenu funkciju, argument ako ga ima i ispisuje to u .fun
				Operation o=operations.get(operationnames.getSelectedItem());
				if(!o.isNoArguments()) o.setArguments(arg.getText());
				XML.XMLFunctionWriter(projectPath+"\\data", o);
				/*
				 * prosledjuje ta dva fajla c++
				 * c++ to cita, izvrsava odgovarajucu uperaciju
				 * cuva kontekst programa koga vraca javi
				 * java cita taj kontekst vrsi potrebno azuriranje 
				 * (to je neophodno jer java mora da ima uvida u promene na layerima, a ne samo na konacnoj slici)
				 */
				String exepath=projectPath+"\\code\\Project1\\Debug\\Project1.exe "
						+path+" "+projectPath+"\\data\\"+o.getName()+(o.isBasic()?"2":"")+".fun";
				Runtime runtime=Runtime.getRuntime();
				String oldtext=errors.getText();
				Color c=errors.getForeground();
				try {
					
					errors.setForeground(Color.BLUE);
					errors.setText("function executing");
					
					//deaktiviraju se komponente
					
					disableComponents();
					
					//pokrece se izvrsvanje
					
					Process process=runtime.exec(exepath);
					System.out.println("ulazi u wait");
					
					//ceka se da se program zavrsi
					
					process.waitFor();
					
					//aktiviraju se komponente
					enableComponents();
					
					//cita se xml
					xml.XMLReader(path);
					errors.setForeground(Color.GREEN);
					errors.setText("function completed");
					
					//vrsi se update konteksta kako bi se doslo do izmene i prozora
					updateContext();
					
				} catch (IOException e1) {
					errors.setText(e1.toString());
					errors.setForeground(Color.RED);
					return;
				} catch (InterruptedException e1) {
					errors.setText(e1.toString());
					errors.setForeground(Color.RED);
					return;
				}
				errors.setText("no errors happened");
			}
			catch(Errors ee) {
				errors.setText(ee.toString());
				errors.setForeground(Color.RED);
			}
		});
		
	}
	private void addEast() {
		east=new Panel(new GridLayout(2,1));
		createLayerPanel();
		createSelectionPanel();
		add(east,BorderLayout.EAST);
	}
	private void createErrorLabel() {
		errors=new Label("No error happened",Label.CENTER);
		errors.setPreferredSize(new Dimension(400,60));
		errors.setFont(new Font("Ariel",50,20));
		errors.setForeground(Color.GREEN);
		Panel p=new Panel();
		add(p,BorderLayout.SOUTH);
		p.add(errors);
	}
	/*
	 * pocetni sadrzaj mape
	 */
	private void initializeOperationMap() {
		operations.put("add", new Operation("add",true,false));
		operations.put("sub", new Operation("sub",true,false));
		operations.put("insub", new Operation("insub",true,false));
		operations.put("mul", new Operation("mul",true,false));
		operations.put("div", new Operation("div",true,false));
		operations.put("indiv", new Operation("indiv",true,false));
		operations.put("pow", new Operation("pow",true,false));
		operations.put("min", new Operation("min",true,false));
		operations.put("max", new Operation("max",true,false));
		operations.put("log", new Operation("log",true,true));
		operations.put("abs", new Operation("abs",true,true));
		operations.put("inv", new Operation("inv",true,true));
		operations.put("gray", new Operation("gray",true,true));
		operations.put("bw", new Operation("bw",true,true));
		operations.put("med", new Operation("med",true,true));
	}
	class IsSavedDialog extends Dialog{

		public IsSavedDialog(Frame arg0) {
			super(arg0,"Exit",true);
			setBounds(300,300,300,300);
			Panel px=new Panel(new GridLayout(3,1));
			Label q=new Label("Do you want to save image?",Label.CENTER);
			px.add(q);
			Panel mid=new Panel();
			TextField tf=new TextField();
			tf.setPreferredSize(new Dimension(100,20));
			Label l=new Label("path:");
			mid.add(l);
			mid.add(tf);
			px.add(mid);
			Button yes=new Button("Save");
			Button no=new Button("Don't save");
			Button cancel=new Button("Cancel");
			Panel down=new Panel();
			down.add(yes);
			down.add(no);
			down.add(cancel);
			px.add(down);
			add(px);
			yes.addActionListener(e->{
				try {
					//cita se tekst iz textfielda
					String path=tf.getText();
					
					//uzima se instanca odgovarajuceg formatera na osnovu putanje
					//koriste se regex-i za matchovanje ekstenzije
					
					Formater f=Formater.getInstance(path);
					//ako slika jos nije postavljna baca se izuzetak
					
					if(image==null) throw new Errors("no image to save");
					
					//postavlja sliku koju je potrebno sacvati u image polje 
					//objekta formatera
					f.setImage(image);
					//vrsi cuvanje te slike u odgovarajucem formatu
					f.save(path);
					isSaved=true;
					MainFrame.this.dispose();
				}
				catch(Errors ee) {
					errors.setText(ee.toString());
					errors.setForeground(Color.RED);
					dispose();
				}
				
			});
			no.addActionListener(e->{
				MainFrame.this.dispose();
				
			});
			cancel.addActionListener(e->{
				dispose();
			});
			setVisible(true);
			addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosing(WindowEvent arg0) {
					dispose();
				}
				
			});
		}
		
	}
	public MainFrame() {
		super("prozor");
		
		c=new slika();
		add(c);
		initializeOperationMap();
		//image=b.getIm();
		setBounds(0,0,800,800);
		//proveri da li je slika ucitana i formater
		createMenu();
		createErrorLabel();
		addEast();
		createFunctionPanel();
		setVisible(true);
		addWindowListener(new WindowAdapter(){

			@Override
			public void windowClosing(WindowEvent e) {
				if(isSaved) dispose();
				else new IsSavedDialog(MainFrame.this);
			}
			
		});
		//slika koja se stvara je prazna slika velicine 100X100
		try {
			image=new Image();
		} catch (Errors e1) {
			// TODO Auto-generated catch block
			errors.setText("System error");
			errors.setForeground(Color.RED);
		}
	}
	//update svih delova prozora  pri ucitavanju celog konteksta
	
	private void errorUpdate() {
		errors.setText("No error happened");
		errors.setFont(new Font("Ariel",50,20));
		errors.setForeground(Color.GREEN);
		
	}
	private void updateSelections() {
		selectionnames.removeAll();
		image.getSelections().keySet().stream().forEach(k->{
			selectionnames.add(k);
		});
		if(image.getSelections().isEmpty()) {
			configure.setEnabled(false);
			activeSelection.setEnabled(false);
			deleteSelection.setEnabled(false);
		}
		else {
			configure.setEnabled(true);
			activeSelection.setEnabled(true);
			deleteSelection.setEnabled(true);
		}
		if(!image.getSelections().isEmpty())activeSelection.setState(image.getSelection(selectionnames.getSelectedItem()).isActive());
	}
	private void updateLayers() {
		layernames.removeAll();
		image.getLayers().keySet().stream().forEach(k->{
			System.out.println(k);
		});
		image.getLayers().keySet().stream().forEach(k->{
			
			layernames.add(k);
		});
		if(!image.getLayers().isEmpty()) {
			opacity.setText(image.getLayer(layernames.getSelectedItem()).getOpacity()+"");
			active.setState(image.getLayer(layernames.getSelectedItem()).isActive());
			visible.setState(image.getLayer(layernames.getSelectedItem()).isVisible());
		}
		if(image.getLayers().isEmpty()) {
			changeLayer.setEnabled(false);
			deleteLayer.setEnabled(false);
		}
		else {
			changeLayer.setEnabled(true);
			deleteLayer.setEnabled(true);
		}
	}
	private void updateFunctions() {
		operationnames.removeAll();
		operations.keySet().stream().forEach(k->{
			operationnames.add(k);
		});
		if(!operations.get(operationnames.getSelectedItem()).isBasic()) {del.setEnabled(true);saveFun.setEnabled(true);}
		else {del.setEnabled(false); saveFun.setEnabled(false); }
		if(operations.get(operationnames.getSelectedItem()).isNoArguments()) arg.setEnabled(false);
		else arg.setEnabled(true);
		operations.get(operationnames.getSelectedItem()).printComponents(comp);
	}
	private void updateContext() {
		if(image==null) image=new Image(null,0,0,null);
		errorUpdate();
		updateFunctions();
		updateLayers();
		updateSelections();
		disableComponents();
		setPicture();
		enableComponents();
		isSaved=false;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(System.getProperty("user.dir"));
		new MainFrame();
	}

}//D:\POOP\tj180023d\slike
