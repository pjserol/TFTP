package Domaine;

public class Constantes {
	public static String UPLOAD_DIRECTORY = "C:\\Temp\\";
	public static int LISTENING_SERVER_PORT = 69;
	public static int BUFFER_SIZE = 516;
	public static String REGEX_ADDRESS_IP_V4 = "^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}";
	public static String MODE_ENVOI = "octet";
	public static byte ZERO_BYTE = (byte)0;
	public static byte RRQ_BYTE = (byte)1;
	public static byte WRQ_BYTE = (byte)2;
	public static byte DATA_BYTE = (byte)3;
	public static byte ACK_BYTE = (byte)4;
	public static byte ERROR_BYTE = (byte)5;
	public static String INFORMATION_MESSAGE_DOWNLOAD_DONE = "Download executed without any error";
	public static String INFORMATION_MESSAGE_INFORMATION_TITLE = "Information";
	public static String ERROR_MESSAGE_PORT_NUMBER = "Port number must be an integer";
	public static String ERROR_MESSAGE_INFORMATION_TITLE = "Error";
	public static String ERROR_MESSAGE_SERVER_NOT_RESPONDING = "Server not responding";
	public static String ERROR_MESSAGE_SERVER_WRONG_TID = "The TID HAS CHANGED";
	public static String ERROR_MESSAGE_SERVER_FILE_EXIST = "File already exist";
}
