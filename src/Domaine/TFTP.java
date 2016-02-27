package Domaine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class TFTP {
	private DatagramSocket socket;
	private SocketAddress addrServer;
	private DatagramPacket receivedPacket;
	private DatagramPacket sentPacket;
	private byte[] receivedArray;
	private ByteBuffer sentArray;
	private ArrayList TFTPPacket;
	private int TID;
	private boolean isValidRequest;

	public TFTP() throws SocketException {
		this(InetAddress.getLoopbackAddress(), Constantes.LISTENING_SERVER_PORT);
	}

	public TFTP(InetAddress addrServer, int port) throws SocketException {
		this.socket = new DatagramSocket();
		this.socket.setSoTimeout(10000);
		this.addrServer = new InetSocketAddress(addrServer, port);
		this.receivedArray = new byte[Constantes.BUFFER_SIZE];
		this.sentArray = ByteBuffer.allocate(Constantes.BUFFER_SIZE);
		this.receivedPacket = new DatagramPacket(this.receivedArray, this.receivedArray.length);
		this.TFTPPacket = new ArrayList();
		this.TID = 0;
		this.isValidRequest = true;
	}

	public void sendFile(String localFileName, String remoteFileName) throws Exception {
		int i = 1;
		int nbLu = 512;
		File file = new File(Constantes.UPLOAD_DIRECTORY + localFileName);
		byte[] buffer = null;
		FileInputStream stream = new FileInputStream(file);
		
		this.sendFirstPacket("WRQ", remoteFileName);
		
		while(nbLu != -1) {
			if(this.isValidRequest) {
				buffer = new byte[nbLu];
				nbLu = stream.read(buffer);
				byte[] test = new byte[nbLu];
				
				for( int k = 0; k < nbLu; k++) {
					test[k] = buffer[k];
				}
				
				this.TFTPPacket.clear();
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				this.TFTPPacket.add(Constantes.DATA_BYTE);
				this.TFTPPacket.add((byte)(i / 255));
				this.TFTPPacket.add((byte)(i % 255));
				this.TFTPPacket.add(test);
				
				this.sendPacket();
				
				i++;
			} else {
				this.isValidRequest = true;
			}
		}
		
		stream.close();
	}

	public void receiveFile(String localFileName) throws Exception {
		File file = new File(Constantes.UPLOAD_DIRECTORY + localFileName);
		
		if(file.exists())
		{
			throw new Exception(Constantes.ERROR_MESSAGE_SERVER_FILE_EXIST);
		}
		
		FileWriter stream = new FileWriter(file);
		
		this.sendFirstPacket("RRQ", localFileName);

		while (this.receivedPacket.getLength() >= 512) {
			if(this.isValidRequest) {
				stream.write(new String(this.receivedArray).substring(4));
				
				this.TFTPPacket.clear();
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				this.TFTPPacket.add(Constantes.ACK_BYTE);
				this.TFTPPacket.add(this.receivedArray[2]);
				this.TFTPPacket.add(this.receivedArray[3]);
	
				this.receivedArray = new byte[512];
	
				this.sendPacket();
			} else {
				this.isValidRequest = true;
			}
		}
		
		stream.write(new String(this.receivedArray).substring(4).trim());
		
		stream.close();
		
		this.sendLastPacket();
	}

	private void buildSentPacket() {
		int i = 0;

		for(int j = 0; j < this.TFTPPacket.size(); j++) {
			if (this.TFTPPacket.get(j) instanceof Byte) {
				i++;
			} else if (this.TFTPPacket.get(j) instanceof byte[]) {
				i += new String((byte[])this.TFTPPacket.get(j)).length();
			}
		}

		
		this.sentArray = ByteBuffer.allocate(i);
		this.sentArray.clear();
		
		for (Object element : this.TFTPPacket) {
			if (element instanceof Byte) {
				this.sentArray.put((byte) element);
			} else if (element instanceof byte[]) {
				this.sentArray.put((byte[]) element);
			}
		}
	}

	private void sendFirstPacket(String packetType, String nomFichier) throws Exception {
		switch (packetType) {
			case "RRQ":
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				this.TFTPPacket.add(Constantes.RRQ_BYTE);
				this.TFTPPacket.add(nomFichier.getBytes());
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				this.TFTPPacket.add(Constantes.MODE_ENVOI.getBytes());
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				break;
			case "WRQ":
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				this.TFTPPacket.add(Constantes.WRQ_BYTE);
				this.TFTPPacket.add(nomFichier.getBytes());
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				this.TFTPPacket.add(Constantes.MODE_ENVOI.getBytes());
				this.TFTPPacket.add(Constantes.ZERO_BYTE);
				break;
		}
		
		this.sendPacket();
	}
	
	private void sendLastPacket() throws Exception
	{
		this.TFTPPacket.clear();
		this.TFTPPacket.add(Constantes.ZERO_BYTE);
		this.TFTPPacket.add(Constantes.ACK_BYTE);
		this.TFTPPacket.add(this.receivedArray[2]);
		this.TFTPPacket.add(this.receivedArray[3]);
		
		this.buildSentPacket();

		this.sentPacket = new DatagramPacket(this.sentArray.array(), this.sentArray.array().length, this.addrServer);
		this.socket.send(this.sentPacket);

	}
	
	private void sendPacket() throws Exception
	{
		try {
			this.buildSentPacket();

			this.sentPacket = new DatagramPacket(this.sentArray.array(), this.sentArray.array().length, this.addrServer);
			this.socket.send(this.sentPacket);

			this.receivedPacket = new DatagramPacket(this.receivedArray, this.receivedArray.length);
			
			if (this.receivedPacket.getData().length >= 512) {
				this.socket.receive(this.receivedPacket);
				this.analyseReceivedPacket();
				}
		} catch (SocketTimeoutException timeoutException) {
			throw new Exception(Constantes.ERROR_MESSAGE_SERVER_NOT_RESPONDING);
		}
	}

	private boolean analyseReceivedPacket() throws Exception {
		this.receivedArray = new byte[this.receivedPacket.getLength()];
		this.receivedArray = this.receivedPacket.getData();
		char[] test = new char[this.receivedPacket.getLength() - 1];

		if(this.TID == 0) {
			this.TID = this.receivedPacket.getPort();
		} else if(this.TID != this.receivedPacket.getPort()) {
			throw new Exception(Constantes.ERROR_MESSAGE_SERVER_WRONG_TID);
		}
		
		if (this.receivedArray[1] == 5) {
			for (int i = 4; i < this.receivedPacket.getLength() - 1; i++) {
				test[i - 4] = (char) this.receivedArray[i];
			}

			throw new Exception(new String(test));
		} else if (this.TID != 0 && (this.receivedArray[1] == 1 || this.receivedArray[1] == 2)) {
			this.isValidRequest = false;
		} else {
			this.addrServer = new InetSocketAddress(this.receivedPacket.getAddress(), this.receivedPacket.getPort());
		}
		
		return isValidRequest;
	}
}
