import java.util.*;
import java.util.Map.Entry;

public class User {
	private Integer id;
	private String name, password, email;
	private UserType type;
	private UserVoucherMap uvMap = new UserVoucherMap();
	private Set<Notification> notifications = new HashSet<>();
	public User(Integer id, String name, String password, String email, UserType type) {
		this.id=id;
		this.name=name;
		this.password=password;
		this.email=email;
		this.type=type;
	}
	public void update(Notification notification) {
		notifications.add(notification);
	}
	public UserVoucherMap getVoucherMap() {
		return uvMap;
	}
	public Set<Voucher> getVouchers(){
		Set<Voucher> vouchers = new HashSet<>();
		Iterator<Entry<Integer, Set<Voucher>>> it = uvMap.entrySet().iterator();
		while(it.hasNext()) {
			Iterator<Voucher> iter = it.next().getValue().iterator();
			while(iter.hasNext()) {
				vouchers.add(iter.next());
			}
		}
		return vouchers;
	}
	public String getName() {
		return name;
	}
	public Integer getId() {
		return id;
	}
	public String getEmail() {
		return email;
	}
	public UserType getType() {
		return type;
	}
	public String getPassword() {
		return password;
	}
	public String toString() {
		return "["+id+";"+name+";"+email+";"+type+"]";
	}
	public HashSet<Notification> getNotifications(){
		return (HashSet<Notification>) notifications;
	}
}
