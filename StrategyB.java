import java.util.*;
import java.util.Map.Entry;

public class StrategyB implements Strategy{

	@Override
	public Voucher execute(Campaign c) {
		if(c.getObservers().isEmpty())
			return null;
		User selectedUser = null;
		Integer max = 0, suma;
		String maxEmail = "";
		Iterator<User> it = c.getObservers().iterator();
		while(it.hasNext()) {
			User helper = it.next();
			Iterator<Entry<Integer, Set<Voucher>>> iter = helper.getVoucherMap().entrySet().iterator();
			while(iter.hasNext()) {
				Entry<Integer, Set<Voucher>> entry = iter.next();
				if(entry.getKey()==c.getId()) {
					Iterator<Voucher> iterator = entry.getValue().iterator();
					suma=0;
					while(iterator.hasNext()) {
						if(iterator.next().getStatus()==VoucherStatusType.USED)
							suma++;
					}
					if(suma > max) {
						max = suma;
						maxEmail = helper.getEmail();
					}
				}
			}
		}
		Iterator<User> iter = c.getObservers().iterator();
		while(iter.hasNext()) {
			User helper = iter.next();
			if(helper.getEmail().compareTo(maxEmail)==0) {
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
		voucher = new LoyaltyVoucher(id, code, email, campaignID, 50);
		selectedUser.getVoucherMap().addVoucher(voucher);
		c.getVoucherMap().addVoucher(voucher);
		return voucher;
	}

}
