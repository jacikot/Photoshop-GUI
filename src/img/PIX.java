package img;

public class PIX {
	private byte r,g,b,a;
	public PIX(int rr, int gg, int bb, int aa) {
		r=(byte)(rr%256);
		g=(byte)(gg%256);
		b=(byte)(bb%256);
		a=(byte)(aa%256);
	}
	public PIX() {
		this(0,0,0,0);
	}
	public byte getR() {
		return r;
	}
	public void setR(int r) {
		this.r = (byte)(r%256);
	}
	public byte getG() {
		return g;
	}
	public void setG(int g) {
		this.g = (byte)(g%256);
	}
	public byte getB() {
		return b;
	}
	public void setB(int b) {
		this.b = (byte)(b%256);
	}
	public byte getA() {
		return a;
	}
	public void setA(int a) {
		this.a = (byte)(a%256);
	}
	
}
