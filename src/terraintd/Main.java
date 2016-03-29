package terraintd;

import terraintd.types.TypeGenerator;
import terraintd.window.Window;

public class Main {

	public static void main(String[] args) {
		TypeGenerator.generateValues();

		GameLogic.reset();

		GameLogic.cfg.read();
		GameLogic.cfg.apply();

		Window.setWindowVisible(true);
	}
}
