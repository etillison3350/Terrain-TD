package terraintd;

import javax.swing.JFrame;

import terraintd.types.TypeGenerator;
import terraintd.window.Window;

public class Main {

	public static void main(String[] args) {
		TypeGenerator.generateValues();

		Window w = new Window();
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
