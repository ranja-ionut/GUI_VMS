import java.util.*;

public class UserVoucherMap extends ArrayMap<Integer, Set<Voucher>>{
	
	public boolean addVoucher(Voucher v) {
		HashSet<Entry<Integer, Set<Voucher>>> entries = (HashSet<Entry<Integer, Set<Voucher>>>) entrySet();
		if(entries.isEmpty() || !super.containsKey(v.getCampaignID())) {
			Set<Voucher> value = new HashSet<>();
			value.add(v);
			super.put(v.getCampaignID(), value);
			return true;
		}
		Entry<Integer, Set<Voucher>> entry;
		Iterator<Entry<Integer, Set<Voucher>>> it = entries.iterator();
		while(it.hasNext()) {
			entry = it.next();
			if(entry.getKey()==v.getCampaignID())
			{
				Set<Voucher> voucherSet = entry.getValue();
				voucherSet.add(v);
				return entries.add(entry);
			}
		}
		return false;
	}
}
