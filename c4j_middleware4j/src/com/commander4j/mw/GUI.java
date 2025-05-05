package com.commander4j.mw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import com.commander4j.Interface.Mapping.Map;
import com.commander4j.gui.JButton4j;
import com.commander4j.gui.JCheckBox4j;
import com.commander4j.gui.JLabel4j_std;
import com.commander4j.gui.JList4j;
import com.commander4j.prop.JPropQuickAccess;
import com.commander4j.sys.Common;
import com.commander4j.util.JHelp;
import com.commander4j.util.Utility;

public class GUI extends JFrame
{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton4j btnStart;
	private JButton4j btnStop;

	private JLabel4j_std lblStatus = new JLabel4j_std();
	private final JLabel4j_std lblInterfaceStatus = new JLabel4j_std("Interface Status :");
	private final JLabel4j_std lblDescription = new JLabel4j_std("Description :");
	private JLabel4j_std label_NoOfMaps = new JLabel4j_std("");
	private JList4j<Map> listMaps = new JList4j<Map>();
	private static GUI frame;
	private JCheckBox4j checkboxEmailEnabled = new JCheckBox4j("");
	private JPropQuickAccess qa = new JPropQuickAccess();
	private JLabel4j_std textFieldDescription;
	private Font defaultfont = new Font("Courier New", Font.BOLD, 12);
	private static int widthadjustment = 0;
	private static int heightadjustment = 0;


	private void ConfirmExit()
	{
		if (Common.core.isRunning())
		{
			int question = JOptionPane.showConfirmDialog(frame, "Closing application with stop interfaces ?", "Confirm", JOptionPane.YES_NO_OPTION, 0, Common.icon_confirm);

			if (question == 0)
			{
				Common.core.stopMaps();
				System.exit(0);
			}
		}
		else
		{
			System.exit(0);
		}
	}

	class WindowListener extends WindowAdapter
	{
		public void windowClosing(WindowEvent e)
		{
			ConfirmExit();
		}
	}

	private void populateList(String defaultitem)
	{

		DefaultComboBoxModel<Map> defComboBoxMod = new DefaultComboBoxModel<Map>();
		int sel = -1;

		for (int j = 0; j < Common.core.cfg.getMaps().size(); j++)
		{
			defComboBoxMod.addElement(Common.core.cfg.getMaps().get(j));

		}

		ListModel<Map> jList1Model = defComboBoxMod;
		listMaps.setModel(jList1Model);

		listMaps.setCellRenderer(Common.renderer_list);
		listMaps.ensureIndexIsVisible(sel);
		label_NoOfMaps.setBounds(889, 12, 60, 22);
		label_NoOfMaps.setFont(defaultfont);
		label_NoOfMaps.setText(String.valueOf(Common.core.cfg.getMaps().size()));
	}

	/**
	 * Create the frame.
	 */
	public GUI()
	{
		setResizable(false);
		setTitle("Commander4j Middleware" + " " + Core.appVersion);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		widthadjustment = Utility.getOSWidthAdjustment();
		heightadjustment = Utility.getOSHeightAdjustment();
		
		GraphicsDevice gd = Utility.getGraphicsDevice();

		GraphicsConfiguration gc = gd.getDefaultConfiguration();

		Rectangle screenBounds = gc.getBounds();

		setSize(1200+widthadjustment, 680+heightadjustment);
		
		setBounds(screenBounds.x + ((screenBounds.width - GUI.this.getWidth()) / 2), screenBounds.y + ((screenBounds.height - GUI.this.getHeight()) / 2), GUI.this.getWidth()+widthadjustment, GUI.this.getHeight()+heightadjustment);

		contentPane = new JPanel();
		setContentPane(contentPane);
		JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setBackground(Common.color_app_window);
		desktopPane.setBounds(0, 0,  GUI.this.getWidth(), GUI.this.getHeight());
		contentPane.add(desktopPane);
		desktopPane.setLayout(null);
		
		addWindowListener(new WindowListener());

		JButton4j btnClose = new JButton4j(Common.icon_close);
		btnClose.setBounds(841, 586, 150, 36);
		btnClose.setText("Close");
		btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ConfirmExit();
			}
		});
		contentPane.setLayout(null);
		desktopPane.add(btnClose);

		btnStart = new JButton4j(Common.icon_ok);
		btnStart.setBounds(237, 586, 150, 36);
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Common.core.loadMaps();
				
				checkboxEmailEnabled.setSelected(qa.getBoolean(Common.props, qa.getRootURL() +"//enableEmailNotifications"));
				textFieldDescription.setText(qa.getString(Common.props, qa.getRootURL() +"//description"));
				
				Common.core.runMaps();
				if (Common.core.cfg.getMapDirectoryErrorCount() > 0)
				{
					String errorMessage = "";

					for (int x = 0; x < Common.core.cfg.getMapDirectoryErrorCount(); x++)
					{
						errorMessage = errorMessage + Common.core.cfg.getMapDirectoryErrors().get(x) + "\n";
					}

					JOptionPane.showMessageDialog(frame, errorMessage, "Map Errors", JOptionPane.ERROR_MESSAGE);

				}
				else
				{
					btnStart.setEnabled(false);
					btnStop.setEnabled(true);

					populateList("");
					btnClose.setEnabled(false);
					lblStatus.setBackground(new Color(0, 128, 0));
					lblStatus.setFont(defaultfont);
					lblStatus.setText("Running");
				}
			}
		});
		btnStart.setMnemonic(KeyEvent.VK_ENTER);
		btnStart.setText("Start");
		btnStart.setSelectedIcon(Common.icon_cancel);
		btnStart.setOpaque(true);
		desktopPane.add(btnStart);
		lblStatus.setBounds(607, 12, 131, 22);
		
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setForeground(Color.BLACK);
		lblStatus.setPreferredSize(new Dimension(40, 40));
		lblStatus.setBackground(Color.WHITE);
		lblStatus.setOpaque(true);
		lblStatus.setText("Idle");
		lblStatus.setFont(defaultfont);
		
		desktopPane.add(lblStatus);
		lblInterfaceStatus.setBounds(466, 12, 131, 22);
		lblInterfaceStatus.setHorizontalAlignment(SwingConstants.TRAILING);
		lblInterfaceStatus.setFont(defaultfont);

		desktopPane.add(lblInterfaceStatus);
		
		lblDescription.setBounds(10, 12, 131, 22);
		lblDescription.setHorizontalAlignment(SwingConstants.TRAILING);
		lblDescription.setFont(defaultfont);

		desktopPane.add(lblDescription);
		
		textFieldDescription = new JLabel4j_std();
		textFieldDescription.setBounds(151, 12, 305, 22);
		textFieldDescription.setFont(defaultfont);
		desktopPane.add(textFieldDescription);

		JLabel4j_std lblNumberOfMaps = new JLabel4j_std("Number of Maps :");
		lblNumberOfMaps.setFont(defaultfont);
		lblNumberOfMaps.setBounds(748, 12, 131, 22);
		lblNumberOfMaps.setHorizontalAlignment(SwingConstants.TRAILING);
		desktopPane.add(lblNumberOfMaps);
		desktopPane.add(label_NoOfMaps);
		
		JLabel4j_std lblEmailEnabled = new JLabel4j_std("Email Notifications :");
		lblEmailEnabled.setFont(defaultfont);
		lblEmailEnabled.setBounds(959, 12, 168, 22);
		lblEmailEnabled.setHorizontalAlignment(SwingConstants.TRAILING);
		desktopPane.add(lblEmailEnabled);

		JScrollPane scrollPaneMaps = new JScrollPane();
		scrollPaneMaps.setBounds(5, 80, 1157, 503);
		desktopPane.add(scrollPaneMaps);
		listMaps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPaneMaps.setViewportView(listMaps);

		JButton4j buttonHelp = new JButton4j(Common.icon_help);
		buttonHelp.setBounds(690, 586, 150, 36);
		buttonHelp.setText("Help");
		
		final JHelp help = new JHelp();
		help.enableHelpOnButton(buttonHelp, "https://wiki.commander4j.com/index.php?title=Middleware4j");
		
		desktopPane.add(buttonHelp);

		JLabel4j_std lblIdDescriptionType_1 = new   JLabel4j_std("                                           Email   Map    Map     Connector   Connector   Path(s)");
		lblIdDescriptionType_1.setFont(defaultfont);
		lblIdDescriptionType_1.setBounds(10, 42, 1300, 22);
		JLabel4j_std lblIdDescriptionType = new     JLabel4j_std("Map Id   Description                       Notify   In    Out       Type        Count     Input / Output");
		lblIdDescriptionType.setBounds(10, 58, 1300, 22);
		lblIdDescriptionType.setFont(defaultfont);
		desktopPane.add(lblIdDescriptionType);
		

		desktopPane.add(lblIdDescriptionType_1);

		JButton4j btnRefresh = new JButton4j(Common.icon_refresh);
		btnRefresh.setBounds(539, 586, 150, 36);
		btnRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				populateList("");
			}
		});
		btnRefresh.setText("Refresh");
		desktopPane.add(btnRefresh);

		btnStop = new JButton4j(Common.icon_cancel);
		btnStop.setBounds(388, 586, 150, 36);
		btnStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				populateList("");
				Common.core.stopMaps();

				btnStart.setEnabled(true);
				btnStop.setEnabled(false);
				btnClose.setEnabled(true);
				lblStatus.setBackground(Color.RED);
				lblStatus.setText("Stopped");
			}
		});
		btnStop.setEnabled(false);
		btnStop.setText("Stop");
		btnStop.setOpaque(true);
		btnStop.setMnemonic(KeyEvent.VK_ENTER);

		desktopPane.add(btnStop);
		checkboxEmailEnabled.setEnabled(false);
		
		checkboxEmailEnabled.setBounds(1137, 12, 25, 23);

		checkboxEmailEnabled.setFocusable(false);
		desktopPane.add(checkboxEmailEnabled);
		
		checkboxEmailEnabled.setSelected(qa.getBoolean(Common.props, qa.getRootURL() +"//enableEmailNotifications"));
		textFieldDescription.setText(qa.getString(Common.props, qa.getRootURL() +"//description"));

		populateList("");
		
		setVisible(true);

	}
}
