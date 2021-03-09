import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.Timer;

import javax.swing.*;
import javax.swing.table.*;

public class GUIVMS extends JFrame{
	private static final long serialVersionUID = 2283858403015868596L;
	private User mainUser = null;
	private LocalDateTime appDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),
			ZoneId.systemDefault());
	private String inputFileDirectory = null;
	private Integer mainCampaignID = null;
	private static Integer totalEvents=null, totalCampaigns=null;
	private Vector<Timer> timers = new Vector<>();
	private Long time = (long) 60000; // un minut
	public GUIVMS(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1080,960);
		
		JPanel mainPanel = new JPanel(new GridLayout(1,2));
		
		JTextField name = new JTextField();
		JPasswordField password = new JPasswordField();
		JButton chooseInputFileBtn = new JButton("Load input directory");
		chooseInputFileBtn.setBackground(Color.orange);
		JFileChooser chooseInputFile = new JFileChooser();
		chooseInputFile.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooseInputFile.setApproveButtonText("Select directory");
		chooseInputFileBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				JFrame choose = new JFrame("Choose Input Files Directory");
				choose.setSize(800,600);
				chooseInputFile.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {
						if(event.getActionCommand().compareTo(JFileChooser.APPROVE_SELECTION)==0)
						{
							inputFileDirectory = chooseInputFile.getSelectedFile().getPath();
							try {
								File checker = new File(inputFileDirectory);
								Boolean c=false, u=false, e=false, m=false;
								for(File file:checker.listFiles()) {
									if(file.getName().compareTo("campaigns.txt")==0)
										c = true;
									if(file.getName().compareTo("events.txt")==0)
										e = true;
									if(file.getName().compareTo("users.txt")==0)
										u = true;
									if(file.getName().compareTo("emails.txt")==0)
										m = true;
								}
								if(c==false || u==false || e == false || m==false)
									throw new Exception();
							}
							catch(Exception e){
								JOptionPane.showMessageDialog(null, "Nu ai ales un folder valid, incearca din nou"
										, "Selectie gresita!", JOptionPane.ERROR_MESSAGE);
								return;
							}
							choose.dispose();
						}
						if(event.getActionCommand().compareTo(JFileChooser.CANCEL_SELECTION)==0) {
							choose.dispose();
						}
					}
					
				});
				choose.add(chooseInputFile);
				choose.setVisible(true);
			}
			
		});
		JButton loadVMS = new JButton("Load VMS"), logIn = new JButton("Log In");
		loadVMS.setBackground(Color.CYAN);
		JPanel secondaryPanel = new JPanel(new GridLayout(2,2));
		loadVMS.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					String[] args = new String[1];
					if(inputFileDirectory==null)
						throw new Exception();
					args[0]=inputFileDirectory+"/";
					Test.main(args);
					totalEvents = Test.getTotalEvents();
					totalCampaigns = Test.getTotalCampaigns();
					for(Campaign campaign:VMS.getInstance().getCampaigns()) {
						Timer helperTimer = new Timer();
						TimerTask helperTask = new CheckStatusTask(campaign.getId(),appDate);
						helperTimer.scheduleAtFixedRate(helperTask, 0, time);
						timers.add(helperTimer);
					}
					
					mainPanel.remove(loadVMS);
					mainPanel.remove(chooseInputFileBtn);
					mainPanel.repaint();
					mainPanel.revalidate();
					mainPanel.setLayout(new GridLayout(2,1));
					secondaryPanel.add(new JLabel("User Name:"));
					secondaryPanel.add(name);
					secondaryPanel.add(new JLabel("User Password:"));
					secondaryPanel.add(password);
					mainPanel.add(secondaryPanel);
					mainPanel.add(logIn);
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Nu ai ales locatia fisierelor de input"
							, "Eroare la incarcare", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		logIn.setBackground(Color.DARK_GRAY);
		logIn.setForeground(Color.WHITE);
		logIn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				Iterator<User> it = VMS.getInstance().getUsers().iterator();
				@SuppressWarnings("deprecation")
				String n = name.getText(), pswd = password.getText();
				while(it.hasNext()) {
					User user = it.next();
					if(user.getName().compareTo(n)==0 && user.getPassword().compareTo(pswd)==0)
						mainUser = user;
				}
				if(mainUser==null) {
					JOptionPane.showMessageDialog(null, "Nume sau parola gresita!"
							, "Eroare la logare", JOptionPane.ERROR_MESSAGE);
				}
				else {
					mainPanel.remove(secondaryPanel);
					mainPanel.remove(logIn);
					mainPanel.repaint();
					mainPanel.revalidate();
					mainPanel.setLayout(new BorderLayout());
					if(mainUser.getType() == UserType.ADMIN) {
						mainPanel.add(adminPanel());
					}
					if(mainUser.getType() == UserType.GUEST) {
						mainPanel.add(guestPanel());
					}
				}
			}
		});
		mainPanel.add(loadVMS);
		mainPanel.add(chooseInputFileBtn);
		add(mainPanel);
		setVisible(true);
	}
	private Vector<Vector<Object>> refreshCampaignRows(Integer version){
		Vector<Vector<Object>> campaignRows = new Vector<>();
		if(version==0) {
			Iterator<Entry<Integer, Set<Voucher>>> it = mainUser.getVoucherMap().entrySet().iterator();
			while(it.hasNext()) {
				Entry<Integer, Set<Voucher>> entry = it.next();
				Vector<Object> helper = new Vector<>();
				helper.add(entry.getKey());
				helper.add(VMS.getInstance().getCampaign(entry.getKey()).getName());
				helper.add(VMS.getInstance().getCampaign(entry.getKey()).getDescription());
				helper.add(VMS.getInstance().getCampaign(entry.getKey()).getStart().toString().replace("T", " "));
				helper.add(VMS.getInstance().getCampaign(entry.getKey()).getStatus());
				helper.add(entry.getValue().size());
				campaignRows.add(helper);
			}
		}
		else {
			for(Campaign campaign:VMS.getInstance().getCampaigns()) {
				Vector<Object> helper = new Vector<>();
				helper.add(campaign.getId());
				helper.add(campaign.getName());
				helper.add(campaign.getDescription());
				helper.add(campaign.getStart().toString().replace("T", " "));
				helper.add(campaign.getStatus());
				Integer suma = 0;
				Iterator<Entry<String, Set<Voucher>>> it = campaign.getVoucherMap().entrySet().iterator();
				while(it.hasNext()) {
					suma+=it.next().getValue().size();
				}
				helper.add(suma);
				campaignRows.add(helper);
			}
		}
		return campaignRows;
	}
	
	private Vector<Vector<Object>> refreshVoucherRows(Integer id){
		Vector<Vector<Object>> voucherRows = new Vector<>();
		if(id==0) {
			Iterator<Entry<Integer, Set<Voucher>>> it = mainUser.getVoucherMap().entrySet().iterator();
			while(it.hasNext()) {
				Entry<Integer, Set<Voucher>> entry = it.next();
				Iterator<Voucher> iter = entry.getValue().iterator();
				while(iter.hasNext()) {
					Voucher voucher = iter.next();
					Vector<Object> helper = new Vector<>();
					helper.add(voucher.getId());
					helper.add(voucher.getCode());
					helper.add(VMS.getInstance().getCampaign(entry.getKey()).getName());
					helper.add(voucher.getStatus());
					if(voucher.getUsedDate()!=null)
						helper.add(voucher.getUsedDate().toString().replace("T", " ").substring(0,16));
					else
						helper.add(null);
					helper.add(voucher.getClass().getName());
					helper.add(voucher.getTypeValue());
					voucherRows.add(helper);
				}
			}
		}
		else
		{
			Iterator<Entry<String, Set<Voucher>>> it = 
					VMS.getInstance().getCampaign(id).getVoucherMap().entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, Set<Voucher>> entry = it.next();
				Iterator<Voucher> iter = entry.getValue().iterator();
				while(iter.hasNext()) {
					Voucher voucher = iter.next();
					Vector<Object> helper = new Vector<>();
					helper.add(voucher.getId());
					helper.add(voucher.getCode());
					helper.add(VMS.getInstance().getCampaign(id).getName());
					helper.add(voucher.getStatus());
					if(voucher.getUsedDate()!=null)
						helper.add(voucher.getUsedDate().toString().replace("T", " ").substring(0,16));
					else
						helper.add(null);
					helper.add(voucher.getClass().getName());
					helper.add(voucher.getTypeValue());
					voucherRows.add(helper);
				}
			}
		}
		return voucherRows;
	}
	
	private JTabbedPane guestPanel() {
		JTabbedPane guest = new JTabbedPane();
		
		Vector<String> campaignColumns = new Vector<>();
		
		campaignColumns.add("Campaign ID");
		campaignColumns.add("Campaign Name");
		campaignColumns.add("Campaign Description");
		campaignColumns.add("Campaign Start Date");
		campaignColumns.add("Campaign Status");
		campaignColumns.add("Number of vouchers");
		
		Vector<String> voucherColumns = new Vector<>();
		
		voucherColumns.add("Voucher Id");
		voucherColumns.add("Voucher Code");
		voucherColumns.add("Voucher From Campaign");
		voucherColumns.add("Voucher Status");
		voucherColumns.add("Voucher Used Date");
		voucherColumns.add("Voucher Type");
		voucherColumns.add("Voucher Value/Percent");
		
		JTable campaignTable = new JTable(refreshCampaignRows(0), campaignColumns),
					voucherTable = new JTable(refreshVoucherRows(0), voucherColumns);
		JScrollPane campaigns = new JScrollPane(campaignTable), vouchers = new JScrollPane(voucherTable);
		campaignTable.setEnabled(false);
		voucherTable.setEnabled(false);
		guest.addTab("Campaigns", campaigns);
		guest.addTab("My Vouchers", vouchers);
		guest.addTab("My Notifications", seeNotificationsPane());
		return guest;
	}
	
	private boolean isAcceptable(Integer id, String name) {
		for(Campaign campaign:VMS.getInstance().getCampaigns()) {
			if(campaign.getId()==id||campaign.getName().compareTo(name)==0)
				return false;
		}
		return true;
	}
	
	private Integer getUsedVoucher(User user, Integer campaignID) {
		Integer total = 0;
		Iterator<Entry<Integer, Set<Voucher>>> it = user.getVoucherMap().entrySet().iterator();
		while(it.hasNext()) {
			Entry<Integer, Set<Voucher>> entry = it.next();
			if(entry.getKey()==campaignID) {
				Iterator<Voucher> iter = entry.getValue().iterator();
				while(iter.hasNext()) {
					if(iter.next().getStatus()==VoucherStatusType.USED)
						total++;
				}
			}
		}
		return total;
	}
	
	private JScrollPane seeNotificationsPane() {
		Vector<String> notificationColumns = new Vector<>();
		notificationColumns.add("Notification Type");
		notificationColumns.add("Notification From Campaign With Id");
		notificationColumns.add("Notification Sent At Date");
		notificationColumns.add("Voucher IDs");
		Vector<Vector<String>> notificationRows = new Vector<>();
		
		for(Notification notification:mainUser.getNotifications()) {
			Vector<String> helper = new Vector<>();
			helper.add(notification.getType().toString());
			helper.add(notification.getCampaignID().toString());
			helper.add(notification.getDate().toString().replace("T", " "));
			helper.add(notification.getCodes().get(mainUser.getEmail()).toString());
			notificationRows.add(helper);
		}
		JTable notificationTable = new JTable(notificationRows, notificationColumns);
		notificationTable.setEnabled(false);
		JScrollPane seeNotifications = new JScrollPane(notificationTable);
		return seeNotifications;
	}
	
	private void replaceLine(String path, Integer version) {
	    try {
	        BufferedReader file = new BufferedReader(new FileReader(path));
	        StringBuffer inputBuffer = new StringBuffer();
	        String line;
	        
	        Integer i=0;
	        
	        while ((line = file.readLine()) != null) {
	        	if(i == version) {
	        		if(version == 1) {
		        		Integer helper = totalEvents+1;
		        		totalEvents++;
		        		line = helper.toString();
	        		}
	        		if(version == 0) {
	        			Integer helper = totalCampaigns+1;
	        			totalCampaigns++;
	        			line = helper.toString();
	        		}
	        	}
	        	i++;
	        	inputBuffer.append(line);
	            inputBuffer.append('\n');
	        }
	        file.close();

	        FileOutputStream fileOut = new FileOutputStream(path);
	        fileOut.write(inputBuffer.toString().getBytes());
	        fileOut.close();

	    } catch (Exception e) {
	    	System.out.println("Eroare la salvarea datelor");
	    }
	}
	
	private JTabbedPane adminPanel() {
		JTabbedPane admin = new JTabbedPane();
		JPanel mainPanelCampaigns = new JPanel(new GridLayout(2,1));
		JPanel mainPanelVouchers = new JPanel(new GridLayout(2,1));
		JPanel mainPanelNotifications = new JPanel();
		JScrollPane seeNot = seeNotificationsPane();
		mainPanelNotifications.add(seeNot);
		Vector<String> campaignColumns = new Vector<>();
		
		campaignColumns.add("Campaign ID");
		campaignColumns.add("Campaign Name");
		campaignColumns.add("Campaign Description");
		campaignColumns.add("Campaign Start Date");
		campaignColumns.add("Campaign Status");
		campaignColumns.add("Number of vouchers");
		
		Vector<String> voucherColumns = new Vector<>();
		
		voucherColumns.add("Voucher Id");
		voucherColumns.add("Voucher Code");
		voucherColumns.add("Voucher From Campaign");
		voucherColumns.add("Voucher Status");
		voucherColumns.add("Voucher Used Date");
		voucherColumns.add("Voucher Type");
		voucherColumns.add("Voucher Value/Percent");
		
		TableModel campaignModel = new DefaultTableModel(refreshCampaignRows(1),campaignColumns);
		JTable campaignTable = new JTable(campaignModel),
					voucherTable = new JTable();
		JScrollPane campaigns = new JScrollPane(campaignTable), vouchers = new JScrollPane(voucherTable);
		campaignTable.setAutoCreateRowSorter(true);
		campaignTable.setEnabled(false);
		voucherTable.setAutoCreateRowSorter(true);
		voucherTable.setEnabled(false);
		
		mainPanelVouchers.add(vouchers);
		JPanel secondaryPanel = new JPanel(new GridLayout(1,5));
		JPanel helpLoad = new JPanel(new GridLayout(1,2));
		JPanel loadVouchers = new JPanel(new GridLayout(2,1));
		JButton getCampaignID = new JButton("Load Campaign's Vouchers");
		getCampaignID.setBackground(Color.YELLOW);
		helpLoad.add(new JLabel("Campaign Id: "));
		JTextField getID = new JTextField();
		helpLoad.add(getID);
		loadVouchers.add(helpLoad);
		loadVouchers.add(getCampaignID);
		
		getCampaignID.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Integer id = Integer.parseInt(getID.getText());
					boolean valid = false;
					for(Campaign campaign:VMS.getInstance().getCampaigns()) {
						if(campaign.getId()==id)
							valid=true;
					}
					if(valid==false)
						throw new Exception();
					mainCampaignID = id;
					DefaultTableModel dtm = (DefaultTableModel) voucherTable.getModel();
					dtm.setDataVector(refreshVoucherRows(id), voucherColumns);
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "CampaignID lipseste sau nu exista o campanie cu "
							+ "acest ID!", "Input gresit!", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		secondaryPanel.add(loadVouchers);
		JPanel addVoucher = new JPanel(new GridLayout(2,1));
		JPanel helpAdd = new JPanel(new GridLayout(4,2));
		helpAdd.add(new JLabel("Campaign Id:"));
		JTextField voucherCampaignId = new JTextField();
		helpAdd.add(voucherCampaignId);
		helpAdd.add(new JLabel("User Email:"));
		JTextField userEmail = new JTextField();
		helpAdd.add(userEmail);
		helpAdd.add(new JLabel("Voucher Type:"));
		String[] voucherTypes = {"GiftVoucher", "LoyaltyVoucher"};
		JComboBox<String> voucherType = new JComboBox<>(voucherTypes);
		helpAdd.add(voucherType);
		helpAdd.add(new JLabel("Voucher Value:"));
		JTextField voucherValue = new JTextField();
		helpAdd.add(voucherValue);
		JButton addVoucherBtn = new JButton("Add Voucher");
		addVoucherBtn.setBackground(Color.GREEN);
		addVoucher.add(helpAdd);
		addVoucher.add(addVoucherBtn);
		addVoucherBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Integer id = Integer.parseInt(voucherCampaignId.getText());
					boolean valid = false;
					for(Campaign campaign:VMS.getInstance().getCampaigns()) {
						if(campaign.getId()==id)
							valid=true;
					}
					if(valid==false)
						throw new Exception();
					mainCampaignID = id;
					String email = userEmail.getText();
					Float value = Float.parseFloat(voucherValue.getText());
					VMS.getInstance().getCampaign(id).generateVoucher(email, voucherType.getSelectedItem()
							.toString(), value);
					DefaultTableModel dtm = (DefaultTableModel) voucherTable.getModel();
					dtm.setDataVector(refreshVoucherRows(id), voucherColumns);
					String result = mainUser.getId().toString()+";generateVoucher;"+id+";"+email+";"+
							voucherType.getSelectedItem().toString()+";"+
							value.toString().substring(0, value.toString().length()-2)+"\n";
					Files.write(Paths.get(inputFileDirectory+"/"+"events.txt"), result.getBytes(),
							StandardOpenOption.APPEND);
					replaceLine(inputFileDirectory+"/"+"events.txt",1);
					}
					catch(Exception e) {
						JOptionPane.showMessageDialog(null, "Nu ai introdus bine datele"
								, "Input gresit!", JOptionPane.ERROR_MESSAGE);
					}
			}
			
		});
		secondaryPanel.add(addVoucher);
		
		JPanel findVoucher = new JPanel(new GridLayout(2,1));
		JPanel helpFind = new JPanel(new GridLayout(1,2));
		helpFind.add(new JLabel("Voucher Code:"));
		JTextField voucherCode = new JTextField();
		JButton highlightVoucher = new JButton("Find Voucher");
		highlightVoucher.setBackground(Color.RED);
		highlightVoucher.setForeground(Color.WHITE);
		highlightVoucher.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				String code = voucherCode.getText();
				DefaultTableModel dtm = (DefaultTableModel) voucherTable.getModel();
				for(int i = 0 ; i < dtm.getRowCount();i++) {
					if(code.compareTo(dtm.getValueAt(i, 1).toString())==0)
					{
						voucherTable.setSelectionBackground(Color.RED);
						voucherTable.setSelectionForeground(Color.WHITE);
						voucherTable.setRowSelectionInterval(i, i);
						VMS.getInstance().getCampaign(mainCampaignID).
							redeemVoucher(voucherCode.getText(), appDate);
						dtm.setDataVector(refreshVoucherRows(mainCampaignID), voucherColumns);
						voucherTable.setRowSelectionInterval(i, i);
						Integer idVoucher = 
								VMS.getInstance().getCampaign(mainCampaignID).getVoucher(code).getId();
						String result = mainUser.getId().toString()+";redeemVoucher;"+mainCampaignID+";"
						+idVoucher.toString()+";"+appDate.toString().replace("T", " ").substring(0,16);
						try {
							Files.write(Paths.get(inputFileDirectory+"/"+"events.txt"), result.getBytes(),
									StandardOpenOption.APPEND);
						} catch (IOException e) {
							System.err.println("Eroare la salvarea datelor");
						}
						replaceLine(inputFileDirectory+"/"+"events.txt",1);
						return;
					}
					else
					{
						voucherTable.clearSelection();
					}
				}
			}
			
		});
		helpFind.add(voucherCode);
		findVoucher.add(helpFind);
		findVoucher.add(highlightVoucher);
		secondaryPanel.add(findVoucher);
		JButton sendMultipleVouchers = new JButton("Send Multiple Vouchers From emails.txt");
		sendMultipleVouchers.setBackground(Color.DARK_GRAY);
		sendMultipleVouchers.setForeground(Color.WHITE);
		sendMultipleVouchers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					File f = new File(inputFileDirectory+"\\emails.txt");
					Scanner scan = new Scanner(f);
					scan.useDelimiter(";|\\n|\\r\\n|\\r");
					int n = scan.nextInt();
					for(int i=0;i<n;i++) {
						int id = scan.nextInt();
						String email = scan.next();
						String type = scan.next();
						Float value = scan.nextFloat();
						VMS.getInstance().getCampaign(id).generateVoucher(email, type, value);
						String result = mainUser.getId().toString()+";generateVoucher;"+id+";"+email+";"+
								voucherType.getSelectedItem().toString()+";"+
								value.toString().substring(0, value.toString().length()-2)+"\n";
						Files.write(Paths.get(inputFileDirectory+"/"+"events.txt"), result.getBytes(),
								StandardOpenOption.APPEND);
						replaceLine(inputFileDirectory+"/"+"events.txt",1);
					}
					scan.close();
				}
				catch(Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Fisierul emails.txt este gol sau nu este formatat"
							+ " corect", "Eroare in emails.txt", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		secondaryPanel.add(sendMultipleVouchers);
		mainPanelVouchers.add(secondaryPanel);
		
		JTextField campaignID = new JTextField(), campaignName = new JTextField(), campaignDescription
				 = new JTextField(), startDate = new JTextField(), endDate = new JTextField(), budget
						 = new JTextField(), strategy = new JTextField();
		
		JPanel addEditCampaign = new JPanel(new GridLayout(8,2)), 
				seeCloseCampaign = new JPanel(new GridLayout(2,2));
		JButton addCampaignBtn = new JButton("Add Campaign"), editCampaignBtn = new JButton("Edit Campaign"),
				closeCampaignBtn = new JButton("Close Campaign"), seeCampaignDetailsBtn = new JButton("See "
						+ "Campaign Details");
		addCampaignBtn.setBackground(Color.GREEN);
		editCampaignBtn.setBackground(Color.YELLOW);
		closeCampaignBtn.setBackground(Color.RED);
		closeCampaignBtn.setForeground(Color.WHITE);
		seeCampaignDetailsBtn.setBackground(Color.DARK_GRAY);
		seeCampaignDetailsBtn.setForeground(Color.WHITE);
		JTextField closeSeeCampaignId = new JTextField();
		mainPanelCampaigns.add(campaigns);
		secondaryPanel = new JPanel(new GridLayout(1,4));
		addEditCampaign.add(new JLabel("Campaign ID: "));
		addEditCampaign.add(campaignID);
		addEditCampaign.add(new JLabel("Campaign Name: "));
		addEditCampaign.add(campaignName);
		addEditCampaign.add(new JLabel("Campaign Description: "));
		addEditCampaign.add(campaignDescription);
		addEditCampaign.add(new JLabel("Start Date: "));
		addEditCampaign.add(startDate);
		addEditCampaign.add(new JLabel("End Date: "));
		addEditCampaign.add(endDate);
		addEditCampaign.add(new JLabel("Budget: "));
		addEditCampaign.add(budget);
		addEditCampaign.add(new JLabel("Strategy: "));
		addEditCampaign.add(strategy);
		addCampaignBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
				Integer id = Integer.parseInt(campaignID.getText());
				boolean valid = false;
				for(Campaign campaign:VMS.getInstance().getCampaigns()) {
					if(campaign.getId()==id)
						valid=true;
				}
				if(valid==true||id<1) {
					JOptionPane.showMessageDialog(null, "Eroare in campul CampaignID"
							, "Input gresit!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(!isAcceptable(id, campaignName.getText())) {
					JOptionPane.showMessageDialog(null, "Eroare in campul CampaignName - Campania exista deja"
							, "Campanie duplicata", JOptionPane.ERROR_MESSAGE);
					return;
				}
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				LocalDateTime start = LocalDateTime.parse(startDate.getText(), formatter);
				LocalDateTime finish = LocalDateTime.parse(endDate.getText(), formatter);
				Integer budg = Integer.parseInt(budget.getText());
				Campaign campaign = new Campaign(id, campaignName.getText(), campaignDescription.getText()
						, start, finish, budg, strategy.getText(), appDate);
				VMS.getInstance().addCampaign(campaign);
				DefaultTableModel dtm = (DefaultTableModel) campaignTable.getModel();
				dtm.setDataVector(refreshCampaignRows(1), campaignColumns);
				String result=id+";"+campaignName.getText()+";"+campaignDescription.getText()+";"
						+start.toString().replace("T", " ")+";"+finish.toString().replace("T", " ")+";"+
						budg.toString()+";"+strategy.getText()+"\n";
				Files.write(Paths.get(inputFileDirectory+"/"+"campaigns.txt"), result.getBytes(),
						StandardOpenOption.APPEND);
				replaceLine(inputFileDirectory+"/"+"campaigns.txt",0);
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Nu ai introdus bine datele"
							, "Input gresit!", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		editCampaignBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
				Integer id = Integer.parseInt(campaignID.getText());
				boolean valid = false;
				for(Campaign campaign:VMS.getInstance().getCampaigns()) {
					if(campaign.getId()==id)
						valid=true;
				}
				if(valid==false)
					throw new Exception();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				LocalDateTime start = LocalDateTime.parse(startDate.getText(), formatter);
				LocalDateTime finish = LocalDateTime.parse(endDate.getText(), formatter);
				Integer budg = Integer.parseInt(budget.getText());
				Campaign campaign = new Campaign(id, campaignName.getText(), campaignDescription.getText()
						, start, finish, budg, null, appDate);
				VMS.getInstance().updateCampaign(id, campaign);
				DefaultTableModel dtm = (DefaultTableModel) campaignTable.getModel();
				dtm.setDataVector(refreshCampaignRows(1), campaignColumns);
				mainPanelNotifications.removeAll();
				JScrollPane seeNot = seeNotificationsPane();
				mainPanelNotifications.add(seeNot);
				mainPanelNotifications.repaint();
				mainPanelNotifications.revalidate();
				String result=mainUser.getId()+";editCampaign;"+id+";"+campaignName.getText()+";"
				+campaignDescription.getText()+";"+start.toString().replace("T", " ")+";"+
				finish.toString().replace("T", " ")+";"+budg.toString()+"\n";
				Files.write(Paths.get(inputFileDirectory+"/"+"events.txt"), result.getBytes(),
						StandardOpenOption.APPEND);
				replaceLine(inputFileDirectory+"/"+"events.txt",1);
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Nu ai introdus bine datele"
							, "Input gresit!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		closeCampaignBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Integer id = Integer.parseInt(closeSeeCampaignId.getText());
					boolean valid = false;
					for(Campaign campaign:VMS.getInstance().getCampaigns()) {
						if(campaign.getId()==id)
							valid=true;
					}
					if(valid==false)
						throw new Exception();
					VMS.getInstance().cancelCampaign(id);
					DefaultTableModel dtm = (DefaultTableModel) campaignTable.getModel();
					dtm.setDataVector(refreshCampaignRows(1), campaignColumns);
					mainPanelNotifications.removeAll();
					JScrollPane seeNot = seeNotificationsPane();
					mainPanelNotifications.add(seeNot);
					mainPanelNotifications.repaint();
					mainPanelNotifications.revalidate();
					String result=mainUser.getId()+";cancelCampaign;"+id+"\n";
					Files.write(Paths.get(inputFileDirectory+"/"+"events.txt"), result.getBytes(),
							StandardOpenOption.APPEND);
					replaceLine(inputFileDirectory+"/"+"events.txt",1);
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Nu ai introdus bine datele"
							, "Input gresit!", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		JFrame detailsFrame = new JFrame("Campaign Details");
		JTabbedPane detailsBonus = new JTabbedPane();
		JTextArea details = new JTextArea();
		JScrollPane detailsPane = new JScrollPane(details);
		JTable observerTable = new JTable();
		observerTable.setEnabled(false);
		JScrollPane observerPane = new JScrollPane(observerTable);
		detailsBonus.add(detailsPane, "Campaign Details");
		detailsBonus.add(observerPane, "Campaign's Observers");
		details.setEditable(false);
		detailsFrame.add(detailsBonus);
		detailsFrame.setVisible(false);
		detailsFrame.setSize(500,400);
		seeCampaignDetailsBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					Integer id = Integer.parseInt(closeSeeCampaignId.getText());
					boolean valid = false;
					for(Campaign campaign:VMS.getInstance().getCampaigns()) {
						if(campaign.getId()==id)
							valid=true;
					}
					if(valid==false)
						throw new Exception();
					Campaign campaign = VMS.getInstance().getCampaign(id);
					Vector<String> observerColumns = new Vector<>();
					observerColumns.add("Observer Id");
					observerColumns.add("Observer Name");
					observerColumns.add("Observer Email");
					observerColumns.add("Observer Type");
					observerColumns.add("Used Vouchers");
					Vector<Vector<String>> observerRows = new Vector<>();
					for(User user:campaign.getObservers()) {
						Vector<String> helper = new Vector<>();
						helper.add(user.getId().toString());
						helper.add(user.getName());
						helper.add(user.getEmail());
						helper.add(user.getType().toString());
						helper.add(getUsedVoucher(user, id).toString());
						observerRows.add(helper);
					}
					((DefaultTableModel) observerTable.getModel()).setDataVector(observerRows, observerColumns);
					String helper = "Campaign Id: " + campaign.getId()+ "\n" +
									"Campaign Name: " + campaign.getName() + "\n"+
									"Campaign Description: " + campaign.getDescription() + "\n"+
									"Campaign Start Date: " + campaign.getStart().toString().replace("T", " ")+ "\n"+
									"Campaign End Date: " + campaign.getFinish().toString().replace("T", " ")+ "\n"+
									"Campaign Status: " + campaign.getStatus() + "\n" +
									"Campaign Voucher Map: " +campaign.getVoucherMap() + "\n"+
									"Campaign Users: " + campaign.getUsers() + "\n"+
									"Campaign Strategy Type: " + campaign.getStrategy();
					campaignName.setText(campaign.getName());
					campaignID.setText(campaign.getId().toString());
					campaignDescription.setText(campaign.getDescription());
					startDate.setText(campaign.getStart().toString().replace("T", " "));
					endDate.setText(campaign.getFinish().toString().replace("T", " "));
					budget.setText(campaign.getTotalV().toString());
					strategy.setText(campaign.getStrategy());
					details.setText(helper);
					details.setFont(new Font("Verdana", Font.BOLD, 24));
					detailsFrame.setVisible(true);
					
				}
				catch(Exception e) {
					JOptionPane.showMessageDialog(null, "Nu ai introdus bine datele"
							, "Input gresit!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		addEditCampaign.add(addCampaignBtn);
		addEditCampaign.add(editCampaignBtn);
		secondaryPanel.add(addEditCampaign);
		seeCloseCampaign.add(new JLabel("Campaing ID: "));
		seeCloseCampaign.add(closeSeeCampaignId);
		seeCloseCampaign.add(closeCampaignBtn);
		seeCloseCampaign.add(seeCampaignDetailsBtn);
		secondaryPanel.add(seeCloseCampaign);
		JButton refreshCampaigns = new JButton("Refresh Campaigns");
		refreshCampaigns.setBackground(Color.LIGHT_GRAY);
		
		refreshCampaigns.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				DefaultTableModel dtm = (DefaultTableModel) campaignTable.getModel();
				dtm.setDataVector(refreshCampaignRows(1), campaignColumns);
			}
			
		});
		secondaryPanel.add(refreshCampaigns);
		mainPanelCampaigns.add(secondaryPanel);
		mainPanelCampaigns.setSize(1080,900);
		
		admin.addTab("Campaigns", mainPanelCampaigns);
		admin.addTab("Vouchers", mainPanelVouchers);
		admin.addTab("Notifications", mainPanelNotifications);
		
		return admin;
	}
}

class TestGUI{
	public static void main(String[] args) {
		new GUIVMS("Interfata Grafica VMS");
	}
}