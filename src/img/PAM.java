package img;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAM implements Formater {
	private native void convertPAMtoBMP(String PAMfilepath,String BMPfilepath);
	private native void convertBMPtoPAM(String BMPfilepath,String PAMfilepath);
	private String defaultFilepath=System.getProperty("user.dir")+"\\data\\pom.bmp";
	private Image image=null;
	//vraca ime layera iz putanje
	public String getFileName(String path) {
		Pattern p=Pattern.compile("^.*\\(.*)\\.pam");
		Matcher mat=p.matcher(path);
		if(mat.matches()) {
			return mat.group(1);
		}
		else return null;
	}
	/*
	 * vrsi citanje pam formata
	 * ne radi java direktn citanje vec se vrsi konverzija pomocu biblioteke iz pam u bmp
	 * a onda mi citamo bmp sa default putanje 
	 */
	@Override
	public void open(String filepath) throws Errors {
		// TODO Auto-generated method stub
		System.loadLibrary("Dll1");
		File f=new File(filepath);
		if(!f.exists()) throw new Errors("File does not exist");
		convertPAMtoBMP(filepath,defaultFilepath);
		BMP formater=new BMP();
		formater.open(defaultFilepath);
		image=formater.getIm();
	}
	/*
	 * save se radi slicno izvrsi se save u bmp, a onda 
	 * dll uradi konverziju
	 */
	@Override
	public void save(String filepath) {
		// TODO Auto-generated method stub
		System.loadLibrary("Dll1");
		BMP formater=new BMP();
		try {
			if(image!=null) {
				formater.setImage(image);
				formater.save(defaultFilepath);
				convertBMPtoPAM(defaultFilepath,filepath);
			}
			else throw new Error();
		}
		catch(Error e) {
			e.printStackTrace();
		}
		
		
	}


	@Override
	public Image getIm() {
		// TODO Auto-generated method stub
		return image;
	}
	@Override
	public void setImage(Image i) {
		// TODO Auto-generated method stub
		image=i;
	}

}
