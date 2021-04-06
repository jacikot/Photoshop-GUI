package img;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Formater {
	public static final String []rgxs= {"^.*\\.bmp","^.*\\.pam"};
	public void open(String filepath) throws Errors;
	public void save(String filepath);
	public Image getIm();
	public void setImage(Image i);
	/*
	 * vrsi dohvatanje odgovarajuceg formatera 
	 * na osnovu ekstenzije zadate putanje
	 * ako ekstenzija ne odgovara ni pam ni bmp
	 * baca se izuzetak koji se iscrtava
	 */
	public static Formater getInstance(String path) throws Errors {
		Pattern p=Pattern.compile("^.*\\.bmp");
		Matcher mat=p.matcher(path);
		if(mat.matches()) return new BMP();
		p=Pattern.compile("^.*\\.pam");
		mat=p.matcher(path);
		if(mat.matches()) return new PAM();
		else throw new Errors("Incorrect file format");
	}
	public String getFileName(String path);
}
