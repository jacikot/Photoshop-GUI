package img;

import java.awt.Canvas;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class BMP implements Formater {
	private Image image=null;
	private native synchronized void writeBMPHeader(int w,int h,String s); 
	public Image getIm() {
		return image;
	}
	public void setImage(Image i) {
		image=i;
	}
	private static int get(byte b) {
		return (b>=0)?b:(int)b+256;
	}
	//vraca ime Layera iz putanje to parsira
	public String getFileName(String path) {
		Pattern p=Pattern.compile("^.*\\\\([^\\.]*).bmp$");
		Matcher mat=p.matcher(path);
		if(mat.matches()) {
			return mat.group(1);
		}
		else return null;
	}
	/*
	 * ucitava sliku u bmp tako sto ucita zaglavlje zbog dimenzija, a zatim sve piksele
	 */
	@Override
	public void open(String filepath) throws Errors {
		// TODO Auto-generated method stub
		File f=new File(filepath);
		if(!f.exists()) throw new Errors("File does not exists");
		try {
			byte[]data=Files.readAllBytes(f.toPath());
			int DIBsize=get(data[14])+((get(data[15]))<<8)+((get(data[16]))<<16)+((get(data[17]))<<24);
			int width=get(data[18])+((get(data[19]))<<8)+((get(data[20]))<<16)+((get(data[21]))<<24);
			int height=get(data[22])+((get(data[23]))<<8)+((get(data[24]))<<16)+((get(data[25]))<<24);
			int bitsPerPixel=get(data[28])+((get(data[29]))<<8);
			int offPixels = DIBsize+14;
			System.out.println(width+" "+height);
			PIX[][]pixels=new PIX[width][height];
			for (int i=0;i<height;i++) {
				for (int j=0;j<width;j++) {
					pixels[j][i]=new PIX();
					pixels[j][i].setB(data[offPixels++]);
					pixels[j][i].setG(data[offPixels++]);
					pixels[j][i].setR(data[offPixels++]);
					if(bitsPerPixel==32)pixels[j][i].setA(data[offPixels++]);
				}
				if(bitsPerPixel==24&&width%4>0) offPixels+=width%4;
			}
			image=new Image(ImageIO.read(f),height,width,pixels);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("Error reading file");
		}
		
			
	}

	/*
	 * cuva u bmpu odgovarajucu sliku postavljenu u polju image
	 * poziva biblioteku za ispis zaglavlja, a zatim appenduje i piksele
	 */
	@Override
	public void save(String filepath) {
		// TODO Auto-generated method stub
		try {
			if(image==null) throw new Error();
			System.loadLibrary("Dll1");
			//pazi za path na odbrani nece biti isti
			writeBMPHeader(image.getWidth(),image.getHeight(),filepath);
			File f=new File(filepath);
			BufferedWriter bw=new BufferedWriter(new FileWriter(f,true));
			//System.out.println(image.getPixels().length+" "+image.getPixels()[0].length+"lalalalal");
			byte []bytes=new byte[image.getPixels().length*image.getPixels()[0].length*4];
			for(int i=0;i<image.getPixels().length;i++) {
				for(int j=0;j<image.getPixels()[i].length;j++) {
					bytes[(i+j*image.getPixels().length)*4]=image.getPixels()[i][j].getB();
					bytes[(i+j*image.getPixels().length)*4+1]=image.getPixels()[i][j].getG();
					bytes[(i+j*image.getPixels().length)*4+2]=image.getPixels()[i][j].getR();
					bytes[(i+j*image.getPixels().length)*4+3]=image.getPixels()[i][j].getA();
				}
			}
			Files.write(f.toPath(), bytes, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Opening file error");
		} catch (Error e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
	
	

}


