package terraintd.types;

import terraintd.Language;

public enum TargetType {

	FIRST(false),
	LAST(true),
	NEAREST(false),
	FARTHEST(true),
	STRONGEST(true),
	WEAKEST(false),
	VALUABLEST(true),
	WORTHLESSEST(false);

	public final boolean max;

	private TargetType(boolean max) {
		this.max = max;
	}

	public String toString() {
		return Language.get("target-" + this.name());
	}

}
