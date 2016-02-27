package IHM;

import javafx.scene.Parent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;

import java.awt.Color;

import javax.swing.JButton;

import Domaine.Constantes;
import Domaine.TFTP;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

public class TFTP_APP {

	private JFrame frmTftpApplication;
	private JTextField tbxHostname;
	private JTextField tbxPort;
	private JTextField tbxRemoteName;
	private JTextField tbxLocalFileName;
	private TFTP tftp;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		TFTP_APP window = new TFTP_APP();
		window.frmTftpApplication.setVisible(true);
	}

	/**
	 * Create the application.
	 */
	public TFTP_APP() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.initializeForm();
		this.initializeServerArea();
		this.initializeFileArea();
	}

	private void initializeForm() {
		this.frmTftpApplication = new JFrame();
		this.frmTftpApplication.getContentPane().setFont(
				new Font("Tahoma", Font.PLAIN, 11));
		this.frmTftpApplication.setResizable(false);
		this.frmTftpApplication.setTitle("TFTP Application");
		this.frmTftpApplication.setBounds(100, 100, 450, 214);
		this.frmTftpApplication.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frmTftpApplication.getContentPane().setLayout(null);
	}

	private void initializeServerArea() {
		JPanel pnlServerInformation = new JPanel();
		pnlServerInformation.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "Server informations",
				TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLACK));
		pnlServerInformation.setBounds(10, 11, 424, 58);
		this.frmTftpApplication.getContentPane().add(pnlServerInformation);
		pnlServerInformation.setLayout(null);

		JLabel lblHostname = new JLabel("Hostname :");
		lblHostname.setBounds(10, 21, 63, 15);
		pnlServerInformation.add(lblHostname);
		lblHostname.setFont(new Font("Tahoma", Font.PLAIN, 12));

		this.tbxHostname = new JTextField();
		this.tbxHostname.setBounds(83, 19, 86, 20);
		pnlServerInformation.add(this.tbxHostname);
		this.tbxHostname.setToolTipText("@IP or hostname");
		this.tbxHostname.setColumns(10);
		lblHostname.setLabelFor(this.tbxHostname);

		JLabel lblPort = new JLabel("Port :");
		lblPort.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPort.setBounds(272, 21, 46, 14);
		pnlServerInformation.add(lblPort);

		this.tbxPort = new JTextField();
		lblPort.setLabelFor(this.tbxPort);
		this.tbxPort.setToolTipText("Port number");
		this.tbxPort.setBounds(328, 19, 86, 20);
		pnlServerInformation.add(this.tbxPort);
		this.tbxPort.setColumns(10);
	}

	private void initializeFileArea() {
		JPanel pnlFileInformation = new JPanel();
		pnlFileInformation.setBorder(new TitledBorder(new TitledBorder(
				UIManager.getBorder("TitledBorder.border"), "File information",
				TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)), "File information", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		pnlFileInformation.setBounds(10, 85, 424, 89);
		this.frmTftpApplication.getContentPane().add(pnlFileInformation);
		pnlFileInformation.setLayout(null);

		JLabel lblRemoteName = new JLabel("Remote name :");
		lblRemoteName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRemoteName.setBounds(10, 23, 99, 14);
		pnlFileInformation.add(lblRemoteName);

		this.tbxRemoteName = new JTextField();
		lblRemoteName.setLabelFor(this.tbxRemoteName);
		this.tbxRemoteName.setToolTipText("Remote name of the file ");
		this.tbxRemoteName.setBounds(119, 21, 86, 20);
		pnlFileInformation.add(this.tbxRemoteName);
		this.tbxRemoteName.setColumns(10);

		JButton btnReceive = new JButton("Get File");
		btnReceive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					getFile();
				} catch (Exception exception) {
					TFTP_APP.showMessage(exception.getMessage(), Constantes.ERROR_MESSAGE_INFORMATION_TITLE, JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnReceive.setBounds(325, 20, 89, 23);
		pnlFileInformation.add(btnReceive);

		JLabel lblNewLabel = new JLabel("Local file name : ");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setBounds(10, 55, 99, 14);
		pnlFileInformation.add(lblNewLabel);

		this.tbxLocalFileName = new JTextField();
		lblNewLabel.setLabelFor(this.tbxLocalFileName);
		this.tbxLocalFileName.setBounds(119, 53, 86, 20);
		pnlFileInformation.add(this.tbxLocalFileName);
		this.tbxLocalFileName.setColumns(10);
		JButton btnSend = new JButton("Put File");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sendFile();
				} catch (SocketException socketException) {
					socketException.printStackTrace();
				} catch (UnknownHostException unknownHostException) {
					JOptionPane.showMessageDialog(new JFrame(),
							unknownHostException.getMessage(), "Dialog",
							JOptionPane.ERROR_MESSAGE);
				} catch (IOException ioException) {
					ioException.printStackTrace();
				} catch (Exception exception) {
					JOptionPane.showMessageDialog(new JFrame(),
							exception.getMessage(), "Dialog",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnSend.setBounds(325, 52, 89, 23);
		pnlFileInformation.add(btnSend);
	}

	protected void getFile() throws Exception {
		this.initializeTFTP();
		this.tftp.receiveFile(this.tbxRemoteName.getText());
		TFTP_APP.showMessage(Constantes.INFORMATION_MESSAGE_DOWNLOAD_DONE, Constantes.INFORMATION_MESSAGE_INFORMATION_TITLE, JOptionPane.INFORMATION_MESSAGE);
	}

	protected void sendFile() throws SocketException, UnknownHostException,
			IOException, Exception {
		this.initializeTFTP();
		this.tftp.sendFile(this.tbxLocalFileName.getText(), this.tbxRemoteName.getText());
		TFTP_APP.showMessage(Constantes.INFORMATION_MESSAGE_DOWNLOAD_DONE, Constantes.INFORMATION_MESSAGE_INFORMATION_TITLE, JOptionPane.INFORMATION_MESSAGE);
	}

	private void initializeTFTP() throws Exception {
		try {
			int portNumber = Integer.parseInt(this.tbxPort.getText());
			InetAddress addrServer = InetAddress.getByName(this.tbxHostname.getText().trim());

			this.tftp = new TFTP(addrServer, portNumber);
		} catch (NumberFormatException numberFormatException) {
			throw new Exception(Constantes.ERROR_MESSAGE_PORT_NUMBER);
		} catch (UnknownHostException unknownHostException) {
			throw new Exception("The host \"" + unknownHostException.getMessage() + "\" is unreachable");
		}
	}
	
	private static void showMessage(String text, String title, int errorMessage)
	{
		JOptionPane.showMessageDialog(new JFrame(), text, title, errorMessage);
	}
}
