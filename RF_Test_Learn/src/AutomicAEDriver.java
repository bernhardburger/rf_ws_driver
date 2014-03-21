

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.uc4.*;
import com.uc4.communication.Connection;
import com.uc4.api.Template;
import com.uc4.api.UC4ObjectName;
import com.uc4.api.objects.*;
import com.uc4.communication.requests.*;

public class AutomicAEDriver {
	/**
	 *  This is a keyword library for Automic (aka UC4) Automation Engine test via Robotramework.
	 *  It creates keywords based on the uc4.jar Application interface
	 *  
	 */
	
	private List<Long> usedSessions = new ArrayList<Long>();
	private Connection uc4 = null;
	private String CP ="";
	private int Port =0;
	
	private void setConnection(Connection c){
		this.uc4 = c;
	}
	
	
	
	private com.uc4.communication.Connection getConnection(){
		System.out.println("Connect to " + CP + " "+ Port);
		if (this.uc4 == null){
			
			try{
				Connection c = com.uc4.communication.Connection.open(CP, Port);
				setConnection(c);
			}
			catch(Exception e) {
				System.out.println("Establishing UC4 Connection failed");
				System.out.println(e.getStackTrace());
			}
			
		}
		
		return this.uc4;
	}
	
	public boolean Create_Object(String strName, String strTemplate, String strFolderName) throws Exception{
		/**
		 *  Signature: 	Create_Object(String strName, String strTemplate, String strFolderName)
		 *  Name: 		Create Onject
		 *  Visibility: public
		 *  Returns: 	boolean
		 *  Description	Creates and object of given name, a given Template (as described in the uc4.jar API documentation) 
		 * 				in the given folder name. Folders will not be created by this method
		 * 
		 * 	params:		strName		Name of the object 
		 * 				strTemplate Template Name
		 * 				strFolderName 	Folder name to create the object in
		 */
		boolean retval = true;
		System.out.println("Create Object");
		IFolder folder = null;
		try {
			folder = receiveFolder(strFolderName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			retval = false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			retval = false;
		}
		com.uc4.api.Template t = Template.getTemplateFor(strTemplate);
		System.out.println("create new  name");
		UC4ObjectName objName = new UC4ObjectName(strName);
	 	
		CreateObject create = new CreateObject(objName,t,folder);
		try {
			System.out.println("send create");
			uc4.sendRequestAndWait(create);
			if (create.getMessageBox()!=null){
				retval = false;
				throw new Exception(create.getMessageBox().getText());
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("for crying out loud, that did not work");
			retval = false;
		}
	 	
		return retval;
	}
	private void Set_CP_and_Port(String strCP, int iPort){
		System.out.println("Set CP and Port");
		this.CP = strCP;
		this.Port = iPort;
		
	}
	
	private void Login(int client, String user, String dept, String pw, String lang) throws Exception{
		/**
		 *  Signature: 	Login(int client, String user, String dept, String pw, String lang)
		 *  Name: 		Login
		 *  Visibility: private
		 *  Returns: 	void
		 *  Description	Establishes the connection to a given Automation Engine and set the session
		 */
		System.out.println("Login");
	     try {
	    	 	char lg =lang.toCharArray()[0];
	    	 	System.out.println(lg);
	            com.uc4.communication.requests.CreateSession cs = getConnection().login(client, user, dept, pw, lg);
	            if (cs.isLoginSuccessful()) {
	                System.out.println("     successfully (SystemName: " + cs.getSystemName() + ", SessionID: " + cs.getSessionID() + ", UserIDNR: " + cs.getUserIdnr() + ")");
	                usedSessions.add(new Long(cs.getSessionID()));
	            } else {
	                System.out.println("     unsuccessfully");
	                	throw new Exception("Login unsuccessful for " + user + "/" + dept + "\nMessage: " + cs.getMessageBox().getText(), null);
					 
	            }

	        } catch (IOException e) {
	            System.out.println("Error at login:");
	            System.out.println(e);
	          //  shutdown();
	            
					throw new Exception("Error while establishing a connection to the CP " + ":" , e);
				 
	        } 
	     
	     
	}
	
	public void Logout(){
   	 	try {
			uc4.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	public void ConnectAndLogin(String strCP, int iPort, int client, String user, String dept, String pw, String lang) throws Exception{
		/**
		 *  Signature: 	ConnectAndLogin(String strCP, int iPort, int client, String user, String dept, String pw, String lang)
		 *  Name: 		ConnectAndLogin
		 *  Visibility: public
		 *  Returns: 	void
		 *  Description	Establishes the connection to a given Automation Engine and sets the session
		 *  
		 *  Params:		strCP		ip v4 address of the CP Port
		 *  			iPort		Port number
		 *  			client		AE Client to connect to
		 *  			user		AE username
		 *  			dept		AE user department
		 *  			pw			AE password 
		 *  			lang		AE languade identifier (D|E|F)
		 *  		
		 */
		System.out.println("Connect and login");
    	 Set_CP_and_Port(strCP, iPort);
    	 Login(client, user, dept, pw, lang);
	 }
	

	
	private IFolder receiveFolder(String strFolderName) throws IOException, Exception{
		/**
		 *  Signature: 	receiveFolder(String strFolderName)
		 *  Name: 		receiveFolder
		 *  Visibility: private
		 *  Returns: 	IFolder
		 *  Description	returns a AE folder object by name
		 *  
		 *  Params:		stFolderName	folder name		
		 */
		System.out.println("receiveFolder");
		FolderTree tree = new FolderTree();
		uc4.sendRequestAndWait(tree);
		IFolder folder = null;
		if (strFolderName.equals("/")){
			folder = tree.root();
		}
		else
		{
			folder = tree.root().getSubFolder(strFolderName,true);
			
		}
		if (folder == null) throw new Exception("Cannot find folder:"+strFolderName);
		
		return folder;
	}
	
	public void Search(SearchObject oSearch) throws IOException, Exception {
		
		String stUseString ="";
		String stRecString ="";
		if (oSearch.isSearchUseOfObjects()) stUseString ="for use ";
		if (oSearch.isIncludeSubfolder()) stRecString ="recursively ";
		System.out.print("Searching "+ stUseString + oSearch.getName() + " in " + oSearch.getSearchLocation()+stRecString);
	
		uc4.sendRequestAndWait(oSearch);
	
		if (oSearch.getMessageBox() != null) {
			System.err.println(oSearch.getMessageBox().getText());
			throw new Exception("Search Failed");
		}
	}	
	
	
	
}
