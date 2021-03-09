import java.util.*;

public class CampaignVoucherMap extends ArrayMap<String, Set<Voucher>>{
	
	public boolean addVoucher(Voucher v) {
		HashSet<Entry<String, Set<Voucher>>> entries = (HashSet<Entry<String, Set<Voucher>>>) entrySet();
		if(entries.isEmpty() || !super.containsKey(v.getEmail())) {
			Set<Voucher> value = new HashSet<>();
			value.add(v);
			super.put(v.getEmail(), value);
			return true;
		}
		Entry<String, Set<Voucher>> entry;
		Iterator<Entry<String, Set<Voucher>>> it = entries.iterator();
		while(it.hasNext()) {
			entry = it.next();
			if(entry.getKey().compareTo(v.getEmail())==0)
			{
				Set<Voucher> voucherSet = entry.getValue();
				voucherSet.add(v);
				return entries.add(entry);
			}
		}
		return false;
	}
}
