import java.util.*;
import java.util.Map.Entry;

public class VMS {
	private Vector<Campaign> campaigns = new Vector<>();
	private Vector<User> users = new Vector<>();

	private static VMS instance = new VMS();
	private VMS() {
		
	}
	public static VMS getInstance() {
		return instance;
	}
	
	public Vector<Campaign> getCampaigns(){
		return campaigns;
	}
	
	public Campaign getCampaign(Integer id) {
		for(Campaign campaign:campaigns) {
			if(campaign.getId()==id)
				return campaign;
		}
		return null;
	}
	
	public boolean addCampaign(Campaign campaign) {
		return campaigns.add(campaign);
	}
	
	public void updateCampaign(Integer id, Campaign campaign) {
		HashMap<String, List<Integer>> codes = new HashMap<>();
		for(User user:getUsers()) {
			List<Integer> helperCodes = new ArrayList<>();
			Iterator<Entry<Integer, Set<Voucher>>> iter = user.getVoucherMap().entrySet().iterator();
			while(iter.hasNext()) {
				Set<Voucher> helper = iter.next().getValue();
				Iterator<Voucher> iterator = helper.iterator();
				while(iterator.hasNext()) {
					Voucher helperVoucher = iterator.next();
					if(helperVoucher.getCampaignID()==id) {
						if(helperVoucher.getEmail().compareTo(user.getEmail())==0)
							helperCodes.add(helperVoucher.getId());
					}
				}
			}
			codes.put(user.getEmail(), helperCodes);
		}
		for(Campaign campaignHelper:campaigns) {
			if(campaignHelper.getId()==id) {
				campaignHelper.notifyAllObservers(new Notification(NotificationType.EDIT, 
						campaignHelper.getAppDate(), id, codes));
				campaignHelper.updateWith(campaign);
			}
		}
	}

	public boolean cancelCampaign(Integer id) {
		HashMap<String, List<Integer>> codes = new HashMap<>();
		for(User user:getUsers()) {
			List<Integer> helperCodes = new ArrayList<>();
			Iterator<Entry<Integer, Set<Voucher>>> iter = user.getVoucherMap().entrySet().iterator();
			while(iter.hasNext()) {
				Set<Voucher> helper = iter.next().getValue();
				Iterator<Voucher> iterator = helper.iterator();
				while(iterator.hasNext()) {
					Voucher helperVoucher = iterator.next();
					if(helperVoucher.getCampaignID()==id) {
						if(helperVoucher.getEmail().compareTo(user.getEmail())==0)
							helperCodes.add(helperVoucher.getId());
					}
				}
			}
			codes.put(user.getEmail(), helperCodes);
		}
		for(Campaign campaign:campaigns) {
			if(campaign.getId()==id) {
				campaign.notifyAllObservers(new Notification(NotificationType.CANCEL, 
						campaign.getAppDate(), id, codes));
				if(campaign.getStatus()==CampaignStatusType.NEW || 
						campaign.getStatus()==CampaignStatusType.STARTED) {
					campaign.setStatus(CampaignStatusType.CANCELLED);
					return true;
				}
			}
		}
		return false;
	}
	
	public Vector<User> getUsers() {
		return users;
	}
	
	public boolean addUser(User user) {
		return users.add(user);
	}
	
	public String toString() {
		return "{ "+campaigns+",\n"+users+" }";
	}
}
