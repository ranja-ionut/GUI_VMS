import java.util.*;

public class StrategyA implements Strategy{

	@Override
	public Voucher execute(Campaign c) {
		if(c.getObservers().isEmpty())
			return null;
		Integer selectedUserID = new Random().nextInt(c.getObservers().size())+1;
		User selectedUser = null;
		Iterator<User> it = c.getObservers().iterator();
		while(it.hasNext()) {
			User helper = it.next();
			if(helper.getId()==selectedUserID)
				selectedUser=helper;
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
