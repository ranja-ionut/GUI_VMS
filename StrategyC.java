import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

public class StrategyC implements Strategy{

	@Override
	public Voucher execute(Campaign c) {
		if(c.getObservers().isEmpty())
			return null;
		User selectedUser = null;
		Integer min = c.getTotalV() + 1 , totalV;
		String minEmail = "";
		Iterator<User> it = c.getObservers().iterator();
		while(it.hasNext()) {
			User helper = it.next();
			Iterator<Entry<Integer, Set<Voucher>>> iter = helper.getVoucherMap().entrySet().iterator();
			while(iter.hasNext()) {
				Entry<Integer, Set<Voucher>> entry = iter.next();
				if(entry.getKey()==c.getId()) {
					totalV = entry.getValue().size();
					if(totalV < min) {
						min = totalV;
						minEmail = helper.getEmail();
					}
				}
			}
		}
		Iterator<User> iter = c.getObservers().iterator();
		while(iter.hasNext()) {
			User helper = iter.next();
			if(helper.getEmail().compareTo(minEmail)==0) {
				selectedUser=helper;
			}
		}
		Voucher voucher;
		Integer id, campaignID;
		String code, email;
		id = c.vouchersID;
		c.vouchersID++;
		campaignID = c.getId();
		code = c.generateCode();
		while(Campaign.generatedCodes.contains(code)) {
			code = c.generateCode();
		}
		Campaign.generatedCodes.add(code);
		email = selectedUser.getEmail();
		voucher = new GiftVoucher(id, code, email, campaignID, 100);
		selectedUser.getVoucherMap().addVoucher(voucher);
		c.getVoucherMap().addVoucher(voucher);
		return voucher;
	}

}
