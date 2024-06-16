package com.commander4j.mw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.commander4j.Interface.Mapping.Map;
import com.commander4j.gui.JList4j;
import com.commander4j.sys.Common;
import com.commander4j.util.Utility;

public class StartGUI extends JFrame
{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JButton btnStart;
	private JButton btnStop;

	private JLabel lblStatus = new JLabel();
	private final JLabel lblInterfaceStatus = new JLabel("Interface Status :");
	private JLabel label_NoOfMaps = new JLabel("");
	private JList4j<Map> listMaps = new JList4j<Map>();
	private static StartGUI frame;
	Utility util = new Utility();

	/**
	 * Launch the application.
	 */

	private void ConfirmExit()
	{
		if (Common.smw.isRunning())
		{
			int question = JOptionPane.showConfirmDialog(frame, "Closing application with stop interfaces ?", "Confirm", JOptionPane.YES_NO_OPTION, 0, Common.icon_confirm);

			if (question == 0)
			{
				Common.smw.stopMaps();
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

		for (int j = 0; j < Common.smw.cfg.getMaps().size(); j++)
		{
			defComboBoxMod.addElement(Common.smw.cfg.getMaps().get(j));

		}

		ListModel<Map> jList1Model = defComboBoxMod;
		listMaps.setModel(jList1Model);

		listMaps.setCellRenderer(Common.renderer_list);
		listMaps.ensureIndexIsVisible(sel);

		label_NoOfMaps.setText(String.valueOf(Common.smw.cfg.getMaps().size()));
	}

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					frame = new StartGUI();

					GraphicsDevice gd = Utility.getGraphicsDevice();

					GraphicsConfiguration gc = gd.getDefaultConfiguration();

					Rectangle screenBounds = gc.getBounds();

					frame.setBounds(screenBounds.x + ((screenBounds.width - frame.getWidth()) / 2), screenBounds.y + ((screenBounds.height - frame.getHeight()) / 2), frame.getWidth(), frame.getHeight());

					frame.setVisible(true);

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public StartGUI()
	{
		setResizable(false);
		setTitle("Commander4j Middleware" + " " + StartMain.version);
		util.initLogging("");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		setSize(1142, 662);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		addWindowListener(new WindowListener());

		JButton btnClose = new JButton(Common.icon_close);
		btnClose.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnClose.setText("Close");
		btnClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ConfirmExit();
			}
		});
		btnClose.setBounds(863, 586, 150, 38);
		contentPane.add(btnClose);

		btnStart = new JButton(Common.icon_ok);
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Common.smw.runMaps();
				if (Common.smw.cfg.getMapDirectoryErrorCount() > 0)
				{
					String errorMessage = "";

					for (int x = 0; x < Common.smw.cfg.getMapDirectoryErrorCount(); x++)
					{
						errorMessage = errorMessage + Common.smw.cfg.getMapDirectoryErrors().get(x) + "\n";
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
					lblStatus.setText("Running");
				}
			}
		});
		btnStart.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnStart.setMnemonic(KeyEvent.VK_ENTER);
		btnStart.setText("Start");
		btnStart.setSelectedIcon(Common.icon_cancel);
		btnStart.setOpaque(true);
		btnStart.setBounds(213, 586, 150, 38);
		contentPane.add(btnStart);
		
		lblStatus.setFont(new Font("Dialog", Font.BOLD, 12));
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		lblStatus.setForeground(Color.BLACK);
		lblStatus.setBounds(151, 12, 131, 22);
		lblStatus.setPreferredSize(new Dimension(40, 40));
		lblStatus.setBackground(new Color (238,238,238));
		lblStatus.setOpaque(true);
		lblStatus.setText("Idle");
		
		contentPane.add(lblStatus);
		lblInterfaceStatus.setFont(new Font("Dialog", Font.BOLD, 12));
		lblInterfaceStatus.setHorizontalAlignment(SwingConstants.TRAILING);
		lblInterfaceStatus.setBounds(12, 12, 131, 22);

		contentPane.add(lblInterfaceStatus);

		JLabel lblNumberOfMaps = new JLabel("Number of Maps :");
		lblNumberOfMaps.setFont(new Font("Dialog", Font.BOLD, 12));
		lblNumberOfMaps.setHorizontalAlignment(SwingConstants.TRAILING);
		lblNumberOfMaps.setBounds(305, 12, 131, 22);
		contentPane.add(lblNumberOfMaps);

		label_NoOfMaps.setBounds(443, 12, 60, 22);
		contentPane.add(label_NoOfMaps);

		JScrollPane scrollPaneMaps = new JScrollPane();
		scrollPaneMaps.setBounds(0, 80, 1141, 503);
		contentPane.add(scrollPaneMaps);
		listMaps.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPaneMaps.setViewportView(listMaps);

		JButton buttonHelp = new JButton((Icon) null);
		buttonHelp.setFont(new Font("Dialog", Font.PLAIN, 12));
		buttonHelp.setText("Help");
		buttonHelp.setBounds(701, 586, 150, 38);
		contentPane.add(buttonHelp);

		JLabel lblIdDescriptionType_1 = new   JLabel("                                                 Map      Map       Connector   Connector   Path(s)");
		JLabel lblIdDescriptionType = new     JLabel("Map Id   Description                              In      Out         Type        Count     Input / Output");
		lblIdDescriptionType.setForeground(Color.BLACK);
		lblIdDescriptionType.setFont(new Font("Courier New", Font.BOLD, 12));
		lblIdDescriptionType.setBounds(0, 58, 1300, 22);
		contentPane.add(lblIdDescriptionType);
		

		lblIdDescriptionType_1.setForeground(Color.BLACK);
		lblIdDescriptionType_1.setFont(new Font("Courier New", Font.BOLD, 12));
		lblIdDescriptionType_1.setBounds(0, 42, 1300, 22);
		contentPane.add(lblIdDescriptionType_1);

		JButton btnRefresh = new JButton((Icon) null);
		btnRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				populateList("");
			}
		});
		btnRefresh.setText("Refresh");
		btnRefresh.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnRefresh.setBounds(539, 586, 150, 38);
		contentPane.add(btnRefresh);

		btnStop = new JButton(Common.icon_cancel);
		btnStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				populateList("");
				Common.smw.stopMaps();

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
		btnStop.setFont(new Font("Dialog", Font.PLAIN, 12));
		btnStop.setBounds(375, 586, 150, 38);
		contentPane.add(btnStop);

		Common.smw.init();
		Common.smw.loadMaps();

		populateList("");

	}
}
