import java.time.*;

public abstract class Voucher {
	private Integer id;
	private String code;
	private VoucherStatusType status;
	private LocalDateTime usedDate;
	private String email;
	private Integer campaignID;
	
	public Voucher(Integer id, String code, String email, Integer campaignID) {
		this.id=id;
		this.code=code;
		status=VoucherStatusType.UNUSED;
		usedDate=null;
		this.email=email;
		this.campaignID=campaignID;
	}
	public VoucherStatusType getStatus() {
		return status;
	}
	public void setStatus(VoucherStatusType status) {
		this.status=status;
	}
	public Integer getId() {
		return id;
	}
	public String getCode() {
		return code;
	}
	public String getEmail() {
		return email;
	}
	public Integer getCampaignID() {
		return campaignID;
	}
	public LocalDateTime getUsedDate() {
		return usedDate;
	}
	public void setUsedDate(LocalDateTime date) {
		usedDate=date;
	}
	public abstract float getTypeValue();
	public abstract String toString();
}
