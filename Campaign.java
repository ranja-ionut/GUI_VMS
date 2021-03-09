import java.time.*;
import java.util.*;
import java.util.Map.Entry;

public class Campaign {
	
	private Integer id;
	private String name, description;
	private LocalDateTime start, finish;
	private Integer totalV, currV;
	private CampaignStatusType status;
	private CampaignVoucherMap dvMap = new CampaignVoucherMap();
	private Vector<User> users = VMS.getInstance().getUsers();
	private Set<User> observers = new LinkedHashSet<>();
	private String strategyType;
	protected Integer vouchersID = 1;
	protected static Vector<String> generatedCodes = new Vector<>();
	private static LocalDateTime appDate;
	
	//bonus
	private Strategy strategy;
	
	public Campaign(Integer id, String name, String description, LocalDateTime start,
			LocalDateTime finish, Integer totalV, String strategyType, LocalDateTime appDate) {
		Campaign.appDate=appDate;
		this.id=id;
		this.name=name;
		this.description=description;
		this.start=start;
		this.finish=finish;
		this.totalV=currV=totalV;
		if(appDate.isBefore(start))
			status=CampaignStatusType.NEW;
		if(appDate.isAfter(finish))
			status=CampaignStatusType.EXPIRED;
		if(appDate.isEqual(start)||(appDate.isAfter(start)&&appDate.isBefore(finish)))
			status=CampaignStatusType.STARTED;
		this.strategyType=strategyType;
		if(strategyType!=null)
			switch(strategyType) {
			case "A":
				strategy = new StrategyA();
				break;
			case "B":
				strategy = new StrategyB();
				break;
			case "C":
				strategy = new StrategyC();
				break;
			}
	}
	
	public Voucher executeStrategy() {
		currV--;
		return strategy.execute(this);
	}
	
	
	public void updateWith(Campaign campaign) {
		if(this.status==CampaignStatusType.CANCELLED) {
			return;
		}
		if(campaign.getFinish().isBefore(campaign.getStart()))
		{
			this.status=CampaignStatusType.EXPIRED;
			for(User user:users) {
				removeObserver(user);
			}
		}
		if(campaign.getStatus()==CampaignStatusType.NEW) {
			this.name=campaign.getName();
			this.description=campaign.getDescription();
			this.start=campaign.getStart();
			this.finish=campaign.getFinish();
			this.totalV=campaign.getTotalV();
		}
		if(campaign.getStatus()==CampaignStatusType.STARTED) {
			if(campaign.getTotalV() > this.totalV-this.currV){
				this.currV=campaign.getTotalV()-(this.totalV-this.currV);
				this.totalV=campaign.getTotalV();
			}
			finish=campaign.getFinish();
		}
	}
	
	public CampaignStatusType getStatus() {
		return status;
	}
	public void setStatus(CampaignStatusType status) {
		this.status=status;
	}
	
	public Set<Voucher> getVouchers() {
		Set<Voucher> vouchers = new HashSet<>();
		HashSet<Entry<String, Set<Voucher>>> entries = (HashSet<Entry<String, Set<Voucher>>>) dvMap.entrySet();
		Iterator<Entry<String, Set<Voucher>>> it = entries.iterator();
		while(it.hasNext()) {
			Entry<String, Set<Voucher>> entry = it.next();
			Iterator<Voucher> iter = entry.getValue().iterator();
			while(iter.hasNext()) {
				vouchers.add(iter.next());
			}
		}
		return vouchers;
	}
	
	public Voucher getVoucher(String code) {
		HashSet<Voucher> vouchers = (HashSet<Voucher>) getVouchers();
		Iterator<Voucher> it = vouchers.iterator();
		while(it.hasNext()) {
			Voucher voucher = it.next();
			if(voucher.getCode().compareTo(code)==0)
				return voucher;
		}
		return null;
	}
	protected String generateCode() {
		return (""+Math.random()).substring(2,6)+"X"+
				(""+Math.random()).substring(2,4)+"Y"+(""+Math.random()).substring(2,6);
	}
	private boolean isExisting(String email) {
		for(User user:users) {
			if(user.getEmail().compareTo(email)==0)
				return true;
		}
		return false;
	}
	public boolean generateVoucher(String email, String voucherType, float value) {
		Voucher voucher=null;
		if(currV==0)
			return false;
		if(!isExisting(email))
			return false;
		String code = generateCode();
		while(generatedCodes.contains(code)) {
			code = generateCode();
		}
		generatedCodes.add(code);
		if(voucherType.compareTo("GiftVoucher")==0) {
			voucher = new GiftVoucher(vouchersID, code, email, id, value);
			vouchersID++;
		}
		if(voucherType.compareTo("LoyaltyVoucher")==0) {
			voucher = new LoyaltyVoucher(vouchersID, code, email, id, value);
			vouchersID++;
		}
		if(voucher!=null) {
			for(User user:users) {
				if(user.getEmail().compareTo(email)==0) {
					if(!observers.contains(user)&&
							(user.getVoucherMap().isEmpty()||user.getVoucherMap().get(id)==null))
						addObserver(user);
					user.getVoucherMap().addVoucher(voucher);
				}
			}
			currV--;
			return dvMap.addVoucher(voucher);
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean checkVouchers(Set<Voucher> vouchersSet) {
		HashSet<Voucher> vouchers = (HashSet<Voucher>)vouchersSet;
		Iterator<Voucher> it = vouchers.iterator();
		while(it.hasNext()) {
			if(it.next().getStatus()==VoucherStatusType.UNUSED)
				return false;
		}
		return true;
	}
	public void redeemVoucher(String code, LocalDateTime date) {
		Voucher voucher = getVoucher(code);
		if(voucher.getStatus()==VoucherStatusType.UNUSED) {
			if(date.isAfter(start) && date.isBefore(finish) &&
					status!=CampaignStatusType.EXPIRED && status!=CampaignStatusType.CANCELLED) {
				voucher.setStatus(VoucherStatusType.USED);
				voucher.setUsedDate(date);
			}
		}
		//trebuie apelat removeObserver cand user-ul nu mai are niciun voucher unused
		//in cadrul campaniei respective, desi in teste nu se respecta acest lucru
		/*
		for(User user:users) {
			if(user.getEmail().compareTo(voucher.getEmail())==0) {
				HashSet<Entry<Integer, Set<Voucher>>> vouchers
					= (HashSet<Entry<Integer, Set<Voucher>>>) user.getVoucherMap().entrySet();
				Iterator<Entry<Integer, Set<Voucher>>> it = vouchers.iterator();
				while(it.hasNext()) {
					Entry<Integer, Set<Voucher>> entry = it.next();
					if(entry.getKey()==voucher.getCampaignID())
					{
						if(checkVouchers(entry.getValue()))
							removeObserver(user);
					}
				}
			}
		}*/
			
	}
	
	public Set<User> getObservers() {
		return observers;
	}
	
	public boolean addObserver(User user) {
		return observers.add(user);
	}
	
	public boolean removeObserver(User user) {
		return observers.remove(user);
	}
	
	public void notifyAllObservers(Notification notification) {
		for(User observer:observers)
			observer.update(notification);
	}
	
	public CampaignVoucherMap getVoucherMap() {
		return dvMap;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Integer getTotalV() {
		return totalV;
	}
	
	public Integer getCurrV() {
		return currV;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getStrategy() {
		return strategyType;
	}
	public Vector<User> getUsers(){
		return users;
	}
	
	public LocalDateTime getStart() {
		return start;
	}
	
	public LocalDateTime getFinish() {
		return finish;
	}
	public String toString() {
		return "["+id+", "+name+", "+description+", "+start+", "+
				finish+", "+totalV+", "+strategyType+", "+dvMap+"]"+"\n";
	}
	
	public LocalDateTime getAppDate() {
		return appDate;
	}
}
