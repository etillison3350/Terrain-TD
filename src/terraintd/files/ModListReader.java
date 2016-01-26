package terraintd.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModListReader {

	private ModListReader() {}

	private static HashMap<String, Boolean> mods;

	public static void read() {
		mods = new HashMap<>();

		List<?> json;
		try {
			Files.createFile(Paths.get("terraintd/mods/mod-list.json"));
			json = JSON.parseJSON(new String(Files.readAllBytes(Paths.get("terraintd/mods/mod-list.json"))));
		} catch (IOException e) {
			return;
		}

		if (!(json.get(0) instanceof Map<?, ?>)) return;

		Map<?, ?> map = ((Map<?, ?>) json.get(0));
		for (Object key : map.keySet()) {
			if (!(key instanceof String) || !(map.get(key) instanceof Boolean)) return;
			mods.put((String) key, (Boolean) map.get(key));
		}
	}

	public static void write() {
		try {
			Files.write(Paths.get("terraintd/mods/mod-list.json"), JSON.writeJSON(mods).getBytes());
		} catch (IOException e) {}
	}

	public static boolean isEnabled(String modId) {
		if (!mods.containsKey(modId)) mods.put(modId, true);
		
		return mods.get(modId);
	}

	public static void setEnabled(String modId, boolean enabled) {
		mods.put(modId, enabled);
	}

}
