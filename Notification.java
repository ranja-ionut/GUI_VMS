import java.util.*;
import java.time.*;

public class Notification {
	private NotificationType type;
	private LocalDateTime sentDate;
	private Integer campaignID;
	private HashMap<String, List<Integer>> codes;
	
	public Notification(NotificationType type, LocalDateTime sentDate,
				Integer campaignID, HashMap<String, List<Integer>> codes) {
		this.type=type;
		this.sentDate=sentDate;
		this.campaignID=campaignID;
		this.codes=codes;
	}
	
	public NotificationType getType() {
		return type;
	}
	
	public LocalDateTime getDate() {
		return sentDate;
	}
	
	public Integer getCampaignID() {
		return campaignID;
	}
	
	public HashMap<String, List<Integer>> getCodes(){
		return codes;
	}

}
