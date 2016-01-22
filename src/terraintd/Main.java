package terraintd;

import javax.swing.JFrame;

import terraintd.types.TypeGenerator;
import terraintd.window.Window;

public class Main {

	public static void main(String[] args) {
		TypeGenerator.generateValues();

		Window.setWindowDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Window.setWindowVisible(true);

		GameLogic.reset();

		GameLogic.cfg.read();
		GameLogic.cfg.apply();
	}
}
