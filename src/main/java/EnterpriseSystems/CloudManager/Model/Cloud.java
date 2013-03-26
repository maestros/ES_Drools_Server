package EnterpriseSystems.CloudManager.Model;

import java.util.Map;


public class Cloud {
	
	private Map<Long,Blade> blades;
	
	public Cloud(Map<Long,Blade> blades) {
		this.blades = blades;
	}
	
	public Map<Long, Blade> getBlades() {
		return blades;
	}
	
}
