import java.io.*;
import java.time.format.*;
import java.time.*;
import java.util.*;

public class Test {
	private static Integer totalCampaigns, totalEvents;
	public static Integer getTotalCampaigns() {
		return totalCampaigns;
	}
	public static Integer getTotalEvents() {
		return totalEvents;
	}
	@SuppressWarnings("unused")
	public static void main(String args[]) {
		try {
			String file = args[0].toString();
			int id, budget;
			String name, description, password, email;
			UserType type = null;
			LocalDateTime start, finish;
			
			File f = new File(file+"campaigns.txt");
			Scanner sc = new Scanner(f);
			sc.useDelimiter(";|\\n|\\r\\n|\\r");
			int n = sc.nextInt();
			totalCampaigns=n;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			LocalDateTime appDate = LocalDateTime.parse(sc.next(), formatter);
			for(int i=0;i<n;i++) {
				id = sc.nextInt();
				name = sc.next();
				description = sc.next();
				start = LocalDateTime.parse(sc.next(), formatter);
				finish = LocalDateTime.parse(sc.next(), formatter);
				budget = sc.nextInt();
				String strategyType = sc.next();
				Campaign campaign = new Campaign(id, name, description, start, finish,
						budget, strategyType, appDate);
				VMS.getInstance().addCampaign(campaign);
			}
			sc.close();
			
			f = new File(file+"users.txt");
			sc = new Scanner(f);
			sc.useDelimiter(";|\\n|\\r\\n|\\r");
			n = sc.nextInt();
			for(int i=0;i<n;i++) {
				id = sc.nextInt();
				name = sc.next();
				password = sc.next();
				email = sc.next();
				String typeHelper = sc.next();
				for(UserType helper:UserType.values())
				{
					if(typeHelper.compareTo(helper.toString())==0)
						type=helper;
				}
				User user = new User(id, name, password, email, type);
				VMS.getInstance().addUser(user);
			}
			sc.close();
			
			f = new File(file+"events.txt");
			sc = new Scanner(f);
			sc.useDelimiter(";|\\n|\\r\\n|\\r");
			appDate = LocalDateTime.parse(sc.next(), formatter);
			n = sc.nextInt();
			totalEvents=n;
			int userID, voucherID;
			String event, voucherType;
			float value;
			Set<Voucher> vouchers=null;
			Set<User> observers=null;
			Set<Notification> notifications=null;
			Voucher strategyVoucher;
			for(int i=0;i<n;i++) {
				userID = sc.nextInt();
				event = sc.next();
				switch(event) {
				case "addCampaign":
					id = sc.nextInt();
					name = sc.next();
					description = sc.next();
					start = LocalDateTime.parse(sc.next(), formatter);
					finish = LocalDateTime.parse(sc.next(), formatter);
					budget = sc.nextInt();
					String strategyType = sc.next();
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.ADMIN) {
						Campaign campaign = new Campaign(id, name, description, start, finish,
								budget, strategyType, appDate);
						VMS.getInstance().addCampaign(campaign);
					}
					break;
				case "editCampaign":
					id = sc.nextInt();
					name = sc.next();
					description = sc.next();
					start = LocalDateTime.parse(sc.next(), formatter);
					finish = LocalDateTime.parse(sc.next(), formatter);
					budget = sc.nextInt();
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.ADMIN) {	
						Campaign campaign = new Campaign(id, name, description, start, finish,
								budget, null, appDate);
						VMS.getInstance().updateCampaign(id, campaign);
					}
					break;
				case "cancelCampaign":
					id = sc.nextInt();
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.ADMIN) {
						VMS.getInstance().cancelCampaign(id);
					}
					break;
				case "generateVoucher":
					id = sc.nextInt();
					email = sc.next();
					voucherType = sc.next();
					value = sc.nextFloat();
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.ADMIN) {
						VMS.getInstance().getCampaign(id).generateVoucher(email, voucherType, value);
					}
					break;
				case "redeemVoucher":
					id = sc.nextInt() ;
					voucherID = sc.nextInt();
					LocalDateTime localDate = LocalDateTime.parse(sc.next(),formatter);
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.ADMIN) {
						Iterator<Voucher> it = VMS.getInstance().getCampaign(id).getVouchers().iterator();
						while(it.hasNext()) {
							Voucher voucher = it.next();
							if(voucher.getId()==voucherID) {
								VMS.getInstance().getCampaign(id).redeemVoucher(voucher.getCode(), localDate);
							}
						}
					}
					break;
				case "getVouchers":
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.GUEST) {
						vouchers = VMS.getInstance().getUsers().get(userID-1).getVouchers();
						System.out.println("getVouchers: "+vouchers);
					}
					break;
				case "getObservers":
					id = sc.nextInt();
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.ADMIN) {
						observers = VMS.getInstance().getCampaign(id).getObservers();
						System.out.println("getObservers: "+observers);
					}
					break;
				case "getNotifications":
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.GUEST) {
						notifications = VMS.getInstance().getUsers().get(userID-1).getNotifications();
						if(notifications.isEmpty()) {
							System.out.println("getNotifications: "+notifications);
						}
						else
							System.out.print("getNotifications: ");
						for(Notification notification:notifications) {
							if(notification.getCodes().containsKey(
									VMS.getInstance().getUsers().get(userID-1).getEmail())) {
								System.out.print("["+notification.getCampaignID()+";");
								List<Integer> codes = new ArrayList<>();
								Iterator<Integer> it = notification.getCodes().get(
										VMS.getInstance().getUsers().get(userID-1).getEmail()).iterator();
								while(it.hasNext()) {
									codes.add(it.next());
								}
								Collections.sort(codes);
								System.out.print(codes+";"+notification.getDate().toString().replace("T", " ")
										+";"+notification.getType()+"]"+"\n");
							}
									
						}
					}
					break;
				case "getVoucher":
					id = sc.nextInt();
					if(VMS.getInstance().getUsers().get(userID-1).getType()==UserType.ADMIN) {
						strategyVoucher = VMS.getInstance().getCampaign(id).executeStrategy();
						System.out.println("strategyVoucher" + 
								VMS.getInstance().getCampaign(id).getStrategy()+ ": " + strategyVoucher);
					}
					break;
				}
			}
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
