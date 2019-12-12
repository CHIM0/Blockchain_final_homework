pragma solidity ^0.4.21;
contract bank{
    //工厂结构体
    struct company{
        string companyName;
        address companyAddr;
        uint companyType;//0为银行，1为普通公司
        uint companyMoney;//初始为0
    }
    //账单结构体
    struct receipt{
        address from;
        address to;
        uint mount;
        bool isEvidence;
    }
    //地址到工厂名的映射
    mapping(address => string) companyNames;
    //工厂名到工厂实例的映射
    mapping(string => company) companys;
    //工厂名字到账单的映射
    mapping(string => receipt[]) receipts;
    address public bankAddr;
    string public bankName;

    // 事件----------------------------------------------------------------
    // 货币发行
    event Issue(string companyName,uint amount);
    // 公司查询
    event Check(string companyName, address companyAddr, uint companyMoney, uint compantType);
    // 新公司创建
    event Register(string companyName);
    //账单变更事件
    event ReceiptCreate(string from,string to, uint amount, bool isAdd);
    //账单打印
    event showReceiptEvent(string from, string to, uint amount, bool isEvidence);
    //通用错误事件
    event errEvent(string errMessage);
    //转账事件
    event SendEvent(string fromCompany, string toCompany, uint amount);
    //事件--------------------------------------------------------------------


    //初始化银行
    constructor(uint initbankMoney,string initbankName){
        bankAddr = msg.sender;
        bankName = initbankName;
        companyNames[bankAddr] = bankName;
        //将银行加入到公司列表
        companys[bankName] = company({
            companyName:bankName,
            companyAddr:bankAddr,
            companyType:0,
            companyMoney:initbankMoney
        });
    }


    //银行可用↓-------------------------------------------------

    //虚拟货币发行
    function issue(string companyName, uint amount) public {
        if (msg.sender != bankAddr) {
            emit errEvent("Only bank can issue money.");
            return;
        }
        companys[companyName].companyMoney += amount;
        emit Issue(companyName, amount);
    }
    //银行查看公司信息
    function bankQueryCompany(string companyName) public {
        if (msg.sender != bankAddr) {
            emit errEvent("Only bank can query other company's message.");
            return;
        }
        emit Check(companys[companyName].companyName, companys[companyName].companyAddr, companys[companyName].companyMoney, companys[companyName].companyType);
    }

    //银行可用↑-------------------------------------------------------

    // 公司可用↓------------------------------------------------------
    //公司转账
    function _send(string fromCompany, string toCompany, uint amount) private returns (bool ok){
        if (companys[fromCompany].companyMoney < amount) return false;
        companys[fromCompany].companyMoney -= amount;
        companys[toCompany].companyMoney += amount;
        emit SendEvent(fromCompany, toCompany, amount);
        return true;
    }
    function send(string toCompany, uint amount) public {
        string storage fromCompany = companyNames[msg.sender];
        if(bytes(fromCompany).length == 0) {
            emit errEvent("Your company doesn't existed.");
            return;
        }
        if(bytes(companys[toCompany].companyName).length == 0) {
            emit errEvent("The fromCompany doesn't existed.");
            return;
        }
        _send(fromCompany, toCompany, amount);
    }
    //公司注册
    function companyRegister(string companyName, uint initCompanyMoney) public {
        if(bytes(companyNames[msg.sender]).length != 0){
            emit errEvent("You have already regist a company.");
            return;
        }
        if(initCompanyMoney < 10) {
            emit errEvent("You have no enough initMoney(>10).");
            return;
        }
        if(bytes(companys[companyName].companyName).length != 0) {
            emit errEvent("The company name has been used.");
            return;
        }
        companys[companyName] = company({
            companyName:companyName,
            companyAddr:msg.sender,
            companyType:1,
            companyMoney:initCompanyMoney
        });
        companyNames[msg.sender] = companyName;
        emit Register(companyName);
    }
    //查看自己公司信息
    function queryMyCompany() public {
        string storage name = companyNames[msg.sender];
        emit Check(companys[name].companyName, companys[name].companyAddr, companys[name].companyMoney, companys[name].companyType);
    }
    //打印账单
    function showMyReceipt() public {
        for(uint i = 0; i < receipts[companyNames[msg.sender]].length; i++){
            receipt storage temp = receipts[companyNames[msg.sender]][i];
            if(temp.mount == 0) continue;
            emit showReceiptEvent(
                companyNames[temp.from],
                companyNames[temp.to],
                temp.mount,
                temp.isEvidence
            );
        }
    }
    //创建账单
    function _createReceipt(address fromAddr, address toAddr, uint amount, bool isAdd) private returns (bool ok){
        //判断是否存在相同双方的账单
        //若存在则更新双方账单内容
        string storage fromCompany = companyNames[fromAddr];
        string storage toCompany = companyNames[toAddr];
        bool sameReceiptExist = false;
        for(uint i = 0; i < receipts[fromCompany].length; i++){
            receipt storage temp = receipts[fromCompany][i];
            if(temp.mount != 0 && temp.from == fromAddr && temp.to == toAddr){
                sameReceiptExist = true;
                if(isAdd){
                    receipts[fromCompany][i].mount += amount;
                }else if(!isAdd && receipts[fromCompany][i].mount >= amount){
                    receipts[fromCompany][i].mount -= amount;
                }else{
                    emit errEvent("The money doesn't enough to transfer.");
                    return false;
                }
            }
        }
        if(sameReceiptExist){
            for(i = 0; i < receipts[toCompany].length; i++){
                temp = receipts[toCompany][i];
                if(temp.mount != 0 && temp.from == fromAddr && temp.to == toAddr){
                    if(isAdd){
                        receipts[toCompany][i].mount += amount;
                    }else if(!isAdd && receipts[toCompany][i].mount >= amount){
                        receipts[toCompany][i].mount -= amount;
                    }else{
                        emit errEvent("The money doesn't enough to transfer.");
                        return false;
                    }
                }
            }
            emit ReceiptCreate(fromCompany, toCompany, amount, isAdd);
            return true;
        }else{
            if(isAdd){
                receipts[fromCompany].push(receipt({
                    from:fromAddr,
                    to:toAddr,
                    mount:amount,
                    isEvidence:false
                }));
                receipts[toCompany].push(receipt({
                    from:fromAddr,
                    to:toAddr,
                    mount:amount,
                    isEvidence:false
                }));
                emit ReceiptCreate(fromCompany, toCompany, amount, isAdd);
                return true;
            }
            else{
                emit errEvent("the target receipt doesn't exited.");
                return false;
            }
        }
    }
    //账单创建，由付款方(from)使用
    function createReceipt(string toCompany, uint amount) public {
        string storage fromCompany = companyNames[msg.sender];
        address toAddr = companys[toCompany].companyAddr;
        if(bytes(fromCompany).length == 0){
            emit errEvent("Your company doesn't existed.");
            return;
        }
        if(bytes(companys[toCompany].companyName).length == 0){
            emit errEvent("The company doesn;t exited.");
            return;
        }
        _createReceipt(msg.sender, toAddr, amount, true);
    }
    //账单转移
    function _transferReceipt(address from,address to,address newTo, uint amount) private returns(bool ok){
        if(_createReceipt(from,to,amount,false)){
            if(_createReceipt(from,newTo,amount,true)){
                return true;
            }
        }
        return false;
    }
    //账单转移,由账单收款方(to)使用
    function transferReceipt(string fromCompany, uint amount, string newToCompany) public{
        string storage toCompany = companyNames[msg.sender];
        address fromAddr = companys[fromCompany].companyAddr;
        address newToAddr = companys[newToCompany].companyAddr;
        if(bytes(toCompany).length == 0) {
            emit errEvent("Your company doesn't existed.");
            return;
        }
        if(bytes(companys[fromCompany].companyName).length == 0) {
            emit errEvent("The fromCompany doesn't existed.");
            return;
        }
        if(bytes(companys[newToCompany].companyName).length == 0) {
            emit errEvent("The toCompany doesn't existed.");
            return;
        }
        _transferReceipt(fromAddr,msg.sender,newToAddr,amount);
    }
    //向银行融资,账单收款方（to）可使用
    function bankFinancing(string fromCompany, uint amount)public{
        string storage toCompany = companyNames[msg.sender];
        address fromAddr = companys[fromCompany].companyAddr;
        if(bytes(toCompany).length == 0) {
            emit errEvent("Your company doesn't existed.");
            return;
        }
        if(bytes(companys[fromCompany].companyName).length == 0) {
            emit errEvent("The fromCompany doesn't existed.");
            return;
        }
        if (_transferReceipt(fromAddr, msg.sender, bankAddr, amount)){
            _send(bankName,toCompany,amount);
        }else{
            emit errEvent("Finacing fail.");
        }
    }
    //偿还账单,由from方偿还
    function payBack(string toCompany, uint amount) public {
        string storage fromCompany = companyNames[msg.sender];
        address toAddr = companys[toCompany].companyAddr;
        if(bytes(fromCompany).length == 0){
            emit errEvent("Your company doesn't existed.");
            return;
        }
        if(bytes(companys[toCompany].companyName).length == 0){
            emit errEvent("The company doesn't exited.");
            return;
        }
        if(_send(fromCompany, toCompany, amount)){
            _createReceipt(msg.sender, toAddr, amount, false);
        }
    }
    // 公司可用↑---------------------------------------------------------
}