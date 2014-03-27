

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.uc4.*;
import com.uc4.communication.Connection;
import com.uc4.api.SearchResultItem;
import com.uc4.api.StatisticSearchItem;
import com.uc4.api.Template;
import com.uc4.api.UC4ObjectName;
import com.uc4.api.UC4UserName;
import com.uc4.api.objects.*;
import com.uc4.communication.requests.*;
/**
 *  This is a keyword library for Automic (aka UC4) Automation Engine test via Robotramework.
 *  It creates keywords based on the uc4.jar Application interface
 *  
 */
public class AutomicAEDriver {
	
	public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";
	public static final String ROBOT_LIBRARY_DOC_FORMAT = "HTML";
	public static final String ROBOT_LIBRARY_VERSION = "0.0.2";
	
	private List<Long> usedSessions = new ArrayList<Long>();
	private Connection uc4 = null;
	private String CP ="";
	private int Port =0;
	private SearchObject oCurrentSearch = null;
	
	private String strBasePath = ""; //base path for searches, shall be initialized after login to client
	private String strResultBasePath ="" ; // base path for search results
	
	/**
	 *  sets the base path of the client (called by execute search -> will go private in the future)
	 */
	public void Init_Search_Base_Path(){
		this.strBasePath = uc4.getSessionInfo().getSystemName()+" - "+ uc4.getSessionInfo().getClient()+ "/";
		this.strResultBasePath = uc4.getSessionInfo().getClient()+ "/";
	}
	
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
	/**
	 * Creates and object of given name, a given Template (as described in the uc4.jar API documentation) in the given folder name. Folders will not be created by this method<br>
	 * 
	 * 	params:<br>		strName		Name of the object <br>
	 * 				strTemplate Template Name<br>
	 * 				strFolderName 	Folder name to create the object in<br>
	 */
	public void Create_Object(String strName, String strTemplate, String strFolderName) throws Exception{
		

		System.out.println("Create Object");
		IFolder folder = null;
		folder = receiveFolder(strFolderName);
		
		com.uc4.api.Template t = Template.getTemplateFor(strTemplate);
		System.out.println("create new  name");
		UC4ObjectName objName = new UC4ObjectName(strName);
	 	
		CreateObject create = new CreateObject(objName,t,folder);
		System.out.println("send create");
		uc4.sendRequestAndWait(create);
			if (create.getMessageBox()!=null){
				
				throw new Exception(create.getMessageBox().getText());
				
			}
			
		}
	
	 	
	/**
	 * 	sets CP Address and port<br>
	 *  
	 *  params:<br>		strCP ipv4 Address of the AE<br>
	 *  			iPort Portnumber of the CP<br>
	 */
	public void Set_CP_and_Port(String strCP, int iPort){
		
		System.out.println("Set CP and Port");
		this.CP = strCP;
		this.Port = iPort;
		
	}
	

	/**
	 *  Establishes the connection to a given Automation Engine, performs login and sets the session<br>
	 *  
	 *  params:<br>		client	client number to connect to<br>
	 *  			user	AE user name<br>
	 *  			dept	AE user department<br>
	 *  			pw		AE user password<br>
	 *  			lang	AE language Identifiert (D,E,F)<br>
	 */
	public void Login(int client, String user, String dept, String pw, String lang) throws Exception{
		
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
	
	/**
	 *  closes the uc4 connection
	 *  
	 *  		
	 */
	public void Logout() throws IOException{
		
			uc4.close();
		
    }
	
	/**
	 *  Establishes the connection to a given Automation Engine and sets the session<br>
	 *  
	 *  Params:<br>		strCP		ip v4 address of the CP Port<br>
	 *  			iPort		Port number<br>
	 *  			client		AE Client to connect to<br>
	 *  			user		AE username<br>
	 *  			dept		AE user department<br>
	 *  			pw			AE password 
	 *  			lang		AE languade identifier (D|E|F)
	 *  		
	 */
	public void ConnectAndLogin(String strCP, int iPort, int client, String user, String dept, String pw, String lang) throws Exception{
		
		System.out.println("Connect and login");
    	 Set_CP_and_Port(strCP, iPort);
    	 Login(client, user, dept, pw, lang);
	 }
	

	/**
	 *  returns a AE folder object by name<br>
	 *  
	 *  Params:	<br>	stFolderName	folder name<br>		
	 */
	private IFolder receiveFolder(String strFolderName) throws IOException, Exception{
		
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
	
	/**
	 *  Executes the current search object - the search object has to be initialized first (Initialize_search), and other parameters have to be set with given keywords
	 *  			Search results are retrieved using Get_Search_Result_* keywords<br>
	 *  
	 *  Params:<br>		strSearchString		String Expression to search for<br>
	 *  			strSearchPath		String describing the path to start the search (base path is set automatically)<br>
	 *  			bRecursive			true if search should be conducted recursively, false otherwise<br>
	 */
	public void Search(String strSearchString, String strSearchPath, boolean bRecursive) throws IOException, Exception {
		System.out.println("Search");
		Init_Search_Base_Path();
		this.oCurrentSearch.setSearchLocation(this.strBasePath+strSearchPath, bRecursive);
		this.oCurrentSearch.setName(strSearchString);
		String stUseString ="";
		String stRecString ="";
		if (this.oCurrentSearch.isSearchUseOfObjects()) stUseString ="for use ";
		if (this.oCurrentSearch.isIncludeSubfolder()) stRecString ="recursively ";
		System.out.print("Searching "+ stUseString + this.oCurrentSearch.getName() + " in " + this.oCurrentSearch.getSearchLocation()+stRecString);
	
		uc4.sendRequestAndWait(this.oCurrentSearch);
	
		if (this.oCurrentSearch.getMessageBox() != null) {
			System.err.println(this.oCurrentSearch.getMessageBox().getText());
			throw new Exception("Search Failed");
		}
	}	
	
	/**
	 * 	Creates a new search Object and sets the Select search for all Object types, you must deselect types or clear and select if you want and otherwise configure search
	 */
	public void Initialize_Search(){
		this.oCurrentSearch = new SearchObject();
		this.oCurrentSearch.selectAllObjectTypes();
	}
	
	/**
	 * 	Returns the number of found instances of the search
	 */
	public int Get_Search_Result_Size(){
		return this.oCurrentSearch.size();
	}
	
	/**
	 * 	Returns ArrayList of Names iterating over the search result iterator
	 */	
	public ArrayList<String> Get_Search_Result_Names(){
		System.out.println("getSearchResultNames");
		ArrayList<String> list = new ArrayList();
			Iterator it = oCurrentSearch.resultIterator();
			while (it.hasNext()==true){
			SearchResultItem sIt = (SearchResultItem) it.next();
			list.add(sIt.getName());
			}
		return list;
	}
	/**
	 * 	Sets  or onsets the Search for use in the currentSearch
	 */
	public void Set_Search_For_Use(boolean searchForUse){
		System.out.println("setSearchForUse");
		this.oCurrentSearch.setSearchUseOfObjects(searchForUse);
		
	}
	/**
	 * 	Returns ArrayList of Names iterating over the search result iterator
	 */
	
	public ArrayList<String> Get_Search_Result_Folders(){
		System.out.println("getSearchResultPaths");
		ArrayList<String> list = new ArrayList();
		Iterator it = oCurrentSearch.resultIterator();
		while (it.hasNext()==true){
			SearchResultItem sIt = (SearchResultItem) it.next();
			list.add(sIt.getFolder());
		}
		return list;
	}
	/**
	 * 	Deletes an Object given by the object name
	 */
	
	public void Delete_Object (String objectName) throws IOException{
		System.out.println("Delete object");
		UC4ObjectName objName = null;
        if (objectName.indexOf('/') != -1) {
            objName = new UC4UserName(objectName);
        } else {
            objName = new UC4ObjectName(objectName);
        }

        DeleteObject delete = new DeleteObject(objName);
        uc4.sendRequestAndWait(delete);
        if (delete.getMessageBox() != null) {
            System.err.println(delete.getMessageBox().getText());
            throw new IOException("Failed to delete object:" + objectName + ": " + delete.getMessageBox().getText());
        }
		
	}
	
	/**
	 * 	Deletes a given Folder
	 */
	public void Delete_Folder(String folderName) throws Exception{
		 System.out.print("Delete  folder " + folderName + " ... ");
		 	IFolder folder = receiveFolder(folderName);
	        DeleteObject delete = new DeleteObject(folder);
	        uc4.sendRequestAndWait(delete);
	        if (delete.getMessageBox() != null) {
	            System.err.println(delete.getMessageBox().getText());
	            throw new IOException("Failed to delete object:" + folder.fullPath() + ": " + delete.getMessageBox().getText());
	        }
	}
	
	/**
	 * 	Executes an Object synchronously and returns the runID
	 */
	public int Execute_Object (String objectName) throws IOException{
		System.out.println("Execute Object");
		
		ExecuteObject ex = new ExecuteObject(new UC4ObjectName(objectName));
		uc4.sendRequestAndWait(ex);
		return ex.getRunID();
	}
	
	/**
	 * 	Returns the end state of an execution run id - to be implemented
	 * @throws IOException 
	 */
	
	public String Get_End_State_by_RUNID(String runID) throws IOException{
		System.out.println("Get End Status by RunID");
		String state = "";
		GenericStatistics genstat = new GenericStatistics();
		
		genstat.setRunID(runID);
		
		uc4.sendRequestAndWait(genstat);
		Iterator it = genstat.resultIterator();
		if (it.hasNext()){
			StatisticSearchItem item = (StatisticSearchItem) it.next();
			state= item.getStatus();
		}
		
		return state;
		
		
		
	}
	
	
}
