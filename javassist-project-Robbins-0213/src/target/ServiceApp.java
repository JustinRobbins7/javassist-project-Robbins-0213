package target;

public class ServiceApp {
	int srcX = 11, srcY = 11;
	String srcID = "Service";
	
	public void move(int dx, int dy, String str) {}
	
	public static void main(String[] args) {
		System.out.println("[ServiceApp] Run.");
		ServiceApp a = new ServiceApp();
		a.move(0, 0, null);
		System.out.println("[ServiceApp] Done.");
	}
}
