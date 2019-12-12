package fisco_bcos.server;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLDecoder;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.apache.commons.io.IOUtils;
import org.fisco.bcos.channel.client.PEMManager;
import org.fisco.bcos.channel.client.Service;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ECKeyPair;
import org.fisco.bcos.web3j.crypto.gm.GenCredential;
import org.fisco.bcos.web3j.protocol.Web3j;
import org.fisco.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.tx.gas.StaticGasProvider;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoder;
import org.fisco.bcos.web3j.tx.txdecode.TransactionDecoderFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App
{	
	static int post = 8082;
	static ApplicationContext context;
	static Service service;
	static ChannelEthereumService channelEthereumService;
	static Web3j web3j;
	static BigInteger gasPrice = new BigInteger("300000000");
    static BigInteger gasLimit = new BigInteger("300000000");
	static String contractAddr = "0x7cbb545a8be40acf5a9bae7bc830c15b65c70b75";
	static String abi = "[{\"constant\":false,\"inputs\":[],\"name\":\"queryMyCompany\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"bankName\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"fromCompany\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"bankFinancing\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"bankAddr\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"toCompany\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"createReceipt\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"companyName\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"issue\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"fromCompany\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"uint256\"},{\"name\":\"newToCompany\",\"type\":\"string\"}],\"name\":\"transferReceipt\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"companyName\",\"type\":\"string\"}],\"name\":\"bankQueryCompany\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"companyName\",\"type\":\"string\"},{\"name\":\"initCompanyMoney\",\"type\":\"uint256\"}],\"name\":\"companyRegister\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"toCompany\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"payBack\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"toCompany\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"send\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"showMyReceipt\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"name\":\"initbankMoney\",\"type\":\"uint256\"},{\"name\":\"initbankName\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"companyName\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"Issue\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"companyName\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"companyAddr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"companyMoney\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"compantType\",\"type\":\"uint256\"}],\"name\":\"Check\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"companyName\",\"type\":\"string\"}],\"name\":\"Register\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"from\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"to\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"isAdd\",\"type\":\"bool\"}],\"name\":\"ReceiptCreate\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"from\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"to\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"isEvidence\",\"type\":\"bool\"}],\"name\":\"showReceiptEvent\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"errMessage\",\"type\":\"string\"}],\"name\":\"errEvent\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"fromCompany\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"toCompany\",\"type\":\"string\"},{\"indexed\":false,\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"SendEvent\",\"type\":\"event\"}]";
	static Bank contract;
	static Credentials credentials;
	
	public static Map<String,String> formData2Dic(String formData ) {
        Map<String,String> result = new HashMap<>();
        if(formData== null || formData.trim().length() == 0) {
            return result;
        }
        final String[] items = formData.split("&");
        Arrays.stream(items).forEach(item ->{
            final String[] keyAndVal = item.split("=");
            if( keyAndVal.length == 2) {
                try{
                    final String key = URLDecoder.decode( keyAndVal[0],"utf8");
                    final String val = URLDecoder.decode( keyAndVal[1],"utf8");
                    result.put(key,val);
                }catch (UnsupportedEncodingException e) {}
            }
        });
        return result;
    }
	
	static  class requestHandler implements HttpHandler
	{
		String resourceName;
		requestHandler(String resourceName)
		{
			this.resourceName = resourceName;
		}
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	String requestMethod = exchange.getRequestMethod();
        	Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "application/json;charset=utf-8");
            String response = "{\"ok\":false}";
            switch(resourceName) {
            case "login":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    //获得表单提交数据(post)
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    System.out.println(postString);
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    credentials = GenCredential.create(privateKey); 
                    //根据合约地址加载合约
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    //调用合约方法发送交易
                    try {
                    	TransactionReceipt transactionReceipt = contract.queryMyCompany().send(); 
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	System.out.println(ex);
                    }
                }
            	break;
            case "regist":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    System.out.println(postString);
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String companyName = postInfo.get("companyName");
                    String initCompanyMoney_S = postInfo.get("initCompanyMoney");
                    
                    if(privateKey==null || companyName == null || initCompanyMoney_S == null) {
                    	break;
                    }
                    
                    BigInteger initCompanyMoney = new BigInteger(initCompanyMoney_S,10);
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.companyRegister(companyName, initCompanyMoney).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "bank/issue":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String companyName = postInfo.get("companyName");
                    String amount_S = postInfo.get("amount");
                    
                    if(privateKey==null || companyName == null || amount_S == null) {
                    	break;
                    }
                    
                    BigInteger amount = new BigInteger(amount_S,10);
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.issue(companyName, amount).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "bank/queryCompany":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String companyName = postInfo.get("companyName");
                    
                    if(privateKey==null || companyName == null) {
                    	break;
                    }                                      
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.bankQueryCompany(companyName).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "company/send":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String toCompany = postInfo.get("toCompany");
                    String amount_S = postInfo.get("amount");
                    
                    if(privateKey==null || toCompany == null || amount_S == null) {
                    	break;
                    }
                    
                    BigInteger amount = new BigInteger(amount_S,10);
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.send(toCompany, amount).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "company/showMyReceipt":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    
                    if(privateKey==null) {
                    	break;
                    }
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.showMyReceipt().send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "company/createReceipt":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String toCompany = postInfo.get("toCompany");
                    String amount_S = postInfo.get("amount");
                    
                    if(privateKey==null || toCompany == null || amount_S == null) {
                    	break;
                    }
                    
                    BigInteger amount = new BigInteger(amount_S,10);
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.createReceipt(toCompany, amount).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "company/transferReceipt":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String fromCompany = postInfo.get("fromCompany");
                    String amount_S = postInfo.get("amount");
                    String newToCompany = postInfo.get("newToCompany");
                    
                    if(privateKey==null || fromCompany == null || amount_S == null || newToCompany == null) {
                    	break;
                    }
                    
                    BigInteger amount = new BigInteger(amount_S,10);
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.transferReceipt(fromCompany, amount, newToCompany).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "company/bankFinancing":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String fromCompany = postInfo.get("fromCompany");
                    String amount_S = postInfo.get("amount");
                    
                    if(privateKey==null || fromCompany == null || amount_S == null) {
                    	break;
                    }
                    
                    BigInteger amount = new BigInteger(amount_S,10);
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.bankFinancing(fromCompany, amount).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            case "company/payBack":
            	if(requestMethod.equalsIgnoreCase("POST")) {
                    String postString = IOUtils.toString(exchange.getRequestBody());
                    Map<String,String> postInfo = formData2Dic(postString);
                    String privateKey = postInfo.get("privateKey");
                    String toCompany = postInfo.get("toCompany");
                    String amount_S = postInfo.get("amount");
                    
                    if(privateKey==null || toCompany == null || amount_S == null) {
                    	break;
                    }
                    
                    BigInteger amount = new BigInteger(amount_S,10);
                    
                    credentials = GenCredential.create(privateKey); 
                    contract = Bank.load(contractAddr, web3j, credentials, new StaticGasProvider(gasPrice, gasLimit));
                    try {
                    	TransactionReceipt transactionReceipt = contract.payBack(toCompany, amount).send();
                        TransactionDecoder txDecodeSampleDecoder = TransactionDecoderFactory.buildTransactionDecoder(abi, "");
                        List<Log> logs = transactionReceipt.getLogs();
                        String eventResult = txDecodeSampleDecoder.decodeEventReturnJson(logs);
                        response = eventResult;
                    }catch(Exception ex) {
                    	response = "{\"ok\":false}";
                    	System.out.println(ex);
                    }
                }
            	break;
            default:
            }
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes("UTF-8").length);
            OutputStream responseBody = exchange.getResponseBody();
            OutputStreamWriter writer = new OutputStreamWriter(responseBody, "UTF-8");
            writer.write(response);
            writer.close();
            responseBody.close();
        }
    }
	
	
	public static void init() throws Exception
	{
	       //读取配置文件，SDK与区块链节点建立连接
        context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        
        service = context.getBean(Service.class);
        service.run();
        channelEthereumService = new ChannelEthereumService();
        channelEthereumService.setChannelService(service);
        
        //获取Web3j对象
        web3j = Web3j.build(channelEthereumService, service.getGroupId());
	}
	
    public static void main( String[] args ) throws Exception
    {		
    	//initial
    	init();
    	
    	//run server
    	HttpServer server = HttpServer.create(new InetSocketAddress(post), 0);
        server.createContext("/api/login", new requestHandler("login"));//finish
        server.createContext("/api/regist", new requestHandler("regist"));//finish
        server.createContext("/api/bank/issue", new requestHandler("bank/issue"));
        server.createContext("/api/bank/queryCompany", new requestHandler("bank/queryCompany"));
        server.createContext("/api/company/send", new requestHandler("company/send"));//finish
        server.createContext("/api/company/showMyReceipt", new requestHandler("company/showMyReceipt"));//
        server.createContext("/api/company/createReceipt", new requestHandler("company/createReceipt"));
        server.createContext("/api/company/transferReceipt", new requestHandler("company/transferReceipt"));
        server.createContext("/api/company/bankFinancing", new requestHandler("company/bankFinancing"));
        server.createContext("/api/company/payBack", new requestHandler("company/payBack"));
        server.start();
        System.out.println("Server listening on localhost:"+post);
    	
//    	//部署合约用↓
//    	init();
//        String privateKey = "6d3bcf4b339a12674f8623f459eb1f6525e71299e681c676b8fa409a3d1a2f44"; 
//        //指定外部账户私钥，用于交易签名
//        Credentials credentials = GenCredential.create(privateKey); 
//        String name = "bank";
//        BigInteger init_money = new BigInteger("10000000000");
//        //部署合约 
//        Bank contract = Bank.deploy(web3j, credentials, new StaticGasProvider(gasPrice, gasLimit), init_money, name).send();
//        System.out.println(contract.getContractAddress());
    }
}
