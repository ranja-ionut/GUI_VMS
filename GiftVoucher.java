
public class GiftVoucher extends Voucher{
	private float value;
	public GiftVoucher(Integer id, String code, String email, Integer campaignID, float value) {
		super(id, code, email, campaignID);
		this.value=value;
	}
	public float getTypeValue() {
		return value;
	}
	public String toString() {
		if(getUsedDate()!=null)
			return "["+getId()+";"+getStatus()+";"+getEmail()+";"+value+";"
					+getCampaignID()+";"+getUsedDate().toString().replace("T", " ")+"]";
		else
			return "["+getId()+";"+getStatus()+";"+getEmail()+";"+value+";"
					+getCampaignID()+";"+getUsedDate()+"]";
	}
}
