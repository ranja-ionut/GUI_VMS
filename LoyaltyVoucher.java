
public class LoyaltyVoucher extends Voucher{
	private float percent;
	public LoyaltyVoucher(Integer id, String code, String email, Integer cid, float percent) {
		super(id, code, email, cid);
		this.percent=percent;
	}
	public float getTypeValue() {
		return percent;
	}
	public String toString() {
		if(getUsedDate()!=null)
			return "["+getId()+";"+getStatus()+";"+getEmail()+";"+percent+";"
					+getCampaignID()+";"+getUsedDate().toString().replace("T", " ")+"]";
		else
			return "["+getId()+";"+getStatus()+";"+getEmail()+";"+percent+";"
					+getCampaignID()+";"+getUsedDate()+"]";
	}
}
