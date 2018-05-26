import java.io.IOException;
import java.net.InetAddress;

public class ConnectFour {
	public static void main(String[] args) throws IOException {
		new MainMenuGUI();
		System.out.println(InetAddress.getLocalHost());
		System.out.println(InetAddress.getLocalHost().getHostName());
	}
}
