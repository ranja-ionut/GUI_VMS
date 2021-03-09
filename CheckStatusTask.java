import java.time.*;
import java.util.*;

public class CheckStatusTask extends TimerTask{
	private Integer campaignId;
	private LocalDateTime currDate;
	public CheckStatusTask(Integer campaignId, LocalDateTime currDate) {
		this.campaignId=campaignId;
		this.currDate=currDate;
	}
	public void run() {
		if(campaignId != null) {
			Campaign campaign = VMS.getInstance().getCampaign(campaignId);
			if(campaign.getStatus()!=CampaignStatusType.CANCELLED) {
				if(currDate.isAfter(campaign.getFinish())) {
					campaign.setStatus(CampaignStatusType.EXPIRED);
				}
			}
		}
	}

}
